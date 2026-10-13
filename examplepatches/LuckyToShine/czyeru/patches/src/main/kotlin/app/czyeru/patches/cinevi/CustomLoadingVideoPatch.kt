package app.czyeru.patches.cinevi

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.filePathOption
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption
import app.czyeru.patches.shared.Constants.COMPATIBILITY_CINEVI
import java.io.File

private const val EXTENSION_CLASS = "Lapp/czyeru/extension/cinevi/CustomLoadingVideo;"
private const val BASE_VIEW = "Lchuangyuan/ycj/videolibrary/widget/BaseView;"
private const val ACTION_CONTROL_VIEW = "Lchuangyuan/ycj/videolibrary/widget/ActionControlView;"

/** Keeps the loading video small. It is stored inside the APK. */
private const val MAX_VIDEO_BYTES = 64L * 1024 * 1024

private val COLOR_REGEX = Regex("#[0-9A-Fa-f]{6}")

/**
 * Not shown in the patch list. Tells the extension whenever the player shows or hides its
 * "first load" screen. Does nothing on its own: without the assets from [customLoadingVideoPatch]
 * the extension leaves the original screen alone.
 */
private val customLoadingVideoHookPatch = bytecodePatch {
    extendWith("extensions/extension.mpe")

    execute {
        // BaseView.A has 3 locals, so v0 is free and p0/p1 can be used directly.
        LoadFirstVisibilityFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, $BASE_VIEW->p:$ACTION_CONTROL_VIEW
                iget-object v0, v0, $ACTION_CONTROL_VIEW->i:Landroid/view/View;
                invoke-static {v0, p1}, $EXTENSION_CLASS->onLoadFirstVisibility(Landroid/view/View;I)V
            """
        )
    }
}

@Suppress("unused")
val customLoadingVideoPatch = rawResourcePatch(
    name = "Custom loading video",
    description = "Replaces the loading screen shown when a video starts with your own video. " +
        "WebM (VP9 or AV1) or MP4 (H.265/HEVC).",
    default = false
) {
    compatibleWith(COMPATIBILITY_CINEVI)

    dependsOn(customLoadingVideoHookPatch)

    val videoPath by filePathOption(
        key = "videoPath",
        default = null,
        title = "Video file",
        description = "The video to show. WebM with VP9 or AV1, or MP4 with H.265/HEVC. " +
            "It is stored inside the app, so keep it short and small.",
        required = true,
        allowedExtensions = listOf("webm", "mp4"),
    )

    val scale by stringOption(
        key = "scale",
        default = "fit",
        values = mapOf(
            "Fit (show everything, may add bars)" to "fit",
            "Fill (crop to cover the screen)" to "fill",
            "Stretch (ignore the aspect ratio)" to "stretch",
        ),
        title = "Scaling",
        description = "How the video is sized inside the loading screen.",
        required = true,
    )

    val loop by booleanOption(
        key = "loop",
        default = true,
        title = "Loop",
        description = "Repeat the video until the real video starts.",
        required = true,
    )

    val mute by booleanOption(
        key = "mute",
        default = true,
        title = "Mute",
        description = "Play the loading video without sound.",
        required = true,
    )

    val hideOriginal by booleanOption(
        key = "hideOriginal",
        default = true,
        title = "Hide the original loading screen",
        description = "Turn off to keep Cinevi's logo and text on top of your video.",
        required = true,
    )

    val background by stringOption(
        key = "background",
        default = "#000000",
        title = "Background color",
        description = "Color behind the video when it does not cover the screen, as #RRGGBB. " +
            "Leave empty to keep the original background.",
        required = false,
    )

    val strictFormat by booleanOption(
        key = "strictFormat",
        default = true,
        title = "Only accept WebM (VP9/AV1) and MP4 (H.265)",
        description = "Turn off to embed any video file. Other formats may not play on every phone.",
        required = true,
    )

    execute {
        val source = File(videoPath ?: throw PatchException("Choose a video file."))
        if (!source.isFile) throw PatchException("Video file not found: $source")
        if (source.length() == 0L) throw PatchException("The video file is empty: $source")
        if (source.length() > MAX_VIDEO_BYTES) {
            throw PatchException(
                "The video is ${source.length() / (1024 * 1024)} MiB. The limit is " +
                    "${MAX_VIDEO_BYTES / (1024 * 1024)} MiB because it is stored inside the app."
            )
        }

        val info = VideoFormat.inspect(source)
        if (strictFormat == true && !info.supported) {
            throw PatchException(
                "Unsupported video: $info. Use WebM with VP9 or AV1, or MP4 with H.265/HEVC " +
                    "(for example 'ffmpeg -i in.mp4 -c:v libx265 -tag:v hvc1 out.mp4'), " +
                    "or turn off 'Only accept WebM (VP9/AV1) and MP4 (H.265)'."
            )
        }

        val color = background?.trim().orEmpty()
        if (color.isNotEmpty() && !COLOR_REGEX.matches(color)) {
            throw PatchException("Background color must look like #RRGGBB, got '$color'.")
        }

        source.copyTo(get("assets/custom_loading_video", false), overwrite = true)

        get("assets/custom_loading_video.properties", false).writeText(
            """
                videoBytes=${source.length()}
                scale=${scale ?: "fit"}
                loop=${loop ?: true}
                mute=${mute ?: true}
                hideOriginal=${hideOriginal ?: true}
                background=$color
            """.trimIndent() + "\n"
        )
    }
}
