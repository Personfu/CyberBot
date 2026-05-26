package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Prince Ali Rescue — rescue Prince Ali from Lady Keli in Al-Kharid.
 * Reward: 700 gp, 3 QP, access to Al-Kharid without 10 gp toll.
 *
 * VarPlayer 273: 0=not started, 1-5=stages, 6=complete
 */
public final class PrinceAliRescue extends Quest {

    public PrinceAliRescue() {
        // Stage 0: Talk to Chancellor Hassan in Al-Kharid palace
        steps.put(0, QuestStep.talkTo("Chancellor Hassan",
            () -> PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) > 0));
        // Stage 1: Talk to Osman (spy master in Al-Kharid)
        steps.put(1, QuestStep.talkTo("Osman",
            () -> PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) >= 2));
        // Stage 2: Gather disguise materials (blonde wig, skin paste, women's clothes)
        steps.put(2, QuestStep.talkTo("Leela",
            () -> PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) >= 3));
        // Stage 3: Talk to Ned in Draynor for rope
        steps.put(3, QuestStep.talkTo("Ned",
            () -> Inventory.contains("Rope")
               || PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) >= 4));
        // Stage 4: Rescue prince (go to Draynor Village jail)
        steps.put(4, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3097, 3247, 0),
            () -> PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) >= 5));
        // Stage 5: Return to Hassan
        steps.put(5, QuestStep.talkTo("Chancellor Hassan",
            () -> PlayerSettings.getConfig(Varbits.QUEST_PRINCE_ALI_RESCUE) >= 6));
        steps.put(6, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Prince Ali Rescue"; }
    @Override public int stageVarp()     { return Varbits.QUEST_PRINCE_ALI_RESCUE; }
    @Override public int completeStage() { return 6; }
}
