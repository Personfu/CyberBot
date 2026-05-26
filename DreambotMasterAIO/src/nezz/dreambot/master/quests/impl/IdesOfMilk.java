package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Ides of Milk (2025, F2P) — Help a dairy farmer deal with Brutus, a rogue
 * giant cow that has taken over the Lumbridge cow field. Completing this quest
 * permanently unlocks the ability to rechallenge Brutus for combat XP and
 * Cowhide drops.
 *
 * <p>VarBit {@link Varbits#QUEST_IDES_OF_MILK} (ID ~13065, estimate):
 * <ul>
 *   <li>0 = not started</li>
 *   <li>1 = spoken to farmer</li>
 *   <li>2 = investigated field</li>
 *   <li>3 = spoken to farmer again</li>
 *   <li>4 = defeated Brutus</li>
 *   <li>5 = complete</li>
 * </ul>
 * </p>
 *
 * <p>Requirements: None. Reward: 1 QP, 500 Attack XP, 500 Strength XP,
 * Brutus rechallenge unlock.</p>
 *
 * <p><b>NOTE:</b> Quest steps and VarBit values are approximate and should be
 * calibrated in-game when the quest is first run.</p>
 */
public final class IdesOfMilk extends Quest {

    /** Dairy Farmer NPC near the cow field, north-east of Lumbridge. */
    private static final Tile FARMER_TILE = new Tile(3254, 3264, 0);
    /** Brutus spawn — centre of the Lumbridge cow field. */
    private static final Tile BRUTUS_TILE = new Tile(3259, 3268, 0);

    public IdesOfMilk() {
        // Stage 0: Talk to the Dairy Farmer to accept the quest
        steps.put(0, QuestStep.talkTo("Dairy farmer",
                () -> stage() > 0));

        // Stage 1: Walk into the cow field to investigate Brutus
        steps.put(1, QuestStep.walkTo(FARMER_TILE,
                () -> stage() >= 2));

        // Stage 2: Return to Dairy Farmer with your findings
        steps.put(2, QuestStep.talkTo("Dairy farmer",
                () -> stage() >= 3));

        // Stage 3: Walk to Brutus' spawn and engage him
        steps.put(3, QuestStep.walkTo(BRUTUS_TILE,
                () -> stage() >= 4));

        // Stage 4: Return to Dairy Farmer after defeating Brutus
        steps.put(4, QuestStep.talkTo("Dairy farmer",
                () -> stage() >= 5));

        steps.put(5, QuestStep.noop("complete"));
    }

    private int stage() {
        return PlayerSettings.getBitValue(Varbits.QUEST_IDES_OF_MILK);
    }

    @Override public String name()       { return "Ides of Milk"; }
    @Override public int stageVarbit()   { return Varbits.QUEST_IDES_OF_MILK; }
    @Override public int completeStage() { return 5; }
}
