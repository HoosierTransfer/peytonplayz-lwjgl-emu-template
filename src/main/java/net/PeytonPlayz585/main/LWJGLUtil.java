package net.PeytonPlayz585.main;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.ajax.ReadyStateChangeHandler;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.DataView;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.typedarrays.Uint8ClampedArray;

import com.jcraft.jzlib.InflaterInputStream;

import main.WebGL;

public class LWJGLUtil {
	
	private static HTMLCanvasElement imageLoadCanvas = null;
	private static CanvasRenderingContext2D imageLoadContext = null;
	
	private static byte[] loadedPackage = null;
	public static final void installResources(String fileURI) {
		downloadAssetPack(fileURI);
		
		try {
			AssetRepository.install(loadedPackage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static BufferedImage getResource(String string) {
		return loadPNG(loadResourceBytes(string));
	}
	
	public static InputStream getResourceAsStream(String var1) {
		return loadResource(var1);
	}
	
	public static final BufferedImage loadPNG(byte[] data) {
		ArrayBuffer arr = ArrayBuffer.create(data.length);
		Uint8Array.create(arr).set(data);
		return loadPNG0(arr);
	}
	
	@JSBody(params = { "cccc", "ennn" }, script = "cccc.imageSmoothingEnabled = ennn;")
	private static native void setImageSmoothingMode(CanvasRenderingContext2D cc, boolean en);
	
	@Async
	private static native BufferedImage loadPNG0(ArrayBuffer data);

	private static void loadPNG0(ArrayBuffer data, final AsyncCallback<BufferedImage> ret) {
		final HTMLImageElement toLoad = (HTMLImageElement) WebGL.document.createElement("img");
		toLoad.addEventListener("load", new EventListener<Event>() {
			public void handleEvent(Event evt) {
				if(imageLoadCanvas == null) {
					imageLoadCanvas = (HTMLCanvasElement) WebGL.document.createElement("canvas");
				}
				if(imageLoadCanvas.getWidth() < toLoad.getWidth()) {
					imageLoadCanvas.setWidth(toLoad.getWidth());
				}
				if(imageLoadCanvas.getHeight() < toLoad.getHeight()) {
					imageLoadCanvas.setHeight(toLoad.getHeight());
				}
				if(imageLoadContext == null) {
					imageLoadContext = (CanvasRenderingContext2D) imageLoadCanvas.getContext("2d");
					setImageSmoothingMode(imageLoadContext, false);
				}
				imageLoadContext.clearRect(0, 0, toLoad.getWidth(), toLoad.getHeight());
				imageLoadContext.drawImage(toLoad, 0, 0, toLoad.getWidth(), toLoad.getHeight());
				ImageData pxlsDat = imageLoadContext.getImageData(0, 0, toLoad.getWidth(), toLoad.getHeight());
				Uint8ClampedArray pxls = pxlsDat.getData();
				int totalPixels = pxlsDat.getWidth() * pxlsDat.getHeight();
				freeDataURL(toLoad.getSrc());
				if(pxls.getByteLength() < totalPixels * 4) {
					ret.complete(null);
					return;
				}
				int[] pixels = new int[totalPixels];
				for(int i = 0; i < pixels.length; ++i) {
					pixels[i] = (pxls.get(i * 4) << 16) | (pxls.get(i * 4 + 1) << 8) | pxls.get(i * 4 + 2) | (pxls.get(i * 4 + 3) << 24);
				}
				ret.complete(new BufferedImage(pixels, pxlsDat.getWidth(), pxlsDat.getHeight(), true));
			}
		});
		toLoad.addEventListener("error", new EventListener<Event>() {
			public void handleEvent(Event evt) {
				freeDataURL(toLoad.getSrc());
				ret.complete(null);
			}
		});
		String src = getDataURL(data, "image/png");
		if(src == null) {
			ret.complete(null);
		}else {
			toLoad.setSrc(src);
		}
	}
	
	@Async
	public static native String downloadAssetPack(String assetPackageURI);
	
	private static void downloadAssetPack(String assetPackageURI, final AsyncCallback<String> cb) {
		final XMLHttpRequest request = XMLHttpRequest.create();
		request.setResponseType("arraybuffer");
		request.open("GET", assetPackageURI, true);
		request.setOnReadyStateChange(new ReadyStateChangeHandler() {
			@Override
			public void stateChanged() {
				if(request.getReadyState() == XMLHttpRequest.DONE) {
					Uint8Array bl = Uint8Array.create((ArrayBuffer)request.getResponse());
					loadedPackage = new byte[bl.getByteLength()];
					for(int i = 0; i < loadedPackage.length; ++i) {
						loadedPackage[i] = (byte) bl.get(i);
					}
					cb.complete("yee");
				}
			}
		});
		request.send();
	}
	
	@JSBody(params = { "url" }, script = "URL.revokeObjectURL(url);")
	private static native void freeDataURL(String url);
	
	@JSBody(params = { "buf", "mime" }, script = "return URL.createObjectURL(new Blob([buf], {type: mime}));")
	private static native String getDataURL(ArrayBuffer buf, String mime);
	
	private static final InputStream loadResource(String path) {
		byte[] file = loadResourceBytes(path);
		if (file != null) {
			return new ByteArrayInputStream(file);
		} else {
			return null;
		}
	}
	
	public static final byte[] loadResourceBytes(String path) {
		if(AssetRepository.getResource(path) == null) {
			System.out.println("yee");
		}
		return AssetRepository.getResource(path);
	}
}