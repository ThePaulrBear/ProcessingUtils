package paul.wintz.processing;

import static paul.wintz.utils.Utils.checkPositive;

import java.io.File;

import gifAnimation.GifMaker;
import paul.wintz.utils.logging.Lg;
import processing.core.*;

@SuppressWarnings("unused")
public final class ProcessingUtils {
	private static final String TAG = Lg.makeTAG(ProcessingUtils.class);

	private static PApplet papplet;

	public static void initialize(PApplet pApplet) {
		ProcessingUtils.papplet = pApplet;
	}

	public static PGraphics createPGraphics(int width, int height) {
		checkPositive(width);
		checkPositive(height);

		final PGraphics graphic = papplet.createGraphics(width, height);
		graphic.beginDraw();
		return graphic;
	}

	public static float randomGaussian() {
		return papplet.randomGaussian();
	}

	/**
	 * Create a new GifMaker. <br>
	 * Note that the size of the GIF is <yIntercept> not </yIntercept> set here.
	 */
	public static GifMaker newGifMaker(File file) {
		// GifMaker(PApplet parent, String filename, int quality, int
		// transparentColor)
		return new GifMaker(papplet, file.getPath());
	}

	private ProcessingUtils() {}
}
