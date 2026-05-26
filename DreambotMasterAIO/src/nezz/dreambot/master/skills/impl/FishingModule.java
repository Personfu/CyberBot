package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Progressive fishing trainer:
 * <pre>
 *   1-19:  Shrimp/Anchovies — small fishing net
 *  20-39:  Trout/Salmon — fly rod + feather
 *  40-61:  Lobster — lobster pot
 *  62-75:  Swordfish/Tuna — harpoon
 *  76+:    Karambwan / Shark
 * </pre>
 */
public final class FishingModule extends SkillModule {

    @Override public String name() { return "Fishing"; }
    @Override public Skill  skill() { return Skill.FISHING; }

    @Override public String[] methods() {
        return new String[] { "shrimp", "trout", "lobster", "swordfish", "shark" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 20) return "shrimp";
        if (curr < 40) return "trout";
        if (curr < 62) return "lobster";
        if (curr < 76) return "swordfish";
        return "shark";
    }

    @Override public int tick(String method) {
        if (Inventory.isFull()) {
            // Powerfish low levels; bank lobsters+.
            if ("shrimp".equals(method) || "trout".equals(method)) {
                Inventory.dropAll(it -> it != null && it.getName().toLowerCase().contains("raw "));
                return Calculations.random(600, 1000);
            }
            Walking.walkToBank();
            return Calculations.random(1200, 1800);
        }
        String action = actionFor(method);
        NPC spot = NPCs.closest(n -> n != null && "Fishing spot".equals(n.getName()) && n.hasAction(action));
        if (spot != null && spot.interact(action)) {
            return Calculations.random(2400, 5200);
        }
        return Calculations.random(450, 850);
    }

    private static String actionFor(String method) {
        switch (method == null ? "" : method) {
            case "trout":     return "Lure";
            case "lobster":   return "Cage";
            case "swordfish": return "Harpoon";
            case "shark":     return "Harpoon";
            default:          return "Net";
        }
    }
}
