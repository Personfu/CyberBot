package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Pirate's Treasure — find the pirate treasure hidden on Karamja and Port Sarim.
 * Reward: 450 gp, 1 diamond, 1 gold ring, emerald, 2 QP.
 *
 * VarPlayer 71: 0=not started, 1=have rum, 2=dug chest, 3=complete
 */
public final class PiratesTreasure extends Quest {

    public PiratesTreasure() {
        // Stage 0: Buy rum from Karamja (need 30 gp for boat to Karamja)
        steps.put(0, QuestStep.talkTo("Luthas",
            () -> Inventory.contains("Karamja rum")
               || PlayerSettings.getConfig(Varbits.QUEST_PIRATES_TREASURE) > 0));
        // Stage 1: Bring rum back to Redbeard Frank in Port Sarim
        steps.put(1, QuestStep.talkTo("Redbeard Frank",
            () -> Inventory.contains("Pirate's message")
               || PlayerSettings.getConfig(Varbits.QUEST_PIRATES_TREASURE) >= 2));
        // Stage 2: Dig in Falador park (bring spade + note)
        steps.put(2, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(2996, 3378, 0),
            () -> PlayerSettings.getConfig(Varbits.QUEST_PIRATES_TREASURE) >= 3));
        steps.put(3, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Pirate's Treasure"; }
    @Override public int stageVarp()     { return Varbits.QUEST_PIRATES_TREASURE; }
    @Override public int completeStage() { return 3; }
}
