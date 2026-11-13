package io.freemytv;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RuntimeShader;

/**
 * The Galaxy theme's background: a slowly turning nebula with three layers of twinkling stars, drawn with AGSL into a
 * third-size layer and scaled up (an onn 4K stick's Mali-G31 took ~44 ms a frame at half size). It moves slowly, so the layer is redrawn at most ~15 times a second (`due`); the
 * frames between reuse it. Without AGSL (Android 8 to 12) `ready()` is false and the home screen keeps its plain
 * night background.
 */
final class Galaxy {
    private static final String SHADER =
        "uniform float2 res;\n" +            // the layer's size (px)
        "uniform float t;\n" +               // seconds
        // Dave Hoskins' "hash without sine": small multipliers, so it stays smooth on GPUs with less precision.
        "float hash(float2 p) {\n" +
        "  p = mod(p, 512.0);\n" +
        "  float3 p3 = fract(float3(p.x, p.y, p.x) * 0.1031);\n" +
        "  p3 += dot(p3, p3.yzx + 33.33);\n" +
        "  return fract((p3.x + p3.y) * p3.z);\n" +
        "}\n" +
        "float noise(float2 p) {\n" +
        "  float2 i = floor(p);\n" +
        "  float2 f = fract(p);\n" +
        "  float2 u = f * f * (3.0 - 2.0 * f);\n" +
        "  return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),\n" +
        "             mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);\n" +
        "}\n" +
        "float fbm(float2 p) {\n" +
        "  float v = 0.0;\n" +
        "  float a = 0.55;\n" +
        "  for (int i = 0; i < 3; i++) {\n" +
        "    v += a * noise(p);\n" +
        "    p = float2(p.x * 1.6 + p.y * 1.2, p.y * 1.6 - p.x * 1.2) + 5.0;\n" +
        "    a *= 0.5;\n" +
        "  }\n" +
        "  return v;\n" +
        "}\n" +
        // One layer of stars: one chance per cell, placed off-center, each twinkling at its own pace.
        "float stars(float2 p, float density) {\n" +
        "  float2 id = floor(p);\n" +
        "  float h = hash(id);\n" +
        "  if (h > density) { return 0.0; }\n" +
        "  float2 at = float2(hash(id + 3.1), hash(id + 7.7)) * 0.6 + 0.2;\n" +
        "  float d = length(fract(p) - at);\n" +
        "  float twinkle = 0.55 + 0.45 * sin(t * (0.8 + 2.5 * hash(id + 1.9)) + h * 60.0);\n" +
        "  return smoothstep(0.2, 0.0, d) * twinkle;\n" +
        "}\n" +
        "half4 main(float2 xy) {\n" +
        "  float2 uv = xy / res;\n" +
        "  float aspect = res.x / res.y;\n" +
        "  float2 p = float2(uv.x * aspect, uv.y);\n" +
        // The whole sky turns slowly around a point right of center, behind where the clock isn't.
        "  float2 c = p - float2(aspect * 0.62, 0.42);\n" +
        "  float a = t * 0.006;\n" +
        "  float2 r = float2(c.x * cos(a) - c.y * sin(a), c.x * sin(a) + c.y * cos(a));\n" +
        "  float n = fbm(r * 2.2 + float2(t * 0.012, -t * 0.008));\n" +
        "  float n2 = fbm(r * 4.5 - n * 1.6 + 3.0);\n" +
        // The galaxy's band, a tilted stripe across the screen, and its bright core.
        "  float across = r.y + 0.32 * r.x;\n" +
        "  float band = exp(-across * across * 9.0);\n" +
        "  float core = exp(-dot(r, r) * 6.0);\n" +
        "  half3 col = half3(0.012, 0.010, 0.040);\n" +
        "  col += half3(0.30, 0.10, 0.50) * half(n * n * 1.6 * (0.35 + band));\n" +
        "  col += half3(0.04, 0.22, 0.52) * half(n2 * n2 * n2 * 2.2 * band);\n" +
        "  col += half3(0.95, 0.45, 0.70) * half(pow(n * n2, 2.0) * 2.0 * band);\n" +
        "  col += half3(1.00, 0.80, 0.65) * half(core * 0.35 * (0.5 + n2));\n" +
        // Dust lanes: dark streaks along the band.
        "  col *= half(1.0 - 0.5 * smoothstep(0.55, 0.8, n2) * smoothstep(0.3, 0.6, n) * band);\n" +
        // Stars, drifting at three depths (the near ones faster), thicker along the band.
        "  float s = stars(r * 30.0 + float2(t * 0.05, 0.0), 0.12 + 0.12 * band)\n" +
        "          + 0.75 * stars(r * 55.0 + float2(t * 0.025, 7.0), 0.16 + 0.16 * band);\n" +
        "  half3 tint = mix(half3(0.75, 0.82, 1.0), half3(1.0, 0.88, 0.75), half(hash(floor(r * 30.0))));\n" +
        "  col += tint * half(s);\n" +
        // Darker under the dock and behind the clock, so the text and the glass stay readable.
        "  float scrim = 1.0 - 0.45 * smoothstep(0.55, 1.0, uv.y) - 0.25 * (1.0 - smoothstep(0.0, 0.55, uv.x)) * smoothstep(0.15, 0.75, uv.y);\n" +
        "  col *= half(scrim);\n" +
        "  col += half((hash(xy + t) - 0.5) / 255.0);\n" +   // breaks up banding in the dark gradients
        "  return half4(col, 1.0);\n" +
        "}\n";

    private static final long FRAME_MS = 66;

    private RuntimeShader shader;
    private final Paint paint = new Paint();
    private final Paint layerPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Recording node = Recording.create("galaxy");
    private int w = 1, h = 1, lw = 1, lh = 1;
    private long drawnAt;

    Galaxy() {
        try {
            shader = new RuntimeShader(SHADER);
            paint.setShader(shader);
        } catch (RuntimeException | LinkageError e) {
            AppLog.w("galaxy shader unavailable: " + e.getMessage());
            shader = null;
        }
        node.setUseCompositingLayer(true, layerPaint);
    }

    boolean ready() {
        return shader != null;
    }

    void setSize(int width, int height) {
        w = Math.max(1, width);
        h = Math.max(1, height);
        lw = Math.max(1, Math.round(w / 3f));
        lh = Math.max(1, Math.round(h / 3f));
        node.setPosition(0, 0, lw, lh);
        if (shader != null) shader.setFloatUniform("res", lw, lh);
        drawnAt = 0;
    }

    /** True when the sky should be drawn again to move the galaxy on. */
    boolean due(long up) {
        return shader != null && up - drawnAt >= FRAME_MS;
    }

    void draw(Canvas c, long up) {
        if (shader == null) return;
        if (up - drawnAt >= FRAME_MS || drawnAt == 0) {
            drawnAt = up;
            // Seconds, kept small for float precision (it wraps about once a day).
            shader.setFloatUniform("t", (float) ((up / 1000.0) % 86_400.0));
            Canvas rc = node.beginRecording(lw, lh);
            try {
                rc.drawPaint(paint);
            } finally {
                node.endRecording();
            }
        }
        c.save();
        c.scale((float) w / lw, (float) h / lh);
        node.draw(c);
        c.restore();
    }
}
