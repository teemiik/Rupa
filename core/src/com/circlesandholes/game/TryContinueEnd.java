package com.circlesandholes.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

import static com.circlesandholes.game.Intro.background;
import static com.circlesandholes.game.Intro.background_2;
import static com.circlesandholes.game.Intro.box_din;
import static com.circlesandholes.game.Intro.camera;
import static com.circlesandholes.game.Intro.faild;
import static com.circlesandholes.game.Intro.generator;
import static com.circlesandholes.game.Intro.gest;
import static com.circlesandholes.game.Intro.glyph;
import static com.circlesandholes.game.Intro.glyph_result;
import static com.circlesandholes.game.Intro.glyph_var1;
import static com.circlesandholes.game.Intro.glyph_var2;
import static com.circlesandholes.game.Intro.h_world;
import static com.circlesandholes.game.Intro.lang;
import static com.circlesandholes.game.Intro.size_text;
import static com.circlesandholes.game.Intro.size_text_result;
import static com.circlesandholes.game.Intro.text;
import static com.circlesandholes.game.Intro.text_total;
import static com.circlesandholes.game.Intro.the_end;
import static com.circlesandholes.game.Intro.timer_dynamic_body;
import static com.circlesandholes.game.Intro.w_world;
import static com.circlesandholes.game.Intro.win;

public class TryContinueEnd extends Game implements Screen {
	final Intro intro;
	final int level;

	private SpriteBatch batch;

	private Texture try_image, continue_image;

	private BitmapFont font_counter, font_result, font_var1, font_var2;

	private ImageButton btn_try, btn_continue;

	private Stage stage;

	private boolean one_render = true;

	public TryContinueEnd(final Intro intro, final int level) {
		this.intro = intro;
		this.level = level;

		batch = new SpriteBatch();

		FreeTypeFontGenerator.FreeTypeFontParameter parametr_fall = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parametr_fall.size = size_text;
		parametr_fall.characters = lang;
		parametr_fall.color = Color.WHITE;
		font_counter = generator.generateFont(parametr_fall);
		font_var1 = generator.generateFont(parametr_fall);
		font_var2 = generator.generateFont(parametr_fall);

		FreeTypeFontGenerator.FreeTypeFontParameter parametr_win = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parametr_win.size = size_text_result;
		parametr_win.characters = lang;
		parametr_win.color = Color.WHITE;
		font_result = generator.generateFont(parametr_win);

		//System.out.println(w_world);

		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		try_image = new Texture("Menu/try.png");
		btn_try = new ImageButton(new TextureRegionDrawable(new TextureRegion(try_image)));
		btn_try.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!faild || win) {
					try {
						timer_dynamic_body.cancel();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intro.createdBoard();

					switch (level) {
						case 1: intro.setScreen(new Level1(intro));
							break;
						case 2: intro.setScreen(new Level2(intro));
							break;
						case 3: intro.setScreen(new Level3(intro));
							break;
						case 4: intro.setScreen(new Level4(intro));
							break;
						case 5: intro.setScreen(new Level5(intro));
							break;
						case 6: intro.setScreen(new Level6(intro));
							break;
					}
					faild = true;
					win = false;
					stage.dispose();
					box_din = 0;
				}
				super.clicked(event, x, y);
			}
		});

		continue_image = new Texture("Menu/continue.png");

		btn_continue = new ImageButton(new TextureRegionDrawable(new TextureRegion(continue_image)));
		btn_continue.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (win) {
					try {
						timer_dynamic_body.cancel();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intro.createdBoard();

					int level_up = level + 1;
					switch (level_up) {
						case 2: intro.setScreen(new Level2(intro));
							break;
						case 3: intro.setScreen(new Level3(intro));
							break;
						case 4: intro.setScreen(new Level4(intro));
							break;
						case 5: intro.setScreen(new Level5(intro));
							break;
						case 6: intro.setScreen(new Level6(intro));
							break;
						case 7: the_end = true;
							break;
					}

					stage.dispose();
					win = false;
					box_din = 0;
				}
				super.clicked(event, x, y);
			}
		});

		text_total = "Вы успели пройти уровень за ";
	}

	@Override
	public void create() {

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (!faild) {

			batch.begin();
			if (level < 6)
				batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			else
				batch.draw(background_2, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			btn_try.setX((float) (w_world / 2 - try_image.getTextureData().getWidth() / 2));
			btn_try.setY((float) (h_world / 2 - try_image.getTextureData().getHeight() / 2));

			//btn_try.setOrigin(w_world / 2 - try_image.getTextureData().getWidth() / 2, h_world / 2 - try_image.getTextureData().getHeight() / 2);

			stage.addActor(btn_try);

			String res_str = "Это фиаско!";
			String ext_str = "Попробуйте еще раз)";
			glyph_result.setText(font_result, res_str);
			glyph.setText(font_counter, ext_str);

			font_result.draw(batch, res_str, w_world / 2 - glyph_result.width / 2, (float) (h_world / 2 + try_image.getTextureData().getHeight() * 1.3));
			font_counter.draw(batch, ext_str, w_world / 2 - glyph.width / 2, (float) (h_world / 2 + try_image.getTextureData().getHeight() * 0.9));

			batch.end();

			stage.draw();

		} else if (win) {

			batch.begin();

			if (level < 6)
				batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			else
				batch.draw(background_2, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			btn_continue.setX((float) (w_world / 2 - continue_image.getTextureData().getWidth() / 2));
			btn_continue.setY((float) (h_world * 0.55 - continue_image.getTextureData().getHeight() / 2));

			btn_try.setX((float) (w_world / 2 - try_image.getTextureData().getWidth() / 2));
			btn_try.setY((float) (h_world * 0.2 - try_image.getTextureData().getHeight() / 2));

			stage.addActor(btn_continue);
			stage.addActor(btn_try);

			if (one_render) {
				text_total = text_total + text;
				glyph.setText(font_counter, text_total);
				one_render = false;
			}

			String res_str = "Победа!";
			String var1_str = "Перейдете на следующий уровень?";
			String var2_str = "или попробуйте еще раз...";

			glyph_result.setText(font_result, res_str);
			glyph_var1.setText(font_var1, var1_str);
			glyph_var2.setText(font_var2, var2_str);

			font_result.draw(batch, res_str, w_world / 2 - glyph_result.width / 2, (float) (h_world * 0.55 + continue_image.getTextureData().getHeight() * 1.3));

			font_var1.draw(batch, var1_str, w_world / 2 - glyph_var1.width / 2, (float) (h_world * 0.55 + continue_image.getTextureData().getHeight() / 1.2));

			font_counter.draw(batch, text_total, w_world / 2 - glyph.width / 2, (float) (h_world * 0.55 - continue_image.getTextureData().getHeight() / 1.3));

			font_var2.draw(batch, var2_str, w_world / 2 - glyph_var2.width / 2, (float) (h_world * 0.2 + try_image.getTextureData().getHeight()));


			batch.end();

			stage.draw();

		} else if (the_end) {

			batch.begin();
			if (level < 6)
				batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			else
				batch.draw(background_2, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			String res_str = "Продолжение следует...";

			glyph_result.setText(font_result, res_str);

			font_result.draw(batch, res_str, w_world / 2 - glyph_result.width / 2, h_world / 2 - glyph_result.height / 2);

			batch.end();

		}
	}

	@Override
	public void resize(int width, int height) {
		camera.setToOrtho(false, width, height);
	}


	@Override
	public void dispose() {
		batch.dispose();
	}


	@Override
	public void show() {

	}

	@Override
	public void hide() {

	}
}