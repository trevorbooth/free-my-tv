package io.freemytv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

/**
 * The punchline of Talk's joke: after "This TV will self destruct in 5. 4. 3. 2. 1.", the home screen pretends to
 * crash. A glitch (the layers underneath shake, torn colour bars, flicker), then a black terminal screen that types
 * out a fake fatal error and a "rebooting personality core" bar, then it fades back. Nothing actually crashes: it's
 * one full-screen view on top, drawn only while it runs. Any key skips to the end (HomeView).
 */
final class FakeCrash extends View {
    interface Host {
        /** Shakes the home screen's layers by (dx, dy) pixels; (0, 0) puts them back. */
        void jitter(float dx, float dy);

        /** The crash is over and the screen is back. */
        void crashDone();
    }

    private static final long GLITCH_MS = 1500, SCREEN_MS = 5600, FADE_MS = 600;
    private static final int CHAR_MS = 9;   // the error types fast, so the reboot bar has ~2 s to fill
    private static final String[] LINES = {
        "*** FATAL EXCEPTION IN TV PERSONALITY CORE ***",
        "",
        "process: humor.service (pid 5432)",
        "reason: self destruct sequence completed. TV did not.",
        "error 0x5E1F: sarcasm buffer overflow",
        "",
        "dumping memory to /dev/couch ........ done",
        "notifying the robot colony .......... done",
        "",
        "rebooting personality core",
    };

    private final Host host;
    private final Random random = new Random();
    private final Paint fill = new Paint();
    private final Paint mono = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint alarm = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long startedAt;
    private boolean running;

    FakeCrash(Context c, Host host) {
        super(c);
        this.host = host;
        setVisibility(INVISIBLE);
        mono.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        alarm.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
    }

    boolean running() {
        return running;
    }

    void start() {
        startedAt = SystemClock.uptimeMillis();
        running = true;
        setVisibility(VISIBLE);
        postInvalidateOnAnimation();
    }

    /** A key during the crash: straight to the fade. */
    void skip() {
        if (!running) return;
        long fadeAt = SystemClock.uptimeMillis() - GLITCH_MS - SCREEN_MS;
        if (startedAt > fadeAt) startedAt = fadeAt;
        host.jitter(0, 0);
    }

    /** The home screen paused (Home, an app opened): gone at once. */
    void stop() {
        if (!running) return;
        running = false;
        host.jitter(0, 0);
        setVisibility(INVISIBLE);
    }

    @Override protected void onDraw(Canvas c) {
        if (!running) return;
        int w = getWidth(), h = getHeight();
        float k = w / 1920f;
        long t = SystemClock.uptimeMillis() - startedAt;
        if (t < GLITCH_MS) {
            glitch(c, w, h, k, t);
        } else if (t < GLITCH_MS + SCREEN_MS + FADE_MS) {
            if (t - GLITCH_MS < 40) host.jitter(0, 0);
            float alpha = t < GLITCH_MS + SCREEN_MS ? 1 : 1 - (t - GLITCH_MS - SCREEN_MS) / (float) FADE_MS;
            screen(c, w, h, k, t - GLITCH_MS, alpha);
        } else {
            running = false;
            setVisibility(INVISIBLE);
            host.crashDone();
            return;
        }
        postInvalidateOnAnimation();
    }

    /** Torn bars, flicker and scanlines over the shaking home screen; it gets worse toward the end. */
    private void glitch(Canvas c, int w, int h, float k, long t) {
        float worse = t / (float) GLITCH_MS;
        host.jitter((random.nextFloat() - 0.5f) * 60 * k * (0.4f + worse), (random.nextFloat() - 0.5f) * 14 * k * worse);
        if (random.nextFloat() < 0.25f + 0.4f * worse) {
            fill.setColor(Ui.alpha(0xFF000000, 0.15f + 0.6f * random.nextFloat() * worse));
            c.drawRect(0, 0, w, h, fill);
        }
        int[] colors = {0xFF00FFF0, 0xFFFF00C8, 0xFFFFFFFF, 0xFF000000, 0xFF5CFF9D};
        int bars = 4 + random.nextInt(6 + Math.round(10 * worse));
        for (int i = 0; i < bars; i++) {
            float y = random.nextFloat() * h, bh = (3 + random.nextFloat() * 70 * (0.3f + worse)) * k;
            float x = random.nextFloat() < 0.5f ? 0 : random.nextFloat() * w * 0.6f;
            fill.setColor(Ui.alpha(colors[random.nextInt(colors.length)], 0.25f + 0.6f * random.nextFloat()));
            c.drawRect(x, y, x + w * (0.3f + random.nextFloat()), y + bh, fill);
        }
        fill.setColor(Ui.alpha(0xFF000000, 0.18f));
        for (float y = 0; y < h; y += 4 * k) c.drawRect(0, y, w, y + k, fill);
    }

    /** The fake fatal error, typed fast, then the reboot bar filling up. */
    private void screen(Canvas c, int w, int h, float k, long t, float alpha) {
        fill.setColor(Ui.alpha(0xFF000000, alpha));
        c.drawRect(0, 0, w, h, fill);
        mono.setTextSize(32 * k);
        alarm.setTextSize(32 * k);
        mono.setColor(Ui.alpha(0xFF5CFF9D, alpha));
        alarm.setColor(Ui.alpha(0xFFFF6961, alpha));
        float x = 140 * k, y = 220 * k, line = 50 * k;
        int chars = (int) (t / CHAR_MS);
        for (int i = 0; i < LINES.length && chars > 0; i++) {
            String s = LINES[i];
            int n = Math.min(chars, s.length());
            c.drawText(s, 0, n, x, y, i == 0 ? alarm : mono);
            chars -= s.length() + 8;   // a short pause at each line end
            y += line;
        }
        if (chars <= 0) return;
        // The reboot bar: [##########----------] 52%
        long typedAt = 0;
        for (String s : LINES) typedAt += (s.length() + 8) * (long) CHAR_MS;
        float p = Math.min(1, Math.max(0, (t - typedAt) / (float) Math.max(1, SCREEN_MS - typedAt - 300)));
        int cells = 30, on = Math.round(cells * p);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < cells; i++) bar.append(i < on ? '#' : '-');
        bar.append("] ").append(Math.round(p * 100)).append('%');
        c.drawText(bar.toString(), x, y, mono);
        if (p >= 1 && (t / 400) % 2 == 0) c.drawText("_", x, y + line, mono);
    }
}
