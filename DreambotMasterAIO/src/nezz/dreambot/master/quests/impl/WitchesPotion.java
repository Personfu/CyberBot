package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Witch's Potion — collect rat tail, eye of newt, onion, burnt meat; drink
 * Hetty's potion.
 *
 * <p>Tracked by varp 67. Complete at stage 3.</p>
 */
public final class WitchesPotion extends Quest {

    public WitchesPotion() {
        steps.put(0, QuestStep.talkTo("Hetty", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("Hetty", () -> stage() >= 2));
        steps.put(2, QuestStep.interactObject("Cauldron of water", "Drink-from",
                () -> stage() >= 3));
        steps.put(3, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_WITCHES_POTION); }

    @Override public String name() { return "Witch's Potion"; }
    @Override public int stageVarp() { return Varbits.QUEST_WITCHES_POTION; }
    @Override public int completeStage() { return 3; }
}
