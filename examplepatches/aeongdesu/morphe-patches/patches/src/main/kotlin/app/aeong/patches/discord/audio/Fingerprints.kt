package app.aeong.patches.discord.audio

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

private const val AUDIO_MANAGER = "Landroid/media/AudioManager;"

/**
 * idk but stays on A2DP even it's discord's OpenSL stream
 */

/**
 * Thanks to discord android datamine :yay:
 * DiscordAudioManager2.setCommunicationModeOn(boolean)
 */
internal object SetCommunicationModeOnFingerprint : Fingerprint(
    name = "setCommunicationModeOn",
    parameters = listOf("Z"),
    returnType = "V",
    filters = listOf(methodCall(definingClass = AUDIO_MANAGER, name = "clearCommunicationDevice")),
)

/**
 * DiscordAudioManager2.setActiveAudioDevice(AudioDeviceInfo)
 * why? js layer calls this method independently, also when RTC is connected
 */
internal object SetActiveAudioDeviceFingerprint : Fingerprint(
    name = "setActiveAudioDevice",
    parameters = listOf("Landroid/media/AudioDeviceInfo;"),
    returnType = "V",
    filters = listOf(methodCall(definingClass = AUDIO_MANAGER, name = "setCommunicationDevice")),
)
