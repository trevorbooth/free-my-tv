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
 * crash, as plausibly as it can. A glitch (the layers underneath shake, torn colour bars, flicker), then a black screen
 * that prints a system-style crash dump in plain grey and "Restarting", then it fades back. Nothing actually crashes: it's
 * one full-screen view on top, drawn only while it runs. Any key skips to the end (HomeView).
 */
final class FakeCrash extends View {
    interface Host {
        /** Shakes the home screen's layers by (dx, dy) pixels; (0, 0) puts them back. */
        void jitter(float dx, float dy);

        /** The crash is over and the screen is back. */
        void crashDone();
    }

    private static final long GLITCH_MS = 3000, SCREEN_MS = 5600, FADE_MS = 600;
    private static final int CHAR_MS = 6;   // printed like a log: fast, a pause at each line end
    /** A crash dump with nothing funny in it; the dump file's name carries the real date and time (lines()). */
    private static final String[] LINES = {
        "FATAL EXCEPTION: RenderThread",
        "Process: io.freemytv:home, PID: 9132",
        "java.lang.IllegalStateException: GPU context lost (EGL_CONTEXT_LOST)",
        "    at android.view.ThreadedRenderer.nSyncAndDrawFrame(Native Method)",
        "    at android.view.ThreadedRenderer.draw(ThreadedRenderer.java:832)",
        "    at android.view.ViewRootImpl.performDraw(ViewRootImpl.java:4721)",
        "    at io.freemytv.HomeView.onDraw(HomeView.java:4977)",
        "    at android.os.Looper.loop(Looper.java:288)",
        "",
        "watchdog: system_server not responding (30012 ms)",
        "%DUMP%",
        "",
        "Restarting",
    };
    private String[] lines = LINES;

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
        String stamp = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", java.util.Locale.US).format(new java.util.Date());
        lines = LINES.clone();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equals("%DUMP%")) lines[i] = "dumping state to /data/anr/anr_" + stamp + " ... done";
        }
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

    /** The crash dump, printed fast like a log, then "Restarting" with dots, in plain grey on black. */
    private void screen(Canvas c, int w, int h, float k, long t, float alpha) {
        fill.setColor(Ui.alpha(0xFF000000, alpha));
        c.drawRect(0, 0, w, h, fill);
        mono.setTextSize(26 * k);
        alarm.setTextSize(26 * k);
        mono.setColor(Ui.alpha(0xFFB8B8B8, alpha));
        alarm.setColor(Ui.alpha(0xFFFFFFFF, alpha));
        float x = 110 * k, y = 170 * k, line = 40 * k;
        int chars = (int) (t / CHAR_MS);
        for (int i = 0; i < lines.length && chars > 0; i++) {
            String s = lines[i];
            int n = Math.min(chars, s.length());
            if (i == lines.length - 1 && n == s.length()) {
                // "Restarting" and its dots, one more every 400 ms
                int dots = (int) ((t / 400) % 4);
                c.drawText(s + "....".substring(0, dots), x, y, mono);
            } else {
                c.drawText(s, 0, n, x, y, i == 0 ? alarm : mono);
            }
            chars -= s.length() + 12;   // a short pause at each line end
            y += line;
        }
    }
}
