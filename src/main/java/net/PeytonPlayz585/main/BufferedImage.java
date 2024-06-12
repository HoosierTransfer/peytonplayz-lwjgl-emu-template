package net.PeytonPlayz585.main;

public class BufferedImage {

	public final int[] data;
	public final int w;
	public final int h;
	public final boolean alpha;

	public BufferedImage(int pw, int ph, boolean palpha) {
		this.w = pw;
		this.h = ph;
		this.alpha = palpha;
		this.data = new int[pw * ph];
	}

	public BufferedImage(int[] pdata, int pw, int ph, boolean palpha) {
		if (pdata.length != pw * ph) {
			throw new IllegalArgumentException("array size does not equal image size");
		}
		this.w = pw;
		this.h = ph;
		this.alpha = palpha;
		if (!palpha) {
			for (int i = 0; i < pdata.length; ++i) {
				pdata[i] = pdata[i] | 0xFF000000;
			}
		}
		this.data = pdata;
	}

	public BufferedImage getSubImage(int x, int y, int pw, int ph) {
		int[] img = new int[pw * ph];
		for (int i = 0; i < ph; ++i) {
			System.arraycopy(data, (i + y) * this.w + x, img, i * pw, pw);
		}
		return new BufferedImage(img, pw, ph, alpha);
	}
	
	public int[] getRGB(int startX, int startY, int w, int h,  int[] rgbArray, int offset, int scansize) {
		if (startX < 0 || startY < 0 || w <= 0 || h <= 0 ||
				startX + w > this.w || startY + h > this.h ||
				rgbArray.length < offset + w * h) {
				throw new IllegalArgumentException("Suck my dick nigga");
		}

		for (int y = startY; y < startY + h; y++) {
			for (int x = startX; x < startX + w; x++) {
				int imageDataIndex = y * this.w + x;
				int argb = data[imageDataIndex];
				int alpha = (argb >> 24) & 0xff;
				int red = (argb >> 16) & 0xff;
				int green = (argb >> 8) & 0xff;
				int blue = argb & 0xff;
				int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
      
				rgbArray[offset + (y - startY) * scansize + (x - startX)] = rgb;
			}
		}

		return rgbArray;
	}

}