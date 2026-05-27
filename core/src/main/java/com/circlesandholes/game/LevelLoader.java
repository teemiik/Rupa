package com.circlesandholes.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Loads {@link LevelData} from assets/levels/levelN.json using JsonReader /
 * JsonValue (manual parsing, no reflection) so it works on every backend
 * including GWT.
 */
public final class LevelLoader {

    private LevelLoader() {}

    /** Number of contiguous level files present (level1.json, level2.json, ...). */
    public static int count() {
        int n = 0;
        while (Gdx.files.internal("levels/level" + (n + 1) + ".json").exists()) {
            n++;
        }
        return n;
    }

    public static LevelData load(int level) {
        LevelData data = new LevelData();
        JsonValue root = new JsonReader().parse(Gdx.files.internal("levels/level" + level + ".json"));

        data.board = root.getString("board", "board");
        data.background = root.getString("background", "background");
        data.showHints = root.getBoolean("showHints", false);

        JsonValue holes = root.get("holes");
        if (holes != null) {
            for (JsonValue h = holes.child; h != null; h = h.next) {
                data.holes.add(new float[]{ h.getFloat("x"), h.getFloat("y") });
            }
        }

        JsonValue dyn = root.get("dynamicHoles");
        if (dyn != null) {
            for (JsonValue d = dyn.child; d != null; d = d.next) {
                LevelData.DynamicHole dh = new LevelData.DynamicHole();
                dh.x = d.getFloat("x");
                dh.y = d.getFloat("y");
                dh.xDin = d.getInt("xDin", 0);
                dh.yDin = d.getInt("yDin", 0);
                data.dynamicHoles.add(dh);
            }
        }

        JsonValue ctrl = root.get("dynamicControl");
        if (ctrl != null) {
            data.ctrlLowerX = ctrl.getFloat("lowerX", 0f);
            data.ctrlUpperX = ctrl.getFloat("upperX", 0f);
            data.ctrlInverted = ctrl.getBoolean("inverted", false);
        }

        JsonValue bars = root.get("barriers");
        if (bars != null) {
            for (JsonValue b = bars.child; b != null; b = b.next) {
                LevelData.Barrier bar = new LevelData.Barrier();
                bar.x = b.getFloat("x");
                bar.y = b.getFloat("y");
                bar.w = b.getFloat("w");
                bar.h = b.getFloat("h");
                bar.rotation = b.getFloat("rotation", 0f);
                data.barriers.add(bar);
            }
        }
        return data;
    }
}
