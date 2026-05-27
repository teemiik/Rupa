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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import static com.circlesandholes.game.Intro.background;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.size_text_result;
import static com.circlesandholes.game.Intro.w_world;

/**
 * Level picker: a centered, screen-relative grid laid out from
 * {@link LevelLoader#count()}, so adding a level (just a new JSON file) appears
 * here automatically. Tiles and numbers are generated procedurally — no
 * per-level art needed. A check mark and best time mark completed levels.
 */
public class LevelSelectScreen extends Game implements Screen {

    private final Intro intro;
    private final Screen returnTo;
    private final SpriteBatch batch;
    private final Stage stage;
    private final BitmapFont font;
    private final BitmapFont numberFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private final GlyphLayout numGlyph = new GlyphLayout();
    private final GlyphLayout timeGlyph = new GlyphLayout();
    private final String title = "Выбери уровень";

    private final Texture backArrowTex;
    private final Texture tileTex;
    private final Texture checkTex;
    private final Texture checkBgTex;
    private final Texture shadowTex;
    private final Texture blackTex;
    private float fade = 0.7f;

    private final int count, cols;
    private final float tile, gapX, gapY, startX, topY;

    public LevelSelectScreen(final Intro intro, final Screen returnTo) {
        this.intro = intro;
        this.returnTo = returnTo;
        batch = new SpriteBatch();
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        font = Intro.makeFont(size_text);
        numberFont = Intro.makeFont(size_text_result);
        glyph.setText(font, title);

        count = Math.max(1, LevelLoader.count());
        cols = 3;
        tile = w_world * 0.17f;
        gapX = tile * 0.5f;
        gapY = tile * 0.7f;
        int rows = (count + cols - 1) / cols;

        float gridW = cols * tile + (cols - 1) * gapX;
        float gridH = rows * tile + (rows - 1) * gapY;
        startX = (w_world - gridW) / 2f;
        topY = (h_world + gridH) / 2f - tile;

        tileTex = ProceduralAssets.ball(Math.round(tile), new Color(0.16f, 0.16f, 0.22f, 0.62f));
        checkTex = ProceduralAssets.check(Math.round(tile * 0.4f), Color.WHITE);
        checkBgTex = ProceduralAssets.circle(Math.round(tile * 0.26f), new Color(0.20f, 0.45f, 0.27f, 1f));
        shadowTex = ProceduralAssets.softCircle(64);
        blackTex = ProceduralAssets.solid(Color.BLACK);

        for (int i = 0; i < count; i++) {
            final int levelNumber = i + 1;
            ImageButton button = new ImageButton(new TextureRegionDrawable(new TextureRegion(tileTex)));
            button.getImageCell().size(tile, tile);
            button.setSize(tile, tile);
            button.setPosition(tileX(i % cols), tileY(i / cols));
            stage.addActor(button);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    intro.goToLevel(levelNumber);
                }
            });
        }

        // Back to the start screen — a procedural chevron arrow, top-left.
        int arrowSize = Math.round(w_world * 0.07f);
        backArrowTex = ProceduralAssets.backArrow(arrowSize, Color.WHITE);
        ImageButton back = new ImageButton(new TextureRegionDrawable(new TextureRegion(backArrowTex)));
        back.getImageCell().size(arrowSize, arrowSize);
        back.setSize(arrowSize, arrowSize);
        back.setPosition(w_world * 0.05f, h_world * 0.88f);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (returnTo != null) {
                    intro.setScreen(returnTo);
                } else {
                    intro.showMenu();
                }
            }
        });
        stage.addActor(back);
    }

    private float tileX(int col) {
        return startX + col * (tile + gapX);
    }

    private float tileY(int row) {
        return topY - row * (tile + gapY);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float titleY = h_world - font.getLineHeight() * 2.5f;
        font.draw(batch, title, w_world / 2 - glyph.width / 2, titleY);
        // Soft shadow under each tile (drawn before the tiles, which the stage renders on top).
        float ss = tile * 1.25f;
        batch.setColor(0f, 0f, 0f, 0.3f);
        for (int i = 0; i < count; i++) {
            float cx = tileX(i % cols) + tile / 2f, cy = tileY(i / cols) + tile / 2f;
            batch.draw(shadowTex, cx - ss / 2f, cy - ss / 2f - tile * 0.08f, ss, ss);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        stage.draw();

        // Number on each tile, plus a check + best time on completed levels.
        batch.begin();
        for (int i = 0; i < count; i++) {
            float bx = tileX(i % cols);
            float by = tileY(i / cols);
            float cx = bx + tile / 2f;

            String num = String.valueOf(i + 1);
            numGlyph.setText(numberFont, num);
            numberFont.draw(batch, num, cx - numGlyph.width / 2, by + tile / 2f + numGlyph.height / 2f);

            if (Progress.isCompleted(i + 1)) {
                // "Completed" badge in the upper-right of the tile: green disc + white check.
                float badge = tile * 0.26f;
                float chk = tile * 0.17f;
                float bcx = bx + tile * 0.82f;
                float bcy = by + tile * 0.82f;
                batch.draw(checkBgTex, bcx - badge / 2f, bcy - badge / 2f, badge, badge);
                batch.draw(checkTex, bcx - chk / 2f, bcy - chk / 2f, chk, chk);
                String t = Progress.formatTime(Progress.bestTime(i + 1));
                timeGlyph.setText(font, t);
                font.draw(batch, t, cx - timeGlyph.width / 2, by - tile * 0.06f);
            }
        }
        batch.end();

        drawFade(delta);
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
    public void create() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        numberFont.dispose();
        backArrowTex.dispose();
        tileTex.dispose();
        checkTex.dispose();
        checkBgTex.dispose();
        shadowTex.dispose();
        blackTex.dispose();
        stage.dispose();
    }
}
