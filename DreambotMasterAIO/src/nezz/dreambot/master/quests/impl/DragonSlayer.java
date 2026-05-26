package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Dragon Slayer I — the hardest F2P quest, culminates in slaying Elvarg.
 *
 * <b>Requirements:</b> 32 QP, anti-dragon shield, rune/mithril armour, food.
 * <b>Reward:</b> Ability to wear rune platebody, 18,650 Strength/Defence XP, 2 QP.
 *
 * VarPlayer 176: 0=not started, stages 1-7, 8=complete
 * (Shared varp ID with Black Knights' Fortress — both use 176 but different
 *  bit positions. These implementations use getConfig() which reads the VarPlayer
 *  directly; each quest's own stage values don't collide in practice because
 *  only one quest progresses at a time.)
 */
public final class DragonSlayer extends Quest {

    public DragonSlayer() {
        // Stage 0: Talk to Oziach in Edgeville
        steps.put(0, QuestStep.talkTo("Oziach",
            () -> PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) > 0));
        // Stage 1: Get Oracle clue and three map pieces
        steps.put(1, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3013, 3504, 0), // Ice Mountain Oracle
            () -> Inventory.contains("Map piece")
               || PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) >= 3));
        // Stage 3: Obtain anti-dragon shield from Duke Horacio
        steps.put(3, QuestStep.talkTo("Duke Horacio",
            () -> Inventory.contains("Anti-dragon shield")
               || PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) >= 4));
        // Stage 4: Hire Klarense for the ship key / buy boat
        steps.put(4, QuestStep.talkTo("Klarense",
            () -> Inventory.contains("Crandor map")
               || PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) >= 5));
        // Stage 5: Repair the Lady Lumbridge — talk to Ned in Draynor
        steps.put(5, QuestStep.talkTo("Ned",
            () -> PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) >= 6));
        // Stage 6: Sail to Crandor and defeat Elvarg
        steps.put(6, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(2836, 3168, 0), // Elvarg's lair entrance
            () -> PlayerSettings.getConfig(Varbits.QUEST_DRAGON_SLAYER) >= 8));
        steps.put(8, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Dragon Slayer I"; }
    @Override public int stageVarp()     { return Varbits.QUEST_DRAGON_SLAYER; }
    @Override public int completeStage() { return 8; }
}
