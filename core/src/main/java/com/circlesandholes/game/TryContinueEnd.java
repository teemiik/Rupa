package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import static com.circlesandholes.game.Intro.*;

public class TryContinueEnd extends Game implements Screen {

    final Intro intro;
    final int level;

    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont optionFont;
    private final BitmapFont infoFont;

    private final Texture blackTex;
    private final Texture particleTex;
    private final Texture panelTexLose;
    private final Texture panelShadowTexLose;
    private final Texture panelTexWin;
    private final Texture panelShadowTexWin;
    private final Texture panelTexEnd;
    private final Texture panelShadowTexEnd;
    private float panelW;
    private float panelXLose, panelYLose, panelHLose;
    private float panelShadowXLose, panelShadowYLose;
    private float panelXWin, panelYWin, panelHWin;
    private float panelShadowXWin, panelShadowYWin;
    private float panelXEnd, panelYEnd, panelHEnd;
    private float panelShadowXEnd, panelShadowYEnd;
    private float particleSize;
    private float fade = 0.7f;
    private boolean one_render = true;
    private String recordStr = "";

    private final Vector3 touchPos = new Vector3();

    private float loseTitleY, loseRetryY, loseLevelsY, loseMenuY;
    private float winTitleY, winTimeY, winRecordY, winNextY, winRetryY, winLevelsY, winMenuY;
    private float endContinueY;

    private static final int BURST = 26;
    private final float[] px = new float[BURST];
    private final float[] py = new float[BURST];
    private final float[] vx = new float[BURST];
    private final float[] vy = new float[BURST];
    private final float[] life = new float[BURST];

    private int pressedIndex = -1;
    private float pressTimer = 0f;

public TryContinueEnd(final Intro intro, final int level) {
        this.intro = intro;
        this.level = level;

        batch = new SpriteBatch();

        blackTex = ProceduralAssets.solid(Color.BLACK);
        particleSize = w_world * 0.018f;
        particleTex = ProceduralAssets.circle(Math.max(2, Math.round(particleSize)), Color.WHITE);

        titleFont = Intro.makeFont(Math.round(size_text_result * 1.35f));
        optionFont = Intro.makeFont(size_text_result);
        infoFont = Intro.makeFont(size_text);

        GlyphLayout m = new GlyphLayout();
        float maxW = 0f;
        m.setText(titleFont, Intro.bundle.get("result_lose_title"));
        maxW = Math.max(maxW, m.width);
        m.setText(titleFont, Intro.bundle.get("result_win_title"));
        maxW = Math.max(maxW, m.width);
        for (String s : new String[]{Intro.bundle.get("result_restart"), Intro.bundle.get("result_levels"), Intro.bundle.get("result_menu"), Intro.bundle.get("result_next_level")}) {
            m.setText(optionFont, s);
            maxW = Math.max(maxW, m.width);
        }
        m.setText(infoFont, Intro.bundle.format("result_time", Intro.bundle.get("timer_placeholder")));
        maxW = Math.max(maxW, m.width);
        m.setText(infoFont, Intro.bundle.format("result_record", Intro.bundle.get("timer_placeholder")));
        maxW = Math.max(maxW, m.width);

        float padH = w_world * 0.08f;
        float padV = w_world * 0.08f;
        float radius = w_world * 0.06f;
        panelW = Math.min(maxW + padH * 2, w_world * 0.92f);

        float center = h_world / 2f;
        float titleH = titleFont.getLineHeight();
        float optH = optionFont.getLineHeight();
        float infoH = infoFont.getLineHeight();
        float titleAscent = titleFont.getAscent();
        float optAscent = optionFont.getAscent();
        float optDescent = optionFont.getDescent();
        float nudge = optH * 0.25f;

        // Loss panel
        loseTitleY = center + titleH * 1.5f;
        loseRetryY = center - optH * 0.5f;
        loseLevelsY = center - optH * 2.5f;
        loseMenuY = center - optH * 4.5f;

        float loseContentTop = loseTitleY + titleAscent;
        float loseContentBot = loseMenuY + optDescent;
        float loseShift = center - (loseContentTop + loseContentBot) / 2f + nudge;
        loseTitleY += loseShift;
        loseRetryY += loseShift;
        loseLevelsY += loseShift;
        loseMenuY += loseShift;

        // Win panel
        winTitleY = center + titleH * 3f;
        winTimeY = center + infoH * 1.5f;
        winRecordY = center + infoH * 0.5f;
        winNextY = center - optH * 1f;
        winRetryY = center - optH * 2.5f;
        winLevelsY = center - optH * 4f;
        winMenuY = center - optH * 5.5f;

        float winContentTop = winTitleY + titleAscent;
        float winContentBot = winMenuY + optDescent;
        float winShift = center - (winContentTop + winContentBot) / 2f + nudge;
        winTitleY += winShift;
        winTimeY += winShift;
        winRecordY += winShift;
        winNextY += winShift;
        winRetryY += winShift;
        winLevelsY += winShift;
        winMenuY += winShift;

        // The end panel
        endContinueY = center;

        float endContentTop = endContinueY + optAscent;
        float endContentBot = endContinueY + optDescent;
        float endShift = center - (endContentTop + endContentBot) / 2f + nudge;
        endContinueY += endShift;

        float soff = w_world * 0.007f;

        // Lose panel
        loseContentTop = loseTitleY + titleAscent;
        loseContentBot = loseMenuY + optDescent;
        float loseContentH = loseContentTop - loseContentBot;
        panelHLose = Math.min(loseContentH + padV * 2, h_world * 0.92f);
        panelXLose = (w_world - panelW) / 2f;
        panelYLose = center - panelHLose / 2f;
        panelShadowXLose = panelXLose + soff;
        panelShadowYLose = panelYLose - soff;
        panelTexLose = ProceduralAssets.roundRectGradient(Math.round(panelW), Math.round(panelHLose),
                radius,
                new Color(0.32f, 0.28f, 0.42f, 0.40f), new Color(0.10f, 0.08f, 0.16f, 0.55f));
        panelShadowTexLose = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelHLose),
                radius, new Color(0f, 0f, 0f, 0.25f));

        // Win panel
        winContentTop = winTitleY + titleAscent;
        winContentBot = winMenuY + optDescent;
        float winContentH = winContentTop - winContentBot;
        panelHWin = Math.min(winContentH + padV * 2, h_world * 0.92f);
        panelXWin = (w_world - panelW) / 2f;
        panelYWin = center - panelHWin / 2f;
        panelShadowXWin = panelXWin + soff;
        panelShadowYWin = panelYWin - soff;
        panelTexWin = ProceduralAssets.roundRectGradient(Math.round(panelW), Math.round(panelHWin),
                radius,
                new Color(0.32f, 0.28f, 0.42f, 0.40f), new Color(0.10f, 0.08f, 0.16f, 0.55f));
        panelShadowTexWin = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelHWin),
                radius, new Color(0f, 0f, 0f, 0.25f));

        // The end panel
        endContentTop = endContinueY + optAscent;
        endContentBot = endContinueY + optDescent;
        float endContentH = endContentTop - endContentBot;
        panelHEnd = Math.min(endContentH + padV * 2, h_world * 0.92f);
        panelXEnd = (w_world - panelW) / 2f;
        panelYEnd = center - panelHEnd / 2f;
        panelShadowXEnd = panelXEnd + soff;
        panelShadowYEnd = panelYEnd - soff;
        panelTexEnd = ProceduralAssets.roundRectGradient(Math.round(panelW), Math.round(panelHEnd),
                radius,
                new Color(0.32f, 0.28f, 0.42f, 0.40f), new Color(0.10f, 0.08f, 0.16f, 0.55f));
        panelShadowTexEnd = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelHEnd),
                radius, new Color(0f, 0f, 0f, 0.25f));

        text_total = Intro.bundle.get("result_time");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (pressTimer > 0) {
            pressTimer -= delta;
            if (pressTimer <= 0f) {
                pressTimer = 0f;
                executePressedAction();
                pressedIndex = -1;
            }
        }

        if (!faild) {
            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelShadowTexLose, panelShadowXLose, panelShadowYLose, panelW, panelHLose);
            batch.draw(panelTexLose, panelXLose, panelYLose, panelW, panelHLose);
            drawCentered(titleFont, Intro.bundle.get("result_lose_title"), loseTitleY, false);
            drawCentered(optionFont, Intro.bundle.get("result_restart"), loseRetryY, pressedIndex == 0);
            drawCentered(optionFont, Intro.bundle.get("result_levels"), loseLevelsY, pressedIndex == 1);
            drawCentered(optionFont, Intro.bundle.get("result_menu"), loseMenuY, pressedIndex == 2);
            batch.end();

            if (pressTimer <= 0f && Gdx.input.justTouched()) {
                if (tappedAt(loseRetryY)) pressedIndex = 0;
                else if (tappedAt(loseLevelsY)) pressedIndex = 1;
                else if (tappedAt(loseMenuY)) pressedIndex = 2;
                if (pressedIndex >= 0) pressTimer = 0.08f;
            }

        } else if (win) {
            if (one_render) {
                text_total = Intro.bundle.format("result_time", Progress.formatTime(Intro.finishSeconds));
                Progress.recordTime(level, Intro.finishSeconds);
                recordStr = Intro.bundle.format("result_record", Progress.formatTime(Progress.bestTime(level)));
                spawnBurst();
                one_render = false;
            }

            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelShadowTexWin, panelShadowXWin, panelShadowYWin, panelW, panelHWin);
            batch.draw(panelTexWin, panelXWin, panelYWin, panelW, panelHWin);
            drawCentered(titleFont, Intro.bundle.get("result_win_title"), winTitleY, false);
            drawCentered(infoFont, text_total, winTimeY, false);
            drawCentered(infoFont, recordStr, winRecordY, false);
            drawCentered(optionFont, Intro.bundle.get("result_next_level"), winNextY, pressedIndex == 0);
            drawCentered(optionFont, Intro.bundle.get("result_restart"), winRetryY, pressedIndex == 1);
            drawCentered(optionFont, Intro.bundle.get("result_levels"), winLevelsY, pressedIndex == 2);
            drawCentered(optionFont, Intro.bundle.get("result_menu"), winMenuY, pressedIndex == 3);
            updateAndDrawBurst(delta);
            batch.end();

            if (pressTimer <= 0f && Gdx.input.justTouched()) {
                if (tappedAt(winNextY)) pressedIndex = 0;
                else if (tappedAt(winRetryY)) pressedIndex = 1;
                else if (tappedAt(winLevelsY)) pressedIndex = 2;
                else if (tappedAt(winMenuY)) pressedIndex = 3;
                if (pressedIndex >= 0) pressTimer = 0.08f;
            }

        } else if (the_end) {
            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelShadowTexEnd, panelShadowXEnd, panelShadowYEnd, panelW, panelHEnd);
            batch.draw(panelTexEnd, panelXEnd, panelYEnd, panelW, panelHEnd);
            drawCentered(optionFont, Intro.bundle.get("result_end_title"), endContinueY, false);
            batch.end();
        }

        drawFade(delta);
    }

    private void executePressedAction() {
        if (!faild) {
            if (pressedIndex == 0) doRetry();
            else if (pressedIndex == 1) doLevels();
            else if (pressedIndex == 2) doMenu();
        } else if (win) {
            if (pressedIndex == 0) doContinue();
            else if (pressedIndex == 1) doRetry();
            else if (pressedIndex == 2) doLevels();
            else if (pressedIndex == 3) doMenu();
        }
    }

    private void doRetry() {
        intro.goToLevel(level);
    }

    private void doContinue() {
        int levelUp = level + 1;
        if (levelUp <= LevelLoader.count()) {
            intro.goToLevel(levelUp);
        } else {
            cancelDynamicTimer();
            box_din = 0;
            win = false;
            the_end = true;
        }
    }

    private void doLevels() {
        intro.setScreen(new LevelSelectScreen(intro, this));
    }

    private void doMenu() {
        faild = true;
        win = false;
        the_end = false;
        cancelDynamicTimer();
        intro.showMenu();
    }

    private boolean tappedAt(float y) {
        if (!Gdx.input.justTouched()) return false;
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        float bandW = w_world * 0.8f;
        float bandH = optionFont.getLineHeight() * 1.6f;
        float cy = y - optionFont.getLineHeight() / 2f;
        return touchPos.x >= w_world / 2 - bandW / 2 && touchPos.x <= w_world / 2 + bandW / 2
                && touchPos.y >= cy - bandH / 2 && touchPos.y <= cy + bandH / 2;
    }

    private void drawCentered(BitmapFont font, String s, float y, boolean pressed) {
        glyph.setText(font, s);
        float x = w_world / 2 - glyph.width / 2;
        float o = Math.max(1f, w_world * 0.004f);
        if (pressed) {
            font.setColor(0f, 0f, 0f, 0.6f);
            font.draw(batch, s, x + o * 0.5f, y - o * 0.5f);
            font.setColor(Color.WHITE);
            font.draw(batch, s, x + o, y - o);
        } else {
            font.setColor(0f, 0f, 0f, 0.5f);
            font.draw(batch, s, x + o, y - o);
            font.setColor(Color.WHITE);
            font.draw(batch, s, x, y);
        }
    }

    private void cancelDynamicTimer() {
        if (timer_dynamic_body != null) {
            try {
                timer_dynamic_body.cancel();
            } catch (Exception ignored) {
            }
            timer_dynamic_body = null;
        }
    }

    private void spawnBurst() {
        float cx = w_world / 2f, cy = h_world * 0.62f;
        for (int i = 0; i < BURST; i++) {
            float ang = MathUtils.random(0f, MathUtils.PI2);
            float spd = MathUtils.random(w_world * 0.25f, w_world * 0.7f);
            px[i] = cx;
            py[i] = cy;
            vx[i] = MathUtils.cos(ang) * spd;
            vy[i] = MathUtils.sin(ang) * spd + h_world * 0.2f;
            life[i] = MathUtils.random(0.9f, 1.5f);
        }
    }

    private void updateAndDrawBurst(float delta) {
        float g = h_world * 0.9f;
        for (int i = 0; i < BURST; i++) {
            if (life[i] <= 0f) continue;
            life[i] -= delta;
            vy[i] -= g * delta;
            px[i] += vx[i] * delta;
            py[i] += vy[i] * delta;
            float a = life[i] > 1f ? 1f : (life[i] < 0f ? 0f : life[i]);
            batch.setColor(1f, 1f, 1f, a);
            batch.draw(particleTex, px[i] - particleSize / 2f, py[i] - particleSize / 2f, particleSize, particleSize);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawFade(float delta) {
        if (fade <= 0f) return;
        batch.begin();
        batch.setColor(0f, 0f, 0f, fade);
        batch.draw(blackTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.end();
        fade -= delta / 0.25f;
        if (fade < 0f) fade = 0f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        optionFont.dispose();
        infoFont.dispose();
        blackTex.dispose();
        particleTex.dispose();
        panelTexLose.dispose();
        panelShadowTexLose.dispose();
        panelTexWin.dispose();
        panelShadowTexWin.dispose();
        panelTexEnd.dispose();
        panelShadowTexEnd.dispose();
    }

    @Override
    public void create() {
    }

    @Override
    public void hide() {
    }
}
