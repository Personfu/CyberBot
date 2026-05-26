package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Shield of Arrav — join either Phoenix Gang or Black Arm Gang and retrieve
 * both halves of the Shield of Arrav. Requires a second player for one half
 * (the two gangs have different halves); this stub sends the player through
 * the Phoenix Gang path.
 *
 * VarPlayer 73: 0=not started, 1-8=stages, 9=complete
 */
public final class ShieldOfArrav extends Quest {

    public ShieldOfArrav() {
        // Stage 0: Talk to Reldo in Varrock library
        steps.put(0, QuestStep.talkTo("Reldo",
            () -> PlayerSettings.getConfig(Varbits.QUEST_SHIELD_OF_ARRAV) > 0));
        // Stage 1: Join Phoenix Gang (talk to Straven in Phoenix Gang hideout)
        steps.put(1, QuestStep.walkTo(
            new org.dreambot.api.methods.map.Tile(3244, 3383, 0),
            () -> PlayerSettings.getConfig(Varbits.QUEST_SHIELD_OF_ARRAV) >= 3));
        // Stage 3: Get shield half from chest in Phoenix Gang den
        steps.put(3, QuestStep.interactObject("Chest", "Search",
            () -> Inventory.contains("Shield of Arrav (right half)")
               || PlayerSettings.getConfig(Varbits.QUEST_SHIELD_OF_ARRAV) >= 5));
        // Stage 5: Trade for Black Arm Gang half (needs co-op — stub)
        steps.put(5, QuestStep.talkTo("Straven",
            () -> PlayerSettings.getConfig(Varbits.QUEST_SHIELD_OF_ARRAV) >= 7));
        // Stage 7: Talk to King Roald in Varrock castle
        steps.put(7, QuestStep.talkTo("King Roald",
            () -> PlayerSettings.getConfig(Varbits.QUEST_SHIELD_OF_ARRAV) >= 9));
        steps.put(9, QuestStep.noop("complete"));
    }

    @Override public String name()       { return "Shield of Arrav"; }
    @Override public int stageVarp()     { return Varbits.QUEST_SHIELD_OF_ARRAV; }
    @Override public int completeStage() { return 9; }
}
