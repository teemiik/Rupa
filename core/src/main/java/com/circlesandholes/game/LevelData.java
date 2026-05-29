package com.circlesandholes.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain description of a level, parsed from assets/levels/levelN.json by
 * {@link LevelLoader}. Adding a level means dropping in a new JSON file — no
 * new Java class. All positions/sizes are multipliers of the screen size
 * (x of w_world, y of h_world), matching how the original levels were authored.
 */
public class LevelData {

    /** Which board/background theme to use: "board"/"background" or "board_2"/"background_2". */
    public String board = "board";
    public String background = "background";

    /** Draw the left/right tilt gesture hints (only the first/tutorial level did this). */
    public boolean showHints = false;

    /** Static holes as {x, y} screen-fraction pairs. */
    public List<float[]> holes = new ArrayList<float[]>();

    /** Holes that oscillate via the shared box_din counter. */
    public List<DynamicHole> dynamicHoles = new ArrayList<DynamicHole>();

    /** Tilting barriers (used by the last level). */
    public List<Barrier> barriers = new ArrayList<Barrier>();

    /**
     * Platforms that rotate continuously, adding dynamic obstacles.
     * The physics body is KinematicBody with angular velocity.
     * Positions/sizes use the same w_world/h_world fractions as barriers.
     */
    public List<RotatingPlatform> rotatingPlatforms = new ArrayList<RotatingPlatform>();

    /**
     * Bounds that flip the box_din oscillation direction, evaluated on the first
     * dynamic hole's x. Levels 4/5 trigger on >= upper / <= lower; the last level
     * inverts that (inverted = true).
     */
    public float ctrlLowerX = 0f;
    public float ctrlUpperX = 0f;
    public boolean ctrlInverted = false;

    /** position = base*screen + din*box_din, where din is -1, 0 or +1 per axis. */
    public static class DynamicHole {
        public float x, y;
        public int xDin, yDin;
    }

    public static class Barrier {
        public float x, y;        // sprite position (fractions of w_world / h_world)
        public float w, h;        // sprite size (fractions of w_world / h_world)
        public float rotation;    // degrees
    }

    /**
     * A platform that rotates continuously around its center during gameplay.
     * Pivot variants can be added later; for now all rotate around center.
     */
    public static class RotatingPlatform {
        public float x, y;        // center position (fractions of w_world / h_world)
        public float w, h;        // size (fractions of w_world / h_world)
        public float rotationSpeed; // degrees per second, positive = clockwise
        public String color = "default";
    }
}
