package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.skills.Skill;

/**
 * Stub modules for skills not yet fully implemented.
 * Each is a concrete subclass of {@link SkillModule} so it can be registered
 * and referenced by name from a build plan; the {@code tick()} body is a
 * polite noop until the per-skill logic ships.
 *
 * <p>Skills with dedicated implementations (Attack, Strength, Defense, Ranged,
 * Magic, Prayer, Mining, Woodcutting, Fishing, Cooking, Firemaking, Crafting,
 * Fletching, Runecrafting, Smithing, Thieving, Brutus) are NOT listed here.</p>
 *
 * <p>Remaining stubs: Agility, Herblore, Hunter, Slayer, Farming, Construction,
 * Sailing.</p>
 */
public final class ScaffoldedSkills {

    private ScaffoldedSkills() { }

    public static final class AgilityModule      extends Stub { public AgilityModule()      { super("Agility",      Skill.AGILITY); } }
    public static final class HerbloreModule     extends Stub { public HerbloreModule()     { super("Herblore",     Skill.HERBLORE); } }
    public static final class HunterModule       extends Stub { public HunterModule()       { super("Hunter",       Skill.HUNTER); } }
    public static final class SlayerModule       extends Stub { public SlayerModule()       { super("Slayer",       Skill.SLAYER); } }
    public static final class FarmingModule      extends Stub { public FarmingModule()      { super("Farming",      Skill.FARMING); } }
    public static final class ConstructionModule extends Stub { public ConstructionModule() { super("Construction", Skill.CONSTRUCTION); } }

    /** Sailing is a future-skill placeholder (no DreamBot Skill enum entry yet). */
    public static final class SailingModule extends SkillModule {
        @Override public String name() { return "Sailing"; }
        @Override public Skill  skill() { return Skill.values()[0]; }
        @Override public String[] methods() { return new String[] { "stub" }; }
        @Override public String pickMethod(int c, int t) { return "stub"; }
        @Override public int tick(String method) { return 1000; }
    }

    private static abstract class Stub extends SkillModule {
        private final String name; private final Skill sk;
        Stub(String name, Skill sk) { this.name = name; this.sk = sk; }
        @Override public String name() { return name; }
        @Override public Skill  skill() { return sk; }
        @Override public String[] methods() { return new String[] { "stub" }; }
        @Override public String pickMethod(int curr, int tgt) { return "stub"; }
        @Override public int tick(String method) {
            // Polite noop. Future implementations will dispatch on `method`.
            return 1000;
        }
    }
}
