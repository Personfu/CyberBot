package nezz.dreambot.master.skills;

import nezz.dreambot.master.skills.impl.AttackModule;
import nezz.dreambot.master.skills.impl.CookingModule;
import nezz.dreambot.master.skills.impl.CraftingModule;
import nezz.dreambot.master.skills.impl.DefenseModule;
import nezz.dreambot.master.skills.impl.FiremakingModule;
import nezz.dreambot.master.skills.impl.FishingModule;
import nezz.dreambot.master.skills.impl.FletchingModule;
import nezz.dreambot.master.skills.impl.MagicModule;
import nezz.dreambot.master.skills.impl.MiningModule;
import nezz.dreambot.master.skills.impl.PrayerModule;
import nezz.dreambot.master.skills.impl.RangedModule;
import nezz.dreambot.master.skills.impl.RunecraftingModule;
import nezz.dreambot.master.skills.impl.ScaffoldedSkills;
import nezz.dreambot.master.skills.impl.SmithingModule;
import nezz.dreambot.master.skills.impl.StrengthModule;
import nezz.dreambot.master.skills.impl.ThievingModule;
import nezz.dreambot.master.skills.impl.WoodcuttingModule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of all implemented {@link SkillModule}s. Lookup is name-based to
 * keep the BuildPlan declarative.
 */
public final class SkillRegistry {

    private static final Map<String, SkillModule> BY_NAME = new LinkedHashMap<>();

    static {
        register(new AttackModule());
        register(new StrengthModule());
        register(new DefenseModule());
        register(new RangedModule());
        register(new MagicModule());
        register(new PrayerModule());
        register(new MiningModule());
        register(new WoodcuttingModule());
        register(new FishingModule());
        // Real implementations
        register(new CookingModule());
        register(new FiremakingModule());
        register(new CraftingModule());
        register(new FletchingModule());
        register(new RunecraftingModule());
        register(new SmithingModule());
        register(new ThievingModule());
        // Scaffolded stubs for skills not yet fully implemented
        register(new ScaffoldedSkills.AgilityModule());
        register(new ScaffoldedSkills.HerbloreModule());
        register(new ScaffoldedSkills.HunterModule());
        register(new ScaffoldedSkills.SlayerModule());
        register(new ScaffoldedSkills.FarmingModule());
        register(new ScaffoldedSkills.ConstructionModule());
        register(new ScaffoldedSkills.SailingModule());
    }

    private SkillRegistry() { }

    public static void register(SkillModule s) {
        BY_NAME.put(s.name().toLowerCase(), s);
    }

    public static Optional<SkillModule> byName(String name) {
        return Optional.ofNullable(BY_NAME.get(name == null ? "" : name.toLowerCase()));
    }

    public static Map<String, SkillModule> all() { return BY_NAME; }
}
