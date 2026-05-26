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
 * Fletching trainer — progressive bow-making route to 99.
 *
 * <pre>
 *  1-5   : Arrow shafts from logs (fast XP, nearly zero cost)
 *  5-20  : Shortbows (unstrung)
 * 20-40  : Longbows (unstrung)
 * 40-55  : Oak shortbows (strung) — buy bow strings at GE
 * 55-70  : Maple shortbows (P2P) / Oak longbows (F2P)
 * 70-99  : Yew longbows (strung) — best F2P fletching money
 * </pre>
 *
 * <p>Fletching happens in the bank: withdraw logs + knife, make bows,
 * deposit finished items, repeat.</p>
 */
public final class FletchingModule extends SkillModule {

    @Override public String name()  { return "Fletching"; }
    @Override public Skill  skill() { return Skill.FLETCHING; }

    @Override public String[] methods() {
        return new String[] { "arrow_shaft", "shortbow_u", "longbow_u",
                "oak_shortbow", "oak_longbow", "yew_longbow" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 5)  return "arrow_shaft";
        if (curr < 20) return "shortbow_u";
        if (curr < 40) return "longbow_u";
        if (curr < 55) return "oak_shortbow";
        if (curr < 70) return "oak_longbow";
        return "yew_longbow";
    }

    @Override public int tick(String method) {
        String primary   = primaryItemFor(method);
        String secondary = secondaryItemFor(method);
        String product   = productFor(method);
        String action    = actionFor(method);

        // Ensure materials in inventory
        if (!Inventory.contains(primary)) {
            return withdraw(primary, secondary);
        }
        if (secondary != null && !Inventory.contains(secondary)) {
            return withdraw(primary, secondary);
        }

        // Fletch: use primary on secondary (or just use knife on log)
        Item pItem = Inventory.get(primary);
        if (pItem == null) return 300;

        if (secondary != null) {
            Item sItem = Inventory.get(secondary);
            if (sItem != null) pItem.useOn(sItem);
        } else {
            // Knife on log, or just click
            Item knife = Inventory.get("Knife");
            if (knife != null) knife.useOn(pItem);
        }

        // Wait for interface + click "Make All"
        Sleep.sleepUntil(() ->
            org.dreambot.api.methods.widget.Widgets.getWidget(270, 0) != null, 2_500);
        try {
            var btn = org.dreambot.api.methods.widget.Widgets.getWidget(270, 14);
            if (btn != null) {
                btn.interact("Make");
                Sleep.sleepUntil(() -> !Inventory.contains(primary), 30_000);
            }
        } catch (Throwable ignored) { }

        // Bank finished items
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAll();
            if (!Inventory.contains("Knife")) Bank.withdraw("Knife", 1);
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    private int withdraw(String primary, String secondary) {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAll();
            if (!Inventory.contains("Knife")) Bank.withdraw("Knife", 1);
            int slots = secondary != null ? 13 : 27;
            Bank.withdraw(primary, slots);
            if (secondary != null) Bank.withdrawAll(secondary);
            Bank.close();
        }
        return Calculations.random(500, 800);
    }

    private static String primaryItemFor(String m) {
        switch (m) {
            case "arrow_shaft": return "Logs";
            case "shortbow_u":  return "Logs";
            case "longbow_u":   return "Logs";
            case "oak_shortbow":return "Oak shortbow (u)";
            case "oak_longbow": return "Oak longbow (u)";
            case "yew_longbow": return "Yew longbow (u)";
            default:            return "Logs";
        }
    }
    private static String secondaryItemFor(String m) {
        switch (m) {
            case "oak_shortbow":
            case "oak_longbow":
            case "yew_longbow": return "Bow string";
            default:            return null;
        }
    }
    private static String productFor(String m) {
        switch (m) {
            case "arrow_shaft":  return "Arrow shaft";
            case "shortbow_u":   return "Shortbow (u)";
            case "longbow_u":    return "Longbow (u)";
            case "oak_shortbow": return "Oak shortbow";
            case "oak_longbow":  return "Oak longbow";
            case "yew_longbow":  return "Yew longbow";
            default:             return "";
        }
    }
    private static String actionFor(String m) {
        return "Make";
    }
}
