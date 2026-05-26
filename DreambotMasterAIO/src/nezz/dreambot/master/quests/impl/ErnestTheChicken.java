package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Ernest the Chicken — recover oil can, rubber tube and pressure gauge from
 * Draynor Manor's varied rooms, then return to Professor Oddenstein.
 *
 * <p>Tracked by varp 32. Stages: 0 not started, 1 spoke to Veronica,
 * 2 spoke to Professor, 3 complete.</p>
 */
public final class ErnestTheChicken extends Quest {

    public ErnestTheChicken() {
        steps.put(0, QuestStep.talkTo("Veronica", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("Professor Oddenstein", () -> stage() >= 2));
        steps.put(2, QuestStep.interactObject("Bookcase", "Search",
                () -> stage() >= 3));
        steps.put(3, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_ERNEST_THE_CHICKEN); }

    @Override public String name() { return "Ernest the Chicken"; }
    @Override public int stageVarp() { return Varbits.QUEST_ERNEST_THE_CHICKEN; }
    @Override public int completeStage() { return 3; }
}
