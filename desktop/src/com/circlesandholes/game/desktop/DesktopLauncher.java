package com.circlesandholes.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.circlesandholes.game.Intro;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new Intro(), config);
		config.title = "Circle&Holes";
		config.width = 720;
		config.height = 1280;
	}
}
