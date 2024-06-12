package net.PeytonPlayz585.main;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import main.WebGL;

public class Client {

	public static void main(String args[]) {
		try {
			WebGL.main(args);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		// LWJGLUtil.installResources("assets.epk");

		try {
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		// Minecraft minecraft = new Minecraft(Display.getWidth(), Display.getHeight(),
		// Display.isFullscreen());
		// minecraft.session = new SessionData("fuck", "shit");

		// minecraft.run();
	}

}
