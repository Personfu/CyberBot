package nezz.dreambot.master.tasks;

import nezz.dreambot.master.core.BotState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Priority-ordered task list. The scheduler picks the highest-priority
 * task whose {@link Task#isReady()} returns true and runs one tick.
 *
 * <p>Tasks are stored in a CopyOnWriteArrayList so the GUI thread can iterate
 * for paint without deadlocking the script loop.</p>
 */
public final class TaskScheduler {

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private volatile Task active;
    private volatile BotState lastState = BotState.IDLE;

    public void add(Task t) {
        if (t == null) return;
        tasks.add(t);
        tasks.sort(Comparator.comparingInt(Task::priority));
        t.onAdd();
    }

    public void remove(Task t) {
        if (t == null) return;
        if (tasks.remove(t)) {
            t.onRemove();
        }
    }

    public void clear() {
        for (Task t : tasks) t.onRemove();
        tasks.clear();
    }

    public List<Task> snapshot() {
        return new ArrayList<>(tasks);
    }

    public Task active() { return active; }
    public BotState lastState() { return lastState; }

    /**
     * Execute one tick. Returns the sleep value or a small default if nothing
     * is ready (the AbstractScript onLoop will just spin).
     */
    public int tick() {
        // Prune completed tasks first.
        for (Task t : tasks) {
            if (t.isComplete()) {
                remove(t);
            }
        }
        // Find the first ready task in priority order.
        for (Task t : tasks) {
            if (t.isReady()) {
                active = t;
                lastState = t.state();
                int sleep = t.execute();
                return Math.max(50, sleep);
            }
        }
        active = null;
        lastState = BotState.IDLE;
        return 600;
    }
}
