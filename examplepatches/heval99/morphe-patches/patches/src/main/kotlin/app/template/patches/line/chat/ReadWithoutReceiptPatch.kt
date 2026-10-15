// The viewing session (claimed in ChatHistoryActivity.onCreate) and the two read blocks (the read
// worker for the local read, the sendChatChecked call for the receipt) follow uta-a/linimal
// (GPL-3.0), patches/features/readwithoutreceipt. LINE 26.15 builds the chat-list long-press menu
// from a list of (label, action) rows, so the row is added to that list instead of cloning
// linimal's Compose code, and its label comes from the extension instead of a new resource.

package app.template.patches.line.chat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.util.getReference
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val READ_WITHOUT_RECEIPT = "Lapp/template/extension/line/ReadWithoutReceipt;"
private const val MENU_ACTION = "Lapp/template/extension/line/ReadWithoutReceiptMenuAction;"
private const val CHAT_HISTORY_REQUEST = "Lcom/linecorp/line/chat/request/ChatHistoryRequest;"
private const val REQUEST_EXTRA = "chatHistoryRequest"
private const val STRING = "Ljava/lang/String;"
private const val OBJECT = "Ljava/lang/Object;"
private const val ARRAY_LIST = "Ljava/util/ArrayList;"

/** Highest register the 4-bit operands of invoke (35c) and iget (22c) can address. */
private const val MAX_NIBBLE_REGISTER = 15

/**
 * Shared by both entry points: ChatHistoryActivity claims a reserved chat, and while that chat is
 * open neither the local read (the read worker) nor the receipt (sendChatChecked) runs for it.
 */
internal val readWithoutReceiptSessionPatch = bytecodePatch(
    description = "Skips the local read and the read receipt of a chat opened without receipt.",
) {
    extendWith("extensions/line.mpe")

    execute {
        // The extension reads the chat id through ChatHistoryRequest.getChatId by reflection, from the
        // intent extra the launch dispatcher writes.
        classDefBy(CHAT_HISTORY_REQUEST).methods.singleOrNull {
            it.name == "getChatId" && it.parameterTypes.isEmpty() && it.returnType == STRING &&
                AccessFlags.PUBLIC.isSet(it.accessFlags)
        } ?: throw PatchException("$CHAT_HISTORY_REQUEST has no public getChatId()")
        val usesExtra = ChatLaunchDispatchFingerprint.originalClassDef.methods.any { method ->
            method.implementation?.instructions?.any { it.getReference<StringReference>()?.string == REQUEST_EXTRA } == true
        }
        if (!usesExtra) throw PatchException("The chat launch dispatcher no longer uses the \"$REQUEST_EXTRA\" extra")

        // 1. Claim the reservation right after super.onCreate, before the screen loads its messages.
        ChatHistoryOnCreateFingerprint.method.apply {
            val superCall = getInstruction(0)
            if (superCall.opcode !in setOf(Opcode.INVOKE_SUPER, Opcode.INVOKE_SUPER_RANGE) ||
                superCall.getReference<MethodReference>()?.name != "onCreate"
            ) {
                throw PatchException("$definingClass->onCreate does not start with super.onCreate")
            }
            addInstruction(
                1,
                "invoke-static/range { p0 .. p1 }, $READ_WITHOUT_RECEIPT->" +
                    "onChatCreated(Landroid/app/Activity;Landroid/os/Bundle;)V",
            )
        }

        // 2. The read worker (messageId, chatId, flag): local read, read position and the receipt.
        ReadWorkerFingerprint.method.returnEarlyIfReadBlocked(chatIdParameter = 1)

        // 3. sendChatChecked(seq, chatId, lastMessageId) itself, for any other caller.
        val sendMethod = sendChatCheckedMethod()
        mutableClassDefBy(sendMethod.definingClass).methods.single { method ->
            method.name == sendMethod.name && method.returnType == "V" &&
                method.parameterTypes.map(CharSequence::toString) == sendMethod.parameterTypes.map(CharSequence::toString)
        }.returnEarlyIfReadBlocked(chatIdParameter = 1)
    }
}

@Suppress("unused")
val readWithoutReceiptPatch = bytecodePatch(
    name = "Read without receipt",
    description = "Adds \"Read without receipt\" to the long-press menu of 1:1 and group chats in the " +
        "chat list. The chat opens without marking it read or sending a read receipt while it is open, " +
        "and stays unread.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    dependsOn(readWithoutReceiptSessionPatch)

    execute {
        // The row item (label resource, action) and the long-pressed chat.
        val rowClass = ChatRowMenuItemToStringFingerprint.originalClassDef
        val labelField = rowClass.instanceFields.singleOrNull { it.type == "I" }
            ?: throw PatchException("${rowClass.type} has no single int label field")
        val actionType = rowClass.instanceFields.singleOrNull { it.type != "I" }?.type
            ?: throw PatchException("${rowClass.type} has no single action field")
        val rowConstructor = rowClass.methods.singleOrNull {
            it.name == "<init>" && it.parameterTypes.map(CharSequence::toString) == listOf("I", actionType)
        } ?: throw PatchException("${rowClass.type} has no (int, action) constructor")

        val chatClass = ChatPreviewItemDataToStringFingerprint.originalClassDef
        val chatIdField = chatClass.instanceFields.singleOrNull { it.type == STRING }
            ?: throw PatchException("${chatClass.type} has no single chat id field")
        val chatTypeField = chatClass.instanceFields.singleOrNull { it.type != STRING && it.type != "Z" }
            ?: throw PatchException("${chatClass.type} has no single chat type field")

        // 1. The extension's action becomes a subclass of LINE's action class, so a row accepts it.
        classDefBy(actionType).methods.singleOrNull {
            it.name == "<init>" && it.parameterTypes.isEmpty() && AccessFlags.PUBLIC.isSet(it.accessFlags)
        } ?: throw PatchException("$actionType has no public no-arg constructor")
        mutableClassDefBy(MENU_ACTION).apply {
            setSuperClass(actionType)
            methods.single { it.name == "<init>" }.apply {
                val superInit = instructions.indexOfFirst {
                    it.opcode == Opcode.INVOKE_DIRECT &&
                        it.getReference<MethodReference>()?.let { call ->
                            call.definingClass == OBJECT && call.name == "<init>"
                        } == true
                }
                if (superInit < 0) throw PatchException("$MENU_ACTION constructor has no super call")
                val receiver = (getInstruction(superInit) as FiveRegisterInstruction).registerC
                replaceInstruction(superInit, "invoke-direct { v$receiver }, $actionType-><init>()V")
            }
        }

        // 2. The row list builder (static, chat -> ArrayList of rows) is shared with the chat preview
        //    screen's menu, which hands its action to code that cannot take the injected one. Only the
        //    chat list's bottom-sheet menu, the one other method of the builder's class that calls it,
        //    gets its rows through a generated menuRows helper that appends the injected row.
        val builder = Fingerprint(
            returnType = ARRAY_LIST,
            parameters = listOf(chatClass.type),
            custom = { method, _ ->
                AccessFlags.STATIC.isSet(method.accessFlags) && method.implementation?.instructions?.any {
                    it.opcode == Opcode.INVOKE_DIRECT && it.getReference<MethodReference>()?.let { call ->
                        call.definingClass == rowClass.type && call.name == "<init>"
                    } == true
                } == true
            },
        ).uniqueMatch(this, "row list builder").method
        val builderReference = "${builder.definingClass}->${builder.name}(${chatClass.type})$ARRAY_LIST"
        val menuRows = mutableClassDefBy(READ_WITHOUT_RECEIPT).addStaticHelper(
            name = "menuRows",
            parameters = listOf(chatClass.type),
            returnType = ARRAY_LIST,
            registers = 5,
            smali = """
                invoke-static { p0 }, $builderReference
                move-result-object v0
                iget-object v1, p0, $chatIdField
                iget-object v2, p0, $chatTypeField
                invoke-static { v1, v2 }, $READ_WITHOUT_RECEIPT->menuRowLabel(${STRING}$OBJECT)I
                move-result v2
                new-instance v3, $MENU_ACTION
                invoke-direct { v3, v1 }, $MENU_ACTION-><init>($STRING)V
                new-instance v1, ${rowClass.type}
                invoke-direct { v1, v2, v3 }, $rowConstructor
                invoke-static { v0, v1, v2 }, $READ_WITHOUT_RECEIPT->addMenuRow(${ARRAY_LIST}${OBJECT}I)V
                return-object v0
            """,
        )
        val sheetCalls = mutableClassDefBy(builder.definingClass).methods.flatMap { method ->
            method.instructions.indices.filter { index ->
                method.instructions[index].getReference<MethodReference>()?.toString() == builderReference
            }.map { method to it }
        }
        val (sheet, call) = sheetCalls.singleOrNull()
            ?: throw PatchException("Expected one bottom-sheet call of $builderReference, found ${sheetCalls.size}")
        val chatRegister = (sheet.getInstruction(call) as FiveRegisterInstruction).registerC
        sheet.replaceInstruction(call, "invoke-static { v$chatRegister }, $menuRows")

        // 3. Row label lambdas turn the label id into text with stringResource (the chat list's menu
        //    and the preview menu, which share the row class). The sentinel id is not a resource, so
        //    each lambda's stringResource call goes through a generated labelText helper that returns
        //    the extension's text for it and calls stringResource for every real id.
        val labelLambdas = Fingerprint(
            returnType = OBJECT,
            parameters = listOf(OBJECT, OBJECT),
            custom = { method, _ -> labelCallIndex(method.implementation?.instructions?.toList(), labelField) != null },
        ).matchAllOrNull().orEmpty()
        if (labelLambdas.isEmpty()) throw PatchException("No row label lambda found")
        val stringResource = labelLambdas.map { match ->
            match.method.getInstruction(labelCallIndex(match.method.instructions, labelField)!!).getReference<MethodReference>()!!
        }.distinctBy { it.toString() }.singleOrNull()
            ?: throw PatchException("Row label lambdas call different stringResource methods")
        val composer = stringResource.parameterTypes[0].toString()
        val labelText = mutableClassDefBy(READ_WITHOUT_RECEIPT).addStaticHelper(
            name = "labelText",
            parameters = listOf(composer, "I"),
            returnType = STRING,
            registers = 3,
            smali = """
                invoke-static { p1 }, $READ_WITHOUT_RECEIPT->menuLabel(I)$STRING
                move-result-object v0
                if-eqz v0, :stock
                return-object v0
                :stock
                invoke-static { p0, p1 }, $stringResource
                move-result-object v0
                return-object v0
            """,
        )
        labelLambdas.forEach { match ->
            match.method.apply {
                val index = labelCallIndex(instructions, labelField)!!
                val args = getInstruction(index) as FiveRegisterInstruction
                replaceInstruction(index, "invoke-static { v${args.registerC}, v${args.registerD} }, $labelText")
            }
        }

        // 4. The bottom sheet's action dispatcher closes the menu, then switches on the action type.
        //    The injected row's action is handled right after the close, before that switch.
        val actionPrefix = actionType.removeSuffix(";") + "$"
        Fingerprint(
            returnType = OBJECT,
            parameters = listOf(OBJECT),
            custom = { method, _ ->
                val instructions = method.implementation?.instructions?.toList().orEmpty()
                instructions.firstOrNull()?.opcode == Opcode.CHECK_CAST &&
                    instructions.first().getReference<TypeReference>()?.type == actionType &&
                    instructions.count {
                        it.opcode == Opcode.INSTANCE_OF &&
                            it.getReference<TypeReference>()?.type?.startsWith(actionPrefix) == true
                    } >= 5
            },
        ).uniqueMatch(this, "menu action dispatcher").method.apply {
            val action = (getInstruction(0) as OneRegisterInstruction).registerA
            val close = instructions.indexOfFirst {
                it.opcode == Opcode.INVOKE_INTERFACE && it.getReference<MethodReference>()?.let { call ->
                    call.name == "invoke" && call.parameterTypes.isEmpty() && call.returnType == OBJECT
                } == true
            }
            val switchStart = if (close < 0) null else getInstruction(close + 1)
            // The switch's first type check must still read the action register.
            if (switchStart?.opcode != Opcode.INSTANCE_OF || switchStart.location.labels.isNotEmpty() ||
                (switchStart as TwoRegisterInstruction).registerB != action
            ) {
                throw PatchException("Menu close call not found in $definingClass->$name")
            }
            val scratch = switchStart.registerA
            val unit = instructions.indices.firstNotNullOfOrNull { index ->
                val read = instructions[index]
                val next = instructions.getOrNull(index + 1)
                if (read.opcode == Opcode.SGET_OBJECT && next?.opcode == Opcode.RETURN_OBJECT &&
                    (next as OneRegisterInstruction).registerA == (read as OneRegisterInstruction).registerA
                ) read.getReference<FieldReference>() else null
            } ?: throw PatchException("Unit return not found in $definingClass->$name")
            if (action > MAX_NIBBLE_REGISTER) {
                throw PatchException("Action register v$action is past v$MAX_NIBBLE_REGISTER in $definingClass->$name")
            }
            addInstructionsWithLabels(
                close + 1,
                """
                    invoke-static { v$action }, $READ_WITHOUT_RECEIPT->onMenuAction($OBJECT)Z
                    move-result v$scratch
                    if-eqz v$scratch, :stock
                    sget-object v$scratch, $unit
                    return-object v$scratch
                """,
                ExternalLabel("stock", switchStart),
            )
        }
    }
}

@Suppress("unused")
val readNotificationsWithoutReceiptPatch = bytecodePatch(
    name = "Read notifications without receipt",
    description = "Opening a 1:1 or group chat from a message notification no longer marks it read or " +
        "sends a read receipt while it is open, and the chat stays unread.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    dependsOn(readWithoutReceiptSessionPatch)

    execute {
        // The extension reads request.getBackNavigation() and its only public instance boolean.
        val navigation = BackNavigationArgsToStringFingerprint.originalClassDef
        val markers = navigation.fields.filter {
            it.type == "Z" && AccessFlags.PUBLIC.isSet(it.accessFlags) && !AccessFlags.STATIC.isSet(it.accessFlags)
        }
        if (markers.size != 1) {
            throw PatchException("${navigation.type} has ${markers.size} public booleans, expected isFromNotification only")
        }
        classDefBy(CHAT_HISTORY_REQUEST).methods.singleOrNull {
            it.name == "getBackNavigation" && it.parameterTypes.isEmpty() && it.returnType == navigation.type &&
                AccessFlags.PUBLIC.isSet(it.accessFlags)
        } ?: throw PatchException("$CHAT_HISTORY_REQUEST has no public getBackNavigation()")

        // Every chat launch passes the dispatcher; the extension reserves only notification launches.
        ChatLaunchDispatchFingerprint.method.apply {
            val intent = parameterRegister(0)
            addInstruction(
                0,
                "invoke-static/range { p$intent .. p$intent }, $READ_WITHOUT_RECEIPT->onChatLaunch(Landroid/content/Intent;)V",
            )
        }
    }
}

/** The single match of this fingerprint; more than one would patch the wrong menu code. */
private fun Fingerprint.uniqueMatch(context: BytecodePatchContext, what: String) = with(context) {
    val matches = matchAllOrNull().orEmpty()
    if (matches.size != 1) throw PatchException("Expected one $what, found ${matches.size}")
    matches.single()
}

/** p-register number of parameter [index] (0-based, `this` not counted). */
private fun Method.parameterRegister(index: Int): Int {
    val first = if (AccessFlags.STATIC.isSet(accessFlags)) 0 else 1
    return first + parameterTypes.take(index).sumOf { if (it == "J" || it == "D") 2L else 1L }.toInt()
}

/**
 * Adds a public static method built from [smali] to this extension class and returns its
 * descriptor. [registers] counts locals and parameters.
 */
private fun MutableClass.addStaticHelper(
    name: String,
    parameters: List<String>,
    returnType: String,
    registers: Int,
    smali: String,
): String {
    val implementation = MutableMethodImplementation(registers).apply {
        addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
    }
    val method = MutableMethod(
        ImmutableMethod(
            type,
            name,
            parameters.map { ImmutableMethodParameter(it, null, null) },
            returnType,
            AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            null,
            null,
            implementation,
        ),
    )
    method.addInstructionsWithLabels(0, smali)
    // The placeholder return-void after the inserted code is unreachable; drop it.
    method.removeInstruction(method.instructions.lastIndex)
    methods.add(method)
    directMethods.add(method)
    return "$type->$name(${parameters.joinToString("")})$returnType"
}

/**
 * Index of the `invoke-static {composer, id}, (?, I)String` call that reads the row's label field
 * into text: `iget vId, vRow, <label field>`, the call, then `move-result-object`.
 */
private fun labelCallIndex(instructions: List<Instruction>?, labelField: FieldReference): Int? {
    if (instructions == null) return null
    return instructions.indices.singleOrNull { index ->
        val read = instructions[index]
        val call = instructions.getOrNull(index + 1)
        val reference = call?.getReference<MethodReference>()
        val field = read.getReference<FieldReference>()
        read.opcode == Opcode.IGET && field?.definingClass == labelField.definingClass && field.name == labelField.name &&
            call?.opcode == Opcode.INVOKE_STATIC && reference?.returnType == STRING &&
            reference.parameterTypes.size == 2 && reference.parameterTypes[1].toString() == "I" &&
            (call as FiveRegisterInstruction).registerD == (read as TwoRegisterInstruction).registerA &&
            instructions.getOrNull(index + 2)?.opcode == Opcode.MOVE_RESULT_OBJECT
    }?.plus(1)
}

/**
 * Returns at once when [READ_WITHOUT_RECEIPT] blocks reads for the chat id in parameter
 * [chatIdParameter] (0-based, `this` not counted). Uses v0, which is free at entry.
 */
private fun MutableMethod.returnEarlyIfReadBlocked(chatIdParameter: Int) {
    val parameters = parameterTypes.map(CharSequence::toString)
    if (returnType != "V" || parameters.getOrNull(chatIdParameter) != STRING) {
        throw PatchException("$definingClass->$name is not a void method with a chat id parameter")
    }
    if (implementation!!.registerCount - parameterRegister(parameters.size) < 1) {
        throw PatchException("$definingClass->$name has no free register")
    }
    val chatId = parameterRegister(chatIdParameter)
    addInstructionsWithLabels(
        0,
        """
            invoke-static/range { p$chatId .. p$chatId }, $READ_WITHOUT_RECEIPT->shouldBlockRead($STRING)Z
            move-result v0
            if-eqz v0, :read
            return-void
        """,
        ExternalLabel("read", getInstruction(0)),
    )
}
