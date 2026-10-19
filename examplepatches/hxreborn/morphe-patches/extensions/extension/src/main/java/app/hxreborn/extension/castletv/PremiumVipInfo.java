/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.castletv;

@SuppressWarnings("unused")
public final class PremiumVipInfo {

    private static final String END_DATE = "4102358400000";

    private static final String VIP_NAME = "Premium";

    private static final int PREMIUM_TYPE = 2;

    private static final int ACTIVE_STATUS = 0;

    private static Object instance;

    private PremiumVipInfo() {

    }

    public static synchronized Object get(Class<?> vipInfoClass) {
        if (instance == null) {
            try {
                instance = vipInfoClass.getConstructor(String.class, String.class, Integer.class, Integer.class)
                    .newInstance(END_DATE, VIP_NAME, PREMIUM_TYPE, ACTIVE_STATUS);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return instance;
    }

}
