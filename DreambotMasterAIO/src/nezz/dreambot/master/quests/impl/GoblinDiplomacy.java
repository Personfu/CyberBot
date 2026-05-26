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
        steps.put(10, QuestStep.talkTo("General Bentnoze", () -> stage() >= 20));
        steps.put(20, QuestStep.interactObject("Wardrobe", "Open",
                () -> stage() >= 30));   // gather goblin mail
        steps.put(30, QuestStep.useItemOnObject("Orange goblin mail", "General Wartface",
                () -> stage() >= 100));
        steps.put(100, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_GOBLIN_DIPLOMACY); }

    @Override public String name() { return "Goblin Diplomacy"; }
    @Override public int stageVarp() { return Varbits.QUEST_GOBLIN_DIPLOMACY; }
    @Override public int completeStage() { return 100; }
}
