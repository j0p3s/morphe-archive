/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.moonreader;

import android.content.Context;
import android.content.ContextWrapper;

@SuppressWarnings("unused")
public final class ProPackageContext extends ContextWrapper {

    private static final String PRO_PACKAGE = "com.flyersoft.moonreaderp";

    private ProPackageContext(Context base) {
        super(base);
    }

    public static Context wrap(Context base) {
        return new ContextWrapper(new ProPackageContext(base));
    }

    @Override
    public String getPackageName() {
        return PRO_PACKAGE;
    }

}
