package paul.wintz.processing;

import paul.wintz.stringids.StringId;
import paul.wintz.stringids.StringIdMap;
import paul.wintz.utils.Toast;
import processing.core.PApplet;
import processing.core.PConstants;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.pow;

public class ProcessingToaster implements Toast.Toaster {

    private PApplet pApplet;
    private String message = "";
    private final StringIdMap stringIdMap;

    private final FadeCalculator fadeCalculator = new FadeCalculator();

    public ProcessingToaster(PApplet pApplet, StringIdMap stringIdMap) {
        this.pApplet = pApplet;
        this.stringIdMap = stringIdMap;
    }

    @Override
    public void show(String message) {
        fadeCalculator.start(Instant.now());
        this.message = message;
    }

    @Override
    public void show(String messageFormat, Object... args) {
        Object[] translatedArgs = translateArgs(args);
        this.show(String.format(messageFormat, translatedArgs));
    }

    @Override
    public void show(StringId messageId) {
        String message = checkNotNull(stringIdMap.get(messageId));
        this.show(message);
    }

    @Override
    public void show(StringId messageFormatId, Object... args) {
        Object[] translatedArgs = translateArgs(args);
        this.show(String.format(stringIdMap.get(messageFormatId), translatedArgs));
    }

    private Object[] translateArgs(Object[] args) {
        Object[] translatedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
             if (arg instanceof StringId) {
                translatedArgs[i] = stringIdMap.get((StringId) arg);
            } else  {
                translatedArgs[i] = arg;
            }
        }
        return translatedArgs;
    }

    private class FadeCalculator {
        private static final long MILLISECONDS_TO_DISPLAY = 2000;
        private static final long MILLISECONDS_TO_FADE = 500;

        // Initialize it in the past so that it is not displayed until a toast is triggered;
        private Instant displayStartTime = Instant.now().minusSeconds(999);

        void start(Instant now){
            displayStartTime = now;
        }

        float alpha(Instant now) {
            float millisecondsDisplayed = displayStartTime.until(now, ChronoUnit.MILLIS);

            if(millisecondsDisplayed < MILLISECONDS_TO_DISPLAY) {
                return 255;
            } else {
                float time_since_start_of_fade = millisecondsDisplayed - MILLISECONDS_TO_DISPLAY;
                float scale = (float) (1 - pow(time_since_start_of_fade / MILLISECONDS_TO_FADE, 2));
                return 255 * scale;
            }
        }

    }

    public void display() {
        pApplet.textAlign(PConstants.CENTER, PConstants.BOTTOM);
        pApplet.textSize(24);

        float alpha = fadeCalculator.alpha(Instant.now());

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
