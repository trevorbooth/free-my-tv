package io.freemytv;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Roast Mode's greeting, the first thing typed when the home screen or screensaver comes up: a line for the time of
 * day from assets/roast/greetings.json. Shuffled: each part of the day goes through all its lines in a random order
 * before any comes back, never the same one twice in a row; and only when the last greeting was 30 minutes ago or more
 * (a quick trip Home goes straight to the other messages). Only in Roast Mode (en-XR).
 */
final class Greetings {
    private Greetings() {}

    private static final String[] PARTS = {"morning", "afternoon", "evening", "night"};
    private static String[][] lines;
    private static final long GAP_MS = 30 * 60_000L;
    private static final java.util.Random random = new java.util.Random();
    private static final java.util.List<java.util.List<Integer>> bags = new java.util.ArrayList<>();
    private static String last = "";
    private static long lastAt;

    /** The next greeting, or "" when it's too soon since the last one (or not in Roast Mode). */
    static synchronized String pick(Context c, long now) {
        if (!"en-XR".equals(Ui.language)) return "";
        if (lastAt != 0 && now - lastAt < GAP_MS && now >= lastAt) return "";
        String[][] all = load(c);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int part = hour >= 5 && hour < 12 ? 0 : hour < 17 ? 1 : hour < 22 ? 2 : 3;
        String[] list = all[part];
        if (list.length == 0) return "";
        while (bags.size() < PARTS.length) bags.add(new java.util.ArrayList<>());
        java.util.List<Integer> bag = bags.get(part);
        if (bag.isEmpty()) {
            for (int i = 0; i < list.length; i++) bag.add(i);
            java.util.Collections.shuffle(bag, random);
            // A fresh round never starts with the line that ended the last one.
            if (bag.size() > 1 && list[bag.get(0)].equals(last)) java.util.Collections.swap(bag, 0, bag.size() - 1);
        }
        last = list[bag.remove(0)];
        lastAt = now;
        return last;
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
