/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.protonmail;

import app.hxreborn.extension.proton.PatchSettings;

@SuppressWarnings("unused")
public final class SidebarEntries {

    public static final String SUBSCRIPTION = "hide_subscription";

    public static final String REPORT_A_PROBLEM = "hide_report_a_problem";

    private SidebarEntries() {

    }

    public static boolean isPatched() {
        return false;
    }

    public static boolean isSubscriptionHidden() {
        return isHidden(SUBSCRIPTION);
    }

    public static boolean isReportAProblemHidden() {
        return isHidden(REPORT_A_PROBLEM);
    }

    private static boolean isHidden(String key) {
        return isPatched() && PatchSettings.isEnabled(key, false);
    }

}
