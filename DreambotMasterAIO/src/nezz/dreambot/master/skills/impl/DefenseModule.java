package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.skills.Skill;

/**
 * Defense trainer. Sets combat style to DEFENSIVE; reuses
 * {@link AttackModule#engageMob(String)} for target selection.
 */
public final class DefenseModule extends SkillModule {

    @Override public String name() { return "Defense"; }
    @Override public Skill  skill() { return Skill.DEFENCE; }

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
        if (Combat.getFightMode() != CombatStyle.DEFENSIVE) {
            Combat.toggleAttackStyle(CombatStyle.DEFENSIVE);
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
