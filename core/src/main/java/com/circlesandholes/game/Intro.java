package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bolgov.artem on 09.10.17.
 */

 public class Intro extends Game {

    public SpriteBatch batch;

    private Texture background_image;
    private Texture background_2_image;

    private BitmapFont titleFont;
    private BitmapFont optionFont;
    private GlyphLayout menuGlyph;
    private Texture playTex;
    private float playX, playY, playSize;

    public static String lang = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\\\/?^-+=()*&.:;,{}\\\"´`'<>";

    public static float w_world;
    public static float h_world;
    public static float height_board;

    /** UI icon scale: PNG buttons are authored for a ~1080px-wide screen, so size them relative to that. */
    public static float uiScale = 1f;
    private static final float UI_REFERENCE_WIDTH = 1080f;
    private static float withdt_board;

    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static float time_step;

    private static Texture board_image;
    private static Texture board_2_image;
    public static Texture hole_image;
    public static Texture rectangle_barrier_image;

    public static Texture training_right_image;
    public static Texture training_left_image;

    public static Sprite board;
    public static Sprite board_2;
    public static Sprite circle;
    public static Sprite background;
    public static Sprite background_2;

    /** Background of the level currently being played; regenerated per level. */
    public static Sprite currentBackground;
    private static Texture currentBackgroundTexture;
    public static Sprite training_left;
    public static Sprite training_right;

    public static OrthographicCamera camera;

    public static Texture circle_image;

    static GlyphLayout glyph;
    static GlyphLayout glyph_result;
    static GlyphLayout glyph_var1;
    static GlyphLayout glyph_var2;

    public static String text;
    static String text_total;
    public static String texture_gest_right;
    public static String texture_gest_left;
    public static String texture_gest_right_update;
    public static String texture_gest_left_update;

    public static int minutes = 0;
    public static int seconds = 0;
    public static int size_text;
    public static int size_text_result;
    public static int gest = 0;
    public static int box_din = 0;
    private static int speed_hole;

    /** Seconds taken to finish the current level (snapshot on win, for the record). */
    public static int finishSeconds = 0;
    /** When true, the game timers stop advancing (used by the in-level pause overlay). */
    public static boolean paused = false;
    /** When true, the board ignores the in-progress touch until released (so a menu tap doesn't tilt). */
    public static boolean consumeTouch = false;

    private static Timer timer;
    private static Timer timer_gest;
    public static Timer timer_dynamic_body = null;

    public static boolean box_hole_din_sign = true;
    public static boolean faild = true;
    public static boolean win = false;
    static boolean the_end = false;

    public static FreeTypeFontGenerator generator;

    // --- Procedural theme palette (replaces the shipped PNG art) ---
    private static final Color BALL_COLOR = new Color(0.80f, 0.80f, 0.83f, 1f);
    private static final Color HOLE_COLOR = new Color(0.10f, 0.10f, 0.12f, 1f);
    private static final Color BOARD_COLOR = new Color(0.62f, 0.30f, 0.28f, 1f);
    private static final Color BOARD2_COLOR = new Color(0.34f, 0.40f, 0.46f, 1f);
    private static final Color BARRIER_COLOR = new Color(0.12f, 0.12f, 0.13f, 1f);
    private static final Color BG_WARM_TOP = new Color(0.80f, 0.76f, 0.68f, 1f);
    private static final Color BG_WARM_BOTTOM = new Color(0.52f, 0.13f, 0.18f, 1f);
    private static final Color BG_WARM_GLOW = new Color(0.96f, 0.90f, 0.78f, 1f);
    private static final Color BG_COOL_TOP = new Color(0.62f, 0.72f, 0.80f, 1f);
    private static final Color BG_COOL_BOTTOM = new Color(0.06f, 0.10f, 0.16f, 1f);
    private static final Color BG_COOL_GLOW = new Color(0.82f, 0.92f, 1.0f, 1f);

    @Override
    public void create() {
        generalData(new SpriteBatch());
    }

    /**
     * Single entry point for starting a level: resets the shared per-level state
     * (oscillation timer, counters, win/fail flags) and rebuilds the board, then
     * shows the data-driven screen. Called from the menu, the level picker and
     * the result screen.
     */
    public void goToLevel(int level) {
        if (timer_dynamic_body != null) {
            try {
                timer_dynamic_body.cancel();
            } catch (Exception ignored) {
            }
            timer_dynamic_body = null;
        }
        box_din = 0;
        box_hole_din_sign = true;
        faild = true;
        win = false;
        paused = false;
        consumeTouch = true;
        buildLevelTheme(level);
        createdBoard();
        setScreen(new LevelScreen(this, level, LevelLoader.load(level)));
    }

    /**
     * Picks a per-level colour theme from the level number: a unique-hue gradient
     * background plus a board in the same hue (lighter / less saturated so it
     * reads against the dark bottom of the gradient instead of clashing).
     */
    private static void buildLevelTheme(int level) {
        float hue = ((level - 1) * 47f) % 360f;

        Color top = new Color();
        top.fromHsv(hue, 0.30f, 0.82f);
        top.a = 1f;
        Color bottom = new Color();
        bottom.fromHsv(hue, 0.65f, 0.38f);
        bottom.a = 1f;
        Color glow = new Color();
        glow.fromHsv(hue, 0.18f, 0.95f);
        glow.a = 1f;
        if (currentBackgroundTexture != null) {
            currentBackgroundTexture.dispose();
        }
        currentBackgroundTexture = ProceduralAssets.background(top, bottom, glow);
        currentBackground = new Sprite(currentBackgroundTexture);

        // Board with a vertical bevel (lighter top, darker bottom) in the level's hue.
        Color boardLight = new Color();
        boardLight.fromHsv(hue, 0.40f, 0.80f);
        boardLight.a = 1f;
        Color boardDark = new Color();
        boardDark.fromHsv(hue, 0.58f, 0.52f);
        boardDark.a = 1f;
        if (board_image != null) {
            board_image.dispose();
        }
        if (board_2_image != null) {
            board_2_image.dispose();
        }
        int bw = Math.max(2, Math.round(withdt_board));
        int bh = Math.max(2, Math.round(height_board));
        board_image = ProceduralAssets.roundRectGradient(bw, bh, bh / 2f, boardLight, boardDark);
        board_2_image = ProceduralAssets.roundRectGradient(bw, bh, bh / 2f, boardLight, boardDark);
    }

    /** Returns to the start screen (play / level-select buttons). */
    public void showMenu() {
        paused = false;
        setScreen(null);
        Gdx.input.setInputProcessor(null);
    }

    /** Short haptic pulse on mobile; harmless no-op on platforms without vibration. */
    public static void vibrate(int ms) {
        try {
            Gdx.input.vibrate(ms);
        } catch (Throwable ignored) {
        }
    }

   @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // The start menu is drawn here only while no other screen is active;
        // levels, the picker and result screens are real Screens drawn by super.render().
        if (getScreen() == null) {
            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            drawMenuText(titleFont, "Rupa", h_world * 0.82f);
            batch.draw(playTex, playX, playY, playSize, playSize);
            drawMenuText(optionFont, "Уровни", h_world * 0.34f);
            drawMenuText(optionFont, "Выход", h_world * 0.23f);
            batch.end();

            if (Gdx.input.justTouched()) {
                float tx = Gdx.input.getX();
                float ty = h_world - Gdx.input.getY();
                if (tx >= playX && tx <= playX + playSize && ty >= playY && ty <= playY + playSize) {
                    goToLevel(1);
                } else if (menuTap(tx, ty, h_world * 0.34f)) {
                    setScreen(new LevelSelectScreen(this, null));
                } else if (menuTap(tx, ty, h_world * 0.23f)) {
                    Gdx.app.exit();
                }
            }
        }

        super.render();
    }

    private void drawMenuText(BitmapFont font, String s, float y) {
        menuGlyph.setText(font, s);
        float x = w_world / 2 - menuGlyph.width / 2;
        float o = Math.max(1f, w_world * 0.004f);
        font.setColor(0f, 0f, 0f, 0.5f);
        font.draw(batch, s, x + o, y - o);
        font.setColor(Color.WHITE);
        font.draw(batch, s, x, y);
    }

    private boolean menuTap(float tx, float ty, float y) {
        float bandW = w_world * 0.8f;
        float bandH = optionFont.getLineHeight() * 1.6f;
        float cy = y - optionFont.getLineHeight() / 2f;
        return tx >= w_world / 2 - bandW / 2 && tx <= w_world / 2 + bandW / 2
                && ty >= cy - bandH / 2 && ty <= cy + bandH / 2;
    }

    private void generalData(SpriteBatch batch) {
        this.batch = batch;

        Gdx.app.log("MyTag", String.valueOf(Gdx.graphics.getDensity()));

        w_world = Gdx.graphics.getWidth();
        h_world = Gdx.graphics.getHeight();

        uiScale = w_world / UI_REFERENCE_WIDTH;

        background_image = ProceduralAssets.background(BG_WARM_TOP, BG_WARM_BOTTOM, BG_WARM_GLOW);
        background_2_image = ProceduralAssets.background(BG_COOL_TOP, BG_COOL_BOTTOM, BG_COOL_GLOW);

        background = new Sprite(background_image);
        background_2 = new Sprite(background_2_image);
        currentBackground = background;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w_world / 2, h_world / 2);

        withdt_board = (float) (w_world - w_world * 0.25);
        height_board = (float) (w_world * 0.04);

        board_image = ProceduralAssets.solid(BOARD_COLOR);
        board_2_image = ProceduralAssets.solid(BOARD2_COLOR);
        rectangle_barrier_image = ProceduralAssets.solid(BARRIER_COLOR);

        circle_image = getTextureBall();
        hole_image = getTextureHole();

        createdBoard();
        circle = new Sprite(circle_image);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("10771.ttf"));

        glyph = new GlyphLayout();
        glyph_result = new GlyphLayout();
        glyph_var1 = new GlyphLayout();
        glyph_var2 = new GlyphLayout();

        titleFont = makeFont(Math.round(size_text_result * 1.6f));
        optionFont = makeFont(size_text_result);
        menuGlyph = new GlyphLayout();
        playTex = new Texture("Menu/play.png");
        playSize = w_world * 0.24f;
        playX = (w_world - playSize) / 2f;
        playY = h_world * 0.56f - playSize / 2f;
        Gdx.input.setInputProcessor(null);

        getTimer();
        getTimerGestures();
    }

    static void createdBoard() {
        board = new Sprite(board_image);
        board.setSize(withdt_board, height_board);
        board.setOrigin(withdt_board / 2, height_board / 2);
        board.setX((float) (w_world / 2 - w_world * 0.25 - w_world * 0.25 / 2));
        board.setY((float) (h_world * 0.06));

        board_2 = new Sprite(board_2_image);
        board_2.setSize(withdt_board, height_board);
        board_2.setOrigin(withdt_board / 2, height_board / 2);
        board_2.setX((float) (w_world / 2 - w_world * 0.25 - w_world * 0.25 / 2));
        board_2.setY((float) (h_world * 0.06));
    }

    /**
     * Builds a FreeType font for crisp text: linear filtering smooths glyph
     * edges, and the font is rasterised at the real pixel density (then scaled
     * back) so it stays sharp on HiDPI/Retina where the framebuffer is upscaled.
     */
    public static BitmapFont makeFont(int size) {
        float density = Gdx.graphics.getBackBufferWidth() / (float) Gdx.graphics.getWidth();
        if (density < 1f) density = 1f;

        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = Math.max(1, Math.round(size * density));
        param.characters = lang;
        param.color = Color.WHITE;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;

        BitmapFont font = generator.generateFont(param);
        if (density != 1f) {
            font.getData().setScale(1f / density);
        }
        return font;
    }

    /** Sizes a PNG ImageButton to its texture scaled by {@link #uiScale} so UI icons are screen-relative. */
    public static void fitButton(ImageButton button, float texWidth, float texHeight) {
        float w = texWidth * uiScale;
        float h = texHeight * uiScale;
        button.getImage().setScaling(Scaling.stretch);
        button.getImageCell().size(w, h);
        button.setSize(w, h);
    }

    // Hole + on-screen-text + dynamic-hole tuning, derived from screen width so
    // the game stays resolution-independent (no pre-rendered size buckets).
    // Constants are calibrated so a ~1080px-wide phone matches the old assets.
    private Texture getTextureHole() {
        size_text = Math.max(12, Math.round(w_world * 0.045f));
        size_text = Math.min(60, size_text);
        size_text_result = Math.round(size_text * 1.5f);
        size_text_result = Math.min(90, size_text_result);
        speed_hole = Math.max(5, Math.min(14, Math.round(11000f / w_world)));
        time_step = 0.6f;

        int diameter = Math.round(w_world * 0.0695f);
        return ProceduralAssets.hole(diameter, HOLE_COLOR);
    }

    private Texture getTextureBall() {
        texture_gest_right = "Gestures/training_right_74.png";
        texture_gest_left = "Gestures/training_left_74.png";
        texture_gest_right_update = "Gestures/training_right_64.png";
        texture_gest_left_update = "Gestures/training_left_64.png";

        int diameter = Math.round(w_world * 0.06f);
        return ProceduralAssets.ball(diameter, BALL_COLOR);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose () {
        batch.dispose();

        timer.cancel();
        timer_gest.cancel();

        if (timer_dynamic_body != null)
        timer_dynamic_body.cancel();

        if (currentBackgroundTexture != null)
        currentBackgroundTexture.dispose();

        if (titleFont != null) titleFont.dispose();
    }

    private void getTimer() {

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (paused) return;
                String second, minute;
                seconds++;

                if (seconds == 60) {
                    minutes++;
                    seconds = 0;
                }

                if (seconds < 10) {
                    second = "0" + seconds;
                } else
                    second = String.valueOf(seconds);

                if (minutes < 10) {
                    minute = "0" + minutes;
                } else {
                    minute = String.valueOf(minutes);
                }

                text = minute + ":" + second;
            }
        }, 0, 1000);

    }

    private void getTimerGestures() {

        timer_gest = new Timer();
        timer_gest.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gest++;
            }
        }, 0, 500);

    }

    public static void getTimerDynamicBody() {

        timer_dynamic_body = new Timer();

        timer_dynamic_body.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (paused) return;
                if (box_hole_din_sign) box_din++; else box_din--;

            }
        }, 0, speed_hole);

    }
}
