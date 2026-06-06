package nezz.dreambot.aio.security;

import com.runeguard.client.Runeguard;
import com.runeguard.client.RuneguardSessionException;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Thin, fail-soft wrapper around the RuneGuard Java client
 * ({@code com.runeguard.client.Runeguard}).
 *
 * RuneGuard provides an account-wide RSA signing key that proves runtime
 * session requests came from a client holding the enrolled key. The
 * {@code runeguardjavaclient-0.1.0.jar} must be on the script's classpath.
 *
 * Public API of the underlying client (from the published 0.1.0 jar):
 * <pre>
 *   new Runeguard(String signingKey, Consumer&lt;String&gt; log)
 *   new Runeguard(String signingKey, Duration timeout, Consumer&lt;String&gt; log)
 *   void start(String a, String b, String c) throws RuneguardSessionException
 *   void stop();  void close();
 *   RuneguardSessionException.getCode()
 * </pre>
 *
 * The three {@code start(...)} arguments are not self-describing in the
 * compiled jar. Based on the dashboard ("each script uses its own script
 * token"), we pass them as (scriptToken, scriptName, scriptVersion). If your
 * RuneGuard docs specify a different order, adjust {@link #start()} only.
 */
public class RuneGuardClient implements AutoCloseable {

	private final String signingKey;
	private final String scriptToken;
	private final String scriptName;
	private final String scriptVersion;
	private final Consumer<String> log;

	private Runeguard runeguard;
	private boolean active = false;
	private String lastError = null;
	private String lastErrorCode = null;

	public RuneGuardClient(String signingKey,
						   String scriptToken,
						   String scriptName,
						   String scriptVersion,
						   Consumer<String> log) {
		this.signingKey = signingKey;
		this.scriptToken = scriptToken;
		this.scriptName = scriptName;
		this.scriptVersion = scriptVersion;
		this.log = log != null ? log : s -> {};
	}

	/** @return true if a signing key was supplied and RuneGuard should be used. */
	public boolean isEnabled() {
		return signingKey != null && !signingKey.trim().isEmpty();
	}

	public boolean isActive() {
		return active;
	}

	public String getLastError() {
		return lastError;
	}

	public String getLastErrorCode() {
		return lastErrorCode;
	}

	/**
	 * Initialises and starts a RuneGuard session. Fail-soft: any error is
	 * captured and surfaced via {@link #getLastError()} rather than thrown,
	 * so a misconfigured key never hard-crashes the script.
	 *
	 * @return true if the session started successfully.
	 */
	public boolean start() {
		if (!isEnabled()) {
			log.accept("[RuneGuard] No signing key supplied - running without RuneGuard.");
			return false;
		}
		try {
			runeguard = new Runeguard(signingKey, Duration.ofSeconds(30), log);
			runeguard.start(safe(scriptToken), safe(scriptName), safe(scriptVersion));
			active = true;
			lastError = null;
			lastErrorCode = null;
			log.accept("[RuneGuard] Session started.");
			return true;
		} catch (RuneguardSessionException e) {
			lastError = e.getMessage();
			lastErrorCode = e.getCode();
			log.accept("[RuneGuard] Session error (" + e.getCode() + "): " + e.getMessage());
		} catch (Throwable t) {
			lastError = t.getMessage();
			log.accept("[RuneGuard] Failed to start: " + t.getMessage());
		}
		active = false;
		return false;
	}

	private String safe(String s) {
		return s == null ? "" : s;
	}

	@Override
	public void close() {
		try {
			if (runeguard != null) {
				runeguard.stop();
				runeguard.close();
			}
		} catch (Throwable t) {
			log.accept("[RuneGuard] Error during shutdown: " + t.getMessage());
		} finally {
			active = false;
		}
	}
}
