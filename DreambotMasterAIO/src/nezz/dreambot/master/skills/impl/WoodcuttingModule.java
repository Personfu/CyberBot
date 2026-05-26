package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * Progressive woodcutter. Cuts tree types as level allows:
 * <pre>
 *   1-14:  Tree
 *  15-29:  Oak (powerchop early, bank later)
 *  30-44:  Willow (Draynor Village waterfront)
 *  45-59:  Maple (Seers' Village, P2P)
 *  60-74:  Yew
 *  75+:    Magic
 * </pre>
 */
public final class WoodcuttingModule extends SkillModule {

    @Override public String name() { return "Woodcutting"; }
    @Override public Skill  skill() { return Skill.WOODCUTTING; }

    @Override public String[] methods() {
        return new String[] { "tree", "oak", "willow", "maple", "yew", "magic" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 15) return "tree";
        if (curr < 30) return "oak";
        if (curr < 45) return "willow";
        if (curr < 60) return "maple";
        if (curr < 75) return "yew";
        return "magic";
    }

    @Override public int tick(String method) {
        String wanted = method == null ? "tree" : method;
        if (Inventory.isFull()) {
            // Powerchop early levels, bank later.
            if ("tree".equals(wanted) || "oak".equals(wanted)) {
                Inventory.dropAll(it -> it != null && it.getName().toLowerCase().contains("logs"));
                return Calculations.random(600, 1000);
            }
            Walking.walk(org.dreambot.api.methods.container.impl.bank.BankLocation.getNearest().getCenter());
            return Calculations.random(1200, 1800);
        }
        String want = mapToObjectName(wanted);
        GameObject tree = GameObjects.closest(g -> g != null && want.equalsIgnoreCase(g.getName())
                && (g.hasAction("Chop down") || g.hasAction("Chop")));
        if (tree != null && tree.interact(tree.hasAction("Chop down") ? "Chop down" : "Chop")) {
            return Calculations.random(2400, 4800);
        }
        return Calculations.random(450, 750);
    }

    private static String mapToObjectName(String method) {
        switch (method) {
            case "oak":    return "Oak";
            case "willow": return "Willow";
            case "maple":  return "Maple tree";
            case "yew":    return "Yew";
            case "magic":  return "Magic tree";
            default:       return "Tree";
        }
    }
}

