package paul.wintz.processing;

import static com.google.common.base.Preconditions.*;

import java.util.*;

import paul.wintz.canvas.*;
import paul.wintz.math.Vector2D;
import processing.core.*;

public class PGraphicsLayer extends Layer<PGraphics> {

	private static final List<Transformation> noTransforms = Collections.<Transformation>emptyList();
	private final Object lock = new Object();

	public PGraphicsLayer(int width, int height) {
		super(width, height);
	}

	/**
	 * Draw a line from (x0,y0) to (x1,y1)
	 */
	@Override
	public void line(float x0, float y0, float x1, float y1, Painter painter) {
		bindPainter(painter);
		layer.line(x0, y0, x1, y1);
	}

	@Override
	public void ellipse(float xCenter, float yCenter, float width, float height, Painter painter) {
		ellipse(xCenter, yCenter, width, height, painter, noTransforms);
	}

	@Override
	public void ellipse(float xCenter, float yCenter, float width, float height, Painter painter, List<Transformation> transientTransforms) {
		bindPainter(painter);
		drawWithTransformations(() -> {
			layer.ellipseMode(PConstants.CENTER);
			layer.ellipse(xCenter, yCenter, width, height);
		}, transientTransforms);
	}

	@Override
	public void arc(float xCenter, float yCenter, float width, float height, float startAngle, float endAngle, Painter painter) {
		bindPainter(painter);
		layer.arc(xCenter, yCenter, width, height, startAngle, endAngle);
	}

	@Override
	public void rectangle(float x, float y, float width, float height, Painter painter) {
		bindPainter(painter);
		layer.rect(x, y, width, height);
	}

	@Override
	public void rectangle(float x, float y, float width, float height, Painter painter, List<Transformation> transforms) {
		bindPainter(painter);
		drawWithTransformations(() -> layer.rect(x, y, width, height), transforms);
	}

	@Override
	public void quad(Vector2D corner0, Vector2D corner1, Vector2D corner2, Vector2D corner3, Painter painter) {
		bindPainter(painter);
		layer.quad((float) corner0.x(), (float) corner0.y(),
				(float) corner1.x(), (float) corner1.y(),
				(float) corner2.x(), (float) corner2.y(),
				(float) corner3.x(), (float) corner3.y());
	}

	@Override
	public void drawPath(List<Vector2D> points, Painter painter, List<Transformation> transientTransforms) {
		bindPainter(painter);
		drawWithTransformations(() -> {
			layer.beginShape();
			for (final Vector2D pnt : points) {
				layer.curveVertex((float) pnt.x(), (float) pnt.y());
			}
			layer.endShape();
		}, transientTransforms);
	}

	@Override
	public void drawPolygon(final List<Vector2D> points, final Painter painter) {
		drawPolygon(points, painter, noTransforms);
	}

	@Override
	public void drawPolygon(final List<Vector2D> points, Painter painter, List<Transformation> transientTransforms) {
		synchronized (lock) {
			bindPainter(painter);
			drawWithTransformations(() -> {
				layer.beginShape();

				for (final Vector2D pnt : points) {
					layer.vertex((float) pnt.x(), (float) pnt.y());
				}
				layer.endShape();
			}, transientTransforms);
		}
	}

	/**
	 * Draw a dot at the given coordinates. This is different from a circle in
	 * that the size of the circle does not scale, so it is always the portion
	 * of the layers.
	 */
	public void dot(float x, float y, float radius, Painter painter, List<Transformation> transforms) {
		ellipse(x, y, radius / getScaleX(), radius / getScaleY(), painter, transforms);
	}

	public void dot(Vector2D v, float radius, Painter painter, List<Transformation> transforms) {
		dot((float) v.x(), (float) v.y(), radius, painter, transforms);
	}

	@Override
	public void line(Vector2D start, Vector2D end, Painter painter) {
		line((float) start.x(), (float) start.y(), (float) end.x(), (float) end.y(), painter);
	}

	public void vector2D(Vector2D startPos, Vector2D vector2D, Painter painter) {
		vector2D(startPos, vector2D, 1.0, painter);
	}

	public void vector2D(Vector2D startPos, Vector2D vector2D, double scale, Painter painter) {
		line((float) startPos.x(), (float) startPos.y(), (float) (startPos.x() + scale * vector2D.x()),
				(float) (startPos.y() + scale * vector2D.y()), painter);
	}

	/**
	 * Draw an empty circle.
	 *
	 * @param x
	 *            x-coordinate of center of circle
	 * @param y
	 *            y-coordinate of center of circle
	 * @param radius
	 * @param strokeWeight
	 * @param color
	 * @param layers
	 */
	@Override
	public void circle(float x, float y, float radius, Painter painter) {
		ellipse(x, y, (2 * radius), (2 * radius), painter, noTransforms);
	}


	@Override
	public void drawPath(List<Vector2D> points, Painter painter) {
		drawPath(points, painter, noTransforms);
	}

	@Override
	public void clear() {
		layer.clear();
	}

	@Override
	public void background(Painter painter) {
		final int fill = painter.getFill();
		layer.background(fill);
	}

	@Override
	public final PGraphics createLayer() {
		PGraphics layer = ProcessingUtils.createPGraphics(getWidth(), getHeight());
		layer.beginDraw();
		return layer;
	}

	@Override
	public PGraphics getImage() {
		layer.endDraw();
		return layer;
	}

	private void bindPainter(Painter painter) {

		if (painter.isFilled()) {
			layer.fill(painter.getFill());
		} else {
			layer.noFill();
		}

		if (painter.isStroked()) {
			layer.stroke(painter.getStroke());
			float scale = 0.5f * getAverageScale();
			layer.strokeWeight(painter.getStrokeWeight() / scale);
		} else {
			layer.noStroke();
		}

	}

	@Override
	public void handleNewFrame() {
		layer.translate(getCenterX() * getWidth(), getCenterY() * getHeight());
		layer.rotate(getRotation());
		layer.scale(getScaleX(), getScaleY());
	}

	private int pushedTransientTransformationsCount = 0;

	/**
	 * Sets short-term transformations.
	 *
	 * @param layers
	 * @param transientTransforms
	 */
	private void pushTransientTransformations(PGraphics layer, List<Transformation> transientTransforms) {
		checkNotNull(transientTransforms, "transforms were null");
		checkState(pushedTransientTransformationsCount == 0, "There must be no pushed transformations, but instead there were %s", pushedTransientTransformationsCount);

		pushedTransientTransformationsCount++;
		layer.pushMatrix();
		for (final Transformation transform : transientTransforms) {
			applyTransform(transform);
		}
	}

	/**
	 * Reset the short-term transformations.
	 */
	private void popTransientTransformations(PGraphics layer) {
		checkState(pushedTransientTransformationsCount == 1, "There must be one pushed transformations, but instead there were %s", pushedTransientTransformationsCount);
		pushedTransientTransformationsCount--;
		layer.popMatrix();
	}

	@Override
	public void drawOnto(PGraphics target) {
		checkArgument(target.width  == layer.width, "Widths do not match");
		checkArgument(target.height == layer.height, "Heights do not match");

		layer.endDraw();
		target.image(layer, 0, 0);
		layer.beginDraw();

	}

	@Override
	public void dot(float x, float y, float radius, Painter painter) {
		dot(x, y, radius, painter, noTransforms);
	}

	@Override
	public void dot(Vector2D pos, float radius, Painter painter) {
		dot((float) pos.x(), (float) pos.y(), radius, painter);
	}


	private interface Drawer {
		void draw();
	}

	private void drawWithTransformations(Drawer drawer, List<Transformation> transforms) {
		pushTransientTransformations(layer, transforms);
		drawer.draw();
		popTransientTransformations(layer);
	}

	private void applyTransform(Transformation transformation) {

		if(transformation instanceof Rotation) {

			Rotation rotationTransform = (Rotation) transformation;
			layer.rotate(rotationTransform.angle);

		} else if(transformation instanceof Translation) {

			Translation translationTransform = (Translation) transformation;
			layer.translate(translationTransform.x, translationTransform.y);

		} else if(transformation instanceof Scale) {

			Scale scaleTransform = (Scale) transformation;
			layer.scale(scaleTransform.x, scaleTransform.y);

		} else
			throw new ClassCastException("Unexpected transformation: " + transformation);

	}

}
