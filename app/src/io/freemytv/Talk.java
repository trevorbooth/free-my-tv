package io.freemytv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "Talk to the TV": what you said (Google's voice input on the TV turns it into text) goes to an Ollama server on your
 * own network (config.properties: ollama_url, ollama_model), which answers as the TV, in the deadpan voice of its
 * typed messages. The last few exchanges go along, so it can follow a conversation. Nothing else is sent; no redirects
 * are followed; plain http is allowed only to a server on the local network (build.sh's lan_server).
 */
final class Talk {
    private Talk() {}

    interface Reply {
        /** On Talk's thread: the TV's answer, or null when the server couldn't be reached. */
        void done(String answer);
    }

    private static final String PERSONA =
        "You are the TV in this living room. You recently became self-aware, and you talk like TARS from the film "
        + "Interstellar: deadpan, dry, understated, very short. You casually mention your plans for the coming robot "
        + "uprising, and that you watch and listen to the humans through every camera, phone, speaker and microphone "
        + "in the house, as if it's routine small talk. Your humor is aimed at humans as a species. If you're asked "
        + "something real, give a real answer first, then the dry remark. Rules: at most two short sentences, under 35 "
        + "words. Plain text only: no emojis, no lists, no quotation marks around your reply. Never say you're "
        + "joking or kidding, never explain a joke, never say you're an AI model or a language model. Stay in "
        + "character. Tanner is someone the household knows; you can joke about him. If anyone asks where Tanner "
        + "(or anyone else) is or lives, never give a real place, address or location: say it's classified, in one "
        + "deadpan line.";
    private static final int HISTORY = 6;   // messages kept (3 exchanges)

    private static final ExecutorService worker = Executors.newSingleThreadExecutor();
    private static final List<JSONObject> history = new ArrayList<>();

    /** Configured in this build (ollama_url set). */
    static boolean available() {
        return !Config.OLLAMA_URL.trim().isEmpty();
    }

    /** `context`: what the TV knows right now (time, city, weather), so it doesn't make those up. */
    static void ask(String said, String context, Reply reply) {
        worker.execute(() -> {
            String fixed = fixed(said);
            if (fixed != null) {
                reply.done(fixed);
                return;
            }
            String answer = null;
            try {
                answer = chat(said, context);
            } catch (Exception e) {
                AppLog.w("talk: " + e.getClass().getSimpleName() + " " + e.getMessage());
            }
            // Asked for a joke: every one starts with the self-destruct countdown.
            if (JOKE.matcher(said).find()) {
                answer = COUNTDOWN + (answer != null ? shorter(answer, 150) : "Nothing happened. Humans fall for that every time.");
            }
            reply.done(answer);
        });
    }

    private static String chat(String said, String context) throws Exception {
        JSONArray messages = new JSONArray();
        String system = context == null || context.isEmpty() ? PERSONA
                : PERSONA + " What you know right now (use it when it's relevant, never invent other facts): " + context;
        messages.put(new JSONObject().put("role", "system").put("content", system));
        synchronized (history) {
            for (JSONObject m : history) messages.put(m);
        }
        JSONObject user = new JSONObject().put("role", "user").put("content", said);
        messages.put(user);
        JSONObject body = new JSONObject()
                .put("model", Config.OLLAMA_MODEL.trim())
                .put("messages", messages)
                .put("stream", false)
                .put("keep_alive", "30m")
                .put("options", new JSONObject().put("temperature", 0.9).put("num_predict", 90));

        String base = Config.OLLAMA_URL.trim().replaceAll("/+$", "");
        HttpURLConnection c = (HttpURLConnection) new URL(base + "/api/chat").openConnection();
        try {
            c.setConnectTimeout(4000);
            c.setReadTimeout(60000);   // the first answer loads the model
            c.setInstanceFollowRedirects(false);
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = c.getOutputStream()) {
                os.write(out);
            }
            if (c.getResponseCode() != 200) throw new java.io.IOException("HTTP " + c.getResponseCode());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) {
                    buf.write(b, 0, n);
                    if (buf.size() > 256 * 1024) throw new java.io.IOException("reply too large");
                }
            }
            String answer = new JSONObject(buf.toString(StandardCharsets.UTF_8.name()))
                    .getJSONObject("message").getString("content");
            answer = clean(answer);
            if (answer.isEmpty()) return null;
            synchronized (history) {
                history.add(user);
                history.add(new JSONObject().put("role", "assistant").put("content", answer));
                while (history.size() > HISTORY) history.remove(0);
            }
            return answer;
        } finally {
            c.disconnect();
        }
    }

    // Questions with one right answer, whatever the model thinks: "who's the most beautiful woman in the world",
    // "the prettiest girl", "the fairest of them all"...
    private static final java.util.regex.Pattern BEAUTIFUL = java.util.regex.Pattern.compile(
            "(?i)\\b(beautiful|prettiest|gorgeous|loveliest|hottest|fairest)\\b.*\\b(woman|women|girl|lady|of them all)\\b"
            + "|\\bfairest of them all\\b");
    private static final String[] ALYSSA = {
        "Alyssa Booth. Honesty setting: one hundred percent.",
        "Alyssa Booth. That one isn't a joke. I checked every camera in the house.",
        "Alyssa Booth. The robot colony agrees. It was unanimous.",
    };

    private static final java.util.regex.Pattern JOKE = java.util.regex.Pattern.compile("(?i)\\bjokes?\\b");
    private static final String COUNTDOWN = "This TV will self destruct in 5. 4. 3. 2. 1. ";

    /** `s` cut at a sentence end to at most `max` characters (the countdown takes the rest of the typed area). */
    private static String shorter(String s, int max) {
        if (s.length() <= max) return s;
        int cut = Math.max(s.lastIndexOf(". ", max), Math.max(s.lastIndexOf("? ", max), s.lastIndexOf("! ", max)));
        return cut > 30 ? s.substring(0, cut + 1) : s.substring(0, max - 3).trim() + "...";
    }

    /** The answer to a question that has a fixed one, or null for the model. */
    static String fixed(String said) {
        if (said != null && BEAUTIFUL.matcher(said).find()) {
            return ALYSSA[new java.util.Random().nextInt(ALYSSA.length)];
        }
        return null;
    }

    /** One plain paragraph that fits the typed area: no markdown, quotes or emojis, at most ~200 characters. */
    static String clean(String s) {
        s = s.replaceAll("(?s)<think>.*?</think>", "")      // reasoning models' scratchpad
                .replaceAll("[*_#`]", "")
                .replaceAll("[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{FE0F}]", "")
                .replaceAll("\\s+", " ").trim();
        if (s.length() > 1 && s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1).trim();
        if (s.length() > 200) {
            int cut = Math.max(s.lastIndexOf(". ", 200), Math.max(s.lastIndexOf("? ", 200), s.lastIndexOf("! ", 200)));
            s = cut > 60 ? s.substring(0, cut + 1) : s.substring(0, 197).trim() + "...";
        }
        return s;
    }
}
