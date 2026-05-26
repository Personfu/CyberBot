package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Doric's Quest — talk to Doric, gather 6 clay + 4 copper ore + 2 iron ore.
 *
 * VarPlayer 31: 0=not started, 1=collecting, 2=complete
 */
public final class DoricQuest extends Quest {

    public DoricQuest() {
        steps.put(0, QuestStep.talkTo("Doric",
            () -> PlayerSettings.getConfig(Varbits.QUEST_DORICS_QUEST) > 0));
        steps.put(1, QuestStep.talkTo("Doric",
            () -> PlayerSettings.getConfig(Varbits.QUEST_DORICS_QUEST) >= 2));
        steps.put(2, QuestStep.noop("complete"));
    }

    @Override public String name()         { return "Doric's Quest"; }
    @Override public int stageVarp()       { return Varbits.QUEST_DORICS_QUEST; }
    @Override public int completeStage()   { return 2; }
}
