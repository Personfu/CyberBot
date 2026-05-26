package nezz.dreambot.master.quests;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for a quest implementation. Subclasses populate {@link #steps}
 * keyed by the varbit/varp stage value, and override {@link #stageVarbit()}
 * (varbit-based) or {@link #stageVarp()} (varp-based) depending on which the
 * quest uses for fine-grained stage tracking.
 *
 * <p>Quest-Helper's {@code QuestVarbits} / {@code QuestVarPlayer} maps each
 * quest to its stage tracker. The MasterAIO QuestRegistry registers each
 * concrete Quest by name; the BuildPlan references them by name.</p>
 */
public abstract class Quest {

    public abstract String name();

    /** Override and return -1 if this quest is varp-tracked instead. */
    public int stageVarbit() { return -1; }
    public int stageVarp() { return -1; }

    /** Final stage value when the quest is complete. */
    public abstract int completeStage();

    /** Quest steps keyed by stage value. */
    protected final Map<Integer, QuestStep> steps = new HashMap<>();

    /** Subclasses populate {@link #steps} in their constructor. */
    public final Map<Integer, QuestStep> steps() { return steps; }

    public QuestStep stepFor(int stage) {
        // First try exact match; otherwise pick the closest lower-bound stage
        // so quests with sparse stage tables still progress.
        if (steps.containsKey(stage)) return steps.get(stage);
        int best = Integer.MIN_VALUE;
        QuestStep bestStep = null;
        for (Map.Entry<Integer, QuestStep> e : steps.entrySet()) {
            if (e.getKey() <= stage && e.getKey() > best) {
                best = e.getKey();
                bestStep = e.getValue();
            }
        }
        return bestStep;
    }
}
