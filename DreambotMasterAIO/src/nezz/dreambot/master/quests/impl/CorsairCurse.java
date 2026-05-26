package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * The Corsair Curse — help Cabin Boy Colin and the Corsair captain deal with a
 * curse affecting Port Sarim and Corsair Cove.
 *
 * Reward: 1,000 XP lamp (any skill 30+), 2 QP, 1,500 gp.
 * VarBit 5941: 0=not started, 1-5=stages, 6=complete. TODO: verify in-game
 */
public final class CorsairCurse extends Quest {

    public CorsairCurse() {
        // Stage 0: Talk to Cabin Boy Colin in Port Sarim
        steps.put(0, QuestStep.talkTo("Cabin Boy Colin",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) > 0));
        // Stage 1: Walk to Corsair Cove (south-west of Rimmington)
        steps.put(1, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(2571, 2849, 0),
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) >= 2));
        // Stage 2: Talk to The Captain
        steps.put(2, QuestStep.talkTo("Captain Tock",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) >= 3));
        // Stage 3: Investigate the witch (talk to the Ogress Warrior area)
        steps.put(3, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(2538, 2869, 0),
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) >= 4));
        // Stage 4: Return to Colin
        steps.put(4, QuestStep.talkTo("Cabin Boy Colin",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) >= 5));
        // Stage 5: Final resolution — talk to captain
        steps.put(5, QuestStep.talkTo("Captain Tock",
            () -> PlayerSettings.getBitValue(Varbits.QUEST_CORSAIR_CURSE) >= 6));
        steps.put(6, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "The Corsair Curse"; }
    @Override public int stageVarbit()   { return Varbits.QUEST_CORSAIR_CURSE; }
    @Override public int completeStage() { return 6; }
}
