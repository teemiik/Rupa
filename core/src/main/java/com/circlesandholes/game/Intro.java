package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.circlesandholes.game.Levels.Level1;
import com.circlesandholes.game.Levels.Level2;
import com.circlesandholes.game.Levels.Level3;
import com.circlesandholes.game.Levels.Level4;
import com.circlesandholes.game.Levels.Level5;
import com.circlesandholes.game.Levels.Level6;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bolgov.artem on 09.10.17.
 */

 public class Intro extends Game {

    public SpriteBatch batch;

    private Texture image_button_down;
    private Texture image_button_menu_down;

    private Texture image_level1;
    private Texture image_level2;
    private Texture image_level3;
    private Texture image_level4;
    private Texture image_level5;
    private Texture image_level6;

    private Texture background_image;
    private Texture background_2_image;

    private Stage stage, stage_lvl;

    private boolean menu = false;

    public static String lang = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\\\/?^-+=()*&.:;,{}\\\"´`'<>";

    public static float w_world;
    public static float h_world;
    public static float height_board;
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

    private static Timer timer;
    private static Timer timer_gest;
    public static Timer timer_dynamic_body = null;

    public static boolean box_hole_din_sign = true;
    public static boolean faild = true;
    public static boolean win = false;
    static boolean the_end = false;

    public static FreeTypeFontGenerator generator;

    @Override
    public void create() {
        generalData(new SpriteBatch());
    }

    private void getScreen(int level) {
        switch (level) {
            case 1:
                setScreen(new Level1(this));
                break;
            case 2:
                setScreen(new Level2(this));
                break;
            case 3:
                setScreen(new Level3(this));
                break;
            case 4:
                setScreen(new Level4(this));
                break;
            case 5:
                setScreen(new Level5(this));
                break;
            case 6:
                setScreen(new Level6(this));
                break;
        }
    }

   @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        if (menu) {
            stage_lvl.draw();
        } else {
            stage.draw();
        }

        super.render();
    }

    private void generalData(SpriteBatch batch) {
        this.batch = batch;

        Gdx.app.log("MyTag", String.valueOf(Gdx.graphics.getDensity()));

        w_world = Gdx.graphics.getWidth();
        h_world = Gdx.graphics.getHeight();

        background_image = new Texture("background.jpg");
        background_2_image = new Texture("background_2.jpg");

        background = new Sprite(background_image);
        background_2 = new Sprite(background_2_image);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w_world / 2, h_world / 2);

        withdt_board = (float) (w_world - w_world * 0.25);
        height_board = (float) (w_world * 0.04);

        board_image = new Texture("board.png");
        board_2_image = new Texture("board_2.jpg");
        rectangle_barrier_image = new Texture("rectangle_barrier.jpg");

        circle_image = getTextureBall();
        hole_image = getTextureHole();

        createdBoard();
        circle = new Sprite(circle_image);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("10771.ttf"));

        glyph = new GlyphLayout();
        glyph_result = new GlyphLayout();
        glyph_var1 = new GlyphLayout();
        glyph_var2 = new GlyphLayout();

        Texture image_button_up = new Texture("Menu/play.png");

        Texture image_button_menu_up = new Texture("Menu/menu_button_up.png");
        //image_button_menu_down = new Texture("Menu/menu_button_down.png");

        ImageButton btn = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_button_up)));
        btn.setX(w_world / 2 - image_button_up.getTextureData().getWidth() / 2);
        btn.setY(h_world / 2 - image_button_up.getTextureData().getHeight() / 2);

        ImageButton btn_menu = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_button_menu_up)));
        btn_menu.setX(w_world / 2 - image_button_menu_up.getTextureData().getWidth() / 2);
        btn_menu.setY((float) (h_world / 2 - image_button_menu_up.getTextureData().getHeight() / 2 - h_world * 0.4));

        initializationLevels();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        stage.addActor(btn);
        btn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!menu) {
                    getScreen(1);
                    stage.dispose();
                }
            }
        });

        stage.addActor(btn_menu);
        btn_menu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menu = true;
                Gdx.input.setInputProcessor(stage_lvl);
            }
        });

        getTimer();
        getTimerGestures();
    }

    static void createdBoard() {
        board = new Sprite(board_image, (int) (withdt_board), (int) height_board);
        board.setX((float) (w_world / 2 - w_world * 0.25 - w_world * 0.25 / 2));
        board.setY((float) (h_world * 0.06));

        board_2 = new Sprite(board_2_image, (int) (withdt_board), (int) height_board);
        board_2.setX((float) (w_world / 2 - w_world * 0.25 - w_world * 0.25 / 2));
        board_2.setY((float) (h_world * 0.06));
    }

    private void initializationLevels() {
        getTextureLvl();

        //System.out.println(h_world);

        stage_lvl = new Stage();
        Gdx.input.setInputProcessor(stage_lvl);

        ImageButton level1 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level1)));
        level1.setX((float) (w_world / 2 - image_level1.getTextureData().getWidth() / 2 - image_level1.getTextureData().getWidth() * 1.9));
        level1.setY(h_world / 2 - image_level1.getTextureData().getHeight() / 2 + image_level1.getTextureData().getHeight() * 2);
        stage_lvl.addActor(level1);
        level1.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(1);
                stage.dispose();
                stage_lvl.dispose();
            }
        });

        ImageButton level2 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level2)));
        level2.setX((float) (w_world / 2 - image_level2.getTextureData().getWidth() / 2 - image_level2.getTextureData().getWidth() * 0.65));
        level2.setY(h_world / 2 - image_level2.getTextureData().getHeight() / 2 + image_level2.getTextureData().getHeight() * 2);
        stage_lvl.addActor(level2);
        level2.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(2);
                stage.dispose();
                stage_lvl.dispose();
            }
        });

        ImageButton level3 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level3)));
        level3.setX((float) (w_world / 2  + image_level3.getTextureData().getWidth() / 2 * 0.2));
        level3.setY(h_world / 2 - image_level3.getTextureData().getHeight() / 2 + image_level3.getTextureData().getHeight() * 2);
        stage_lvl.addActor(level3);
        level3.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(3);
                stage.dispose();
                stage_lvl.dispose();
            }
        });

        ImageButton level4 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level4)));
        level4.setX((float) (w_world / 2  + image_level4.getTextureData().getWidth() / 2 * 2.7));
        level4.setY(h_world / 2 - image_level4.getTextureData().getHeight() / 2 + image_level4.getTextureData().getHeight() * 2);
        stage_lvl.addActor(level4);
        level4.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(4);
                stage.dispose();
                stage_lvl.dispose();
            }
        });

        ImageButton level5 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level5)));
        level5.setX((float) (w_world / 2 - image_level5.getTextureData().getWidth() / 2 - image_level5.getTextureData().getWidth() * 1.9));
        level5.setY((h_world / 2 - image_level5.getTextureData().getHeight() / 2 + image_level5.getTextureData().getHeight() / 2));
        stage_lvl.addActor(level5);
        level5.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(5);
                stage.dispose();
                stage_lvl.dispose();
            }
        });

        ImageButton level6 = new ImageButton(new TextureRegionDrawable(new TextureRegion(image_level6)));
        level6.setX((float) (w_world / 2 - image_level6.getTextureData().getWidth() / 2 - image_level6.getTextureData().getWidth() * 0.65));
        level6.setY((h_world / 2 - image_level6.getTextureData().getHeight() / 2 + image_level6.getTextureData().getHeight() / 2));
        stage_lvl.addActor(level6);
        level6.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getScreen(6);
                stage.dispose();
                stage_lvl.dispose();
            }
        });
    }

    private void getTextureLvl() {

        if (h_world >= 1280 && h_world < 1920) {
            image_level1 = new Texture("Menu/Number/number_1_100.png");
            image_level2 = new Texture("Menu/Number/number_2_100.png");
            image_level3 = new Texture("Menu/Number/number_3_100.png");
            image_level4 = new Texture("Menu/Number/number_4_100.png");
            image_level5 = new Texture("Menu/Number/number_5_100.png");
            image_level6 = new Texture("Menu/Number/number_6_100.png");
        } else if (h_world >= 1920 && h_world < 2560) {
            image_level1 = new Texture("Menu/Number/number_1_200.png");
            image_level2 = new Texture("Menu/Number/number_2_200.png");
            image_level3 = new Texture("Menu/Number/number_3_200.png");
            image_level4 = new Texture("Menu/Number/number_4_200.png");
            image_level5 = new Texture("Menu/Number/number_5_200.png");
            image_level6 = new Texture("Menu/Number/number_6_200.png");
        } else if (h_world < 1280) {
            image_level1 = new Texture("Menu/Number/number_1_100.png");
            image_level2 = new Texture("Menu/Number/number_2_100.png");
            image_level3 = new Texture("Menu/Number/number_3_100.png");
            image_level4 = new Texture("Menu/Number/number_4_100.png");
            image_level5 = new Texture("Menu/Number/number_5_100.png");
            image_level6 = new Texture("Menu/Number/number_6_100.png");
        } else if (h_world >= 2560) {
            image_level1 = new Texture("Menu/Number/number_1_200.png");
            image_level2 = new Texture("Menu/Number/number_2_200.png");
            image_level3 = new Texture("Menu/Number/number_3_200.png");
            image_level4 = new Texture("Menu/Number/number_4_200.png");
            image_level5 = new Texture("Menu/Number/number_5_200.png");
            image_level6 = new Texture("Menu/Number/number_6_200.png");
        }
    }

    private Texture getTextureHole() {

        if (h_world >= 1280 && h_world < 1920) {
            size_text = 34;
            size_text_result = 54;
            speed_hole = 10;
            time_step = 0.6f;
            return new Texture("Hole/Circle_Dark_75x75.png");
        } else if (h_world >= 1920 && h_world < 2560) {
            size_text = 64;
            size_text_result = 84;
            speed_hole = 8;
            time_step = 0.55f;
            return new Texture("Hole/Circle_Dark_95x95.png");
        } else if (h_world < 1280) {
            size_text = 24;
            size_text_result = 44;
            speed_hole = 12;
            time_step = 0.65f;
            return new Texture("Hole/Circle_Dark_30x30.png");
        } else if (h_world >= 2560) {
            size_text = 94;
            size_text_result = 114;
            speed_hole = 5;
            time_step = 0.51f;
            return new Texture("Hole/Circle_Dark_128x128.png");
        }

        return null;
    }

    private Texture getTextureBall() {

        if (h_world >= 1280 && h_world < 1920) {

            texture_gest_right = "Gestures/training_right_74.png";
            texture_gest_left = "Gestures/training_left_74.png";

            texture_gest_right_update = "Gestures/training_right_64.png";
            texture_gest_left_update = "Gestures/training_left_64.png";

            return new Texture("Circle/Circle_Grey_65x65.png");
        } else if (h_world >= 1920 && h_world < 2560) {

            texture_gest_right = "Gestures/training_right_94.png";
            texture_gest_left = "Gestures/training_left_94.png";

            texture_gest_right_update = "Gestures/training_right_84.png";
            texture_gest_left_update = "Gestures/training_left_84.png";

            return new Texture("Circle/Circle_Grey_85x85.png");
        } else if (h_world < 1280) {

            texture_gest_right = "Gestures/training_right_94.png";
            texture_gest_left = "Gestures/training_left_94.png";

            texture_gest_right_update = "Gestures/training_right_84.png";
            texture_gest_left_update = "Gestures/training_left_84.png";

            return new Texture("Circle/Circle_Grey_20x20.png");
        } else if (h_world >= 2560) {

            texture_gest_right = "Gestures/training_right_94.png";
            texture_gest_left = "Gestures/training_left_94.png";

            texture_gest_right_update = "Gestures/training_right_84.png";
            texture_gest_left_update = "Gestures/training_left_84.png";

            return new Texture("Circle/Circle_Grey_118x118.png");
        }

        return null;
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
    }

    private void getTimer() {

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
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
                if (box_hole_din_sign) box_din++; else box_din--;

            }
        }, 0, speed_hole);

    }
}
