package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Cook's Assistant — easy F2P starter quest. Speak to the Lumbridge Cook,
 * gather an egg, flour and a bucket of milk, return.
 *
 * <p>VarPlayer {@link Varbits#QUEST_COOKS_ASSISTANT} = 29 tracks progress.
 * Stages: 0=not started, 1=talked, 2=complete.</p>
 */
public final class CooksAssistant extends Quest {

    public CooksAssistant() {
        steps.put(0, QuestStep.talkTo("Cook",
                () -> PlayerSettings.getConfig(Varbits.QUEST_COOKS_ASSISTANT) > 0));
        steps.put(1, QuestStep.useItemOnNpc("Bucket of milk", "Cook",
                () -> haveAll() && PlayerSettings.getConfig(Varbits.QUEST_COOKS_ASSISTANT) >= 2));
        steps.put(2, QuestStep.noop("complete"));
    }

    private static boolean haveAll() {
        return Inventory.contains("Bucket of milk")
            && Inventory.contains("Pot of flour")
            && Inventory.contains("Egg");
    }

    @Override public String name() { return "Cook's Assistant"; }
    @Override public int stageVarp() { return Varbits.QUEST_COOKS_ASSISTANT; }
    @Override public int completeStage() { return 2; }
}
