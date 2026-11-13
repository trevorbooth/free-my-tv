package io.freemytv;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Roast Mode's greeting above the date: a line for the time of day from assets/roast/greetings.json, a new one every
 * 15 minutes. Only in Roast Mode (en-XR); every other language gets none.
 */
final class Greetings {
    private Greetings() {}

    private static final String[] PARTS = {"morning", "afternoon", "evening", "night"};
    private static String[][] lines;

    static String pick(Context c, long now) {
        if (!"en-XR".equals(Ui.language)) return "";
        String[][] all = load(c);
        if (all == null) return "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String[] list = all[hour >= 5 && hour < 12 ? 0 : hour < 17 ? 1 : hour < 22 ? 2 : 3];
        if (list.length == 0) return "";
        long slot = now / (15 * 60_000L);
        return list[(int) ((slot * 7919L) % list.length)];
    }

    private static synchronized String[][] load(Context c) {
        if (lines != null) return lines;
        try (InputStream in = c.getAssets().open("roast/greetings.json")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            JSONObject o = new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
            String[][] all = new String[PARTS.length][];
            for (int i = 0; i < PARTS.length; i++) {
                JSONArray a = o.optJSONArray(PARTS[i]);
                all[i] = new String[a == null ? 0 : a.length()];
                for (int j = 0; j < all[i].length; j++) all[i][j] = a.getString(j);
            }
            lines = all;
        } catch (Exception e) {
            AppLog.w("greetings: " + e.getMessage());
            lines = new String[PARTS.length][0];
        }
        return lines;
    }
}
