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
import com.circlesandholes.game.TryContinueEnd;

import java.util.ArrayList;

import static com.circlesandholes.game.Intro.POSITION_ITERATIONS;
import static com.circlesandholes.game.Intro.VELOCITY_ITERATIONS;
import static com.circlesandholes.game.Intro.background;
import static com.circlesandholes.game.Intro.board;
import static com.circlesandholes.game.Intro.box_din;
import static com.circlesandholes.game.Intro.camera;
import static com.circlesandholes.game.Intro.circle;
import static com.circlesandholes.game.Intro.faild;
import static com.circlesandholes.game.Intro.generator;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.height_board;
import static com.circlesandholes.game.Intro.hole_image;
import static com.circlesandholes.game.Intro.lang;
import static com.circlesandholes.game.Intro.minutes;
import static com.circlesandholes.game.Intro.seconds;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.text;
import static com.circlesandholes.game.Intro.time_step;
import static com.circlesandholes.game.Intro.w_world;
import static com.circlesandholes.game.Intro.win;
import static com.circlesandholes.game.Intro.box_hole_din_sign;

import static com.circlesandholes.game.Intro.getTimerDynamicBody;

/**
 * Created by bolgov.artem on 11.10.17.
 */

public class Level4 extends Game implements ContactListener, Screen {

    final Intro intro;

    private SpriteBatch batch;

    private World world;

    private Body box_board, box_circle, box_hole;
    private Box2DDebugRenderer b2dr;

    private double i_right = 0, i_left = 0;
    private float[] vert;
    private Vector3 touchPos;

    private BitmapFont font_counter;

    private Sprite hole2,
            hole6,
            hole8,
            hole10,
            hole13,
            hole14,
            hole16;

    private Body
            box_hole_din1,
            box_hole_din2,
            box_hole_din3,
            box_hole_din4,
            box_hole_din5,
            box_hole_din6,
            box_hole_din7,
            box_hole_din8;


    private Sprite
            hole_din1,
            hole_din2,
            hole_din3,
            hole_din4,
            hole_din5,
            hole_din6,
            hole_din7,
            hole_din8;

    private ArrayList<Sprite> array_hole;

    public Level4(final Intro intro) {
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

        box_circle = new BoxCircle().createBoxCircle(world, board);
        box_circle.setUserData("circle");

        vert = board.getVertices();

        box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(board.getRotation()));

        array_hole = new ArrayList<Sprite>();

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
            intro.setScreen(new TryContinueEnd(intro, 4));
        else if (win)
            intro.setScreen(new TryContinueEnd(intro, 4));
        else {

            touchPosition();

            circle.setCenter(box_circle.getPosition().x, box_circle.getPosition().y);

            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            for (Sprite hole : array_hole) {
                hole.draw(batch);
            }

            getDynamicHole();

            circle.draw(batch);
            board.draw(batch);
            font_counter.draw(batch, text, (float) (w_world * 0.05), (float) (h_world * 0.98));
            batch.end();

            vert = board.getVertices();

            box_board.setTransform(vert[0], vert[1], (float) Math.toRadians(board.getRotation()));

            if (box_circle.getPosition().y < board.getY() * 0.6 && board.getY() > h_world * 0.3) {
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
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.16 * 1.5), 0);
        box_hole.setUserData("hole");

        hole2 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole2.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole2.setY((float) (h_world * 0.16 * 1.5 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.42), (float) (h_world * 0.55), 0);
        box_hole.setUserData("hole");

        hole6 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole6.setX((float) (w_world * 0.42 - hole_image.getTextureData().getHeight() / 2));
        hole6.setY((float) (h_world * 0.55 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.765), (float) (h_world * 0.586), 0);
        box_hole.setUserData("hole");

        hole8 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole8.setX((float) (w_world * 0.765 - hole_image.getTextureData().getHeight() / 2));
        hole8.setY((float) (h_world * 0.586 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.42), (float) (h_world * 0.703), 0);
        box_hole.setUserData("hole");

        hole10 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole10.setX((float) (w_world * 0.42 - hole_image.getTextureData().getHeight() / 2));
        hole10.setY((float) (h_world * 0.703 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.82), 0);
        box_hole.setUserData("hole");

        hole13 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole13.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole13.setY((float) (h_world * 0.82 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.14), (float) (h_world * 0.898), 0);
        box_hole.setUserData("hole");

        hole14 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole14.setX((float) (w_world * 0.14 - hole_image.getTextureData().getHeight() / 2));
        hole14.setY((float) (h_world * 0.898 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.487), (float) (h_world * 0.938), 0);
        box_hole.setUserData("hole");

        hole16 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole16.setX((float) (w_world * 0.487- hole_image.getTextureData().getHeight() / 2));
        hole16.setY((float) (h_world * 0.938 - hole_image.getTextureData().getHeight() / 2));

        array_hole.add(hole2);
        array_hole.add(hole6);
        array_hole.add(hole8);
        array_hole.add(hole10);
        array_hole.add(hole13);
        array_hole.add(hole14);
        array_hole.add(hole16);

        addDynamicHole();

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
        box_hole_din5 = new BoxHole().createBoxHole(world);
        box_hole_din5.setUserData("hole");
        box_hole_din6 = new BoxHole().createBoxHole(world);
        box_hole_din6.setUserData("hole");
        box_hole_din7 = new BoxHole().createBoxHole(world);
        box_hole_din7.setUserData("hole");
        box_hole_din8 = new BoxHole().createBoxHole(world);
        box_hole_din8.setUserData("hole");

        hole_din1 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din2 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din3 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din4 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din5 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din6 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din7 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole_din8 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
    }

    private void getDynamicHole() {

        if (box_hole_din1.getPosition().x >= w_world * 0.7 * 0.7) {
            box_hole_din_sign = false;
        } else if (box_hole_din1.getPosition().x <= w_world * 0.7 * 0.4) {
            box_hole_din_sign = true;
        }

        box_hole_din1.setTransform((float) (w_world * 0.7 * 0.5 + box_din), (float) (h_world * 0.16 * 1.5 + box_din), 0);

        hole_din1.setX(box_hole_din1.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din1.setY(box_hole_din1.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din1.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din2.setTransform((float) (w_world * 0.7 - box_din), (float) (h_world * 0.31 + box_din), 0);

        hole_din2.setX(box_hole_din2.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din2.setY(box_hole_din2.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din2.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din3.setTransform((float) (w_world * 0.25 - box_din), (float) (h_world * 0.38 + box_din), 0);

        hole_din3.setX(box_hole_din3.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din3.setY(box_hole_din3.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din3.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din4.setTransform((float) (w_world * 0.7 * 1.2 - box_din), (float) (h_world * 0.5 - box_din), 0);

        hole_din4.setX(box_hole_din4.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din4.setY(box_hole_din4.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din4.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din5.setTransform((float) (w_world * 0.556 + box_din), (float) (h_world * 0.545 + box_din), 0);

        hole_din5.setX(box_hole_din5.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din5.setY(box_hole_din5.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din5.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din6.setTransform((float) (w_world * 0.2 + box_din), (float) (h_world * 0.625), 0);

        hole_din6.setX(box_hole_din6.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din6.setY(box_hole_din6.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din6.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din7.setTransform((float) (w_world * 0.2 + box_din), (float) (h_world * 0.742 + box_din), 0);

        hole_din7.setX(box_hole_din7.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din7.setY(box_hole_din7.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din7.draw(batch);

        /////////////////////////////---------------------------------------------------------------

        box_hole_din8.setTransform((float) (w_world * 0.764 + box_din), (float) (h_world * 0.742 + box_din), 0);

        hole_din8.setX(box_hole_din8.getPosition().x - hole_image.getTextureData().getHeight() / 2);
        hole_din8.setY(box_hole_din8.getPosition().y - hole_image.getTextureData().getHeight() / 2);

        hole_din8.draw(batch);
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
                    board.setOrigin(oX, oY);
                    board.setX(vert[10] - oX);
                    board.setY(vert[11] - oY);
                    board.setRotation((float) i_left);
                    i_right = i_left;
                }
                i_right = i_right - 0.7;

                if (board.getRotation() != -28)
                    board.setRotation((float) i_right);
                else
                    i_right = -28;

                i_left = 0;
            } else {
                if (i_left == 0) {
                    oX = 0;
                    oY = (int) height_board;
                    board.setOrigin(oX, oY);
                    board.setX(vert[5]);
                    board.setY(vert[6] - oY);
                    board.setRotation((float) i_right);
                    i_left = i_right;
                }

                i_left = i_left + 0.7;

                if (board.getRotation() != 28)
                    board.setRotation((float) i_left);
                else
                    i_left = 28;

                i_right = 0;
            }
        }
    }
}