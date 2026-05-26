package nezz.dreambot.master.quests.impl;

import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.quests.Quest;
import nezz.dreambot.master.quests.QuestStep;
import org.dreambot.api.methods.settings.PlayerSettings;

/**
 * Vampyre Slayer — gather garlic, a stake and the hammer; kill Count Draynor
 * in his coffin.
 *
 * <p>Tracked by varp 178. Stages: 0 not started, 1 talked, 2 obtained stake,
 * 3 complete.</p>
 */
public final class VampyreSlayer extends Quest {

    public VampyreSlayer() {
        steps.put(0, QuestStep.talkTo("Morgan", () -> stage() > 0));
        steps.put(1, QuestStep.talkTo("Dr Harlow", () -> stage() >= 2));
        steps.put(2, QuestStep.interactObject("Coffin", "Open",
                () -> stage() >= 3));
        steps.put(3, QuestStep.noop("complete"));
    }

    private int stage() { return PlayerSettings.getConfig(Varbits.QUEST_VAMPYRE_SLAYER); }

    @Override public String name() { return "Vampyre Slayer"; }
    @Override public int stageVarp() { return Varbits.QUEST_VAMPYRE_SLAYER; }
    @Override public int completeStage() { return 3; }
}
