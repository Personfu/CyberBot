package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Thieving trainer — F2P progressive route.
 *
 * <pre>
 *  1-5   : Men/Women — Lumbridge  (pickpocket)
 *  5-25  : Farmers  — Draynor village (pickpocket, 9 gp each)
 * 25-40  : Warriors — Falador (pickpocket, 18 gp each)
 * 40-55  : Guards   — Falador (pickpocket, 30 gp each)
 * 55-99  : Knights of Ardougne (P2P) / Guards (F2P)
 * </pre>
 *
 * <p>Eats food when stunned. Uses the nearest edible item in inventory.</p>
 */
public final class ThievingModule extends SkillModule {

    private static final Tile LUMBRIDGE  = new Tile(3231, 3202, 0);
    private static final Tile DRAYNOR   = new Tile(3078, 3248, 0);
    private static final Tile FALADOR   = new Tile(2956, 3378, 0);

    @Override public String name()  { return "Thieving"; }
    @Override public Skill  skill() { return Skill.THIEVING; }

    @Override public String[] methods() {
        return new String[] { "man", "farmer", "warrior", "guard" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 5)  return "man";
        if (curr < 25) return "farmer";
        if (curr < 40) return "warrior";
        return "guard";
    }

    @Override public int tick(String method) {
        // Eat if stunned / low HP
        if (Combat.getHealthPercent() < 40) {
            var food = Inventory.get(i -> i != null && i.hasAction("Eat"));
            if (food != null) food.interact("Eat");
            return Calculations.random(600, 1000);
        }

        String npcName = npcFor(method);
        Tile area      = areaFor(method);

        if (area.distance(Players.getLocal()) > 20) {
            Walking.walk(area);
            return Calculations.random(1200, 2000);
        }

        NPC target = NPCs.closest(n -> n != null
                && n.getName().equals(npcName)
                && n.hasAction("Pickpocket")
                && !n.isInCombat());
        if (target == null) {
            Walking.walk(area);
            return Calculations.random(600, 1000);
        }

        target.interact("Pickpocket");
        Sleep.sleepUntil(() -> Players.getLocal().isAnimating()
                || Inventory.count("Coins") > 0, 1_500);
        return Calculations.random(400, 700);
    }

    private static String npcFor(String m) {
        switch (m) {
            case "man":     return "Man";
            case "farmer":  return "Farmer";
            case "warrior": return "Warrior";
            case "guard":   return "Guard";
            default:        return "Man";
        }
    }
    private static Tile areaFor(String m) {
        switch (m) {
            case "farmer":  return DRAYNOR;
            case "warrior":
            case "guard":   return FALADOR;
            default:        return LUMBRIDGE;
        }
    }
}
