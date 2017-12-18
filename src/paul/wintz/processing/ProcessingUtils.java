package paul.wintz.processing;

import static paul.wintz.utils.Utils.checkPositive;

import java.io.File;

import gifAnimation.GifMaker;
import paul.wintz.utils.logging.Lg;
import processing.core.*;

public final class ProcessingUtils {
	private static final String TAG = Lg.makeTAG(ProcessingUtils.class);

	private static PApplet papplet;

	public static void initialize(PApplet pApplet) {
		ProcessingUtils.papplet = pApplet;
	}

	/**
	 * Maps a value from the domain specified into the given range.
	 */
	public static float map(double value, float startDomain, float stopDomain, float startRange, float stopRange) {
		return PApplet.map((float) value, startDomain, stopDomain, startRange, stopRange);
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
	 *
	 * @param fileName
	 * @return
	 */
	public static GifMaker newGifMaker(File file) {
		// GifMaker(PApplet parent, String filename, int quality, int
		// transparentColor)
		return new GifMaker(papplet, file.getPath());
	}

	public static boolean isInititialized() {
		return papplet != null;
	}

	private ProcessingUtils() {}
}
