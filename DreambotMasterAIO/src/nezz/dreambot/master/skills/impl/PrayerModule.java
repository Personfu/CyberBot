package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * Prayer trainer — bury bones, or offer at an altar for 3.5x XP.
 *
 * <p>Methods:
 * <ul>
 *   <li>{@code bury} — bury any bone in inventory. Slow but free.</li>
 *   <li>{@code altar} — use bones on POH gilded altar (P2P).</li>
 *   <li>{@code chaos_altar} — use bones on Wilderness Chaos Altar (50% save chance, F2P-accessible but risky).</li>
 * </ul></p>
 */
public final class PrayerModule extends SkillModule {

    @Override public String name() { return "Prayer"; }
    @Override public Skill  skill() { return Skill.PRAYER; }

    @Override public String[] methods() {
        return new String[] { "bury", "altar", "chaos_altar" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        // Default to bury for F2P — altars require either P2P or Wildy risk.
        return "bury";
    }

    @Override public int tick(String method) {
        switch (method == null ? "" : method) {
            case "altar":       return altar("Gilded altar");
            case "chaos_altar": return altar("Chaos Altar");
            default:            return bury();
        }
    }

    private int bury() {
        if (Inventory.interact(it -> it != null && it.getName().toLowerCase().contains("bones"), "Bury")) {
            return Calculations.random(800, 1100);
        }
        return Calculations.random(450, 700);
    }

    private int altar(String name) {
        if (!Inventory.contains(it -> it != null && it.getName().toLowerCase().contains("bones"))) {
            return Calculations.random(500, 700);
        }
        if (!Inventory.isItemSelected()) {
            Inventory.interact(it -> it != null && it.getName().toLowerCase().contains("bones"), "Use");
            return Calculations.random(400, 700);
        }
        GameObject altar = GameObjects.closest(name);
        if (altar != null && altar.interact("Use")) {
            return Calculations.random(1200, 1800);
        }
        return Calculations.random(500, 800);
    }
}
