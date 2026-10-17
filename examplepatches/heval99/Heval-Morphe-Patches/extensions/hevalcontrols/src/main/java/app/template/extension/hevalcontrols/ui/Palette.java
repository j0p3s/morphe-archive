package app.template.extension.hevalcontrols.ui;

import android.content.Context;
import android.content.res.Configuration;

import app.template.extension.hevalcontrols.HevalSettings;

/** Colour tokens for the settings screen. Framework only: the .mpe carries no resources. */
final class Palette {
    final boolean light;
    final int background, surface, text, muted, accent, onAccent, line, outline, track, ripple;
    final int warning, warningSurface, error, errorSurface, success;

    private Palette(boolean light, boolean amoled) {
        this.light = light;
        if (light) {
            background = 0xfff6f6f9;
            surface = 0xffffffff;
            text = 0xff1b1b21;
            muted = 0xff5c5d6a;
            accent = 0xff2f55c4;
            onAccent = 0xffffffff;
            line = 0xffdcdde5;
            outline = 0xff7d7f8c;
            track = 0xffd3d5de;
            ripple = 0x222f55c4;
            warning = 0xff6f4a00;
            warningSurface = 0xffffefc7;
            error = 0xffa8231b;
            errorSurface = 0xffffdad5;
            success = 0xff1d6b35;
        } else {
            background = amoled ? 0xff000000 : 0xff121214;
            surface = amoled ? 0xff141419 : 0xff1e1f25;
            text = 0xffeeeef3;
            muted = 0xffaeb0bd;
            accent = 0xffa7bcff;
            onAccent = 0xff0e1b45;
            line = amoled ? 0xff2a2c34 : 0xff34363f;
            outline = 0xff8b8d9a;
            track = 0xff3b3d47;
            ripple = 0x33a7bcff;
            warning = 0xffffcf70;
            warningSurface = amoled ? 0xff2a1f00 : 0xff352a06;
            error = 0xffffb4aa;
            errorSurface = amoled ? 0xff3a0a06 : 0xff4a120c;
            success = 0xff8fd9a0;
        }
    }

    /** The palette for the saved theme choice; "system" follows the night mode. */
    static Palette resolve(Context context, String theme) {
        if (HevalSettings.THEME_LIGHT.equals(theme)) return new Palette(true, false);
        if (HevalSettings.THEME_DARK.equals(theme)) return new Palette(false, false);
        if (HevalSettings.THEME_AMOLED.equals(theme)) return new Palette(false, true);
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return new Palette(night != Configuration.UI_MODE_NIGHT_YES, false);
    }
}
