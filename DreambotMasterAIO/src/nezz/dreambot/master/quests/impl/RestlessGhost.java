package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * The Restless Ghost — gather a ghostspeak amulet from Father Urhney and
 * return a missing skull to the ghost in Lumbridge graveyard.
 *
 * <p>Tracked by varp 107. Complete when stage == 5.</p>
 */
public final class RestlessGhost extends Quest {

    public RestlessGhost() {
        steps.put(0, QuestStep.talkTo("Father Aereck", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("Father Urhney", () -> stage() >= 2));
        steps.put(2, QuestStep.talkTo("Restless Ghost", () -> stage() >= 3));
        steps.put(3, QuestStep.interactObject("Coffin", "Search",
                () -> stage() >= 4));
        steps.put(4, QuestStep.useItemOnNpc("Ghost's skull", "Restless ghost",
                () -> stage() >= 5));
        steps.put(5, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_RESTLESS_GHOST); }

    @Override public String name() { return "Restless Ghost"; }
    @Override public int stageVarp() { return Varbits.QUEST_RESTLESS_GHOST; }
    @Override public int completeStage() { return 5; }
}
