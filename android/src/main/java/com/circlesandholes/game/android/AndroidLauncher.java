package com.circlesandholes.game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.circlesandholes.game.BuildConfig;
import com.circlesandholes.game.Debug;
import com.circlesandholes.game.Intro;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dev affordances on for debug builds, off for release (production) builds.
        Debug.enabled = BuildConfig.DEBUG;
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        initialize(new Intro(), configuration);
    }
}