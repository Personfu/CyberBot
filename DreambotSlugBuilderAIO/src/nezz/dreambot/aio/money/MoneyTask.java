package nezz.dreambot.aio.money;

import nezz.dreambot.aio.task.Task;

/**
 * Base for money-making tasks. A money task always accepts (the engine only
 * registers the single selected module) and exposes profit tracking used by
 * the paint overlay and webhooks.
 */
public abstract class MoneyTask extends Task {

	@Override
	public boolean accept() {
		return true;
	}

	/** Live estimated profit in GP since the task started. */
	public abstract int getProfit();

	/** Short status line for the paint overlay. */
	public abstract String getStatus();
}
