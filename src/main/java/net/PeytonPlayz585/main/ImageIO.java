package net.PeytonPlayz585.main;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ImageIO {
	
	public static BufferedImage read(InputStream var1) {
		ByteArrayInputStream bais = (ByteArrayInputStream)var1;
		byte[] data = bais.readAllBytes();
		
		return LWJGLUtil.loadPNG(data);
	}

	public static BufferedImage read(BufferedImage resource) {
		return resource;
	}

}