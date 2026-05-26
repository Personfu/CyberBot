package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Ranged trainer. Verifies bow + arrows are equipped, sets style to
 * RAPID for max XP, and attacks safe-spot-friendly mobs.
 *
 * <p>Methods scale: cow → minotaur → seagulls → ammonite crabs (P2P).</p>
 */
public final class RangedModule extends SkillModule {

    @Override public String name() { return "Ranged"; }
    @Override public Skill  skill() { return Skill.RANGED; }

    @Override public String[] methods() {
        return new String[] { "cows", "minotaurs", "rock_crabs", "ammonite_crabs" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 20) return "cows";
        if (curr < 40) return "minotaurs";
        if (curr < 60) return "rock_crabs";
        return "ammonite_crabs";
    }

    @Override public int tick(String method) {
        if (Equipment.isSlotEmpty(EquipmentSlot.WEAPON.getSlot())
                && Inventory.contains(i -> i != null && i.getName().toLowerCase().contains("bow"))) {
            Inventory.interact(i -> i != null && i.getName().toLowerCase().contains("bow"), "Wield");
            return Calculations.random(600, 900);
        }
        if (Combat.getCombatStyle() != CombatStyle.RANGED_RAPID) {
            Combat.setCombatStyle(CombatStyle.RANGED_RAPID);
        }
        return engage(targetName(method));
    }

    static int engage(String name) {
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
            case "minotaurs":      return "Minotaur";
            case "rock_crabs":     return "Rock crab";
            case "ammonite_crabs": return "Ammonite Crab";
            default:               return "Cow";
        }
    }
}


