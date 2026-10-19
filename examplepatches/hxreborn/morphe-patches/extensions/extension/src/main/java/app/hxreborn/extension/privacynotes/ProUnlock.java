/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.privacynotes;

import android.webkit.WebView;
import app.morphe.extension.shared.Logger;

import app.hxreborn.extension.WebAssets;

@SuppressWarnings("unused")
public final class ProUnlock {

    private ProUnlock() {
    }

    public static void unlock(WebView view) {
        try {
            view.evaluateJavascript(WebAssets.PRIVACYNOTES_PRO_WEBVIEW, null);
        } catch (Throwable ex) {
            Logger.printException(() -> "Could not inject the Privacy Notes Pro script", ex);
        }
    }

}
