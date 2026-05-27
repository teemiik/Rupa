package com.circlesandholes.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Small persistence layer over libGDX {@link Preferences}: per-level best times
 * (which also double as the "completed" flag) and the control-scheme setting.
 * Works on every backend; survives restarts.
 */
public final class Progress {

    private static final String FILE = "rupa.progress";

    private Progress() {}

    private static Preferences prefs() {
        return Gdx.app.getPreferences(FILE);
    }

    /** Best time for a level in seconds, or -1 if never finished. */
    public static int bestTime(int level) {
        return prefs().getInteger("best_" + level, -1);
    }

    public static boolean isCompleted(int level) {
        return bestTime(level) >= 0;
    }

    /** Stores the time if it beats the current record (or there is none yet). */
    public static void recordTime(int level, int seconds) {
        int current = bestTime(level);
        if (current < 0 || seconds < current) {
            Preferences p = prefs();
            p.putInteger("best_" + level, seconds);
            p.flush();
        }
    }

    /** Formats seconds as MM:SS for display. */
    public static String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return (m < 10 ? "0" + m : String.valueOf(m)) + ":" + (s < 10 ? "0" + s : String.valueOf(s));
    }
}
