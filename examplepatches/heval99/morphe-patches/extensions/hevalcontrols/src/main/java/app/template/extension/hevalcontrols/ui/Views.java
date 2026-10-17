// Based on SysAdminDoc/HushMessenger (GPL-3.0), extensions/messenger/src/main/java/app/hushmessenger/extension/SettingsUi.java:
// framework-only view builders (text, headings, cards, ripple + focus-ring backgrounds, 48 dp buttons, styled
// Switch); modified 2026-10-14: minSdk 23 guards (accessibility heading on API 28+), RTL-safe relative padding
// and alignment, Palette tokens, no field/pseudo-locale helpers.

package app.template.extension.hevalcontrols.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

/** Framework-only view builders, so the screen needs no host resources. */
final class Views {
    final Context context;
    final Palette palette;

    Views(Context context, Palette palette) {
        this.context = context;
        this.palette = palette;
    }

    int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    LinearLayout column() {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }

    LinearLayout row() {
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    TextView text(CharSequence value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        view.setIncludeFontPadding(false);
        view.setLineSpacing(dp(2), 1);
        view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        view.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        return view;
    }

    TextView heading(String value) {
        TextView view = text(value.toUpperCase(Locale.getDefault()), 13, palette.muted, true);
        view.setLetterSpacing(0.06f);
        markHeading(view);
        return view;
    }

    static void markHeading(View view) {
        if (Build.VERSION.SDK_INT >= 28) view.setAccessibilityHeading(true);
    }

    GradientDrawable shape(int fill, int border, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (border != 0) drawable.setStroke(dp(1), border);
        return drawable;
    }

    /** Ripple with a visible focus ring for keyboard and switch-access users. */
    Drawable interactive(int fill, int border, int radiusDp, int focus) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable focused = shape(fill, focus, radiusDp);
        focused.setStroke(dp(2), focus);
        states.addState(new int[] {android.R.attr.state_focused}, focused);
        states.addState(new int[] {}, shape(fill, border, radiusDp));
        return new RippleDrawable(ColorStateList.valueOf(palette.ripple), states, shape(0xffffffff, 0, radiusDp));
    }

    LinearLayout card(int fill, int border) {
        LinearLayout view = column();
        view.setPaddingRelative(dp(16), dp(14), dp(16), dp(14));
        view.setBackground(shape(fill, border, 14));
        return view;
    }

    Button button(String title) {
        Button view = new Button(context);
        view.setText(title);
        view.setTextSize(14);
        view.setAllCaps(false);
        view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        view.setTextColor(new ColorStateList(new int[][] {{-android.R.attr.state_enabled}, {}},
                new int[] {palette.muted, palette.accent}));
        view.setMinHeight(dp(48));
        view.setMinimumHeight(dp(48));
        view.setMinWidth(0);
        view.setMinimumWidth(0);
        view.setPaddingRelative(dp(14), dp(8), dp(14), dp(8));
        view.setBackground(interactive(palette.surface, palette.outline, 12, palette.accent));
        view.setStateListAnimator(null);
        return view;
    }

    Button primaryButton(String title) {
        Button view = button(title);
        view.setTextColor(palette.onAccent);
        view.setBackground(interactive(palette.accent, 0, 12, palette.outline));
        return view;
    }

    /** Adds [child] full width below the previous one. */
    void add(LinearLayout parent, View child, int topDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(topDp);
        parent.addView(child, params);
    }

    void divider(LinearLayout parent) {
        View line = new View(context);
        line.setBackgroundColor(palette.line);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(1) / 2));
        parent.addView(line, params);
    }

    /** A small pill label such as "Restart". */
    TextView badge(String value, int color, int fill) {
        TextView view = text(value, 11, color, true);
        view.setPaddingRelative(dp(8), dp(3), dp(8), dp(3));
        view.setBackground(shape(fill, 0, 10));
        return view;
    }

    /** A Switch drawn from palette shapes; not focusable on its own (its row is). */
    @SuppressWarnings("deprecation")
    Switch toggle(boolean checked) {
        Switch view = new Switch(context);
        view.setShowText(false);
        view.setSplitTrack(false);
        view.setSwitchMinWidth(dp(44));
        view.setMinWidth(dp(48));
        view.setMinHeight(dp(48));
        view.setThumbTintList(null);
        view.setTrackTintList(null);
        StateListDrawable thumbs = new StateListDrawable();
        GradientDrawable off = shape(palette.light ? 0xffffffff : 0xffdfe2ea, palette.outline, 11);
        off.setSize(dp(22), dp(22));
        GradientDrawable on = shape(palette.onAccent, palette.accent, 11);
        on.setSize(dp(22), dp(22));
        GradientDrawable disabled = shape(palette.muted, 0, 11);
        disabled.setSize(dp(22), dp(22));
        thumbs.addState(new int[] {-android.R.attr.state_enabled}, disabled);
        thumbs.addState(new int[] {android.R.attr.state_checked}, on);
        thumbs.addState(new int[] {}, off);
        view.setThumbDrawable(thumbs);
        StateListDrawable tracks = new StateListDrawable();
        GradientDrawable trackOn = shape(palette.accent, palette.accent, 12);
        trackOn.setSize(dp(44), dp(24));
        GradientDrawable trackOff = shape(palette.track, palette.outline, 12);
        trackOff.setSize(dp(44), dp(24));
        tracks.addState(new int[] {android.R.attr.state_checked}, trackOn);
        tracks.addState(new int[] {}, trackOff);
        view.setTrackDrawable(tracks);
        view.setChecked(checked);
        // The row carries focus, the click and the spoken description.
        view.setFocusable(false);
        view.setClickable(false);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return view;
    }
}
