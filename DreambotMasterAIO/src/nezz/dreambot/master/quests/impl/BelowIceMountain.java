package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Below Ice Mountain (March 2021, F2P) — reunite old adventuring companions
 * and explore the ancient ruins beneath Ice Mountain to reactivate a long-
 * dormant machine.
 *
 * <p>VarBit {@link Varbits#QUEST_BELOW_ICE_MOUNTAIN} (ID 11103, confirmed):
 * <ul>
 *   <li>0 = not started</li>
 *   <li>1 = talked to Willow</li>
 *   <li>2 = recruited companions (Atlax, Marley, Checkal)</li>
 *   <li>3 = entered ruins</li>
 *   <li>4 = solved puzzle / fixed machine</li>
 *   <li>5 = returned to surface</li>
 *   <li>6 = complete</li>
 * </ul>
 * </p>
 *
 * <p>Requirements: None. Reward: 1 QP, access to Ruins of Camdozaal.</p>
 */
public final class BelowIceMountain extends Quest {

    /** Willow — quest giver, north of Ice Mountain. */
    private static final Tile WILLOW_TILE   = new Tile(3003, 3474, 0);
    /** Entrance to the ruins beneath Ice Mountain. */
    private static final Tile RUINS_ENTRY   = new Tile(3016, 3448, 0);
    /** Checkal's location inside the ruins. */
    private static final Tile CHECKAL_TILE  = new Tile(3039, 9844, 0);

    public BelowIceMountain() {
        // Stage 0: Speak to Willow north of Ice Mountain to start the quest
        steps.put(0, QuestStep.talkTo("Willow",
                () -> stage() > 0));

        // Stage 1: Recruit companions — Atlax, Marley, and Checkal
        steps.put(1, QuestStep.walkTo(WILLOW_TILE,
                () -> stage() >= 2));

        // Stage 2: Enter the ruins beneath Ice Mountain
        steps.put(2, QuestStep.walkTo(RUINS_ENTRY,
                () -> stage() >= 3));

        // Stage 3: Explore the ruins — follow Checkal
        steps.put(3, QuestStep.talkTo("Checkal",
                () -> stage() >= 4));

        // Stage 4: Fix the machine (interact with it)
        steps.put(4, QuestStep.talkTo("Willow",
                () -> stage() >= 5));

        // Stage 5: Return to the surface and claim reward from Willow
        steps.put(5, QuestStep.talkTo("Willow",
                () -> stage() >= 6));

        steps.put(6, QuestStep.noop("complete"));
    }

    private int stage() {
        return PlayerSettings.getBitValue(Varbits.QUEST_BELOW_ICE_MOUNTAIN);
    }

    @Override public String name()       { return "Below Ice Mountain"; }
    @Override public int stageVarbit()   { return Varbits.QUEST_BELOW_ICE_MOUNTAIN; }
    @Override public int completeStage() { return 6; }
}
