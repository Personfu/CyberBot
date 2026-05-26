package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * X Marks the Spot — short introductory quest given by Veos in the
 * Lumbridge pub. Dig up 4 caskets across Lumbridge/Draynor then claim reward.
 *
 * Reward: 200 gp, 1 QP, access to Great Kourend (Zeah), kudos.
 * Uses varbit 8063 for stage tracking.
 */
public final class XMarksTheSpot extends Quest {

    private static final org.dreambot.api.methods.map.Tile VEOS_TILE    = new org.dreambot.api.methods.map.Tile(3228, 3243, 0);
    private static final org.dreambot.api.methods.map.Tile DIG_1        = new org.dreambot.api.methods.map.Tile(3230, 3209, 0); // south of Lumbridge castle
    private static final org.dreambot.api.methods.map.Tile DIG_2        = new org.dreambot.api.methods.map.Tile(3116, 3303, 0); // Draynor Manor area
    private static final org.dreambot.api.methods.map.Tile DIG_3        = new org.dreambot.api.methods.map.Tile(3079, 3249, 0); // Draynor Village
    private static final org.dreambot.api.methods.map.Tile DIG_4        = new org.dreambot.api.methods.map.Tile(3054, 3244, 0); // Mudskipper Point direction

    public XMarksTheSpot() {
        // Stage 0: Talk to Veos in the Lumbridge pub
        steps.put(0, QuestStep.talkTo("Veos",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) > 0));
        // Stage 1: Dig spot 1 (south of Lumbridge castle)
        steps.put(1, QuestStep.walkTo(DIG_1,
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) >= 2));
        // Stage 2: Dig spot 2 (Draynor area)
        steps.put(2, QuestStep.walkTo(DIG_2,
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) >= 3));
        // Stage 3: Dig spot 3
        steps.put(3, QuestStep.walkTo(DIG_3,
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) >= 4));
        // Stage 4: Dig spot 4
        steps.put(4, QuestStep.walkTo(DIG_4,
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) >= 5));
        // Stage 5: Return to Veos
        steps.put(5, QuestStep.talkTo("Veos",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_X_MARKS_THE_SPOT) >= 7));
        steps.put(7, QuestStep.noop("complete"));
    }

    @Override public String name()        { return "X Marks the Spot"; }
    @Override public int stageVarbit()    { return Varbits.QUEST_X_MARKS_THE_SPOT; }
    @Override public int completeStage()  { return 7; }
}
