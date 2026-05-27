package com.circlesandholes.game;

/**
 * Dev-only affordances (e.g. the instant-win button). Each platform launcher
 * sets this from its own build type — Android from {@code BuildConfig.DEBUG},
 * desktop from a system property set by the gradle {@code run} task — so it is
 * {@code false} in production release builds, with no platform checks here.
 */
public final class Debug {

    public static boolean enabled = false;

    private Debug() {}
}
