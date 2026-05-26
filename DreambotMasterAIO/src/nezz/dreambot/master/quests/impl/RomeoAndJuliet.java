package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Romeo and Juliet — F2P quest worth 5 quest points. Run between Varrock
 * (Romeo / Apothecary / Father Lawrence) and Juliet's house multiple times.
 *
 * <p>Tracked by varp 144. Quest is complete when varp == 100.</p>
 */
public final class RomeoAndJuliet extends Quest {

    public RomeoAndJuliet() {
        steps.put(0,  QuestStep.talkTo("Romeo", () -> stage() > 0));
        steps.put(10, QuestStep.talkTo("Juliet", () -> stage() >= 20));
        steps.put(20, QuestStep.talkTo("Romeo", () -> stage() >= 30));
        steps.put(30, QuestStep.talkTo("Father Lawrence", () -> stage() >= 40));
        steps.put(40, QuestStep.talkTo("Apothecary", () -> stage() >= 50));
        steps.put(50, QuestStep.talkTo("Juliet", () -> stage() >= 60));
        steps.put(60, QuestStep.talkTo("Romeo", () -> stage() >= 100));
        steps.put(100, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_ROMEO_JULIET); }

    @Override public String name() { return "Romeo and Juliet"; }
    @Override public int stageVarp() { return Varbits.QUEST_ROMEO_JULIET; }
    @Override public int completeStage() { return 100; }
}
