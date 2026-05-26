package nezz.dreambot.master.tasks;

import nezz.dreambot.master.core.BotState;

/**
 * Base task contract for the MasterAIO scheduler.
 *
 * <p>Tasks are checked in priority order each loop. The first task whose
 * {@link #isReady()} returns {@code true} has its {@link #execute()} called and
 * the result is returned to {@code AbstractScript#onLoop()} as the sleep value.</p>
 *
 * <p>Concrete tasks: skill trainers, quest runners, banking, looting, fight
 * controllers, break managers, antiban events, muling, etc.</p>
 */
public abstract class Task {

    /** Lower numbers run first. Antiban events ~10, breaks ~20, eating ~30,
     *  banking ~50, looting ~60, quests ~70, skilling ~80, combat ~90. */
    public abstract int priority();

    /** The BotState this task represents while running. */
    public abstract BotState state();

    /** Whether this task wants to run *right now*. */
    public abstract boolean isReady();

    /** Run a single tick of this task; return the sleep value for onLoop(). */
    public abstract int execute();

    /** Whether the task is permanently finished and should be removed. */
    public boolean isComplete() { return false; }

    /** Human-readable label for paint overlays / debug. */
    public String label() { return getClass().getSimpleName(); }

    /** Called once when the task is added to the scheduler. */
    public void onAdd() { }

    /** Called once when the task is removed (complete or cancelled). */
    public void onRemove() { }
}
