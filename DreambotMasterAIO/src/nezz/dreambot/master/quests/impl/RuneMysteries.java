package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Rune Mysteries — unlock Runecrafting skill for the account.
 * Reward: Access to Runecrafting; 875 Magic XP; 850 RC XP; Air talisman.
 *
 * VarPlayer 63: 0=not started, 1-3=stages, 4=complete
 */
public final class RuneMysteries extends Quest {

    public RuneMysteries() {
        // Stage 0: Talk to Duke Horacio on 2nd floor of Lumbridge castle
        steps.put(0, QuestStep.talkTo("Duke Horacio",
            () -> Inventory.contains("Air talisman")
               || PlayerSettings.getConfig(Varbits.QUEST_RUNE_MYSTERIES) > 0));
        // Stage 1: Talk to Sedridor in Wizards' Tower basement
        steps.put(1, QuestStep.talkTo("Sedridor",
            () -> PlayerSettings.getConfig(Varbits.QUEST_RUNE_MYSTERIES) >= 2));
        // Stage 2: Talk to Aubury in Varrock (rune shop)
        steps.put(2, QuestStep.talkTo("Aubury",
            () -> PlayerSettings.getConfig(Varbits.QUEST_RUNE_MYSTERIES) >= 3));
        // Stage 3: Return to Sedridor
        steps.put(3, QuestStep.talkTo("Sedridor",
            () -> PlayerSettings.getConfig(Varbits.QUEST_RUNE_MYSTERIES) >= 4));
        steps.put(4, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Rune Mysteries"; }
    @Override public int stageVarp()     { return Varbits.QUEST_RUNE_MYSTERIES; }
    @Override public int completeStage() { return 4; }
}
