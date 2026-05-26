package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Demon Slayer — talk to Gideon Bede, collect 3 silver keys, obtain silverlight,
 * defeat Delrith using the incantation.
 *
 * VarBit 3532 (post-2021 rework): 0=not started, 1-6=in progress, 7=complete. TODO: verify
 */
public final class DemonSlayer extends Quest {

    public DemonSlayer() {
        // Stage 0: Get quest started from Gideon Bede in Varrock church
        steps.put(0, QuestStep.talkTo("Gideon Bede",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_DEMON_SLAYER) > 0));
        // Stage 1: Collect the three silver keys (from different holders)
        steps.put(1, QuestStep.talkTo("Traiborn",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_DEMON_SLAYER) >= 3));
        steps.put(3, QuestStep.talkTo("Gideon Bede",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_DEMON_SLAYER) >= 5));
        // Stage 5: Get Silverlight sword from the catacombs
        steps.put(5, QuestStep.interactObject("Stone chest", "Search",
            () -> Inventory.contains("Silverlight")));
        // Stage 6: Defeat Delrith — walk to Varrock south-east corner
        steps.put(6, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3227, 3370, 0),
            () -> PlayerSettings.getBitValue(Varbits.QUEST_DEMON_SLAYER) >= 7));
        steps.put(7, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Demon Slayer"; }
    @Override public int stageVarbit()   { return Varbits.QUEST_DEMON_SLAYER; }
    @Override public int completeStage() { return 7; }
}
