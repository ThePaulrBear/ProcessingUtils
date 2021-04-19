package paul.wintz.processing;

import paul.wintz.canvas.Layer;
import paul.wintz.canvas.Painter;
import paul.wintz.math.Vector2D;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.*;

@SuppressWarnings("unused")
public class PGraphicsLayer extends Layer<PGraphics> {

    private static final List<Transformation> noTransforms = Collections.emptyList();
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
        getImage().line(x0, y0, x1, y1);
    }

    @Override
    public void ellipse(float xCenter, float yCenter, float width, float height, Painter painter, List<Transformation> transientTransforms) {
        bindPainter(painter);
        drawWithTransformations(() -> {
            getImage().ellipseMode(PConstants.CENTER);
            getImage().ellipse(xCenter, yCenter, width, height);
        }, transientTransforms);
    }

    @Override
    public void arc(float xCenter, float yCenter, float width, float height, float startAngle, float endAngle, Painter painter) {
        bindPainter(painter);
        getImage().arc(xCenter, yCenter, width, height, startAngle, endAngle);
    }

    @Override
    public void rectangle(float x, float y, float width, float height, Painter painter, List<Transformation> transforms) {
        bindPainter(painter);
        drawWithTransformations(() -> getImage().rect(x, y, width, height), transforms);
    }

    @Override
    public void quad(Vector2D corner0, Vector2D corner1, Vector2D corner2, Vector2D corner3, Painter painter) {
        bindPainter(painter);
        getImage().quad((float) corner0.x(), (float) corner0.y(),
                (float) corner1.x(), (float) corner1.y(),
                (float) corner2.x(), (float) corner2.y(),
                (float) corner3.x(), (float) corner3.y());
    }

    @Override
    public void drawPath(List<Vector2D> points, Painter painter, List<Transformation> transientTransforms) {
        bindPainter(painter);
        drawWithTransformations(() -> {
            getImage().beginShape();
            for (final Vector2D pnt : points) {
                getImage().curveVertex((float) pnt.x(), (float) pnt.y());
            }
            getImage().endShape();
        }, transientTransforms);
    }

    @Override
    public void drawPolygon(final List<Vector2D> points, Painter painter, List<Transformation> transientTransforms) {
        synchronized (lock) {
            bindPainter(painter);
            drawWithTransformations(() -> {
                PGraphics image = getImage();
                image.beginShape();
                for (final Vector2D pnt : points) {
                    image.vertex((float) pnt.x(), (float) pnt.y());
                }
                image.endShape();
            }, transientTransforms);
        }
    }

    @Override
    public void drawText(String text, int x, int y) {
        getImage().text(text, x, y);
    }

    @Override
    public void clear() {
        getImage().clear();
    }

    @Override
    public void fill(int fillColor) {
        getImage().background(fillColor);
    }

    @Override
    protected final PGraphics createImageObject() {
        PGraphics layer = ProcessingUtils.createPGraphics(getWidth(), getHeight());
        layer.beginDraw();
        return layer;
    }

    private void bindPainter(Painter painter) {

        if (painter.isFilled()) {
            getImage().fill(painter.getFill());
        } else {
            getImage().noFill();
        }

        if (painter.isStroked()) {
            getImage().stroke(painter.getStroke());
            getImage().strokeWeight(painter.getStrokeWeight());
        } else {
            getImage().noStroke();
        }

    }

    @Override
    public void handleNewFrame() {
        PGraphics image = getImage();
        image.resetMatrix();
        image.translate(getCenterX() * getWidth(), getCenterY() * getHeight());
        image.rotate(getRotation());
        image.scale(getScaleX(), getScaleY());
    }

    private int pushedTransientTransformationsCount = 0;

    /**
     * Sets short-term transformations.
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
        checkArgument(target.width == getImage().width, "Widths do not match");
        checkArgument(target.height == getImage().height, "Heights do not match");

        getImage().endDraw();
        target.image(getImage(), 0, 0);
        getImage().beginDraw();

    }

    @FunctionalInterface
    private interface Drawer {
        void draw();
    }

    private void drawWithTransformations(Drawer drawer, List<Transformation> transforms) {
        pushTransientTransformations(getImage(), transforms);
        drawer.draw();
        popTransientTransformations(getImage());
    }

    private void applyTransform(Transformation transformation) {

        if (transformation instanceof Rotation) {

            Rotation rotationTransform = (Rotation) transformation;
            getImage().rotate(rotationTransform.angle);

        } else if (transformation instanceof Translation) {

            Translation translationTransform = (Translation) transformation;
            getImage().translate(translationTransform.x, translationTransform.y);

        } else if (transformation instanceof Scale) {

            Scale scaleTransform = (Scale) transformation;
            getImage().scale(scaleTransform.x, scaleTransform.y);

        } else
            throw new ClassCastException("Unexpected transformation: " + transformation);

    }

}
