package com.circlesandholes.game.Levels;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.circlesandholes.game.Intro;
import com.circlesandholes.game.PhysicalBodies.BoxBoard;
import com.circlesandholes.game.PhysicalBodies.BoxCircle;
import com.circlesandholes.game.PhysicalBodies.BoxHole;
import com.circlesandholes.game.PhysicalBodies.BoxRectangleBarrier;
import com.circlesandholes.game.TryContinueEnd;

import java.util.ArrayList;

import static com.circlesandholes.game.Intro.POSITION_ITERATIONS;
import static com.circlesandholes.game.Intro.VELOCITY_ITERATIONS;
import static com.circlesandholes.game.Intro.background_2;
import static com.circlesandholes.game.Intro.board_2;
import static com.circlesandholes.game.Intro.box_din;
import static com.circlesandholes.game.Intro.box_hole_din_sign;
import static com.circlesandholes.game.Intro.camera;
import static com.circlesandholes.game.Intro.circle;
import static com.circlesandholes.game.Intro.faild;
import static com.circlesandholes.game.Intro.generator;
import static com.circlesandholes.game.Intro.gest;
import static com.circlesandholes.game.Intro.getTimerDynamicBody;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.height_board;
import static com.circlesandholes.game.Intro.hole_image;
import static com.circlesandholes.game.Intro.lang;
import static com.circlesandholes.game.Intro.minutes;
import static com.circlesandholes.game.Intro.rectangle_barrier_image;
import static com.circlesandholes.game.Intro.seconds;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.text;
import static com.circlesandholes.game.Intro.time_step;
import static com.circlesandholes.game.Intro.w_world;
import static com.circlesandholes.game.Intro.win;

/**
 * Created by bolgov.artem on 11.10.17.
 */

public class Level6 extends Game implements ContactListener, Screen {

    final Intro intro;

    private SpriteBatch batch;

    private World world;

    private Body box_board, box_circle, box_hole, box_rectangle_barrier;
    private Box2DDebugRenderer b2dr;

    private double i_right = 0, i_left = 0;
    private float[] vert;
    private Vector3 touchPos;

    private BitmapFont font_counter;

    private Sprite hole1,
            hole2,
            hole3,
            hole4,
            hole5,
            hole6,
            hole7;

    private Sprite barrier1,
            barrier2,
            barrier3,
            barrier4,
            barrier5,
            barrier6;

    private Body
            box_hole_din1,
            box_hole_din2,
            box_hole_din3,
            box_hole_din4;

    private Sprite
            hole_din1,
            hole_din2,
            hole_din3,
            hole_din4;

    private ArrayList<Sprite> array_hole;
    private ArrayList<Sprite> array_barrier;

    public Level6(final Intro intro) {
        this.intro = intro;

        batch = new SpriteBatch();

        touchPos = new Vector3();

        world = new World(new Vector2(0, (float) -2), true);
        world.setContactListener(this);

        b2dr = new Box2DDebugRenderer();

        FreeTypeFontGenerator.FreeTypeFontParameter parametr_fall = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametr_fall.size = size_text;
        parametr_fall.characters = lang;
        parametr_fall.color = Color.WHITE;
        font_counter = generator.generateFont(parametr_fall);

        //System.out.println(w_world);
        //System.out.println(12345);

        box_board = new BoxBoard().createBoxBoard(world);
        box_board.setUserData("board");

        box_circle = new BoxCircle().createBoxCircle(world, board_2);
        box_circle.setUserData("circle");

        vert = board_2.getVertices();

        box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(board_2.getRotation()));

        array_hole = new ArrayList<Sprite>();
        array_barrier = new ArrayList<Sprite>();

        getLvl();

        text = "00:00";
        minutes = 0;
        seconds = 0;

        getTimerDynamicBody();
    }

    @Override
    public void create() {

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!faild)
            intro.setScreen(new TryContinueEnd(intro, 6));
        else if (win)
            intro.setScreen(new TryContinueEnd(intro, 6));
        else {

            touchPosition();

            circle.setCenter(box_circle.getPosition().x, box_circle.getPosition().y);

            batch.begin();
            batch.draw(background_2, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            for (Sprite hole : array_hole) {
                hole.draw(batch);
            }

            for (Sprite barrier : array_barrier) {
                barrier.draw(batch);
            }

            getDynamicHole();

            circle.draw(batch);
            board_2.draw(batch);
            font_counter.draw(batch, text, (float) (w_world * 0.05), (float) (h_world * 0.98));
            batch.end();

            vert = board_2.getVertices();
            //b2dr.render(world, camera.combined);


            box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(board_2.getRotation()));

            if (box_circle.getPosition().y < board_2.getY() * 0.6 && board_2.getY() > h_world * 0.3) {
                faild = false;
            } else if (box_circle.getPosition().y < h_world * 0.01) {
                faild = false;
            }

            if (box_circle.getPosition().y > (float) (h_world * 0.98)) {
                win = true;
            }

            update(Gdx.graphics.getDeltaTime());
        }
    }

    private void update(float delta) {
        world.step(time_step, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    @Override
    public void hide() {

    }

    @Override
    public void beginContact(Contact contact) {
        String circle_new = contact.getFixtureA().getBody().getUserData().toString();
        String hole = contact.getFixtureB().getBody().getUserData().toString();

        if (circle_new.equals("circle") && hole.equals("hole")) {
            faild = false;
        }
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
    public void dispose() {
        batch.dispose();
        world.dispose();
    }

    private void getLvl() {

        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.14), (float) (h_world * 0.391), 0);
        box_hole.setUserData("hole");

        hole1 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole1.setX((float) (w_world * 0.14 - hole_image.getTextureData().getHeight() / 2));
        hole1.setY((float) (h_world * 0.391 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.625), (float) (h_world * 0.43), 0);
        box_hole.setUserData("hole");

        hole2 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole2.setX((float) (w_world * 0.625 - hole_image.getTextureData().getHeight() / 2));
        hole2.setY((float) (h_world * 0.43 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.14), (float) (h_world * 0.625), 0);
        box_hole.setUserData("hole");

        hole3 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole3.setX((float) (w_world * 0.14 - hole_image.getTextureData().getHeight() / 2));
        hole3.setY((float) (h_world * 0.625 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.834), (float) (h_world * 0.625), 0);
        box_hole.setUserData("hole");

        hole4 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole4.setX((float) (w_world * 0.834 - hole_image.getTextureData().getHeight() / 2));
        hole4.setY((float) (h_world * 0.625 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.07), (float) (h_world * 0.86), 0);
        box_hole.setUserData("hole");

        hole5 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole5.setX((float) (w_world * 0.07- hole_image.getTextureData().getHeight() / 2));
        hole5.setY((float) (h_world * 0.86 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.903), (float) (h_world * 0.86), 0);
        box_hole.setUserData("hole");

        hole6 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole6.setX((float) (w_world * 0.903 - hole_image.getTextureData().getHeight() / 2));
        hole6.setY((float) (h_world * 0.86 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.417), (float) (h_world * 0.235), 0);
        box_hole.setUserData("hole");

        hole7 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole7.setX((float) (w_world * 0.417 - hole_image.getTextureData().getHeight() / 2));
        hole7.setY((float) (h_world * 0.235 - hole_image.getTextureData().getHeight() / 2));

        array_hole.add(hole1);
        array_hole.add(hole2);
        array_hole.add(hole3);
        array_hole.add(hole4);
        array_hole.add(hole5);
        array_hole.add(hole6);
        array_hole.add(hole7);

        addDynamicHole();
        addRectangleBarrier();

    }

    private void addDynamicHole() {

        box_hole_din1 = new BoxHole().createBoxHole(world);
        box_hole_din1.setUserData("hole");
        box_hole_din2 = new BoxHole().createBoxHole(world);
        box_hole_din2.setUserData("hole");
        box_hole_din3 = new BoxHole().createBoxHole(world);
        box_hole_din3.setUserData("hole");
        box_hole_din4 = new BoxHole().createBoxHole(world);
        box_hole_din4.setUserData("hole");

        hole_din1 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din2 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din3 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din4 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
    }

    private void getDynamicHole() {

        if (box_hole_din1.getPosition().x <= w_world * 0.556) {
            box_hole_din_sign = false;
        } else if (box_hole_din1.getPosition().x >= w_world * 0.765) {
            box_hole_din_sign = true;
        }

        box_hole_din1.setTransform((float) (w_world * 0.765 - box_din), (float) (h_world * 0.3), 0);

        hole_din1.setX(box_hole_din1.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din1.setY(box_hole_din1.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din1.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din2.setTransform((float) (w_world * 0.487), (float) (h_world * 0.47 + box_din), 0);

        hole_din2.setX(box_hole_din2.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din2.setY(box_hole_din2.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din2.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din3.setTransform((float) (w_world * 0.775 - box_din), (float) (h_world * 0.93 - box_din), 0);

        hole_din3.setX(box_hole_din3.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din3.setY(box_hole_din3.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din3.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din4.setTransform((float) (w_world * 0.19 + box_din), (float) (h_world * 0.93 - box_din), 0);

        hole_din4.setX(box_hole_din4.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din4.setY(box_hole_din4.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din4.draw(batch);
    }

    private void addRectangleBarrier() {

        int width = (int) (w_world * 0.306);
        int height = (int) (h_world * 0.0313);

        barrier1 = new Sprite(rectangle_barrier_image, width, height);
        barrier1.setX((float) (w_world * 0.2));
        barrier1.setY((float) (h_world * 0.323));
        barrier1.rotate(30);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier1.getVertices()[0], barrier1.getVertices()[1], (float) Math.toRadians(barrier1.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        /////////////////////////////---------------------------------------------------------------

        barrier2 = new Sprite(rectangle_barrier_image, width, height);
        barrier2.setX((float) (w_world * 0.58));
        barrier2.setY((float) (h_world * 0.515));
        barrier2.rotate(-35);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier2.getVertices()[0], barrier2.getVertices()[1], (float) Math.toRadians(barrier2.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        /////////////////////////////---------------------------------------------------------------

        barrier3 = new Sprite(rectangle_barrier_image, width, height);
        barrier3.setX((float) (w_world * 0.08));
        barrier3.setY((float) (h_world * 0.515));
        barrier3.rotate(-145);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier3.getVertices()[0], barrier3.getVertices()[1], (float) Math.toRadians(barrier3.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        /////////////////////////////---------------------------------------------------------------

        barrier4 = new Sprite(rectangle_barrier_image, width, height);
        barrier4.setX((float) (w_world * 0.58));
        barrier4.setY((float) (h_world * 0.71));
        barrier4.rotate(-145);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier4.getVertices()[0], barrier4.getVertices()[1], (float) Math.toRadians(barrier4.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        /////////////////////////////---------------------------------------------------------------

        barrier5 = new Sprite(rectangle_barrier_image, width, height);
        barrier5.setX((float) (w_world * 0.08));
        barrier5.setY((float) (h_world * 0.71));
        barrier5.rotate(-35);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier5.getVertices()[0], barrier5.getVertices()[1], (float) Math.toRadians(barrier5.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        /////////////////////////////---------------------------------------------------------------

        barrier6 = new Sprite(rectangle_barrier_image, width, height);
        barrier6.setX((float) (w_world * 0.33));
        barrier6.setY((float) (h_world * 0.85));
        barrier6.rotate(90);

        box_rectangle_barrier = new BoxRectangleBarrier().createBoxRectangleBarrier(world, width / 2, height / 2);
        box_rectangle_barrier.setTransform(barrier6.getVertices()[0], barrier6.getVertices()[1], (float) Math.toRadians(barrier6.getRotation()));
        box_rectangle_barrier.setUserData("barrier");

        array_barrier.add(barrier1);
        array_barrier.add(barrier2);
        array_barrier.add(barrier3);
        array_barrier.add(barrier4);
        array_barrier.add(barrier5);
        array_barrier.add(barrier6);

    }

    private void touchPosition() {
        if (Gdx.input.isTouched()) {
            int oX, oY;
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (touchPos.x < w_world / 2) {
                if (i_right == 0) {
                    oX = (int) (w_world - w_world * 0.25);
                    oY = (int) height_board;
                    board_2.setOrigin(oX, oY);
                    board_2.setX(vert[10] - oX);
                    board_2.setY(vert[11] - oY);
                    board_2.setRotation((float) i_left);
                    i_right = i_left;
                }
                i_right = i_right - 0.7;

                if (board_2.getRotation() != -28)
                    board_2.setRotation((float) i_right);
                else
                    i_right = -28;

                i_left = 0;
            } else {
                if (i_left == 0) {
                    oX = 0;
                    oY = (int) height_board;
                    board_2.setOrigin(oX, oY);
                    board_2.setX(vert[5]);
                    board_2.setY(vert[6] - oY);
                    board_2.setRotation((float) i_right);
                    i_left = i_right;
                }

                i_left = i_left + 0.7;

                if (board_2.getRotation() != 28)
                    board_2.setRotation((float) i_left);
                else
                    i_left = 28;

                i_right = 0;
            }
        }
    }
}