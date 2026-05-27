package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import static com.circlesandholes.game.Intro.box_din;
import static com.circlesandholes.game.Intro.camera;
import static com.circlesandholes.game.Intro.faild;
import static com.circlesandholes.game.Intro.glyph;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.size_text_result;
import static com.circlesandholes.game.Intro.text_total;
import static com.circlesandholes.game.Intro.the_end;
import static com.circlesandholes.game.Intro.timer_dynamic_body;
import static com.circlesandholes.game.Intro.w_world;
import static com.circlesandholes.game.Intro.win;

/**
 * Win / lose / "the end" result screen. Uses the same text-option style as the
 * pause menu (centred labels with a shadow on a card panel), with manual tap
 * hit-testing — no scene2d buttons. Records the best time and shows a win burst.
 */
public class TryContinueEnd extends Game implements Screen {

    final Intro intro;
    final int level;

    private final SpriteBatch batch;
    private final BitmapFont titleFont;   // headings
    private final BitmapFont optionFont;   // tappable options
    private final BitmapFont infoFont;     // time / record

    private final Texture blackTex;
    private final Texture particleTex;
    private final Texture panelTex;
    private float panelX, panelY, panelW, panelH;
    private float particleSize;
    private float fade = 0.7f;
    private boolean one_render = true;
    private String recordStr = "";

    private final Vector3 touchPos = new Vector3();

    // Dynamic Y positions computed from font line heights
    private float loseTitleY, loseRetryY, loseLevelsY;
    private float winTitleY, winTimeY, winRecordY, winNextY, winRetryY, winLevelsY;
    private float endContinueY;

    private static final int BURST = 26;
    private final float[] px = new float[BURST];
    private final float[] py = new float[BURST];
    private final float[] vx = new float[BURST];
    private final float[] vy = new float[BURST];
    private final float[] life = new float[BURST];

    public TryContinueEnd(final Intro intro, final int level) {
        this.intro = intro;
        this.level = level;

        batch = new SpriteBatch();

        blackTex = ProceduralAssets.solid(Color.BLACK);
        particleSize = w_world * 0.018f;
        particleTex = ProceduralAssets.circle(Math.max(2, Math.round(particleSize)), Color.WHITE);
        panelW = w_world * 0.84f;
        panelH = h_world * 0.84f;
        panelX = (w_world - panelW) / 2f;
        panelY = (h_world - panelH) / 2f;
        panelTex = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelH),
                w_world * 0.06f, new Color(0.16f, 0.16f, 0.22f, 0.5f));

        titleFont = Intro.makeFont(Math.round(size_text_result * 1.35f));
        optionFont = Intro.makeFont(size_text_result);
        infoFont = Intro.makeFont(size_text);

        computeYPositions();

        text_total = "Уровень пройден за ";
    }

    private void computeYPositions() {
        float panelCenterY = panelY + panelH / 2f;
        float lineHeightTitle = titleFont.getLineHeight();
        float lineHeightOption = optionFont.getLineHeight();
        float lineHeightInfo = infoFont.getLineHeight();
        
        // Lose screen positions
        loseTitleY = panelCenterY + lineHeightTitle * 1.5f;
        loseRetryY = panelCenterY - lineHeightOption * 0.5f;
        loseLevelsY = panelCenterY - lineHeightOption * 2.5f;
        
        // Win screen positions  
        winTitleY = panelCenterY + lineHeightTitle * 3f;
        winTimeY = panelCenterY + lineHeightInfo * 1.5f;
        winRecordY = panelCenterY + lineHeightInfo * 0.5f;
        winNextY = panelCenterY - lineHeightOption * 1f;
        winRetryY = panelCenterY - lineHeightOption * 2.5f;
        winLevelsY = panelCenterY - lineHeightOption * 4f;
        
        // End screen position
        endContinueY = panelCenterY;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!faild) { // lost
            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelTex, panelX, panelY, panelW, panelH);
            drawCentered(titleFont, "Не получилось", loseTitleY);
            drawCentered(optionFont, "Заново", loseRetryY);
            drawCentered(optionFont, "Уровни", loseLevelsY);
            batch.end();

            if (tappedAt(loseRetryY)) {
                doRetry();
            } else if (tappedAt(loseLevelsY)) {
                doLevels();
            }

        } else if (win) {
            if (one_render) {
                text_total = "Уровень пройден за " + Progress.formatTime(Intro.finishSeconds);
                Progress.recordTime(level, Intro.finishSeconds);
                recordStr = "Рекорд " + Progress.formatTime(Progress.bestTime(level));
                spawnBurst();
                one_render = false;
            }

            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelTex, panelX, panelY, panelW, panelH);
            drawCentered(titleFont, "Победа!", winTitleY);
            drawCentered(infoFont, text_total, winTimeY);
            drawCentered(infoFont, recordStr, winRecordY);
            drawCentered(optionFont, "Следующий уровень", winNextY);
            drawCentered(optionFont, "Заново", winRetryY);
            drawCentered(optionFont, "Уровни", winLevelsY);
            updateAndDrawBurst(delta);
            batch.end();

            if (tappedAt(winNextY)) {
                doContinue();
            } else if (tappedAt(winRetryY)) {
                doRetry();
            } else if (tappedAt(winLevelsY)) {
                doLevels();
            }

        } else if (the_end) {
            batch.begin();
            batch.draw(Intro.currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(panelTex, panelX, panelY, panelW, panelH);
            drawCentered(optionFont, "Продолжение следует...", endContinueY);
            batch.end();
        }

        drawFade(delta);
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
        // Keep this result screen alive so Back from the picker returns here.
        intro.setScreen(new LevelSelectScreen(intro, this));
    }

    /** True if a tap landed this frame within the option band centred on baseline y. */
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

    private void drawCentered(BitmapFont font, String s, float y) {
        glyph.setText(font, s);
        float x = w_world / 2 - glyph.width / 2;
        float o = Math.max(1f, w_world * 0.004f);
        font.setColor(0f, 0f, 0f, 0.5f);
        font.draw(batch, s, x + o, y - o);
        font.setColor(Color.WHITE);
        font.draw(batch, s, x, y);
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
        panelTex.dispose();
    }

    @Override
    public void create() {
    }

    @Override
    public void hide() {
    }
}
