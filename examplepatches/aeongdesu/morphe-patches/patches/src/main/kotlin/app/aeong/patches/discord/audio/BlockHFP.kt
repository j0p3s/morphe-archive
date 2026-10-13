package app.aeong.patches.discord.audio

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.aeong.patches.shared.Constants.COMPATIBILITY_DISCORD
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * I don't have to patch webrtc hell wow
 */

@Suppress("unused")
val blockHFP = bytecodePatch(
    name = "Block bluetooth hands-free mode",
    description = "Useful when using bluetooth devices that don't support LE Audio. " +
            "Be aware that this will use the phone's microphone instead!",
    default = true
) {
    compatibleWith(COMPATIBILITY_DISCORD)

    execute {
        /** 
         * setCommunicationModeOn(boolean) -> always false
         */
        SetCommunicationModeOnFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x0
            """
        )

        /** 
         * setActiveAudioDevice(AudioDeviceInfo)
         * -0x7 -> TYPE_BLUETOOTH_SCO
         */
        SetActiveAudioDeviceFingerprint.method.apply {
            val audioManager = getInstruction<ReferenceInstruction>(0).reference as FieldReference

            addInstructions(
                0,
                """
                    invoke-virtual {p1}, Landroid/media/AudioDeviceInfo;->getType()I
                    move-result v0
                    add-int/lit8 v0, v0, -0x7
                    if-nez v0, :other
                    iget-object v0, p0, $audioManager
                    invoke-virtual {v0}, Landroid/media/AudioManager;->clearCommunicationDevice()V
                    return-void
                    :other
                    nop
                """
            )
        }
    }
}
