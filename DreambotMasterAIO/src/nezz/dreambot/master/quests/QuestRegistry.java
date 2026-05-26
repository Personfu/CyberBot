package nezz.dreambot.master.quests;

import nezz.dreambot.master.quests.impl.BlackKnightsFortress;
import nezz.dreambot.master.quests.impl.BelowIceMountain;
import nezz.dreambot.master.quests.impl.CooksAssistant;
import nezz.dreambot.master.quests.impl.CorsairCurse;
import nezz.dreambot.master.quests.impl.DemonSlayer;
import nezz.dreambot.master.quests.impl.DoricQuest;
import nezz.dreambot.master.quests.impl.DragonSlayer;
import nezz.dreambot.master.quests.impl.ErnestTheChicken;
import nezz.dreambot.master.quests.impl.GoblinDiplomacy;
import nezz.dreambot.master.quests.impl.IdesOfMilk;
import nezz.dreambot.master.quests.impl.ImpCatcher;
import nezz.dreambot.master.quests.impl.KnightsSword;
import nezz.dreambot.master.quests.impl.MisthalinMystery;
import nezz.dreambot.master.quests.impl.PiratesTreasure;
import nezz.dreambot.master.quests.impl.PrinceAliRescue;
import nezz.dreambot.master.quests.impl.RestlessGhost;
import nezz.dreambot.master.quests.impl.RomeoAndJuliet;
import nezz.dreambot.master.quests.impl.RuneMysteries;
import nezz.dreambot.master.quests.impl.SheepShearer;
import nezz.dreambot.master.quests.impl.ShieldOfArrav;
import nezz.dreambot.master.quests.impl.VampyreSlayer;
import nezz.dreambot.master.quests.impl.WitchesPotion;
import nezz.dreambot.master.quests.impl.XMarksTheSpot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry of implemented quests. Adding a quest means:
 * <ol>
 *   <li>create a subclass of {@link Quest} in {@code quests.impl},</li>
 *   <li>register it here in the static initializer.</li>
 * </ol>
 */
public final class QuestRegistry {

    private static final Map<String, Quest> BY_NAME = new LinkedHashMap<>();

    static {
        register(new CooksAssistant());
        register(new SheepShearer());
        register(new RomeoAndJuliet());
        register(new RestlessGhost());
        register(new GoblinDiplomacy());
        register(new ErnestTheChicken());
        register(new VampyreSlayer());
        register(new ImpCatcher());
        register(new WitchesPotion());
        register(new MisthalinMystery());
        // Additional F2P quests
        register(new DoricQuest());
        register(new RuneMysteries());
        register(new PiratesTreasure());
        register(new XMarksTheSpot());
        register(new CorsairCurse());
        register(new PrinceAliRescue());
        register(new KnightsSword());
        register(new BlackKnightsFortress());
        register(new DemonSlayer());
        register(new ShieldOfArrav());
        register(new DragonSlayer());
        register(new IdesOfMilk());
        register(new BelowIceMountain());
    }

    private QuestRegistry() { }

    public static void register(Quest q) {
        BY_NAME.put(q.name().toLowerCase(), q);
    }

    public static Optional<Quest> byName(String name) {
        return Optional.ofNullable(BY_NAME.get(name == null ? "" : name.toLowerCase()));
    }

    public static Map<String, Quest> all() { return BY_NAME; }
}
