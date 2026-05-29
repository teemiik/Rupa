package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.circlesandholes.game.PhysicalBodies.*;

import java.util.ArrayList;
import java.util.List;

import static com.circlesandholes.game.Intro.*;

/**
 * Single, data-driven game screen that replaces the six hand-written Level1..6
 * classes. It reads a {@link LevelData} (parsed from a JSON file) and builds the
 * board, ball, static/dynamic holes and barriers from it, while keeping the
 * shared tilt/physics/win-fail logic in one place. Adding a level is now just a
 * new JSON file — see assets/levels/.
 */
public class LevelScreen extends Game implements ContactListener, Screen {

    private final Intro intro;
    private final int level;
    private final LevelData data;

    private final SpriteBatch batch;
    private final World world;
    private final Box2DDebugRenderer b2dr;

    private Body box_board, box_circle;

    private final Sprite boardSprite;
    private final Sprite backgroundSprite;

    private double i_right = 0, i_left = 0;
    private float[] vert;
    private final Vector3 touchPos = new Vector3();
    private boolean touch = false;

    private final BitmapFont font_counter;

    private final int holeHalf;

    private final List<Sprite> staticHoles = new ArrayList<Sprite>();
    private final List<Body> dynamicBodies = new ArrayList<Body>();
    private final List<Sprite> dynamicSprites = new ArrayList<Sprite>();
    private final List<Sprite> barriers = new ArrayList<Sprite>();

    private final List<Body> rotatingPlatformBodies = new ArrayList<Body>();
    private final List<Sprite> rotatingPlatformSprites = new ArrayList<Sprite>();
    private final List<LevelData.RotatingPlatform> rotatingPlatformDataList = new ArrayList<LevelData.RotatingPlatform>();
    private Texture accentPlatformTex, defaultPlatformTex;

    // Tilt-gesture hints (tutorial level only). Two alternating frames per side.
    private Texture hintRightTexA, hintRightTexB, hintLeftTexA, hintLeftTexB;
    private Sprite hintRightA, hintRightB, hintLeftA, hintLeftB;

    // Pause overlay and feedback.
    private final BitmapFont pauseFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Texture blackTex;
    private final Texture pauseIconTex;
    private final Sprite pauseSprite;
    private final Texture shadowTex;
    private final Texture panelTex;
    private final Texture panelShadowTex;
    private float panelX, panelY, panelW, panelH;
    private float pauseX, pauseY, pauseSize;
    private float dbgX, dbgY, dbgW, dbgH;
    private final String[] pauseOptions = {
        Intro.bundle.get("pause_continue"),
        Intro.bundle.get("pause_restart"),
        Intro.bundle.get("pause_levels"),
        Intro.bundle.get("pause_menu")
    };
    private float fade = 0.7f;
    private int pressedPauseIndex = -1;
    private float pausePressTimer = 0f;
    private float pauseShift;

    public LevelScreen(final Intro intro, int level, LevelData data) {
        this.intro = intro;
        this.level = level;
        this.data = data;

        batch = new SpriteBatch();
        // Gravity scales with screen height so the ball moves at a consistent on-screen
        // speed at any resolution (the world is in pixels, so a fixed value felt sluggish
        // on large screens). 640 keeps the current desktop feel unchanged.
        world = new World(new Vector2(0, -2f), true);
        world.setContactListener(this);
        b2dr = new Box2DDebugRenderer();

        boardSprite = "board_2".equals(data.board) ? board_2 : board;
        backgroundSprite = Intro.currentBackground;

        font_counter = Intro.makeFont(size_text);

        box_board = new BoxBoard().createBoxBoard(world);
        box_board.setUserData("board");

        box_circle = new BoxCircle().createBoxCircle(world, boardSprite);
        box_circle.setUserData("circle");

        vert = boardSprite.getVertices();
        box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(boardSprite.getRotation()));

        holeHalf = hole_image.getTextureData().getHeight() / 2;

        buildStaticHoles();
        buildDynamicHoles();
        buildBarriers();
        buildRotatingPlatforms();
        if (data.showHints) {
            buildHints();
        }

        text = Intro.bundle.get("timer_placeholder");
        minutes = 0;
        seconds = 0;

        if (!data.dynamicHoles.isEmpty()) {
            getTimerDynamicBody();
        }

        pauseFont = Intro.makeFont(size_text_result);
        blackTex = ProceduralAssets.solid(Color.BLACK);
        pauseSize = w_world * 0.07f;
        pauseX = w_world - pauseSize - w_world * 0.05f;
        pauseY = h_world - pauseSize - h_world * 0.03f;
        pauseIconTex = ProceduralAssets.pauseIcon(Math.round(pauseSize), Color.WHITE);
        pauseSprite = new Sprite(pauseIconTex);
        pauseSprite.setBounds(pauseX, pauseY, pauseSize, pauseSize);

        dbgW = w_world * 0.2f;
        dbgH = h_world * 0.045f;
        dbgX = w_world * 0.05f;
        dbgY = h_world * 0.86f;

        shadowTex = ProceduralAssets.softCircle(64);
        GlyphLayout pm = new GlyphLayout();
        float maxW = 0f;
        for (String s : pauseOptions) {
            pm.setText(pauseFont, s);
            maxW = Math.max(maxW, pm.width);
        }
        pm.setText(pauseFont, Intro.bundle.get("pause_title"));
        maxW = Math.max(maxW, pm.width);

        float padH = w_world * 0.08f;
        float padV = w_world * 0.08f;
        float radius = w_world * 0.05f;
        float optH = pauseFont.getLineHeight();
        panelW = Math.min(maxW + padH * 2, w_world * 0.92f);

        float pauseAscent = pauseFont.getAscent();
        float pauseDescent = pauseFont.getDescent();
        float centerY = h_world / 2f;
        float pauseTitleY = centerY + optH * 3.3f;
        float pauseOptLastY = centerY + optH * 1.5f - (pauseOptions.length - 1) * optH * 1.8f;

        float contTop = pauseTitleY + pauseAscent;
        float contBot = pauseOptLastY + pauseDescent;
        pauseShift = centerY - (contTop + contBot) / 2f + optH * 0.25f;

        float contentTop = pauseTitleY + pauseAscent + pauseShift;
        float contentBot = pauseOptLastY + pauseDescent + pauseShift;
        float contentH = contentTop - contentBot;
        panelH = Math.min(contentH + padV * 2, h_world * 0.85f);
        panelX = (w_world - panelW) / 2f;
        panelY = centerY - panelH / 2f;

        panelTex = ProceduralAssets.roundRectGradient(Math.round(panelW), Math.round(panelH),
                radius,
                new Color(0.22f, 0.18f, 0.32f, 0.40f), new Color(0.08f, 0.06f, 0.14f, 0.60f));

        panelShadowTex = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelH),
                radius, new Color(0f, 0f, 0f, 0.25f));
    }

    private void drawShadow(float cx, float cy, float diameter) {
        float s = diameter * 1.4f;
        batch.setColor(0f, 0f, 0f, 0.28f);
        batch.draw(shadowTex, cx - s / 2f, cy - s / 2f - diameter * 0.1f, s, s);
        batch.setColor(Color.WHITE);
    }

    private void drawShadowedText(BitmapFont font, String s, float x, float y, boolean pressed) {
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

    private void buildStaticHoles() {
        for (float[] xy : data.holes) {
            float x = w_world * xy[0];
            float y = h_world * xy[1];

            Body body = new BoxHole().createBoxHole(world);
            body.setTransform(x, y, 0);
            body.setUserData("hole");

            Sprite s = newHoleSprite();
            s.setX(x - holeHalf);
            s.setY(y - holeHalf);
            staticHoles.add(s);
        }
    }

    private void buildDynamicHoles() {
        for (int i = 0; i < data.dynamicHoles.size(); i++) {
            Body body = new BoxHole().createBoxHole(world);
            body.setUserData("hole");
            dynamicBodies.add(body);
            dynamicSprites.add(newHoleSprite());
        }
    }

    private void buildBarriers() {
        for (LevelData.Barrier bar : data.barriers) {
            int width = (int) (w_world * bar.w);
            int height = (int) (h_world * bar.h);

            Sprite s = new Sprite(Intro.rectangle_barrier_image, width, height);
            s.setX(w_world * bar.x);
            s.setY(h_world * bar.y);
            s.rotate(bar.rotation);

            Body body = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
            body.setTransform(s.getVertices()[0], s.getVertices()[1], (float) Math.toRadians(s.getRotation()));
            body.setUserData("barrier");

            barriers.add(s);
        }
    }

    private void buildRotatingPlatforms() {
        if (data.rotatingPlatforms.isEmpty()) return;

        accentPlatformTex = ProceduralAssets.pixelBarTexture(new Color(0.8f, 0.4f, 0.2f, 1f));
        defaultPlatformTex = ProceduralAssets.pixelBarTexture(Intro.BARRIER_COLOR);

        for (LevelData.RotatingPlatform rp : data.rotatingPlatforms) {
            int width = (int) (w_world * rp.w);
            int height = (int) (h_world * rp.h);

            Texture tex = "accent".equals(rp.color) ? accentPlatformTex : defaultPlatformTex;
            Sprite s = new Sprite(tex, width, height);
            s.setX(w_world * rp.x - width / 2f);
            s.setY(h_world * rp.y - height / 2f);

            Body body = new BoxRotatingPlatform().create(world, width / 2f, height / 2f);
            body.setTransform(s.getX() + width / 2f, s.getY() + height / 2f, 0);
            body.setUserData("rotatingPlatform");

            rotatingPlatformBodies.add(body);
            rotatingPlatformSprites.add(s);
            rotatingPlatformDataList.add(rp);
        }
    }

    private void buildHints() {
        hintRightTexA = new Texture(texture_gest_right);
        hintRightTexB = new Texture(texture_gest_right_update);
        hintLeftTexA = new Texture(texture_gest_left);
        hintLeftTexB = new Texture(texture_gest_left_update);

        hintRightA = placeRightHint(new Sprite(hintRightTexA));
        hintRightB = placeRightHint(new Sprite(hintRightTexB));
        hintLeftA = placeLeftHint(new Sprite(hintLeftTexA));
        hintLeftB = placeLeftHint(new Sprite(hintLeftTexB));
    }

    private Sprite placeRightHint(Sprite s) {
        s.setX((float) (w_world * 0.8));
        s.setY((float) (h_world * 0.1));
        return s;
    }

    private Sprite placeLeftHint(Sprite s) {
        s.setX((float) (w_world * 0.2 - s.getWidth()));
        s.setY((float) (h_world * 0.1));
        return s;
    }

    private Sprite newHoleSprite() {
        return new Sprite(hole_image,
                hole_image.getTextureData().getHeight(),
                hole_image.getTextureData().getHeight());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!faild || win) {
            if (win) {
                Intro.finishSeconds = minutes * 60 + seconds;
            }
            Intro.vibrate(win ? 60 : 120);
            intro.setScreen(new TryContinueEnd(intro, level));
            return;
        }

        if (Intro.paused) {
            if (pausePressTimer > 0) {
                pausePressTimer -= delta;
                if (pausePressTimer <= 0) {
                    pausePressTimer = 0;
                    executePauseAction();
                    pressedPauseIndex = -1;
                    if (!Intro.paused) { drawFade(delta); return; }
                }
            }
            drawWorld();
            drawPauseOverlay();
            handlePauseInput();
            drawFade(delta);
            return;
        }

        // Tap the pause button (top-right) to pause.
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            if (touchPos.x >= pauseX && touchPos.x <= pauseX + pauseSize
                    && touchPos.y >= pauseY && touchPos.y <= pauseY + pauseSize) {
                Intro.paused = true;
                return;
            }
            if (Debug.enabled && touchPos.x >= dbgX && touchPos.x <= dbgX + dbgW
                    && touchPos.y >= dbgY && touchPos.y <= dbgY + dbgH) {
                win = true; // instant win for testing the win screen (desktop only)
                return;
            }
        }

        touchPosition();

        circle.setCenter(box_circle.getPosition().x, box_circle.getPosition().y);

        drawWorld();

        vert = boardSprite.getVertices();
        box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(boardSprite.getRotation()));

        if (box_circle.getPosition().y < boardSprite.getY() * 0.6 && boardSprite.getY() > h_world * 0.3) {
            faild = false;
        } else if (box_circle.getPosition().y < h_world * 0.01) {
            faild = false;
        }

        if (box_circle.getPosition().y > (float) (h_world * 0.98)) {
            win = true;
        }

        for (int i = 0; i < rotatingPlatformBodies.size(); i++) {
            Body body = rotatingPlatformBodies.get(i);
            Sprite s = rotatingPlatformSprites.get(i);
            s.setPosition(body.getPosition().x - s.getWidth() / 2f, body.getPosition().y - s.getHeight() / 2f);
            s.setRotation((float) Math.toDegrees(body.getAngle()));
        }

        // The world is in device pixels, so advance the simulation proportionally to screen
        // height (normalised to 640) — otherwise the ball covers a smaller fraction of a
        // big screen per frame and feels sluggish. Split into ~0.6 sub-steps for stability.
        float total = time_step * (h_world / 640f);
        int subSteps = Math.max(1, (int) Math.ceil(total / time_step));
        float sub = total / subSteps;
        for (int s = 0; s < subSteps; s++) {
            world.step(sub, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }

        // Rotate platforms manually using real delta time, so rotationSpeed means
        // degrees per real second regardless of screen size or frame rate.
        for (int i = 0; i < rotatingPlatformBodies.size(); i++) {
            Body body = rotatingPlatformBodies.get(i);
            LevelData.RotatingPlatform rp = rotatingPlatformDataList.get(i);
            float angleDelta = (float) Math.toRadians(rp.rotationSpeed) * delta;
            body.setTransform(body.getPosition(), body.getAngle() + angleDelta);
        }

        drawFade(delta);
    }

    private void drawWorld() {
        batch.begin();
        batch.draw(backgroundSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (data.showHints && !touch) {
            drawHints();
        }
        for (Sprite hole : staticHoles) {
            drawShadow(hole.getX() + holeHalf, hole.getY() + holeHalf, hole.getWidth());
            hole.draw(batch);
        }
        for (Sprite barrier : barriers) {
            barrier.draw(batch);
        }
        for (Sprite rp : rotatingPlatformSprites) {
            rp.draw(batch);
        }
        if (!dynamicSprites.isEmpty()) {
            updateDynamicHoles();
        }
        drawShadow(circle.getX() + circle.getWidth() / 2f, circle.getY() + circle.getHeight() / 2f, circle.getWidth());
        circle.draw(batch);
        boardSprite.draw(batch);
        font_counter.draw(batch, text, (float) (w_world * 0.05), (float) (h_world * 0.98));
        pauseSprite.draw(batch);
        if (Debug.enabled) {
            batch.setColor(0f, 0f, 0f, 0.5f);
            batch.draw(blackTex, dbgX, dbgY, dbgW, dbgH);
            batch.setColor(Color.WHITE);
            glyph.setText(font_counter, "WIN");
            font_counter.draw(batch, "WIN", dbgX + dbgW / 2 - glyph.width / 2, dbgY + dbgH / 2 + glyph.height / 2);
        }
        batch.end();
    }

    private void drawPauseOverlay() {
        batch.begin();
        batch.setColor(0f, 0f, 0f, 0.30f);
        batch.draw(blackTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        float soff = w_world * 0.007f;
        batch.draw(panelShadowTex, panelX + soff, panelY - soff, panelW, panelH);
        batch.draw(panelTex, panelX, panelY, panelW, panelH);

        float centerY = h_world / 2f;
        glyph.setText(pauseFont, Intro.bundle.get("pause_title"));
        drawShadowedText(pauseFont, Intro.bundle.get("pause_title"), w_world / 2 - glyph.width / 2, centerY + pauseFont.getLineHeight() * 3.3f + pauseShift, false);

        for (int i = 0; i < pauseOptions.length; i++) {
            glyph.setText(pauseFont, pauseOptions[i]);
            drawShadowedText(pauseFont, pauseOptions[i], w_world / 2 - glyph.width / 2, optionY(i), i == pressedPauseIndex);
        }
        batch.end();
    }

    private float optionY(int i) {
        float centerY = h_world / 2f + pauseFont.getLineHeight() * 1.5f + pauseShift;
        float spacing = pauseFont.getLineHeight() * 1.8f;
        return centerY - i * spacing;
    }

    private void handlePauseInput() {
        if (pausePressTimer > 0 || !Gdx.input.justTouched()) return;
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        float bandW = w_world * 0.6f;
        float bandH = pauseFont.getLineHeight() * 1.6f;
        for (int i = 0; i < pauseOptions.length; i++) {
            float cy = optionY(i) - pauseFont.getLineHeight() / 2f;
            if (touchPos.x >= w_world / 2 - bandW / 2 && touchPos.x <= w_world / 2 + bandW / 2
                    && touchPos.y >= cy - bandH / 2 && touchPos.y <= cy + bandH / 2) {
                pressedPauseIndex = i;
                pausePressTimer = 0.08f;
                return;
            }
        }
    }

    private void executePauseAction() {
        int i = pressedPauseIndex;
        if (i == 0) {
            Intro.paused = false;
            Intro.consumeTouch = true;
        } else if (i == 1) {
            intro.goToLevel(level);
        } else if (i == 2) {
            intro.setScreen(new LevelSelectScreen(intro, this));
        } else if (i == 3) {
            Intro.paused = false;
            intro.showMenu();
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

    private void drawHints() {
        if (Intro.gest % 2 == 0) {
            hintRightB.draw(batch);
            hintLeftA.draw(batch);
        } else {
            hintRightA.draw(batch);
            hintLeftB.draw(batch);
        }
    }

    private void updateDynamicHoles() {
        // The first dynamic hole drives the oscillation: when it reaches a bound
        // the shared box_din counter reverses (incremented/decremented by Intro's
        // timer thread). The last level inverts the comparison direction.
        float ctrlX = dynamicBodies.get(0).getPosition().x;
        if (!data.ctrlInverted) {
            if (ctrlX >= data.ctrlUpperX * w_world) {
                Intro.box_hole_din_sign = false;
            } else if (ctrlX <= data.ctrlLowerX * w_world) {
                Intro.box_hole_din_sign = true;
            }
        } else {
            if (ctrlX <= data.ctrlLowerX * w_world) {
                Intro.box_hole_din_sign = false;
            } else if (ctrlX >= data.ctrlUpperX * w_world) {
                Intro.box_hole_din_sign = true;
            }
        }

        for (int i = 0; i < dynamicBodies.size(); i++) {
            LevelData.DynamicHole dh = data.dynamicHoles.get(i);
            Body body = dynamicBodies.get(i);
            Sprite s = dynamicSprites.get(i);

            float x = w_world * dh.x + dh.xDin * Intro.box_din;
            float y = h_world * dh.y + dh.yDin * Intro.box_din;
            body.setTransform(x, y, 0);

            s.setX(body.getPosition().x - holeHalf);
            s.setY(body.getPosition().y - holeHalf);
            drawShadow(s.getX() + holeHalf, s.getY() + holeHalf, s.getWidth());
            s.draw(batch);
        }
    }

    private void touchPosition() {
        // Ignore the touch that started a level / dismissed the pause menu until it is released,
        // so that tap does not also tilt the board.
        if (Intro.consumeTouch) {
            if (Gdx.input.isTouched()) return;
            Intro.consumeTouch = false;
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (touchPos.y > h_world * 0.9f) return; // top HUD strip (timer / pause button)

            int oX, oY;
            touch = true;

            if (touchPos.x < w_world / 2) {
                if (i_right == 0) {
                    oX = (int) (w_world - w_world * 0.25);
                    oY = (int) height_board;
                    boardSprite.setOrigin(oX, oY);
                    boardSprite.setX(vert[10] - oX);
                    boardSprite.setY(vert[11] - oY);
                    boardSprite.setRotation((float) i_left);
                    i_right = i_left;
                }
                i_right = i_right - 0.7;

                if (boardSprite.getRotation() != -28)
                    boardSprite.setRotation((float) i_right);
                else
                    i_right = -28;

                i_left = 0;
            } else {
                if (i_left == 0) {
                    oX = 0;
                    oY = (int) height_board;
                    boardSprite.setOrigin(oX, oY);
                    boardSprite.setX(vert[5]);
                    boardSprite.setY(vert[6] - oY);
                    boardSprite.setRotation((float) i_right);
                    i_left = i_right;
                }

                i_left = i_left + 0.7;

                if (boardSprite.getRotation() != 28)
                    boardSprite.setRotation((float) i_left);
                else
                    i_left = 28;

                i_right = 0;
            }
        }
    }

    @Override
    public void beginContact(Contact contact) {
        String a = contact.getFixtureA().getBody().getUserData().toString();
        String b = contact.getFixtureB().getBody().getUserData().toString();

        if (a.equals("circle") && b.equals("hole")) {
            faild = false;
        }
    }

    @Override
    public void create() {
    }

    @Override
    public void show() {
        // Gameplay reads input by polling, so detach the previous screen's stage;
        // otherwise its buttons (e.g. the level-select tiles) keep catching taps.
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void hide() {
    }

    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        pauseFont.dispose();
        blackTex.dispose();
        pauseIconTex.dispose();
        shadowTex.dispose();
        panelTex.dispose();
        panelShadowTex.dispose();
        if (hintRightTexA != null) {
            hintRightTexA.dispose();
            hintRightTexB.dispose();
            hintLeftTexA.dispose();
            hintLeftTexB.dispose();
        }
        if (accentPlatformTex != null) {
            accentPlatformTex.dispose();
            defaultPlatformTex.dispose();
        }
    }
}
