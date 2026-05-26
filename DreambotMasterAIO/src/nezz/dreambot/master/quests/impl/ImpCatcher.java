package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Imp Catcher — gather four colored beads and return to Wizard Mizgog.
 * Easy quest, often the first F2P magic-XP source.
 *
 * <p>Tracked by varp 160. Stages: 0 not started, 1 talked, 2 complete.</p>
 */
public final class ImpCatcher extends Quest {

    public ImpCatcher() {
        steps.put(0, QuestStep.talkTo("Wizard Mizgog", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("Wizard Mizgog",
                () -> stage() >= 2 || haveBeads()));
        steps.put(2, QuestStep.noop("complete"));
    }

    private static boolean haveBeads() {
        return Inventory.contains("Red bead") && Inventory.contains("Yellow bead")
            && Inventory.contains("Black bead") && Inventory.contains("White bead");
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_IMP_CATCHER); }

    @Override public String name() { return "Imp Catcher"; }
    @Override public int stageVarp() { return Varbits.QUEST_IMP_CATCHER; }
    @Override public int completeStage() { return 2; }
}
