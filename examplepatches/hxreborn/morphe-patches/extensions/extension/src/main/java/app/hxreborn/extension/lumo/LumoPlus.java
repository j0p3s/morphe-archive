/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.lumo;

import android.webkit.WebView;
import app.morphe.extension.shared.Logger;

import app.hxreborn.extension.WebAssets;

@SuppressWarnings("unused")
public final class LumoPlus {

    private LumoPlus() {
    }

    public static void unlock(WebView view) {
        try {
            if (view == null) {
                return;
            }
            view.evaluateJavascript(WebAssets.LUMO_PLUS_WEBVIEW, null);
        } catch (Throwable ex) {
            Logger.printException(() -> "Could not inject the Lumo Plus script", ex);
        }
    }

}
