package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Misthalin Mystery — solve the locked mansion puzzle on the mysterious
 * island. Quest-helper marks the stage varbit at 6557 and a complete value
 * of 70.
 *
 * <p>The actual puzzle has many room-by-room subtasks (read books, find
 * keys, interact with statues). We stub the main beats and leave the
 * step bodies as detail TODOs — full automation requires recording the
 * room layout which varies slightly per encounter.</p>
 */
public final class MisthalinMystery extends Quest {

    public MisthalinMystery() {
        steps.put(0,  QuestStep.talkTo("Abigaile",
                () -> stage() > 0));
        steps.put(1,  QuestStep.interactObject("Boat", "Board",
                () -> stage() >= 5));
        steps.put(5,  QuestStep.interactObject("Bookcase", "Search",
                () -> stage() >= 10));
        steps.put(10, QuestStep.interactObject("Key", "Take",
                () -> stage() >= 20));
        steps.put(20, QuestStep.interactObject("Mahogany door", "Open",
                () -> stage() >= 30));
        steps.put(30, QuestStep.interactObject("Statue", "Search",
                () -> stage() >= 40));
        steps.put(40, QuestStep.interactObject("Crate", "Search",
                () -> stage() >= 50));
        steps.put(50, QuestStep.interactObject("Strange door", "Open",
                () -> stage() >= 60));
        steps.put(60, QuestStep.interactObject("Killer", "Attack",
                () -> stage() >= 70));
        steps.put(70, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getBitValue(Varbits.QUEST_MISTHALIN_MYSTERY); }

    @Override public String name() { return "Misthalin Mystery"; }
    @Override public int stageVarbit() { return Varbits.QUEST_MISTHALIN_MYSTERY; }
    @Override public int completeStage() { return 70; }
}
