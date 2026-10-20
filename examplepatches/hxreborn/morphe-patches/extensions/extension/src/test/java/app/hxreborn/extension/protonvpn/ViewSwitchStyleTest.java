/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.hxreborn.extension.protonvpn;

import static org.junit.Assert.assertNull;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import app.hxreborn.extension.proton.PatchedBuild;

@RunWith(RobolectricTestRunner.class)
public final class ViewSwitchStyleTest {

    private final View view = new View(RuntimeEnvironment.getApplication());

    @Test
    public void offersNoThumbWithoutTheHostStyles() {
        assertNull(ViewSwitchStyle.thumb(this.view));
    }

    @Test
    public void offersNoTrackWithoutTheHostStyles() {
        assertNull(ViewSwitchStyle.track(this.view));
    }

    @Test
    @Config(qualifiers = "night")
    public void offersNoThumbInDarkThemeWithoutTheHostStyles() {
        PatchedBuild.useApplicationContext();

        assertNull(ViewSwitchStyle.thumb(this.view));
        assertNull(ViewSwitchStyle.track(this.view));
    }

}
