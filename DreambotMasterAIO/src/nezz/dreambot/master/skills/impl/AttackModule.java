package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Attack trainer. Sets combat style to ACCURATE so XP funnels into Attack,
 * then attacks the highest-level NPC the player can sustainably kill given
 * their current combat stats.
 *
 * <p>Designed for F2P: training mob progression is chickens → cows → rats →
 * goblins → flesh crawlers → ogresses; assumes the user has cleared the
 * starter Tutorial Island and probably some Combat instructor XP.</p>
 */
public final class AttackModule extends SkillModule {

    @Override public String name() { return "Attack"; }
    @Override public Skill  skill() { return Skill.ATTACK; }

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
        if (Combat.getCombatStyle() != CombatStyle.ATTACK) {
            Combat.setCombatStyle(CombatStyle.ATTACK);
        }
        return engageMob(targetName(method));
    }

    static int engageMob(String name) {
        if (Players.getLocal().isInCombat() || Players.getLocal().getInteractingCharacter() != null) {
            return Calculations.random(900, 1600);
        }
        NPC mob = NPCs.closest(n -> n != null && name.equalsIgnoreCase(n.getName())
                && !n.isInCombat() && n.getHealthPercent() > 0);
        if (mob != null && mob.interact("Attack")) {
            return Calculations.random(1500, 2400);
        }
        return Calculations.random(700, 1100);
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
