package io.freemytv;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Headlines in place of the crypto prices: the titles of an RSS or Atom feed (config.properties' news_feed, NPR News by
 * default), fetched in the background every 20 minutes, one shown at a time for 15 seconds. HTTPS only, no redirects
 * followed, at most 1 MB read; only the titles are kept. Nothing is fetched until the home screen or screensaver asks.
 */
final class News {
    private News() {}

    static final long SHOW_MS = 15_000L;
    private static final long REFRESH_MS = 20 * 60_000L, RETRY_MS = 2 * 60_000L;
    private static final int MAX_BYTES = 1 << 20, MAX_TITLES = 30;

    private static volatile String[] titles = new String[0];
    private static long fetchedAt, triedAt;
    private static boolean fetching;
    private static final ExecutorService worker = Executors.newSingleThreadExecutor();

    static boolean has() {
        return titles.length > 0;
    }

    /** The headline for this moment, or null before the first fetch. */
    static String headline(long now) {
        String[] t = titles;
        return t.length == 0 ? null : t[(int) ((now / SHOW_MS) % t.length)];
    }

    /** Fetches again when the last one is old (or failed a while ago); `done` runs on the worker after a success. */
    static synchronized void refreshIfStale(Runnable done) {
        long now = System.currentTimeMillis();
        if (fetching || now - fetchedAt < REFRESH_MS || now - triedAt < RETRY_MS) return;
        String url = Config.NEWS_FEED.trim();
        if (!url.startsWith("https://")) return;
        fetching = true;
        triedAt = now;
        worker.execute(() -> {
            String[] got = null;
            try {
                got = fetch(url);
            } catch (Exception e) {
                AppLog.w("news: " + e.getClass().getSimpleName());
            }
            synchronized (News.class) {
                fetching = false;
                if (got != null && got.length > 0) {
                    titles = got;
                    fetchedAt = System.currentTimeMillis();
                }
            }
            if (got != null && got.length > 0 && done != null) done.run();
        });
    }

    private static String[] fetch(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setConnectTimeout(8000);
            c.setReadTimeout(10000);
            c.setInstanceFollowRedirects(false);
            c.setRequestProperty("User-Agent", "FreeMyTV");
            if (c.getResponseCode() != 200) throw new IOException("HTTP " + c.getResponseCode());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                    if (out.size() > MAX_BYTES) throw new IOException("feed too large");
                }
            }
            return titles(out.toByteArray());
        } finally {
            c.disconnect();
        }
    }

    /** The titles of the feed's items (RSS <item>, Atom <entry>), not the feed's own title. */
    private static String[] titles(byte[] xml) throws Exception {
        XmlPullParser p = Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        p.setInput(new ByteArrayInputStream(xml), null);
        List<String> out = new ArrayList<>();
        boolean inItem = false;
        for (int e = p.getEventType(); e != XmlPullParser.END_DOCUMENT && out.size() < MAX_TITLES; e = p.next()) {
            if (e == XmlPullParser.START_TAG) {
                String name = p.getName();
                if ("item".equals(name) || "entry".equals(name)) {
                    inItem = true;
                } else if (inItem && "title".equals(name)) {
                    String t = p.nextText().replaceAll("\\s+", " ").trim();
                    if (!t.isEmpty()) out.add(t.length() > 200 ? t.substring(0, 200) : t);
                }
            } else if (e == XmlPullParser.END_TAG && ("item".equals(p.getName()) || "entry".equals(p.getName()))) {
                inItem = false;
            }
        }
        return out.toArray(new String[0]);
    }
}
