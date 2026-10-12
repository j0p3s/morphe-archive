package app.template.patches.protonvpn.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.PROTONVPN_COMPATIBILITY

fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return
    try {
        val field = impl.javaClass.getDeclaredField("registerCount")
        field.isAccessible = true
        field.setInt(impl, needed)
    } catch (e: Exception) {}
}

fun MutableMethod.clearBody() {
    val impl = implementation ?: return
    val instructions = impl.instructions.toList()
    instructions.forEach { impl.removeInstruction(0) }
    try {
        val field = impl.javaClass.getDeclaredField("tryBlocks")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(impl) as? java.util.List<*>)?.clear()
    } catch (e: Exception) {}
}

@Suppress("unused")
val protonVpnHideCountriesPatch = bytecodePatch(
    name = "Show Free Countries Only",
    description = "Hide unusable premium countries from the list",
    default = true,
) {
    compatibleWith(PROTONVPN_COMPATIBILITY)

    execute {
        RepositoryFilterServersFingerprint.method.let { method ->
            method.ensureRegisters(9)
            method.clearBody()
            val smali = (
                "invoke-direct {p0}, Lfj/l;->j()Lfj/l\$b;\n" +
                "move-result-object v0\n" +
                "invoke-virtual {v0}, Lfj/l\$b;->d()Ljava/util/List;\n" +
                "move-result-object v0\n" +
                "new-instance v1, Ljava/util/ArrayList;\n" +
                "invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V\n" +
                "invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;\n" +
                "move-result-object v2\n" +
                ":loop\n" +
                "invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z\n" +
                "move-result v3\n" +
                "if-eqz v3, :done\n" +
                "invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;\n" +
                "move-result-object v3\n" +
                "check-cast v3, Lcom/protonvpn/android/servers/Server;\n" +
                "invoke-virtual {v3}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z\n" +
                "move-result v4\n" +
                "if-eqz v4, :loop\n" +
                "invoke-interface {v1, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z\n" +
                "goto :loop\n" +
                ":done\n" +
                "move-object v0, v1\n" +
                "return-object v0"
            )
            method.addInstructions(0, smali)
        }

        RepositoryVpnCountriesFingerprint.method.let { method ->
            method.ensureRegisters(10)
            method.clearBody()
            val smali = (
                "iget-object v0, p0, Lfj/l\$b;->c:Ljava/util/List;\n" +
                "new-instance v1, Ljava/util/ArrayList;\n" +
                "invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V\n" +
                "invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;\n" +
                "move-result-object v2\n" +
                ":country_loop\n" +
                "invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z\n" +
                "move-result v3\n" +
                "if-eqz v3, :country_done\n" +
                "invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;\n" +
                "move-result-object v3\n" +
                "check-cast v3, Lcom/protonvpn/android/models/vpn/VpnCountry;\n" +
                "invoke-virtual {v3}, Lcom/protonvpn/android/models/vpn/VpnCountry;->getServerList()Ljava/util/List;\n" +
                "move-result-object v4\n" +
                "invoke-interface {v4}, Ljava/util/List;->iterator()Ljava/util/Iterator;\n" +
                "move-result-object v5\n" +
                "const/4 v8, 0x0\n" +
                ":server_loop\n" +
                "invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z\n" +
                "move-result v6\n" +
                "if-eqz v6, :server_done\n" +
                "invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;\n" +
                "move-result-object v6\n" +
                "check-cast v6, Lcom/protonvpn/android/servers/Server;\n" +
                "invoke-virtual {v6}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z\n" +
                "move-result v7\n" +
                "if-eqz v7, :server_loop\n" +
                "const/4 v8, 0x1\n" +
                "goto :server_done\n" +
                ":server_done\n" +
                "if-eqz v8, :country_loop\n" +
                "invoke-interface {v1, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z\n" +
                "goto :country_loop\n" +
                ":country_done\n" +
                "move-object v0, v1\n" +
                "return-object v0"
            )
            method.addInstructions(0, smali)
        }
    }
}