package app.template.patches.line.layout

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.util.getReference
import app.template.patches.line.shared.removeConstantAdd
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Removes the `sget-object <tab>` + `ArrayList.add` pair of each of [tabs] from the main tab list
 * builder, so LINE never adds those tabs to the bottom navigation. The tab enum is taken from the
 * builder's own HOME/CHAT/SQUARE/CALL reads, which must all be the same enum.
 */
internal fun BytecodePatchContext.removeMainTabs(vararg tabs: String) {
    val anchorOwners = MainTabListFingerprint.instructionMatches.map {
        it.instruction.getReference<FieldReference>()?.definingClass
    }.toSet()
    val tabEnum = anchorOwners.singleOrNull()
        ?: throw PatchException("Main tab list anchors read different classes: $anchorOwners")
    if (classDefBy(tabEnum).superclass != "Ljava/lang/Enum;") {
        throw PatchException("Main tab type $tabEnum is not an enum")
    }
    MainTabListFingerprint.method.apply {
        tabs.forEach { removeConstantAdd(it, tabEnum) }
    }
}
