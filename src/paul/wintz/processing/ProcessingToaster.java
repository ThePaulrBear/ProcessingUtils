package paul.wintz.processing;

import paul.wintz.utils.Toast;
import processing.core.PApplet;
import processing.core.PConstants;

public class ProcessingToaster implements Toast.Toaster {

    private PApplet pApplet;
    private String message = "";
    private int framesSinceKeyPress;

    public ProcessingToaster(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    @Override
    public void show(String message) {
        framesSinceKeyPress = 0;
        this.message = message;
    }

    public void display() {
        framesSinceKeyPress++;

        pApplet.textAlign(PConstants.CENTER, PConstants.BOTTOM);
        pApplet.textSize(24);

        // TODO: Make these calculations more structured
        final int alpha = 255 - 8 * framesSinceKeyPress;
        final int x = pApplet.width / 2;
        final int y = pApplet.height - 30;

        // draw Shadow
        pApplet.fill(0, alpha);
        pApplet.text(message, x + 2.0f, y + 2.0f);

        // drawText
        pApplet.fill(255, alpha);
        pApplet.text(message, x, y);
    }
}
