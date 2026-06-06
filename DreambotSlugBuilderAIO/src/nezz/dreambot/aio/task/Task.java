package nezz.dreambot.aio.task;

/**
 * A single unit of behaviour in the AIO engine. The {@link TaskManager}
 * evaluates tasks in priority order each loop and runs the first one whose
 * {@link #accept()} returns true.
 *
 * Tasks are intentionally small and self-contained so modules (money makers,
 * skills, etc.) can be added without touching the engine.
 */
public abstract class Task {

	private boolean started = false;

	/** Human-readable name shown on the paint overlay. */
	public abstract String name();

	/** @return true if this task currently wants control of the loop. */
	public abstract boolean accept();

	/**
	 * Perform one slice of work.
	 *
	 * @return sleep time in ms before the next loop, or a negative value to
	 *         request the script stop.
	 */
	public abstract int execute();

	/** Called once, lazily, the first time this task is accepted. */
	public void onStart() {
	}

	/** Called when the script stops. */
	public void onStop() {
	}

	final void ensureStarted() {
		if (!started) {
			started = true;
			onStart();
		}
	}
}
