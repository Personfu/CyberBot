package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.skills.Skill;

/**
 * Stub modules for the 14 skills not yet implemented to "priority" depth.
 * Each is a concrete subclass of {@link SkillModule} so it can be registered
 * and referenced by name from a build plan; the {@code tick()} body is a
 * polite noop until the per-skill logic ships.
 *
 * <p>Listed: Agility, Cooking, Construction, Crafting, Farming, Firemaking,
 * Fletching, Herblore, Hunter, Runecrafting, Slayer, Smithing, Thieving,
 * Sailing.</p>
 */
public final class ScaffoldedSkills {

    private ScaffoldedSkills() { }

    public static final class AgilityModule       extends Stub { public AgilityModule()       { super("Agility",      Skill.AGILITY); } }
    public static final class CookingModule       extends Stub { public CookingModule()       { super("Cooking",      Skill.COOKING); } }
    public static final class FiremakingModule    extends Stub { public FiremakingModule()    { super("Firemaking",   Skill.FIREMAKING); } }
    public static final class CraftingModule      extends Stub { public CraftingModule()      { super("Crafting",     Skill.CRAFTING); } }
    public static final class FletchingModule     extends Stub { public FletchingModule()     { super("Fletching",    Skill.FLETCHING); } }
    public static final class HerbloreModule      extends Stub { public HerbloreModule()      { super("Herblore",     Skill.HERBLORE); } }
    public static final class HunterModule        extends Stub { public HunterModule()        { super("Hunter",       Skill.HUNTER); } }
    public static final class RunecraftingModule  extends Stub { public RunecraftingModule()  { super("Runecrafting", Skill.RUNECRAFTING); } }
    public static final class SlayerModule        extends Stub { public SlayerModule()        { super("Slayer",       Skill.SLAYER); } }
    public static final class SmithingModule      extends Stub { public SmithingModule()      { super("Smithing",     Skill.SMITHING); } }
    public static final class ThievingModule      extends Stub { public ThievingModule()      { super("Thieving",     Skill.THIEVING); } }
    public static final class FarmingModule       extends Stub { public FarmingModule()       { super("Farming",      Skill.FARMING); } }
    public static final class ConstructionModule  extends Stub { public ConstructionModule()  { super("Construction", Skill.CONSTRUCTION); } }

    /** Sailing is a future-skill placeholder (per Dreambot's "Sailing" forum). */
    public static final class SailingModule extends SkillModule {
        @Override public String name() { return "Sailing"; }
        @Override public Skill  skill() { return Skill.values()[0]; }   // Sailing has no DreamBot enum entry yet
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
