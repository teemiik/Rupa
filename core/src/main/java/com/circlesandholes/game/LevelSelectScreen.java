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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import static com.circlesandholes.game.Intro.*;

public class LevelSelectScreen extends Game implements Screen {

    private final Intro intro;
    private final Screen returnTo;
    private final SpriteBatch batch;
    private final Stage stage;
    private final BitmapFont font;
    private final BitmapFont numberFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private final GlyphLayout timeGlyph = new GlyphLayout();
    private final String title = Intro.bundle.get("level_select_title");

    private final Texture backArrowTex;
    private final Texture tileTex;
    private final Texture checkTex;
    private final Texture checkBgTex;
    private final Texture shadowTex;
    private final Texture blackTex;
    private float fade = 0.7f;

    private final int count, cols;
    private final float tile, gapX, gapY, startX, baseTopY, baseTitleY;
    private final Group gridGroup;
    private final float arrowY;

    private float scrollY = 0f;
    private final float maxScroll;
    private float lastPointerY;
    private boolean dragging = false;
    private float dragAccum = 0f;

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

        int arrowSize = Math.round(w_world * 0.07f);
        arrowY = h_world * 0.88f;
        float arrowCenterY = arrowY + arrowSize / 2f;
        baseTitleY = arrowCenterY + glyph.height / 2f;

        float gridW = cols * tile + (cols - 1) * gapX;
        float gridH = rows * tile + (rows - 1) * gapY;
        startX = (w_world - gridW) / 2f;
        float top = (h_world + gridH) / 2f - tile;
        float minTop = baseTitleY - font.getLineHeight() - tile - font.getLineHeight() * 0.5f;
        baseTopY = Math.min(top, minTop);

        float bottomMargin = tile * 0.3f;
        float lastRowY = tileY(rows - 1);
        maxScroll = Math.max(0, -lastRowY + tile + bottomMargin);

tileTex = ProceduralAssets.gradientCircle(Math.round(tile),
                new Color(0.32f, 0.28f, 0.42f, 1f), new Color(0.08f, 0.06f, 0.14f, 1f));
        checkTex = ProceduralAssets.check(Math.round(tile * 0.17f), Color.WHITE);
        checkBgTex = ProceduralAssets.gradientCircle(Math.round(tile * 0.26f),
                new Color(0.32f, 0.62f, 0.38f, 1f), new Color(0.12f, 0.32f, 0.18f, 1f));
        shadowTex = ProceduralAssets.gradientCircle(Math.round(tile),
                new Color(0f, 0f, 0f, 0.25f), new Color(0f, 0f, 0f, 0.25f));
        blackTex = ProceduralAssets.solid(Color.BLACK);

        gridGroup = new Group();
        Label.LabelStyle numStyle = new Label.LabelStyle(numberFont, Color.WHITE);
        for (int i = 0; i < count; i++) {
            final int levelNumber = i + 1;
            ImageButton button = new ImageButton(new TextureRegionDrawable(new TextureRegion(tileTex)));
            button.getImageCell().size(tile, tile);
            button.setSize(tile, tile);
            button.setTransform(true);
            button.setPosition(tileX(i % cols), tileY(i / cols));
            gridGroup.addActor(button);

            Label numLabel = new Label(String.valueOf(levelNumber), numStyle);
            numLabel.setAlignment(Align.center);
            numLabel.setSize(tile, tile);
            numLabel.setPosition(0, 0);
            button.addActor(numLabel);

            if (Progress.isCompleted(levelNumber)) {
                float badge = tile * 0.26f;
                float chk = tile * 0.17f;
                Image checkBg = new Image(new TextureRegionDrawable(new TextureRegion(checkBgTex)));
                checkBg.setSize(badge, badge);
                checkBg.setPosition(tile * 0.82f - badge / 2f, tile * 0.82f - badge / 2f);
                button.addActor(checkBg);
                Image check = new Image(new TextureRegionDrawable(new TextureRegion(checkTex)));
                check.setSize(chk, chk);
                check.setPosition(tile * 0.82f - chk / 2f, tile * 0.82f - chk / 2f);
                button.addActor(check);
            }

            button.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button_) {
                    button.setScale(0.92f);
                    return super.touchDown(event, x, y, pointer, button_);
                }
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button_) {
                    button.setScale(1f);
                    super.touchUp(event, x, y, pointer, button_);
                }
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (dragAccum < tile * 0.3f) {
                        try {
                            intro.goToLevel(levelNumber);
                        } catch (Exception ignored) {
                        }
                    }
                }
            });
        }
        stage.addActor(gridGroup);

        backArrowTex = ProceduralAssets.backArrow(arrowSize, Color.WHITE);
        ImageButton back = new ImageButton(new TextureRegionDrawable(new TextureRegion(backArrowTex)));
        back.getImageCell().size(arrowSize, arrowSize);
        back.setSize(arrowSize, arrowSize);
        back.setTransform(true);
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
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                back.setScale(0.92f);
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                back.setScale(1f);
                super.touchUp(event, x, y, pointer, button);
            }
        });
        stage.addActor(back);
    }

    private float tileX(int col) {
        return startX + col * (tile + gapX);
    }

    private float tileY(int row) {
        return baseTopY - row * (tile + gapY);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleScroll();

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, title, w_world / 2 - glyph.width / 2, baseTitleY + scrollY);
        float soff = w_world * 0.007f;
        for (int i = 0; i < count; i++) {
            ImageButton btn = (ImageButton) gridGroup.getChildren().get(i);
            float s = btn.getScaleX();
            float bx = tileX(i % cols), by = tileY(i / cols) + scrollY;
            float cx = bx + tile * s / 2f, cy = by + tile * s / 2f;
            float sh = tile * s;
            batch.draw(shadowTex, cx + soff - sh / 2f, cy - soff - sh / 2f, sh, sh);
        }
        batch.end();

        stage.act(delta);
        stage.draw();

        batch.begin();
        for (int i = 0; i < count; i++) {
            if (Progress.isCompleted(i + 1)) {
                float bx = tileX(i % cols);
                float by = tileY(i / cols) + scrollY;
                float cx = bx + tile / 2f;
                String t = Progress.formatTime(Progress.bestTime(i + 1));
                timeGlyph.setText(font, t);
                font.draw(batch, t, cx - timeGlyph.width / 2, by - tile * 0.06f);
            }
        }
        batch.end();

        drawFade(delta);
    }

    private void handleScroll() {
        if (maxScroll <= 0f) return;

        if (Gdx.input.isTouched()) {
            float touchY = Gdx.input.getY();
            touchY = Gdx.graphics.getHeight() - touchY;
            touchY = touchY * (h_world / Gdx.graphics.getHeight());

            if (!dragging) {
                if (touchY < arrowY) {
                    lastPointerY = touchY;
                    dragAccum = 0f;
                    dragging = true;
                }
            } else {
                float dy = touchY - lastPointerY;
                dragAccum += Math.abs(dy);
                float newScroll = scrollY + dy;
                newScroll = Math.min(maxScroll, Math.max(0, newScroll));
                if (newScroll != scrollY) {
                    scrollY = newScroll;
                    gridGroup.setY(scrollY);
                }
                lastPointerY = touchY;
            }
        } else {
            dragging = false;
        }
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
