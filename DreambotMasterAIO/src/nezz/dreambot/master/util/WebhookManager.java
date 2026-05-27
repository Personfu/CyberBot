package nezz.dreambot.master.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends Discord webhook notifications (ban alerts, script events).
 * All methods are fire-and-forget — a failed webhook never crashes the bot.
 */
public final class WebhookManager {

    private static final String WEBHOOK_URL =
        "https://discord.com/api/webhooks/1508963095769256118/Fx-RXRMRU-W4kx4u6g13C0s6P9lrr0sG2Dlk42_DgJ4iJW8y_nUYMS7_Tq4OClm0lPLK";

    private static final String BOT_NAME = "FLLC Master AIO";

    private WebhookManager() {}

    /** Fires a ban-detection alert to your Discord. */
    public static void sendBanNotification(String playerName) {
        send(":warning: **BAN DETECTED** — `" + playerName
            + "` received login response 4. Script stopped.");
    }

    /** Send any freeform message. Silent on all errors. */
    public static void send(String content) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(5_000);

            String body = "{\"username\":\"" + BOT_NAME
                + "\",\"content\":\"" + escape(content) + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 429) {
                // Rate-limited by Discord — silent fail
            }
            conn.disconnect();
        } catch (Throwable ignored) {
            // Never let a webhook failure crash the bot
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
