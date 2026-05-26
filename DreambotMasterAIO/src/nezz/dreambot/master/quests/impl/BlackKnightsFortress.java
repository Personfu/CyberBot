package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Black Knights' Fortress — infiltrate the fortress near Ice Mountain and
 * foil the Black Knights' plan by sabotaging their invincibility potion.
 *
 * <b>Requirements:</b> 12 Quest Points, Iron chainbody, Cabbage.
 * VarPlayer 176: 0=not started, 1=in progress, 2=complete
 */
public final class BlackKnightsFortress extends Quest {

    public BlackKnightsFortress() {
        // Stage 0: Talk to Sir Amik Varze in Falador
        steps.put(0, QuestStep.talkTo("Sir Amik Varze",
            () -> PlayerSettings.getConfig(Varbits.QUEST_BLACK_KNIGHTS_FORTRESS) > 0));
        // Stage 1: Infiltrate the fortress (needs iron chainbody + cabbage)
        steps.put(1, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3016, 3515, 0),
            () -> PlayerSettings.getConfig(Varbits.QUEST_BLACK_KNIGHTS_FORTRESS) >= 2));
        steps.put(2, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Black Knights' Fortress"; }
    @Override public int stageVarp()     { return Varbits.QUEST_BLACK_KNIGHTS_FORTRESS; }
    @Override public int completeStage() { return 2; }
}
