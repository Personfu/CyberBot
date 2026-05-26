package nezz.dreambot.master.quests;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.tasks.Task;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Scheduler-friendly wrapper around a {@link Quest}. Each tick:
 * <ol>
 *   <li>read the quest stage via the appropriate varbit/varp,</li>
 *   <li>look up the {@link QuestStep} for that stage,</li>
 *   <li>tick the step,</li>
 *   <li>self-complete when the stage equals {@code quest.completeStage()}.</li>
 * </ol>
 */
public final class QuestTask extends Task {

    private final Quest  quest;
    private final Logger log;
    private boolean done;
    private int lastStage = Integer.MIN_VALUE;

    public QuestTask(Quest quest, Logger log) {
        this.quest = quest;
        this.log = log;
    }

    @Override public int priority() { return 70; }
    @Override public BotState state() { return BotState.QUESTING; }
    @Override public boolean isReady() { return !done; }
    @Override public boolean isComplete() { return done; }
    @Override public String label() { return "quest:" + quest.name() + ":" + lastStage; }

    @Override public int execute() {
        int stage = readStage();
        if (stage != lastStage) {
            log.info("[" + quest.name() + "] stage: " + lastStage + " -> " + stage);
            lastStage = stage;
        }
        if (stage >= quest.completeStage()) {
            log.info("[" + quest.name() + "] complete (stage=" + stage + ")");
            done = true;
            return 200;
        }
        QuestStep step = quest.stepFor(stage);
        if (step != null) {
            step.tick();
        } else {
            log.warn("[" + quest.name() + "] no step for stage " + stage);
        }
        return Calculations.random(450, 750);
    }

    private int readStage() {
        if (quest.stageVarbit() >= 0) return PlayerSettings.getBitValue(quest.stageVarbit());
        if (quest.stageVarp()   >= 0) return PlayerSettings.getConfig(quest.stageVarp());
        return 0;
    }
}
