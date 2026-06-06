package nezz.dreambot.aio.task;

import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the ordered list of {@link Task}s and selects the active one each loop.
 * Tasks earlier in the list have higher priority.
 */
public class TaskManager {

	private final List<Task> tasks = new ArrayList<>();
	private Task current;

	public TaskManager add(Task task) {
		tasks.add(task);
		return this;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public Task getCurrent() {
		return current;
	}

	/**
	 * Evaluates tasks in priority order and executes the first that accepts.
	 *
	 * @return the sleep value returned by the executed task, or a small idle
	 *         sleep if nothing accepted.
	 */
	public int loop() {
		for (Task task : tasks) {
			boolean accepted;
			try {
				accepted = task.accept();
			} catch (Throwable t) {
				Logger.log("[TaskManager] accept() failed for " + task.name() + ": " + t.getMessage());
				continue;
			}
			if (accepted) {
				current = task;
				task.ensureStarted();
				try {
					return task.execute();
				} catch (Throwable t) {
					Logger.log("[TaskManager] execute() failed for " + task.name() + ": " + t.getMessage());
					return 600;
				}
			}
		}
		current = null;
		return 600;
	}

	public void stopAll() {
		for (Task task : tasks) {
			try {
				task.onStop();
			} catch (Throwable t) {
				Logger.log("[TaskManager] onStop() failed for " + task.name() + ": " + t.getMessage());
			}
		}
	}
}
