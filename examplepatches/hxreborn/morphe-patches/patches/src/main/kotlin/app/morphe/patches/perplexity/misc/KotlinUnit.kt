/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.perplexity.misc

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags

private const val KOTLIN_UNIT = "Lkotlin/Unit;"

internal fun BytecodePatchContext.returnUnitEarly(method: MutableMethod) {
    val instanceField = classDefBy(KOTLIN_UNIT).fields.single {
        AccessFlags.STATIC.isSet(it.accessFlags) && it.type == KOTLIN_UNIT
    }

    method.addInstructions(
        0,
        """
            sget-object v0, $KOTLIN_UNIT->${instanceField.name}:$KOTLIN_UNIT
            return-object v0
        """,
    )
}
