// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/disablevoom: the line://home/ scheme
// handler returns LINE's own "not handled" result, and the standalone VOOM feed finishes after
// super.onCreate. The result singleton is now derived from the handler's return type (its
// "Error(" subclass) instead of being pinned.

package app.template.patches.line.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val disableVoomPatch = bytecodePatch(
    name = "Disable VOOM",
    description = "VOOM links, shares and notifications do nothing, and the standalone VOOM feed " +
        "closes at once. Use with Hide VOOM tab to remove the tab as well.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // 1. The scheme handler returns its "not handled" result at entry: the static singleton
        //    of the result type whose class prints as "Error(...)". The handler itself returns it
        //    for a URL without path segments.
        VoomSchemeHandlerFingerprint.method.apply {
            val resultType = returnType
            val notHandled = classDefBy(resultType).staticFields.filter { field ->
                classDefByOrNull(field.type)?.methods?.any { method ->
                    method.name == "toString" && method.implementation?.instructions?.any {
                        it.getReference<StringReference>()?.string?.startsWith("Error(") == true
                    } == true
                } == true
            }.singleOrNull() ?: throw PatchException("No single Error result singleton on $resultType")
            addInstructions(
                0,
                """
                    sget-object v0, $resultType->${notHandled.name}:${notHandled.type}
                    return-object v0
                """,
            )
        }

        // 2. The standalone feed: finish right after super.onCreate so it never renders.
        val superIndex = LineVoomActivityOnCreateFingerprint.instructionMatches.first().index
        LineVoomActivityOnCreateFingerprint.method.addInstructions(
            superIndex + 1,
            """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """,
        )
    }
}
