package io.freemytv;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Sci-fi blaster "pews" for the home screen (moving focus and opening): synthesized here (no recorded sound), a
 * falling chirp with a metallic ring, several of each so it isn't the same one every time. Each is written once as a
 * WAV in the cache and played quietly through SoundPool. It only plays on the home screen, never inside apps.
 * SoundPool.play is a call to the audio server (5 to 16 ms on a KTC TV's MediaTek, Android 11), once per key: played
 * on the UI thread it made the first frames of a focus move stutter, so it plays from a thread of its own.
 */
final class Click {
    private static final int RATE = 48_000;
    private volatile SoundPool pool;
    private int[] move = new int[0], open = new int[0];
    private final java.util.Random random = new java.util.Random();
    /** {start Hz, end Hz, seconds} of each pew: short ones for moving, longer and lower ones for opening. */
    private static final float[][] MOVES = {
            {2600, 420, 0.11f}, {2900, 520, 0.10f}, {2300, 380, 0.12f}, {3200, 600, 0.09f}, {2450, 340, 0.13f}};
    private static final float[][] OPENS = {{2100, 160, 0.30f}, {2400, 200, 0.26f}, {1900, 140, 0.34f}};
    private final HandlerThread thread = new HandlerThread("click", Process.THREAD_PRIORITY_BACKGROUND);
    private final Handler handler;

    Click(Context c) {
        thread.start();
        handler = new Handler(thread.getLooper());
        try {
            pool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build();
            move = new int[MOVES.length];
            for (int i = 0; i < MOVES.length; i++) move[i] = pool.load(pew(c, "pew-move-" + i + ".wav", MOVES[i], i), 1);
            open = new int[OPENS.length];
            for (int i = 0; i < OPENS.length; i++) open[i] = pool.load(pew(c, "pew-open-" + i + ".wav", OPENS[i], 10 + i), 1);
        } catch (IOException | RuntimeException e) {
            AppLog.w("click sound unavailable: " + e.getMessage());
            release();
        }
    }

    void move() {
        if (pool == null || !Prefs.sounds()) return;
        handler.post(() -> {
            SoundPool p = pool;
            if (p != null && move.length > 0) p.play(move[random.nextInt(move.length)], 0.16f, 0.16f, 1, 0, 1f);
        });
    }

    void open() {
        if (pool == null || !Prefs.sounds()) return;
        handler.post(() -> {
            SoundPool p = pool;
            if (p != null && open.length > 0) p.play(open[random.nextInt(open.length)], 0.22f, 0.22f, 1, 0, 1f);
        });
    }

    void release() {
        SoundPool p = pool;
        pool = null;
        if (p != null) handler.post(p::release);   // after the plays already queued, never during one
        thread.quitSafely();
    }

    /**
     * One pew: a chirp falling fast from start to end Hz (exponentially, like a struck spring), a detuned partial and a
     * softer octave for the metallic ring, a hint of noise in the first few ms, 2 ms attack and a quick decay.
     */
    private static String pew(Context c, String name, float[] shape, int seed) throws IOException {
        File f = new File(c.getCacheDir(), name);
        if (f.exists() && f.length() > 44) return f.getPath();
        float f0 = shape[0], f1 = shape[1], seconds = shape[2];
        int n = Math.round(RATE * seconds);
        ByteBuffer b = ByteBuffer.allocate(44 + n * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes("US-ASCII")).putInt(36 + n * 2).put("WAVE".getBytes("US-ASCII"));
        b.put("fmt ".getBytes("US-ASCII")).putInt(16).putShort((short) 1).putShort((short) 1)
                .putInt(RATE).putInt(RATE * 2).putShort((short) 2).putShort((short) 16);
        b.put("data".getBytes("US-ASCII")).putInt(n * 2);
        java.util.Random noise = new java.util.Random(seed);
        double tau = seconds * 0.22, p1 = 0, p2 = 0, p3 = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            double freq = f1 + (f0 - f1) * Math.exp(-t / tau);
            p1 += 2 * Math.PI * freq / RATE;
            p2 += 2 * Math.PI * freq * 1.503 / RATE;
            p3 += 2 * Math.PI * freq * 2.0 / RATE;
            double env = Math.min(1, t / 0.002) * Math.exp(-t / (seconds * 0.38));
            double s = 0.62 * Math.sin(p1) + 0.24 * Math.sin(p2) + 0.1 * Math.sin(p3)
                    + (t < 0.006 ? 0.25 * (noise.nextDouble() * 2 - 1) * (1 - t / 0.006) : 0);
            b.putShort((short) Math.round(Math.max(-1, Math.min(1, s * env * 0.8)) * Short.MAX_VALUE));
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(b.array());
        }
        return f.getPath();
    }
}
