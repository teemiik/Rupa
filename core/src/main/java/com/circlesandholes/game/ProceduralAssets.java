package com.circlesandholes.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Generates the game's primitive art at runtime instead of shipping PNGs at
 * several fixed resolutions. The ball, holes, board and barriers are just
 * circles and rectangles, and the backgrounds are vertical gradients, so they
 * can be produced procedurally at the exact size the current screen needs.
 * This removes the resolution "buckets" that used to pick between pre-rendered
 * assets, and makes recolouring/theming a one-line change.
 */
public final class ProceduralAssets {

    private ProceduralAssets() {}

    /**
     * Anti-aliased filled circle of the given diameter (px), used for the ball
     * and holes. Coverage is computed analytically per pixel — full colour
     * inside the radius, fading to transparent across a ~1px edge — which gives
     * clean edges at any size without the aliasing a Pixmap#fillCircle leaves.
     */
    public static Texture circle(int diameter, Color color) {
        if (diameter < 2) diameter = 2;

        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None); // store straight alpha, no source-over
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        // Visible radius tuned so the disc's antialiased edge lands right on the
        // physics radius (BoxCircle uses diameter/2): half a pixel less makes the
        // ball rest on the board with neither overlap (sinking) nor a gap.
        float r = Math.max(1f, diameter / 2f - 0.5f);
        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - r;
                float dy = y + 0.5f - r;
                float edge = r - (float) Math.sqrt(dx * dx + dy * dy);
                float alpha = edge >= 1f ? 1f : (edge <= 0f ? 0f : edge);
                if (alpha > 0f) {
                    pm.setColor(color.r, color.g, color.b, alpha * color.a);
                    pm.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    /**
     * Anti-aliased left-pointing chevron ("back" arrow) in a square texture.
     * Drawn as two thick strokes meeting at the left tip, with coverage based
     * on distance to those strokes so the diagonals stay smooth.
     */
    public static Texture backArrow(int size, Color color) {
        if (size < 4) size = 4;

        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float pad = size * 0.28f;
        float tipX = pad, tipY = size / 2f;          // left point
        float farX = size - pad;                     // right ends
        float topY = pad, botY = size - pad;
        float half = size * 0.085f;                  // half stroke width

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float px = x + 0.5f, py = y + 0.5f;
                float d = Math.min(
                        distanceToSegment(px, py, tipX, tipY, farX, topY),
                        distanceToSegment(px, py, tipX, tipY, farX, botY));
                float edge = half - d;
                float alpha = edge >= 1f ? 1f : (edge <= 0f ? 0f : edge);
                if (alpha > 0f) {
                    pm.setColor(color.r, color.g, color.b, alpha * color.a);
                    pm.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    private static float distanceToSegment(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float len2 = dx * dx + dy * dy;
        float t = len2 <= 0f ? 0f : ((px - ax) * dx + (py - ay) * dy) / len2;
        if (t < 0f) t = 0f; else if (t > 1f) t = 1f;
        float cx = ax + t * dx, cy = ay + t * dy;
        float ex = px - cx, ey = py - cy;
        return (float) Math.sqrt(ex * ex + ey * ey);
    }

    /**
     * Small solid-colour texture for the board and barriers. Kept tiny on
     * purpose: callers stretch it to the needed size with Sprite#setSize, so a
     * single texture covers any dimension without leaking memory on resize.
     */
    public static Texture solid(Color color) {
        Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    /**
     * Vertical two-colour gradient used for backgrounds. Rendered into a thin
     * tall strip and stretched across the screen; linear filtering hides the
     * low width. Pixmap row 0 (top colour) ends up at the top of the screen
     * because callers wrap this in a Sprite, which corrects the texture flip.
     */
    public static Texture gradient(Color top, Color bottom) {
        int w = 8;
        int h = 256;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = (float) y / (h - 1);
            float r = top.r + (bottom.r - top.r) * t;
            float g = top.g + (bottom.g - top.g) * t;
            float b = top.b + (bottom.b - top.b) * t;
            pm.setColor(r, g, b, 1f);
            pm.drawLine(0, y, w, y);
        }
        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }

    /** Ball: filled disc with a soft radial highlight (upper-left) for a rounded look. */
    public static Texture ball(int diameter, Color base) {
        if (diameter < 2) diameter = 2;
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float r = Math.max(1f, diameter / 2f - 0.5f);
        float cx = diameter / 2f, cy = diameter / 2f;
        float hx = diameter * 0.36f, hy = diameter * 0.34f; // highlight centre (pixmap y is top-down)
        float hr = diameter * 0.6f;

        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
                float alpha = r - (float) Math.sqrt(dx * dx + dy * dy);
                if (alpha <= 0f) continue;
                if (alpha > 1f) alpha = 1f;

                float hdx = x + 0.5f - hx, hdy = y + 0.5f - hy;
                float hl = 1f - (float) Math.sqrt(hdx * hdx + hdy * hdy) / hr;
                hl = (hl <= 0f ? 0f : hl) * 0.45f;

                pm.setColor(clamp01(base.r + hl), clamp01(base.g + hl), clamp01(base.b + hl), alpha * base.a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Hole: filled disc shaded darker in the centre and lighter at the rim, so it reads as recessed. */
    public static Texture hole(int diameter, Color base) {
        if (diameter < 2) diameter = 2;
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float r = Math.max(1f, diameter / 2f - 0.5f);
        float cx = diameter / 2f, cy = diameter / 2f;

        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha = r - dist;
                if (alpha <= 0f) continue;
                if (alpha > 1f) alpha = 1f;

                float shade = (dist / r - 0.5f) * 0.22f; // <0 near centre (darker), >0 near rim (lighter)
                pm.setColor(clamp01(base.r + shade), clamp01(base.g + shade), clamp01(base.b + shade), alpha * base.a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Anti-aliased check mark, drawn as two strokes (used to mark completed levels). */
    public static Texture check(int size, Color color) {
        if (size < 4) size = 4;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float ax = size * 0.20f, ay = size * 0.55f;
        float bx = size * 0.42f, by = size * 0.74f;
        float cx = size * 0.80f, cy = size * 0.28f;
        float half = size * 0.09f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float px = x + 0.5f, py = y + 0.5f;
                float d = Math.min(distanceToSegment(px, py, ax, ay, bx, by),
                        distanceToSegment(px, py, bx, by, cx, cy));
                float alpha = half - d;
                if (alpha <= 0f) continue;
                if (alpha > 1f) alpha = 1f;
                pm.setColor(color.r, color.g, color.b, alpha * color.a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Pause glyph: two vertical bars. */
    public static Texture pauseIcon(int size, Color color) {
        if (size < 4) size = 4;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        pm.setColor(color);
        int barW = Math.max(1, Math.round(size * 0.22f));
        int barH = Math.round(size * 0.7f);
        int top = Math.round(size * 0.15f);
        int gap = Math.round(size * 0.16f);
        int leftX = Math.round(size / 2f - gap / 2f - barW);
        pm.fillRectangle(leftX, top, barW, barH);
        pm.fillRectangle(Math.round(size / 2f + gap / 2f), top, barW, barH);
        return toTexture(pm);
    }

    /** Power / quit glyph: an open ring with a vertical bar at the top. */
    public static Texture powerIcon(int size, Color color) {
        if (size < 8) size = 8;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float cx = size / 2f, cy = size / 2f;
        float r = size * 0.30f;
        float sw = size * 0.075f;            // ring half-thickness
        float barHalf = size * 0.06f;
        float barTopY = cy - size * 0.40f;
        float barBotY = cy - size * 0.02f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float px = x + 0.5f, py = y + 0.5f;
                float dx = px - cx, dy = py - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                float ring = sw - Math.abs(dist - r);
                if (dy < 0f && Math.abs(dx) < barHalf * 2.2f) {
                    ring = -1f; // gap at the top where the bar sits
                }
                float bar = barHalf - distanceToSegment(px, py, cx, barTopY, cx, barBotY);

                float a = Math.max(ring, bar);
                if (a > 0f) {
                    if (a > 1f) a = 1f;
                    pm.setColor(color.r, color.g, color.b, a * color.a);
                    pm.drawPixel(x, y);
                }
            }
        }
        return toTexture(pm);
    }

    /** White disc with a soft radial alpha falloff — tint at draw time for shadows or glows. */
    public static Texture softCircle(int size) {
        if (size < 2) size = 2;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x + 0.5f - r, dy = y + 0.5f - r;
                float t = (float) Math.sqrt(dx * dx + dy * dy) / r;
                if (t >= 1f) continue;
                float a = 1f - t;
                a *= a; // softer toward the edge
                pm.setColor(1f, 1f, 1f, a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Anti-aliased filled rounded rectangle, for panels behind text. */
    public static Texture roundRect(int w, int h, float radius, Color color) {
        if (w < 2) w = 2;
        if (h < 2) h = 2;
        if (radius < 0f) radius = 0f;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        float hw = w / 2f, hh = h / 2f;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float qx = Math.abs(x + 0.5f - hw) - (hw - radius);
                float qy = Math.abs(y + 0.5f - hh) - (hh - radius);
                float mx = Math.max(qx, 0f), my = Math.max(qy, 0f);
                float d = (float) Math.sqrt(mx * mx + my * my) + Math.min(Math.max(qx, qy), 0f) - radius;
                float a = 0.5f - d;
                if (a <= 0f) continue;
                if (a > 1f) a = 1f;
                pm.setColor(color.r, color.g, color.b, a * color.a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Rounded rectangle filled with a vertical gradient (top → bottom). Generate at the real size so corners aren't stretched. */
    public static Texture roundRectGradient(int w, int h, float radius, Color top, Color bottom) {
        if (w < 2) w = 2;
        if (h < 2) h = 2;
        if (radius < 0f) radius = 0f;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        float hw = w / 2f, hh = h / 2f;
        for (int y = 0; y < h; y++) {
            float ty = (float) y / (h - 1);
            float r = top.r + (bottom.r - top.r) * ty;
            float g = top.g + (bottom.g - top.g) * ty;
            float b = top.b + (bottom.b - top.b) * ty;
            for (int x = 0; x < w; x++) {
                float qx = Math.abs(x + 0.5f - hw) - (hw - radius);
                float qy = Math.abs(y + 0.5f - hh) - (hh - radius);
                float mx = Math.max(qx, 0f), my = Math.max(qy, 0f);
                float d = (float) Math.sqrt(mx * mx + my * my) + Math.min(Math.max(qx, qy), 0f) - radius;
                float a = 0.5f - d;
                if (a <= 0f) continue;
                if (a > 1f) a = 1f;
                pm.setColor(r, g, b, a);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Rich background baked into one (small, stretched) texture: vertical gradient + soft upper glow + corner vignette. */
    public static Texture background(Color top, Color bottom, Color glow) {
        int w = 128, h = 220;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float ty = (float) y / (h - 1);
            float br = top.r + (bottom.r - top.r) * ty;
            float bg = top.g + (bottom.g - top.g) * ty;
            float bb = top.b + (bottom.b - top.b) * ty;
            for (int x = 0; x < w; x++) {
                float tx = (float) x / (w - 1);
                float r = br, g = bg, b = bb;

                float gdx = tx - 0.5f, gdy = ty - 0.3f;
                float gA = 1f - (float) Math.sqrt(gdx * gdx + gdy * gdy) / 0.55f;
                if (gA > 0f) {
                    gA *= gA * 0.22f;
                    r += (glow.r - r) * gA;
                    g += (glow.g - g) * gA;
                    b += (glow.b - b) * gA;
                }

                float vdx = tx - 0.5f, vdy = ty - 0.5f;
                float vig = ((float) Math.sqrt(vdx * vdx + vdy * vdy) - 0.4f) / 0.4f;
                if (vig > 0f) {
                    if (vig > 1f) vig = 1f;
                    float darken = 1f - vig * vig * 0.4f;
                    r *= darken;
                    g *= darken;
                    b *= darken;
                }
                pm.setColor(clamp01(r), clamp01(g), clamp01(b), 1f);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static Texture toTexture(Pixmap pm) {
        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return texture;
    }
}
