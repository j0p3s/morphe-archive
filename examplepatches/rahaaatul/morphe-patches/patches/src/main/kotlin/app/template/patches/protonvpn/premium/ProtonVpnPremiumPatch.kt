package app.template.patches.protonvpn.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.PROTONVPN_COMPATIBILITY
import app.template.patches.shared.Constants.PROTONVPN_REPRODUCIBLE_COMPATIBILITY
import app.template.patches.shared.returnEarly

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
        (field.get(impl) as? MutableList<*>)?.clear()
    } catch (e: Exception) {}
}

@Suppress("unused")
val protonVpnPremiumPatch = bytecodePatch(
    name = "Unlock VPN Plus",
    description = "Unlocks Plus features and routes connections through free servers",
) {
    compatibleWith(PROTONVPN_COMPATIBILITY)
    compatibleWith(PROTONVPN_REPRODUCIBLE_COMPATIBILITY)

    execute {
        // === Tier injection at VpnUser constructor ===
        VpnUserConstructorFingerprint.methodOrNull?.addInstructions(
            11,
            "const/16 p2, 0x1",
        )
        VpnUserConstructorFingerprint.methodOrNull?.addInstructions(
            22,
            """
                const/4 p2, 0x3
                invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p12
            """,
        )
        VpnUserConstructorFingerprintV2.methodOrNull?.addInstructions(
            11,
            "const/16 p2, 0x1",
        )
        VpnUserConstructorFingerprintV2.methodOrNull?.addInstructions(
            22,
            """
                const/4 p2, 0x3
                invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p12
            """,
        )

        // === Belt-and-suspenders: patch getters for any cached VpnUser objects ===
        VpnUserGetUserTierFingerprint.methodOrNull?.addInstructions(0, "const/4 v0, 0x3\nreturn v0")
        VpnUserGetMaxTierFingerprint.methodOrNull?.addInstructions(
            0,
            "const/4 v0, 0x3\n" +
            "invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\n" +
            "move-result-object v0\n" +
            "return-object v0",
        )
        VpnUserIsFreeUserFingerprint.methodOrNull?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        VpnUserIsUserPlusOrAboveFingerprint.methodOrNull?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        VpnUserGetUserTierNameFingerprint.methodOrNull?.addInstructions(0,
            "const-string v0, \"vpn2022\"\nreturn-object v0")

        VpnUserGetUserTierFingerprintV2.methodOrNull?.addInstructions(0, "const/4 v0, 0x3\nreturn v0")
        VpnUserGetMaxTierFingerprintV2.methodOrNull?.addInstructions(
            0,
            "const/4 v0, 0x3\n" +
            "invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\n" +
            "move-result-object v0\n" +
            "return-object v0",
        )
        VpnUserIsFreeUserFingerprintV2.methodOrNull?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        VpnUserIsUserPlusOrAboveFingerprintV2.methodOrNull?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        VpnUserGetUserTierNameFingerprintV2.methodOrNull?.addInstructions(0,
            "const-string v0, \"vpn2022\"\nreturn-object v0")

        // === Server access gates (UI only) ===
        ServerGroupGetAvailableFingerprint.methodOrNull?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // === Free-servers-only (makes VPN connection work server-side) ===
        if (ServerListFilterFingerprint.methodOrNull != null) {
            val idx = ServerListFilterFingerprint.instructionMatches[0].index
            ServerListFilterFingerprint.method.removeInstruction(idx + 2)
            ServerListFilterFingerprint.method.removeInstruction(idx + 1)
            ServerListFilterFingerprint.method.removeInstruction(idx)
            ServerListFilterFingerprint.method.removeInstruction(idx - 1)
            ServerListFilterFingerprint.method.addInstructionsWithLabels(idx - 1, """
                invoke-virtual {p6}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
                move-result p0
                if-nez p0, :pass
                const/4 p0, 0x0
                return p0
                :pass
                nop
            """)
        }

        GetBestScoreServerFingerprint.methodOrNull?.addInstructions(0, """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
            move-result-object v1
            :loop
            invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
            move-result v2
            if-eqz v2, :done
            invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
            move-result-object v2
            check-cast v2, Lcom/protonvpn/android/servers/Server;
            invoke-virtual {v2}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
            move-result v3
            if-eqz v3, :loop
            invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            goto :loop
            :done
            invoke-interface {v0}, Ljava/util/List;->isEmpty()Z
            move-result v1
            if-nez v1, :skip
            move-object p1, v0
            :skip
            nop
        """)
        GetBestScoreServerFingerprintV2.methodOrNull?.addInstructions(0, """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
            move-result-object v1
            :loop
            invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
            move-result v2
            if-eqz v2, :done
            invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
            move-result-object v2
            check-cast v2, Lcom/protonvpn/android/servers/Server;
            invoke-virtual {v2}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
            move-result v3
            if-eqz v3, :loop
            invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            goto :loop
            :done
            invoke-interface {v0}, Ljava/util/List;->isEmpty()Z
            move-result v1
            if-nez v1, :skip
            move-object p1, v0
            :skip
            nop
        """)

        // === Feature flags and UI ===
        IsFeatureFlagEnabledFingerprint.methodOrNull?.addInstructions(0, "const/4 p1, 0x1\nreturn p1")

        GetNetShieldAvailabilityFingerprint.methodOrNull?.addInstructions(0, """
            sget-object p0, Lcom/protonvpn/android/netshield/NetShieldAvailability;->AVAILABLE:Lcom/protonvpn/android/netshield/NetShieldAvailability;
            return-object p0
        """)
        GetNetShieldAvailabilityFingerprintV2.methodOrNull?.addInstructions(0, """
            sget-object p0, Lcom/protonvpn/android/netshield/NetShieldAvailability;->AVAILABLE:Lcom/protonvpn/android/netshield/NetShieldAvailability;
            return-object p0
        """)

        GetFilterButtonsFingerprint.methodOrNull?.addInstructions(0, """
            invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
            move-result-object v0
            return-object v0
        """)

        ProfileAvailableTypesFingerprint.methodOrNull?.addInstructions(0, """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            sget-object v1, Lcom/protonvpn/android/profiles/ui/ProfileType;->Standard:Lcom/protonvpn/android/profiles/ui/ProfileType;
            invoke-virtual {v0, v1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
            return-object v0
        """)

        ProfileCountriesFingerprint.methodOrNull?.replaceInstruction(
            ProfileCountriesFingerprint.instructionMatches[0].index,
            "invoke-virtual {p2, v0}, Lcom/protonvpn/android/servers/ServerManager2;->getFreeCountries(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
        )
    }
}

@Suppress("unused")
val protonVpnHideCountriesPatch = bytecodePatch(
    name = "Show Free Countries Only",
    description = "Hide unusable premium countries from the list",
    default = true,
) {
    compatibleWith(PROTONVPN_COMPATIBILITY)
    compatibleWith(PROTONVPN_REPRODUCIBLE_COMPATIBILITY)

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