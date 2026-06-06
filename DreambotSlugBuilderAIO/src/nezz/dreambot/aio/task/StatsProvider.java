package nezz.dreambot.aio.task;

/**
 * Implemented by any active module that wants its profit/status surfaced on the
 * paint overlay and in webhook updates.
 */
public interface StatsProvider {
	/** Estimated profit in GP since start (may be 0 for non-profit modules). */
	int getProfit();

	/** Short status line for the overlay. */
	String getStatus();
}
