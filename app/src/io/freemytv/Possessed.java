package io.freemytv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Roast Mode's "the TV is alive" messages (a sentient TV asking for help): typed above the date one character at a
 * time, held with a blinking cursor, erased, then the next. The first one after the home screen comes up is a greeting for the time of day (Greetings);
 * the rest come from assets/roast/messages.json, at random, never one of the last 200 again. Only in Roast Mode
 * (en-XR); it runs on the main thread's Handler and only while the home screen runs (start/stop).
 */
final class Possessed {
    private static final int TYPING = 0, HOLD = 1, ERASING = 2, PAUSE = 3;
    private static final int RECENT = 200;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private Context context;
    private Runnable changed;
    private String text = "";
    private int shown, phase;
    private boolean cursor, first;
    private long holdUntil;

    private static String[] lines;
    private static final List<Integer> recent = new ArrayList<>();

    void start(Context c, Runnable onChange) {
        handler.removeCallbacks(step);
        context = c.getApplicationContext();
        changed = onChange;
        if (!"en-XR".equals(Ui.language)) {
            text = "";
            shown = 0;
            return;
        }
        first = true;
        begin(700);
    }

    void stop() {
        handler.removeCallbacks(step);
    }

    String text() {
        return text;
    }

    int shown() {
        return shown;
    }

    boolean cursor() {
        return cursor;
    }

    private void begin(long delay) {
        String next = first ? Greetings.pick(context, System.currentTimeMillis()) : "";
        first = false;
        if (next.isEmpty()) next = next();
        text = next;
        shown = 0;
        cursor = true;
        phase = TYPING;
        handler.postDelayed(step, delay);
    }

    private final Runnable step = new Runnable() {
        @Override public void run() {
            long up = SystemClock.uptimeMillis();
            long delay;
            switch (phase) {
                case TYPING:
                    shown++;
                    cursor = true;
                    if (shown >= text.length()) {
                        shown = text.length();
                        phase = HOLD;
                        holdUntil = up + 3500 + 45L * text.length();
                        delay = 530;
                    } else {
                        delay = pace(text.charAt(shown - 1));
                    }
                    break;
                case HOLD:
                    cursor = !cursor;
                    if (up >= holdUntil) {
                        phase = ERASING;
                        cursor = true;
                    }
                    delay = 530;
                    break;
                case ERASING:
                    shown = Math.max(0, shown - 2);
                    if (shown == 0) {
                        phase = PAUSE;
                        delay = 900 + random.nextInt(900);
                    } else {
                        delay = 14;
                    }
                    break;
                default:
                    begin(0);
                    return;
            }
            if (changed != null) changed.run();
            handler.postDelayed(this, delay);
        }
    };

    /** How long after this character before the next: unsteady, longer after punctuation, now and then a stall. */
    private long pace(char ch) {
        if (ch == '.' || ch == '?' || ch == '!') return 320 + random.nextInt(260);
        if (ch == ',' || ch == ':' || ch == ';' || ch == '-') return 150 + random.nextInt(120);
        if (random.nextInt(40) == 0) return 450 + random.nextInt(500);   // hesitates
        return 35 + random.nextInt(60);
    }

    private String next() {
        String[] all = load(context);
        if (all.length == 0) return "";
        int pick = random.nextInt(all.length);
        for (int tries = 0; tries < 20 && recent.contains(pick); tries++) pick = random.nextInt(all.length);
        recent.add(pick);
        if (recent.size() > Math.min(RECENT, all.length / 2)) recent.remove(0);
        return all[pick];
    }

    private static synchronized String[] load(Context c) {
        if (lines != null) return lines;
        try (InputStream in = c.getAssets().open("roast/messages.json")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            JSONArray a = new JSONObject(out.toString(StandardCharsets.UTF_8.name())).getJSONArray("lines");
            String[] all = new String[a.length()];
            for (int i = 0; i < all.length; i++) all[i] = a.getString(i);
            lines = all;
        } catch (Exception e) {
            AppLog.w("messages: " + e.getMessage());
            lines = new String[0];
        }
        return lines;
    }

    /** A message broken into lines that fit `width`, at most `max` (the last one takes the rest). */
    static final class Wrapped {
        final String text;
        final Paint paint;
        final String[] lines;

        Wrapped(String text, Paint paint, String[] lines) {
            this.text = text;
            this.paint = paint;
            this.lines = lines;
        }
    }

    /**
     * Draws what has been typed of `t` so far, line by line from the baseline `base`, with the block cursor after the
     * last character when `cursor`. Shared by the home screen and the screensaver.
     */
    static void draw(Canvas c, Wrapped t, int shown, boolean cursor, float x, float base, float lineH) {
        int left = shown;
        for (int i = 0; i < t.lines.length; i++) {
            String line = t.lines[i];
            boolean last = left <= line.length() || i == t.lines.length - 1;
            String part = line.substring(0, Math.max(0, Math.min(left, line.length())));
            if (last && cursor) part += "\u2588";
            if (!part.isEmpty()) c.drawText(part, x - Ui.leftBearing(t.paint, part), base + i * lineH, t.paint);
            left -= line.length() + 1;   // the space the wrap took out
            if (last) break;
        }
    }

    /** The small heading over the typed messages. */
    static final String LABEL = "TV'S THOUGHTS";

    static Wrapped wrap(String text, Paint p, float width, int max) {
        List<String> out = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String tryLine = line.length() == 0 ? w : line + " " + w;
            if (line.length() > 0 && p.measureText(tryLine) > width && out.size() < max - 1) {
                out.add(line.toString());
                line.setLength(0);
                line.append(w);
            } else {
                line.setLength(0);
                line.append(tryLine);
            }
        }
        out.add(line.toString());
        return new Wrapped(text, p, out.toArray(new String[0]));
    }
}
