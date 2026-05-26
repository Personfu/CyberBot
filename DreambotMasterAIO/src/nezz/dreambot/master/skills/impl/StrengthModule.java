package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.skills.Skill;

/**
 * Strength trainer. Sets combat style to AGGRESSIVE; same target progression
 * as {@link AttackModule}. The shared {@link AttackModule#engageMob(String)}
 * picks/attacks the mob — only the style toggle differs.
 */
public final class StrengthModule extends SkillModule {

    @Override public String name() { return "Strength"; }
    @Override public Skill  skill() { return Skill.STRENGTH; }

    @Override public String[] methods() {
        return new String[] { "chickens", "cows", "rock_crabs", "ogresses" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 5)   return "chickens";
        if (curr < 20)  return "cows";
        if (curr < 50)  return "rock_crabs";
        return "ogresses";
    }

    @Override public int tick(String method) {
        if (Combat.getFightMode() != CombatStyle.AGGRESSIVE) {
            Combat.toggleAttackStyle(CombatStyle.AGGRESSIVE);
        }
        return AttackModule.engageMob(targetName(method));
    }

    private static String targetName(String method) {
        switch (method == null ? "" : method) {
            case "cows":       return "Cow";
            case "rock_crabs": return "Rock crab";
            case "ogresses":   return "Ogress Warrior";
            default:           return "Chicken";
        }
    }
}
