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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import com.circlesandholes.game.PhysicalBodies.BoxBoard;
import com.circlesandholes.game.PhysicalBodies.BoxCircle;
import com.circlesandholes.game.PhysicalBodies.BoxHole;
import com.circlesandholes.game.PhysicalBodies.BoxRectangleBarrier;

import java.util.ArrayList;
import java.util.List;

import static com.circlesandholes.game.Intro.POSITION_ITERATIONS;
import static com.circlesandholes.game.Intro.VELOCITY_ITERATIONS;
import static com.circlesandholes.game.Intro.background;
import static com.circlesandholes.game.Intro.background_2;
import static com.circlesandholes.game.Intro.board;
import static com.circlesandholes.game.Intro.board_2;
import static com.circlesandholes.game.Intro.camera;
import static com.circlesandholes.game.Intro.circle;
import static com.circlesandholes.game.Intro.faild;
import static com.circlesandholes.game.Intro.generator;
import static com.circlesandholes.game.Intro.getTimerDynamicBody;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.height_board;
import static com.circlesandholes.game.Intro.hole_image;
import static com.circlesandholes.game.Intro.lang;
import static com.circlesandholes.game.Intro.minutes;
import static com.circlesandholes.game.Intro.seconds;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.size_text_result;
import static com.circlesandholes.game.Intro.text;
import static com.circlesandholes.game.Intro.texture_gest_left;
import static com.circlesandholes.game.Intro.texture_gest_left_update;
import static com.circlesandholes.game.Intro.texture_gest_right;
import static com.circlesandholes.game.Intro.texture_gest_right_update;
import static com.circlesandholes.game.Intro.time_step;
import static com.circlesandholes.game.Intro.w_world;
import static com.circlesandholes.game.Intro.win;

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

    // Tilt-gesture hints (tutorial level only). Two alternating frames per side.
    private Texture hintRightTexA, hintRightTexB, hintLeftTexA, hintLeftTexB;
    private Sprite hintRightA, hintRightB, hintLeftA, hintLeftB;

    // Pause overlay and feedback.
    private final BitmapFont pauseFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Texture blackTex;
    private final Texture pauseIconTex;
    private final Sprite pauseSprite;
    private final Texture exitIconTex;
    private final Sprite exitSprite;
    private final Texture shadowTex;
    private final Texture panelTex;
    private float panelX, panelY, panelW, panelH;
    private float pauseX, pauseY, pauseSize;
    private float dbgX, dbgY, dbgW, dbgH;
    private final String[] pauseOptions = {"Продолжить", "Заново", "Уровни"};
    private float fade = 0.7f;

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
        if (data.showHints) {
            buildHints();
        }

        text = "00:00";
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

        float exS = w_world * 0.1f;
        exitIconTex = ProceduralAssets.powerIcon(Math.round(exS), Color.WHITE);
        exitSprite = new Sprite(exitIconTex);
        exitSprite.setBounds(w_world / 2 - exS / 2, h_world * 0.30f - exS / 2, exS, exS);

        shadowTex = ProceduralAssets.softCircle(64);
        panelW = w_world * 0.7f;
        panelH = h_world * 0.60f;
        panelX = (w_world - panelW) / 2f;
        panelY = h_world * 0.20f;
        panelTex = ProceduralAssets.roundRect(Math.round(panelW), Math.round(panelH),
                w_world * 0.05f, new Color(0.18f, 0.18f, 0.24f, 0.55f));
    }

    private void drawShadow(float cx, float cy, float diameter) {
        float s = diameter * 1.4f;
        batch.setColor(0f, 0f, 0f, 0.28f);
        batch.draw(shadowTex, cx - s / 2f, cy - s / 2f - diameter * 0.1f, s, s);
        batch.setColor(Color.WHITE);
    }

    private void drawShadowedText(BitmapFont font, String s, float x, float y) {
        float o = Math.max(1f, w_world * 0.004f);
        font.setColor(0f, 0f, 0f, 0.5f);
        font.draw(batch, s, x + o, y - o);
        font.setColor(Color.WHITE);
        font.draw(batch, s, x, y);
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

        // The world is in device pixels, so advance the simulation proportionally to screen
        // height (normalised to 640) — otherwise the ball covers a smaller fraction of a
        // big screen per frame and feels sluggish. Split into ~0.6 sub-steps for stability.
        float total = time_step * (h_world / 640f);
        int subSteps = Math.max(1, (int) Math.ceil(total / time_step));
        float sub = total / subSteps;
        for (int s = 0; s < subSteps; s++) {
            world.step(sub, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
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
        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(blackTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.draw(panelTex, panelX, panelY, panelW, panelH);

        glyph.setText(pauseFont, "Пауза");
        drawShadowedText(pauseFont, "Пауза", w_world / 2 - glyph.width / 2, h_world * 0.72f);

        for (int i = 0; i < pauseOptions.length; i++) {
            glyph.setText(pauseFont, pauseOptions[i]);
            drawShadowedText(pauseFont, pauseOptions[i], w_world / 2 - glyph.width / 2, optionY(i));
        }
        exitSprite.draw(batch);
        batch.end();
    }

    private float optionY(int i) {
        float centerY = h_world * 0.61f;
        float spacing = pauseFont.getLineHeight() * 1.8f;
        return centerY - i * spacing;
    }

    private void handlePauseInput() {
        if (!Gdx.input.justTouched()) return;
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        float bandW = w_world * 0.6f;
        float bandH = pauseFont.getLineHeight() * 1.6f;
        for (int i = 0; i < pauseOptions.length; i++) {
            float cy = optionY(i) - pauseFont.getLineHeight() / 2f;
            if (touchPos.x >= w_world / 2 - bandW / 2 && touchPos.x <= w_world / 2 + bandW / 2
                    && touchPos.y >= cy - bandH / 2 && touchPos.y <= cy + bandH / 2) {
                if (i == 0) {                  // resume
                    Intro.paused = false;
                    Intro.consumeTouch = true;
                } else if (i == 1) {           // restart (goToLevel resets paused + consumeTouch)
                    intro.goToLevel(level);
                } else {                       // levels — stay paused so Back returns to the pause menu
                    intro.setScreen(new LevelSelectScreen(intro, this));
                }
                return;
            }
        }

        if (touchPos.x >= exitSprite.getX() && touchPos.x <= exitSprite.getX() + exitSprite.getWidth()
                && touchPos.y >= exitSprite.getY() && touchPos.y <= exitSprite.getY() + exitSprite.getHeight()) {
            Gdx.app.exit();
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
        exitIconTex.dispose();
        shadowTex.dispose();
        panelTex.dispose();
        if (hintRightTexA != null) {
            hintRightTexA.dispose();
            hintRightTexB.dispose();
            hintLeftTexA.dispose();
            hintLeftTexB.dispose();
        }
    }
}
