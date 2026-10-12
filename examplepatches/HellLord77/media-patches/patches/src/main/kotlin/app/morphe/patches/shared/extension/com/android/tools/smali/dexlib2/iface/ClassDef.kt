package app.morphe.patches.shared.extension.com.android.tools.smali.dexlib2.iface

import com.android.tools.smali.dexlib2.iface.ClassDef

fun ClassDef.isExtension() = startsWith("Lapp/morphe/extension/")

fun ClassDef.isNotExtension() = !isExtension()