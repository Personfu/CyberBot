package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Goblin Diplomacy — dye goblin mail orange and present to the goblin
 * generals in the Goblin Village. Quest is varp-tracked at index
 * {@link Varbits#QUEST_GOBLIN_DIPLOMACY} (130) with complete value 100.
 */
public final class GoblinDiplomacy extends Quest {

    public GoblinDiplomacy() {
        steps.put(0,  QuestStep.talkTo("General Wartface", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("General Bentnoze", () -> stage() >= 2));
        steps.put(2, QuestStep.interactObject("Wardrobe", "Open",
                () -> stage() >= 3));   // gather goblin mail
        steps.put(3, QuestStep.useItemOnObject("Orange goblin mail", "General Wartface",
                () -> stage() >= 5));
        steps.put(5, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getBitValue(Varbits.QUEST_GOBLIN_DIPLOMACY); }

    @Override public String name() { return "Goblin Diplomacy"; }
    @Override public int stageVarbit() { return Varbits.QUEST_GOBLIN_DIPLOMACY; }
    @Override public int completeStage() { return 5; }
}
