package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Sheep Shearer — gather 20 balls of wool for Fred the Farmer.
 *
 * <p>Tracked by varbit {@link Varbits#QUEST_SHEEP_SHEARER}. Stages are
 * 0=not started, 1=talked, 21=complete (20 wool + return).</p>
 */
public final class SheepShearer extends Quest {

    public SheepShearer() {
        steps.put(0, QuestStep.talkTo("Fred the Farmer",
                () -> PlayerSettings.getBitValue(Varbits.QUEST_SHEEP_SHEARER) > 0));
        // Stage 1-20: shear sheep then spin wool on a spinning wheel. The
        // simplified loop below shears a sheep when wool is needed; the
        // spinning step is delegated to the Crafting skill module when run
        // as part of a build plan. For pure questing without leveling, set
        // a SkillTask phase to spin wool before this one.
        steps.put(1, QuestStep.interactObject("Sheep", "Shear",
                () -> Inventory.count("Ball of wool") >= 20));
        steps.put(20, QuestStep.talkTo("Fred the Farmer",
                () -> PlayerSettings.getBitValue(Varbits.QUEST_SHEEP_SHEARER) >= 21));
        steps.put(21, QuestStep.noop("complete"));
    }

    @Override public String name() { return "Sheep Shearer"; }
    @Override public int stageVarbit() { return Varbits.QUEST_SHEEP_SHEARER; }
    @Override public int completeStage() { return 21; }
}
