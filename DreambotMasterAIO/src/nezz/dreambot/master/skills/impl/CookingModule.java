package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

/**
 * Cooking trainer — full progressive F2P route to 99.
 *
 * <pre>
 *  1-25  : Shrimp / Anchovies  (Lumbridge kitchen range)
 * 25-45  : Trout               (range for -5% burn bonus)
 * 45-68  : Lobster             (stop burning at 74 w/ gauntlets, 68 on range)
 * 68-84  : Swordfish           (stop burning at 86 on range)
 * 84-99  : Sharks              (stop burning at 99 w/ gauntlets)
 * </pre>
 *
 * <h3>Processing loop integration</h3>
 * The FishingModule drops raw fish into the bank. CookingModule withdraws,
 * cooks them all on the nearest range, then re-banks the cooked fish for GE.
 */
public final class CookingModule extends SkillModule {

    private static final Tile LUM_RANGE  = new Tile(3210, 3215, 0);
    private static final Tile LUM_BANK   = new Tile(3208, 3220, 0);
    // Widget for cooking interface: parent=270, "Cook All" child varies by DB version
    private static final int COOK_WIDGET_PARENT = 270;
    private static final int COOK_WIDGET_BUTTON = 14; // "Cook" option (all)

    @Override public String name()  { return "Cooking"; }
    @Override public Skill  skill() { return Skill.COOKING; }

    @Override public String[] methods() {
        return new String[] { "shrimp", "trout", "lobster", "swordfish", "shark" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 25)  return "shrimp";
        if (curr < 45)  return "trout";
        if (curr < 68)  return "lobster";
        if (curr < 84)  return "swordfish";
        return "shark";
    }

    @Override public int tick(String method) {
        String raw = rawFor(method);
        if (raw == null) return 600;

        // 1. If we have raw fish, cook them
        if (Inventory.contains(raw)) {
            return cookInventory(raw);
        }

        // 2. Withdraw raw fish from bank
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 4_000);
        }
        if (Bank.isOpen()) {
            if (Bank.count(raw) == 0) {
                // Out of this raw fish — signal done (SkillTask checks level)
                Bank.close();
                return 600;
            }
            Bank.withdrawAll(raw);
            Sleep.sleepUntil(() -> Inventory.contains(raw), 2_000);
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private int cookInventory(String raw) {
        // Walk to range
        if (LUM_RANGE.distance(Players.getLocal()) > 8) {
            Walking.walk(LUM_RANGE);
            return Calculations.random(1200, 2000);
        }

        GameObject range = GameObjects.closest(g -> g != null
                && (g.getName().equals("Range") || g.getName().contains("Cooking range")
                    || g.getName().contains("Fire"))
                && g.hasAction("Cook"));
        if (range == null) {
            Walking.walk(LUM_RANGE);
            return Calculations.random(800, 1200);
        }

        Item fish = Inventory.get(raw);
        if (fish == null) return 300;

        fish.useOn(range);
        Sleep.sleepUntil(() ->
            org.dreambot.api.methods.widget.Widgets.get(COOK_WIDGET_PARENT, COOK_WIDGET_BUTTON) != null,
            3_000);

        try {
            var cookBtn = org.dreambot.api.methods.widget.Widgets
                    .get(COOK_WIDGET_PARENT, COOK_WIDGET_BUTTON);
            if (cookBtn != null) {
                cookBtn.interact("Cook");
                Sleep.sleepUntil(() -> !Inventory.contains(raw), 60_000);
            }
        } catch (Throwable ignored) { }

        // Bank cooked fish
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAllItems();
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    private static String rawFor(String method) {
        switch (method == null ? "" : method) {
            case "shrimp":    return "Raw shrimps";
            case "trout":     return "Raw trout";
            case "lobster":   return "Raw lobster";
            case "swordfish": return "Raw swordfish";
            case "shark":     return "Raw shark";
            default:          return null;
        }
    }
}
