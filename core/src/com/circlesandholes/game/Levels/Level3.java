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

/**
 * Created by bolgov.artem on 11.10.17.
 */

public class Level3 extends Game implements ContactListener, Screen {

    final Intro intro;

    private SpriteBatch batch;

    private World world;

    private Body box_board, box_circle, box_hole;
    private Box2DDebugRenderer b2dr;

    private double i_right = 0, i_left = 0;
    private float[] vert;
    private Vector3 touchPos;

    private BitmapFont font_counter;

    private Sprite  hole1,
            hole2,
            hole3,
            hole4,
            hole5,
            hole6,
            hole7,
            hole8,
            hole9,
            hole10,
            hole11,
            hole12,
            hole13,
            hole14,
            hole15,
            hole16,
            hole17,
            hole18,
            hole19,
            hole20,
            hole21,
            hole22,
            hole23,
            hole24,
            hole25,
            hole26,
            hole27,
            hole28,
            hole29,
            hole30,
            hole31,
            hole32,
            hole33,
            hole34;

    private ArrayList<Sprite> array_hole;

    public Level3(final Intro intro) {
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
            intro.setScreen(new TryContinueEnd(intro, 3));
        else if (win)
            intro.setScreen(new TryContinueEnd(intro, 3));
        else {

            touchPosition();

            circle.setCenter(box_circle.getPosition().x, box_circle.getPosition().y);

            batch.begin();
            batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            for (Sprite hole : array_hole) {
                hole.draw(batch);
            }

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
        box_hole.setTransform((float) (w_world * 0.7 * 0.4), (float) (h_world * 0.16 * 1.5), 0);
        box_hole.setUserData("hole");

        hole1 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole1.setX((float) (w_world * 0.7 * 0.4 - hole_image.getTextureData().getHeight() / 2));
        hole1.setY((float) (h_world * 0.16 * 1.5 - hole_image.getTextureData().getHeight() / 2));

        ////////////////////////--------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.16 * 1.5), 0);
        box_hole.setUserData("hole");

        hole2 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole2.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole2.setY((float) (h_world * 0.16 * 1.5 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.6), (float) (h_world * 0.16 * 1.7), 0);
        box_hole.setUserData("hole");

        hole3 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole3.setX((float) (w_world * 0.7 * 0.6 - hole_image.getTextureData().getHeight() / 2));
        hole3.setY((float) (h_world * 0.16 * 1.7 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.16 * 2.2), 0);
        box_hole.setUserData("hole");

        hole4 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole4.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole4.setY((float) (h_world * 0.16 * 2.2 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.5), (float) (h_world * 0.16 * 2.45), 0);
        box_hole.setUserData("hole");

        hole5 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole5.setX((float) (w_world * 0.7 * 0.5 - hole_image.getTextureData().getHeight() / 2));
        hole5.setY((float) (h_world * 0.16 * 2.45 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 1.1), (float) (h_world * 0.16 * 2.7), 0);
        box_hole.setUserData("hole");

        hole6 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole6.setX((float) (w_world * 0.7 * 1.1 - hole_image.getTextureData().getHeight() / 2));
        hole6.setY((float) (h_world * 0.16 * 2.7 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.8), (float) (h_world * 0.16 * 2.95), 0);
        box_hole.setUserData("hole");

        hole7 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole7.setX((float) (w_world * 0.7 * 0.8 - hole_image.getTextureData().getHeight() / 2));
        hole7.setY((float) (h_world * 0.16 * 2.95 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.4), (float) (h_world * 0.84 * 0.61), 0);
        box_hole.setUserData("hole");

        hole8 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole8.setX((float) (w_world * 0.7 * 0.4 - hole_image.getTextureData().getHeight() / 2));
        hole8.setY((float) (h_world * 0.84 * 0.61 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 1.1), (float) (h_world * 0.84 * 0.65), 0);
        box_hole.setUserData("hole");

        hole9 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole9.setX((float) (w_world * 0.7 * 1.1 - hole_image.getTextureData().getHeight() / 2));
        hole9.setY((float) (h_world * 0.84 * 0.65 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.6), (float) (h_world * 0.84 * 0.7), 0);
        box_hole.setUserData("hole");

        hole10 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole10.setX((float) (w_world * 0.7 * 0.6 - hole_image.getTextureData().getHeight() / 2));
        hole10.setY((float) (h_world * 0.84 * 0.7 - hole_image.getTextureData().getHeight() / 2));


        //11-20
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.84), (float) (h_world * 0.16 * 1.7), 0);
        box_hole.setUserData("hole");

        hole11 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole11.setX((float) (w_world * 0.84 - hole_image.getTextureData().getHeight() / 2));
        hole11.setY((float) (h_world * 0.16 * 1.7 - hole_image.getTextureData().getHeight() / 2));

        ////////////////////////--------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.3), (float) (h_world * 0.16 * 2.2), 0);
        box_hole.setUserData("hole");

        hole12 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole12.setX((float) (w_world * 0.7 * 0.3 - hole_image.getTextureData().getHeight() / 2));
        hole12.setY((float) (h_world * 0.16 * 2.2 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.7), (float) (h_world * 0.16 * 2.45), 0);
        box_hole.setUserData("hole");

        hole13 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole13.setX((float) (w_world * 0.7 * 0.7 - hole_image.getTextureData().getHeight() / 2));
        hole13.setY((float) (h_world * 0.16 * 2.45 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.84 * 0.65), 0);
        box_hole.setUserData("hole");

        hole14 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole14.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole14.setY((float) (h_world * 0.84 * 0.65 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.5), (float) (h_world * 0.63), 0);
        box_hole.setUserData("hole");

        hole15 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole15.setX((float) (w_world * 0.7 * 0.5 - hole_image.getTextureData().getHeight() / 2));
        hole15.setY((float) (h_world * 0.63 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.7), (float) (h_world * 0.67), 0);
        box_hole.setUserData("hole");

        hole16 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole16.setX((float) (w_world * 0.7 * 0.7 - hole_image.getTextureData().getHeight() / 2));
        hole16.setY((float) (h_world * 0.67 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.3), (float) (h_world * 0.63), 0);
        box_hole.setUserData("hole");

        hole17 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole17.setX((float) (w_world * 0.7 * 0.3 - hole_image.getTextureData().getHeight() / 2));
        hole17.setY((float) (h_world * 0.63 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 1.3), (float) (h_world * 0.84 * 0.7), 0);
        box_hole.setUserData("hole");

        hole18 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole18.setX((float) (w_world * 0.7 * 1.3 - hole_image.getTextureData().getHeight() / 2));
        hole18.setY((float) (h_world * 0.84 * 0.7 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.84), (float) (h_world * 0.84 * 0.84), 0);
        box_hole.setUserData("hole");

        hole19 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole19.setX((float) (w_world * 0.84 - hole_image.getTextureData().getHeight() / 2));
        hole19.setY((float) (h_world * 0.84 * 0.84 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.5), (float) (h_world * 0.84 * 0.84), 0);
        box_hole.setUserData("hole");

        hole20 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole20.setX((float) (w_world * 0.7 * 0.5 - hole_image.getTextureData().getHeight() / 2));
        hole20.setY((float) (h_world * 0.84 * 0.84 - hole_image.getTextureData().getHeight() / 2));

        //21 - 30
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.3), (float) (h_world * 0.84 * 0.88), 0);
        box_hole.setUserData("hole");

        hole21 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole21.setX((float) (w_world * 0.7 * 0.3 - hole_image.getTextureData().getHeight() / 2));
        hole21.setY((float) (h_world * 0.84 * 0.88 - hole_image.getTextureData().getHeight() / 2));

        ////////////////////////--------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.8), (float) (h_world * 0.84 * 0.88), 0);
        box_hole.setUserData("hole");

        hole22 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole22.setX((float) (w_world * 0.7 * 0.8 - hole_image.getTextureData().getHeight() / 2));
        hole22.setY((float) (h_world * 0.84 * 0.88 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7), (float) (h_world * 0.78), 0);
        box_hole.setUserData("hole");

        hole23 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole23.setX((float) (w_world * 0.7 - hole_image.getTextureData().getHeight() / 2));
        hole23.setY((float) (h_world * 0.78 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 1.3), (float) (h_world * 0.82), 0);
        box_hole.setUserData("hole");

        hole24 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole24.setX((float) (w_world * 0.7 * 1.3 - hole_image.getTextureData().getHeight() / 2));
        hole24.setY((float) (h_world * 0.82 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 1.1), (float) (h_world * 0.86), 0);
        box_hole.setUserData("hole");

        hole25 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole25.setX((float) (w_world * 0.7 * 1.1 - hole_image.getTextureData().getHeight() / 2));
        hole25.setY((float) (h_world * 0.86 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.7), (float) (h_world * 0.82), 0);
        box_hole.setUserData("hole");

        hole26 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole26.setX((float) (w_world * 0.7 * 0.7 - hole_image.getTextureData().getHeight() / 2));
        hole26.setY((float) (h_world * 0.82 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.5), (float) (h_world * 0.78), 0);
        box_hole.setUserData("hole");

        hole27 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole27.setX((float) (w_world * 0.7 * 0.5 - hole_image.getTextureData().getHeight() / 2));
        hole27.setY((float) (h_world * 0.78 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.4), (float) (h_world * 0.86), 0);
        box_hole.setUserData("hole");

        hole28 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole28.setX((float) (w_world * 0.7 * 0.4 - hole_image.getTextureData().getHeight() / 2));
        hole28.setY((float) (h_world * 0.86 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.8), (float) (h_world * 0.93), 0);
        box_hole.setUserData("hole");

        hole29 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole29.setX((float) (w_world * 0.7 * 0.8 - hole_image.getTextureData().getHeight() / 2));
        hole29.setY((float) (h_world * 0.93 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.5), (float) (h_world * 0.93), 0);
        box_hole.setUserData("hole");

        hole30 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole30.setX((float) (w_world * 0.7 * 0.5 - hole_image.getTextureData().getHeight() / 2));
        hole30.setY((float) (h_world * 0.93 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.9), (float) (h_world * 0.84 * 0.79), 0);
        box_hole.setUserData("hole");

        hole31 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole31.setX((float) (w_world * 0.7 * 0.9 - hole_image.getTextureData().getHeight() / 2));
        hole31.setY((float) (h_world * 0.84 * 0.79 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.15), (float) (h_world * 0.16 * 1.7), 0);
        box_hole.setUserData("hole");

        hole32 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole32.setX((float) (w_world * 0.7 * 0.15 - hole_image.getTextureData().getHeight() / 2));
        hole32.setY((float) (h_world * 0.16 * 1.7 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.16), (float) (h_world * 0.16 * 2.95), 0);
        box_hole.setUserData("hole");

        hole33 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole33.setX((float) (w_world * 0.7 * 0.16 - hole_image.getTextureData().getHeight() / 2));
        hole33.setY((float) (h_world * 0.16 * 2.95 - hole_image.getTextureData().getHeight() / 2));

        ///////////////////////---------------------------------------------------------------------
        box_hole = new BoxHole().createBoxHole(world);
        box_hole.setTransform((float) (w_world * 0.7 * 0.15), (float) (h_world * 0.82), 0);
        box_hole.setUserData("hole");

        hole34 = new Sprite(hole_image, hole_image.getTextureData().getHeight(), hole_image.getTextureData().getHeight());
        hole34.setX((float) (w_world * 0.7 * 0.15 - hole_image.getTextureData().getHeight() / 2));
        hole34.setY((float) (h_world * 0.82 - hole_image.getTextureData().getHeight() / 2));

        array_hole.add(hole1);
        array_hole.add(hole2);
        array_hole.add(hole3);
        array_hole.add(hole4);
        array_hole.add(hole5);
        array_hole.add(hole6);
        array_hole.add(hole7);
        array_hole.add(hole8);
        array_hole.add(hole9);
        array_hole.add(hole10);
        array_hole.add(hole11);
        array_hole.add(hole12);
        array_hole.add(hole13);
        array_hole.add(hole14);
        array_hole.add(hole15);
        array_hole.add(hole16);
        array_hole.add(hole17);
        array_hole.add(hole18);
        array_hole.add(hole19);
        array_hole.add(hole20);
        array_hole.add(hole21);
        array_hole.add(hole22);
        array_hole.add(hole23);
        array_hole.add(hole24);
        array_hole.add(hole25);
        array_hole.add(hole26);
        array_hole.add(hole27);
        array_hole.add(hole28);
        array_hole.add(hole29);
        array_hole.add(hole30);
        array_hole.add(hole31);
        array_hole.add(hole32);
        array_hole.add(hole33);
        array_hole.add(hole34);

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