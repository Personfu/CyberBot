package nezz.dreambot.aio.webhook;

import org.dreambot.api.utilities.Logger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Posts periodic progress updates to a Discord webhook. Fail-soft: network
 * errors are logged but never interrupt the script. Runs each send on a
 * daemon thread so the game loop is never blocked.
 */
public class WebhookManager {

	private final String url;
	private final long intervalMs;
	private long lastSend = 0;
	private boolean enabled;

	public WebhookManager(boolean enabled, String url, int intervalMinutes) {
		this.enabled = enabled && url != null && url.startsWith("http");
		this.url = url;
		this.intervalMs = Math.max(1, intervalMinutes) * 60_000L;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/** Sends an update if the interval has elapsed. */
	public void maybeSend(String title, String body) {
		if (!enabled) return;
		long now = System.currentTimeMillis();
		if (now - lastSend < intervalMs) return;
		lastSend = now;
		send(title, body);
	}

	/** Forces an immediate send (e.g. on start/stop). */
	public void send(String title, String body) {
		if (!enabled) return;
		final String payload = buildPayload(title, body);
		Thread t = new Thread(() -> post(payload), "aio-webhook");
		t.setDaemon(true);
		t.start();
	}

	private String buildPayload(String title, String body) {
		String desc = escape(body);
		String name = escape(title);
		return "{\"embeds\":[{\"title\":\"" + name + "\",\"description\":\"" + desc
				+ "\",\"color\":5793266}]}";
	}

	private void post(String payload) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("User-Agent", "SlugBuilderAIO");
			conn.setConnectTimeout(8000);
			conn.setReadTimeout(8000);
			conn.setDoOutput(true);
			byte[] out = payload.getBytes(StandardCharsets.UTF_8);
			try (OutputStream os = conn.getOutputStream()) {
				os.write(out);
			}
			int code = conn.getResponseCode();
			if (code >= 300) {
				Logger.log("[Webhook] Discord returned HTTP " + code);
			}
		} catch (Throwable t) {
			Logger.log("[Webhook] Send failed: " + t.getMessage());
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private String escape(String s) {
		if (s == null) return "";
		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}
}
