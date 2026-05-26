package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

/**
 * Crafting trainer — progressive F2P route to 99.
 *
 * <pre>
 *  1-7   : Leather gloves (need: leather + needle + thread)
 *  7-14  : Leather boots
 * 14-18  : Leather cowl
 * 18-28  : Leather body
 * 28-38  : Leather chaps
 * 38-42  : Hard leather body
 * 42-54  : Gold rings → Gold necklaces → Gold amulets
 * 54-63  : Sapphire → Emerald → Ruby → Diamond rings/amulets
 * 63-99  : Diamond amulets / Dragonhide bodies (P2P); Gold amulets (F2P best)
 * </pre>
 *
 * <p>All crafting is done at the bank inventory screen using needle + thread
 * for leather, or a ring mould + gold bar for jewellery.</p>
 */
public final class CraftingModule extends SkillModule {

    @Override public String name()  { return "Crafting"; }
    @Override public Skill  skill() { return Skill.CRAFTING; }

    @Override public String[] methods() {
        return new String[] {
            "leather_gloves", "leather_boots", "leather_cowl", "leather_body",
            "hard_leather_body", "gold_ring", "gold_necklace", "gold_amulet",
            "sapphire_amulet", "emerald_amulet", "ruby_amulet"
        };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 7)  return "leather_gloves";
        if (curr < 14) return "leather_boots";
        if (curr < 18) return "leather_cowl";
        if (curr < 28) return "leather_body";
        if (curr < 42) return "hard_leather_body";
        if (curr < 50) return "gold_ring";
        if (curr < 54) return "gold_necklace";
        if (curr < 60) return "gold_amulet";
        if (curr < 65) return "sapphire_amulet";
        if (curr < 70) return "emerald_amulet";
        return "ruby_amulet";
    }

    @Override public int tick(String method) {
        String mat1 = mat1For(method);
        String mat2 = mat2For(method);
        String tool = toolFor(method);

        // Ensure materials in inventory
        if (!Inventory.contains(mat1)
                || (mat2 != null && !Inventory.contains(mat2))
                || (tool != null && !Inventory.contains(tool))) {
            return withdrawMats(mat1, mat2, tool);
        }

        // Use mat1 on mat2 (or just click mat1 if no secondary)
        Item a = Inventory.get(mat1);
        if (a == null) return 300;

        if (mat2 != null) {
            Item b = Inventory.get(mat2);
            if (b != null) a.useOn(b);
        } else if (tool != null) {
            Item t = Inventory.get(tool);
            if (t != null) t.useOn(a);
        } else {
            a.interact("Make");
        }

        // Wait for make-x interface and confirm
        Sleep.sleepUntil(() ->
            org.dreambot.api.methods.widget.Widgets.getWidget(270, 0) != null, 2_500);
        try {
            var btn = org.dreambot.api.methods.widget.Widgets.getWidget(270, 14);
            if (btn != null) {
                btn.interact("Make");
                Sleep.sleepUntil(() -> !Inventory.contains(mat1), 30_000);
            }
        } catch (Throwable ignored) { }

        // Deposit
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAll();
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    private int withdrawMats(String mat1, String mat2, String tool) {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAll();
            if (tool != null) Bank.withdraw(tool, 1);
            // Withdraw correct proportions
            if (mat2 != null) {
                // mould + bar: withdraw 14 of each
                Bank.withdraw(mat1, 14);
                Bank.withdraw(mat2, 14);
            } else {
                Bank.withdrawAll(mat1);
            }
            Bank.close();
        }
        return Calculations.random(500, 800);
    }

    // ── data maps ─────────────────────────────────────────────────────────────

    private static String mat1For(String m) {
        switch (m) {
            case "leather_gloves":
            case "leather_boots":
            case "leather_cowl":
            case "leather_body":   return "Leather";
            case "hard_leather_body": return "Hard leather";
            case "gold_ring":
            case "gold_necklace":
            case "gold_amulet":    return "Gold bar";
            case "sapphire_amulet":return "Gold bar";
            case "emerald_amulet": return "Gold bar";
            case "ruby_amulet":    return "Gold bar";
            default:               return "Leather";
        }
    }
    private static String mat2For(String m) {
        switch (m) {
            case "sapphire_amulet": return "Sapphire";
            case "emerald_amulet":  return "Emerald";
            case "ruby_amulet":     return "Ruby";
            default:                return null;
        }
    }
    private static String toolFor(String m) {
        switch (m) {
            case "leather_gloves":
            case "leather_boots":
            case "leather_cowl":
            case "leather_body":
            case "hard_leather_body": return "Needle";
            case "gold_ring":   return "Ring mould";
            case "gold_necklace": return "Necklace mould";
            case "gold_amulet":
            case "sapphire_amulet":
            case "emerald_amulet":
            case "ruby_amulet": return "Amulet mould";
            default: return null;
        }
    }
}
