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

    /** Soft 3D sphere: radial specular highlight (upper-left) plus gentle darkening
     *  toward the edges and an anti-aliased outline.  Replaces the banded pixel-art
     *  circles in the level-select screen. */
    public static Texture sphere(int diameter, Color base) {
        if (diameter < 2) diameter = 2;
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float r = diameter / 2f;
        float cx = r, cy = r;
        float hx = diameter * 0.36f, hy = diameter * 0.34f;
        float hr = diameter * 0.55f;

        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float edge = r - dist;
                if (edge <= 0f) continue;
                float alpha = edge >= 1f ? 1f : edge;

                float t = dist / r;
                float shade = -t * t * 0.35f;

                float hdx = x + 0.5f - hx, hdy = y + 0.5f - hy;
                float hl = 1f - (float) Math.sqrt(hdx * hdx + hdy * hdy) / hr;
                hl = (hl <= 0f ? 0f : hl) * 0.45f;

                pm.setColor(
                    clamp01(base.r + shade + hl),
                    clamp01(base.g + shade + hl),
                    clamp01(base.b + shade + hl),
                    alpha * base.a
                );
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Circle filled with a vertical gradient (top → bottom), opaque, anti-aliased edge.
     *  Matches the look of the pause/result panel backgrounds. */
    public static Texture gradientCircle(int diameter, Color top, Color bottom) {
        if (diameter < 2) diameter = 2;
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float r = diameter / 2f;
        for (int y = 0; y < diameter; y++) {
            float ty = (float) y / (diameter - 1);
            float cr = clamp01(top.r + (bottom.r - top.r) * ty);
            float cg = clamp01(top.g + (bottom.g - top.g) * ty);
            float cb = clamp01(top.b + (bottom.b - top.b) * ty);
            float ca = top.a + (bottom.a - top.a) * ty;
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - r, dy = y + 0.5f - r;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float edge = r - dist;
                if (edge <= 0f) continue;
                float alpha = edge >= 1f ? 1f : edge;
                pm.setColor(cr, cg, cb, alpha * ca);
                pm.drawPixel(x, y);
            }
        }
        return toTexture(pm);
    }

    /** Hard-edged pixel-art circle with banded shading. */
    public static Texture pixelCircle(int diameter, Color base) {
        if (diameter < 2) diameter = 2;
        Pixmap pm = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        float r = diameter / 2f;
        float cx = diameter / 2f, cy = diameter / 2f;

        for (int y = 0; y < diameter; y++) {
            for (int x = 0; x < diameter; x++) {
                float dx = x + 0.5f - cx, dy = y + 0.5f - cy;
                float distSq = dx * dx + dy * dy;
                if (distSq >= r * r) continue;

                float shade = 0f;
                if (dx < -0.3f && dy < -0.3f) shade = 0.18f;
                else if (dx > 0.3f && dy > 0.3f) shade = -0.10f;

                pm.setColor(
                    clamp01(base.r + shade),
                    clamp01(base.g + shade),
                    clamp01(base.b + shade),
                    base.a
                );
                pm.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        return texture;
    }

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

    /** Pixel-art bar/platform texture with hard-edged highlight/shadow bands. */
    public static Texture pixelBarTexture(Color base) {
        int w = 16, h = 6;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        Color highlight = new Color(
            clamp01(base.r + 0.25f), clamp01(base.g + 0.25f), clamp01(base.b + 0.25f), base.a);
        Color shadow = new Color(
            clamp01(base.r - 0.15f), clamp01(base.g - 0.15f), clamp01(base.b - 0.15f), base.a);
        Color darkShadow = new Color(
            clamp01(base.r - 0.30f), clamp01(base.g - 0.30f), clamp01(base.b - 0.30f), base.a);

        for (int x = 0; x < w; x++) {
            pm.setColor(highlight);
            pm.drawPixel(x, h - 1);
            pm.setColor(base);
            pm.drawPixel(x, h - 2);
            pm.drawPixel(x, h - 3);
            pm.drawPixel(x, h - 4);
            pm.setColor(shadow);
            pm.drawPixel(x, 1);
            pm.setColor(darkShadow);
            pm.drawPixel(x, 0);
        }

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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

    /** Hard-edged pixel-art check mark. */
    public static Texture pixelCheck(int size, Color color) {
        if (size < 4) size = 4;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();

        pm.setColor(color.r, color.g, color.b, color.a);
        int ax = Math.round(size * 0.20f);
        int ay = Math.round(size * 0.55f);
        int bx = Math.round(size * 0.42f);
        int by = Math.round(size * 0.74f);
        int cx = Math.round(size * 0.80f);
        int cy = Math.round(size * 0.28f);
        pm.drawLine(ax, ay, bx, by);
        pm.drawLine(bx, by, cx, cy);

        Texture texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();
        return texture;
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

    /** Soft drop-shadow circle: black with a gaussian-like falloff and baked-in alpha.
     *  Callers draw it with white vertex colour and offset the position. */
    public static Texture dropShadow(int size, float maxAlpha) {
        if (size < 2) size = 2;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        float r = size / 2f;
        float falloff = 1.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x + 0.5f - r, dy = y + 0.5f - r;
                float t = (float) Math.sqrt(dx * dx + dy * dy) / r;
                if (t >= 1f) continue;
                float a = maxAlpha * (float) Math.pow(1f - t, falloff);
                pm.setColor(0f, 0f, 0f, a);
                pm.drawPixel(x, y);
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

    /** Rounded rectangle filled with a vertical gradient (top → bottom) with a 1‑pixel border. */
    public static Texture roundRectGradientBordered(int w, int h, float radius, Color borderColor, Color top, Color bottom) {
        if (w < 3) w = 3;
        if (h < 3) h = 3;
        if (radius < 1f) radius = 1f;
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
                boolean border = x == 0 || x == w - 1 || y == 0 || y == h - 1
                        || (a < 1f && (x < radius || x >= w - radius || y < radius || y >= h - radius));
                if (border) {
                    pm.setColor(borderColor.r, borderColor.g, borderColor.b, a * borderColor.a);
                } else {
                    pm.setColor(r, g, b, a);
                }
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

    /** Pixel-art-style background: gradient + glow + vignette + stars + terrain + clouds + posterization. */
    public static Texture backgroundPixel(Color top, Color bottom, Color glow) {
        int w = 128, h = 220;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        long seed = (long) (top.r * 1000) ^ ((long) (top.g * 1000) << 10)
                ^ ((long) (top.b * 1000) << 20) ^ ((long) (bottom.r * 1000) << 5)
                ^ ((long) (bottom.g * 1000) << 15) ^ ((long) (bottom.b * 1000) << 25);
        java.util.Random rng = new java.util.Random(seed);

        float[] terrain = new float[w];
        float baseH = 16 + rng.nextFloat() * 20;
        for (int x = 0; x < w; x++) {
            float n = (float) (Math.sin(x * 0.18 + seed * 0.01) * 0.5
                    + Math.sin(x * 0.07 + seed * 0.02) * 0.3
                    + Math.sin(x * 0.03 + seed * 0.03) * 0.2);
            terrain[x] = baseH + (n + 1f) * 0.5f * baseH * 0.8f;
        }

        int cloudY = (int) (h * (0.12f + rng.nextFloat() * 0.25f));
        int cloudW = 10 + rng.nextInt(18);
        int cloudX = 5 + rng.nextInt(w - cloudW - 10);
        boolean[][] cloud = new boolean[cloudW][8];
        for (int cy = 0; cy < 8; cy++) {
            for (int cx = 0; cx < cloudW; cx++) {
                float n = (float) (Math.sin(cx * 0.7 + cy * 1.2 + seed * 0.005) * 0.5 + 0.5);
                cloud[cx][cy] = n > 0.45f;
            }
        }

        int cloudY2 = (int) (h * (0.30f + rng.nextFloat() * 0.15f));
        int cloudW2 = 7 + rng.nextInt(14);
        int cloudX2 = 30 + rng.nextInt(w - cloudW2 - 40);
        boolean[][] cloud2 = new boolean[cloudW2][6];
        for (int cy = 0; cy < 6; cy++) {
            for (int cx = 0; cx < cloudW2; cx++) {
                float n = (float) (Math.sin(cx * 0.6 + cy * 1.4 + seed * 0.007 + 50) * 0.5 + 0.5);
                cloud2[cx][cy] = n > 0.5f;
            }
        }

        for (int y = 0; y < h; y++) {
            float ty = (float) y / (h - 1);
            for (int x = 0; x < w; x++) {
                float tx = (float) x / (w - 1);
                float r = top.r + (bottom.r - top.r) * ty;
                float g = top.g + (bottom.g - top.g) * ty;
                float b = top.b + (bottom.b - top.b) * ty;

                float gdx = tx - 0.5f;
                float gdy = ty - 0.3f;
                float gA = 1f - (float) Math.sqrt(gdx * gdx + gdy * gdy) / 0.55f;
                if (gA > 0f) {
                    gA *= gA * 0.22f;
                    r += (glow.r - r) * gA;
                    g += (glow.g - g) * gA;
                    b += (glow.b - b) * gA;
                }

                float vdx = tx - 0.5f;
                float vdy = ty - 0.5f;
                float vig = ((float) Math.sqrt(vdx * vdx + vdy * vdy) - 0.4f) / 0.4f;
                if (vig > 0f) {
                    if (vig > 1f) vig = 1f;
                    float darken = 1f - vig * vig * 0.4f;
                    r *= darken;
                    g *= darken;
                    b *= darken;
                }

                int terrainH = Math.round(terrain[x]);
                if (y >= h - terrainH) {
                    float t = (float) (y - (h - terrainH)) / terrainH;
                    r *= 0.30f + t * 0.15f;
                    g *= 0.30f + t * 0.15f;
                    b *= 0.30f + t * 0.15f;
                }

                boolean inCloud = false;
                float cf = 0f;
                if (x >= cloudX && x < cloudX + cloudW && y >= cloudY && y < cloudY + 8) {
                    inCloud = cloud[x - cloudX][y - cloudY];
                    cf = 0.7f;
                }
                if (x >= cloudX2 && x < cloudX2 + cloudW2 && y >= cloudY2 && y < cloudY2 + 6) {
                    inCloud = inCloud || cloud2[x - cloudX2][y - cloudY2];
                    cf = 0.6f;
                }
                if (inCloud) {
                    r = r + (1f - r) * cf * 0.5f;
                    g = g + (1f - g) * cf * 0.5f;
                    b = b + (1f - b) * cf * 0.5f;
                }

                r = Math.round(r * 15f) / 15f;
                g = Math.round(g * 15f) / 15f;
                b = Math.round(b * 15f) / 15f;

                pm.setColor(clamp01(r), clamp01(g), clamp01(b), 1f);
                pm.drawPixel(x, y);
            }
        }

        pm.setBlending(Pixmap.Blending.None);
        for (int i = 0; i < 40 + rng.nextInt(35); i++) {
            int sx = rng.nextInt(w);
            int sy = rng.nextInt((int) (h * 0.55f));
            float bright = 0.5f + rng.nextFloat() * 0.5f;
            boolean big = rng.nextFloat() < 0.12f;
            pm.setColor(bright, bright, bright, 1f);
            pm.drawPixel(sx, sy);
            if (big && sx + 1 < w && sy + 1 < h * 0.55f) {
                pm.drawPixel(sx + 1, sy);
                pm.drawPixel(sx, sy + 1);
                pm.drawPixel(sx + 1, sy + 1);
            }
        }

        // --- Trees and houses on terrain (varied per background via RNG) ---
        pm.setBlending(Pixmap.Blending.None);

        int numTrees = rng.nextFloat() < 0.9f ? 1 + rng.nextInt(5) : 0;
        int numHouses = rng.nextFloat() < 0.7f ? rng.nextInt(3) : 0;

        // Tree colours
        float[][] treeGreens = {
            {45f/255f, 90f/255f, 39f/255f},
            {61f/255f, 122f/255f, 55f/255f},
            {77f/255f, 138f/255f, 71f/255f},
            {29f/255f, 74f/255f, 23f/255f},
            {93f/255f, 154f/255f, 87f/255f},
            {58f/255f, 107f/255f, 53f/255f},
        };
        float trunkR = 74f/255f, trunkG = 53f/255f, trunkB = 32f/255f;

        for (int t = 0; t < numTrees; t++) {
            int tx = 6 + rng.nextInt(w - 12);
            int treeTerrain = Math.round(terrain[tx]);
            int groundY = h - treeTerrain;
            if (groundY < 6) continue;

            int trunkH = 3;
            pm.setColor(trunkR, trunkG, trunkB, 1f);
            for (int ty = 0; ty < trunkH; ty++) {
                int py = groundY - 1 - ty;
                if (py >= 0) pm.drawPixel(tx, py);
            }

            float[] cg = treeGreens[rng.nextInt(treeGreens.length)];
            pm.setColor(cg[0], cg[1], cg[2], 1f);
            int canopyBase = groundY - trunkH;
            int[] hw = {0, 1, 2, 2, 1, 0};
            for (int row = 0; row < hw.length; row++) {
                int py = canopyBase - row;
                if (py < 0) break;
                for (int dx = -hw[row]; dx <= hw[row]; dx++) {
                    int px = tx + dx;
                    if (px >= 0 && px < w) pm.drawPixel(px, py);
                }
            }
        }

        // House colours
        float[][] wallColors = {
            {196f/255f, 168f/255f, 130f/255f},
            {212f/255f, 184f/255f, 146f/255f},
            {180f/255f, 152f/255f, 114f/255f},
            {139f/255f, 115f/255f, 85f/255f},
        };
        float[][] roofColors = {
            {139f/255f, 69f/255f, 19f/255f},
            {160f/255f, 82f/255f, 45f/255f},
            {107f/255f, 52f/255f, 16f/255f},
            {123f/255f, 68f/255f, 35f/255f},
        };
        float windowR = 1.0f, windowG = 204f/255f, windowB = 136f/255f;
        float doorR = 42f/255f, doorG = 26f/255f, doorB = 16f/255f;

        for (int hi = 0; hi < numHouses; hi++) {
            int hx = 10 + rng.nextInt(w - 20);
            int houseTerrain = Math.round(terrain[hx]);
            int groundY = h - houseTerrain;
            if (groundY < 10) continue;

            int halfW = 3 + rng.nextInt(2);
            int houseH = 5 + rng.nextInt(2);
            float[] wc = wallColors[rng.nextInt(wallColors.length)];
            pm.setColor(wc[0], wc[1], wc[2], 1f);
            for (int dy = 0; dy < houseH; dy++) {
                for (int dx = -halfW; dx <= halfW; dx++) {
                    int px = hx + dx;
                    int py = groundY - 1 - dy;
                    if (px >= 0 && px < w && py >= 0) pm.drawPixel(px, py);
                }
            }

            float[] rc = roofColors[rng.nextInt(roofColors.length)];
            pm.setColor(rc[0], rc[1], rc[2], 1f);
            int roofH = 3;
            for (int row = 0; row < roofH; row++) {
                int halfRoof = halfW + 1 - row;
                int py = groundY - 1 - houseH - row;
                if (py < 0) break;
                for (int dx = -halfRoof; dx <= halfRoof; dx++) {
                    int px = hx + dx;
                    if (px >= 0 && px < w) pm.drawPixel(px, py);
                }
            }

            pm.setColor(doorR, doorG, doorB, 1f);
            pm.drawPixel(hx, groundY - 1);
            pm.drawPixel(hx, groundY - 2);

            pm.setColor(windowR, windowG, windowB, 1f);
            pm.drawPixel(hx - 2, groundY - 3);
            pm.drawPixel(hx + 2, groundY - 3);
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
