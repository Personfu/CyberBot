package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * The Knight's Sword — help the squire recover Sir Vyvin's ceremonial sword.
 * Requires: 10 Mining. Reward: 12,725 Smithing XP (boosts Smithing from 1→29).
 *
 * VarPlayer 122: 0=not started, 4=complete
 */
public final class KnightsSword extends Quest {

    public KnightsSword() {
        // Stage 0: Talk to Squire in Falador castle courtyard
        steps.put(0, QuestStep.talkTo("Squire",
            () -> PlayerSettings.getConfig(Varbits.QUEST_KNIGHTS_SWORD) > 0));
        // Stage 1: Talk to Reldo in Varrock library
        steps.put(1, QuestStep.talkTo("Reldo",
            () -> PlayerSettings.getConfig(Varbits.QUEST_KNIGHTS_SWORD) >= 2));
        // Stage 2: Talk to Thurgo south of Port Sarim (bring redberry pie)
        steps.put(2, QuestStep.talkTo("Thurgo",
            () -> Inventory.contains("Imcando hammer")
               || PlayerSettings.getConfig(Varbits.QUEST_KNIGHTS_SWORD) >= 3));
        // Stage 3: Mine 2 blurite ore (Asgarnian Ice Dungeon)
        steps.put(3, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3010, 3149, 0),
            () -> Inventory.count("Blurite ore") >= 2));
        // Stage 4: Return to Thurgo with blurite ore
        steps.put(4, QuestStep.talkTo("Thurgo",
            () -> PlayerSettings.getConfig(Varbits.QUEST_KNIGHTS_SWORD) >= 4));
        steps.put(5, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "The Knight's Sword"; }
    @Override public int stageVarp()     { return Varbits.QUEST_KNIGHTS_SWORD; }
    @Override public int completeStage() { return 4; }
}
