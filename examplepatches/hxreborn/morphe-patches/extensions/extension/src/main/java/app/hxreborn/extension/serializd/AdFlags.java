/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.serializd;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class AdFlags {

    private static final Pattern ENABLED_FLAG = Pattern
        .compile("\"(shouldShowAds|shouldShowStickyNavigationAds|shouldUseSingletonMrec)\"(\\s*:\\s*)true");

    private AdFlags() {

    }

    public static String disable(String body) {
        if (body == null || !body.contains("\"should")) {
            return body;
        }
        return ENABLED_FLAG.matcher(body).replaceAll("\"$1\"$2false");
    }

}
