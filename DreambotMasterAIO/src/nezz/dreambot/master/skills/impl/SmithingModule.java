package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.ge.GESellTask;
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

/**
 * Smithing trainer — smelt ores into bars, then smith bars into items.
 * Queues finished products to the GE for sale.
 *
 * <pre>
 *  1-15  : Bronze bars (buy copper + tin from GE or mine them)
 * 15-30  : Iron bars   (50% chance of failure without ring of forging)
 * 30-40  : Steel bars  → sell at GE for profit
 * 40-50  : Gold bars   (best XP, use Edgeville furnace)
 * 50-70  : Mithril bars → sell
 * 70-99  : Adamant/Rune bars → sell
 * </pre>
 *
 * <p>After smelting, leftover bars are smithed into the highest-tier item
 * for the bar type and queued at GE (e.g. steel bars → steel platebodies).</p>
 */
public final class SmithingModule extends SkillModule {

    private static final Tile LUMBRIDGE_FURNACE = new Tile(3227, 3257, 0);
    private static final Tile EDGEVILLE_FURNACE = new Tile(3109, 3499, 0);
    private static final Tile VARROCK_ANVIL     = new Tile(3188, 3425, 0);
    private static final Tile GE_BANK           = new Tile(3167, 3487, 0);

    @Override public String name()  { return "Smithing"; }
    @Override public Skill  skill() { return Skill.SMITHING; }

    @Override public String[] methods() {
        return new String[] { "bronze", "iron", "steel", "gold", "mithril", "adamant" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 15)  return "bronze";
        if (curr < 30)  return "iron";
        if (curr < 40)  return "steel";
        if (curr < 50)  return "gold";
        if (curr < 70)  return "mithril";
        return "adamant";
    }

    @Override public int tick(String method) {
        String ore1   = ore1For(method);
        String ore2   = ore2For(method);
        String bar    = barFor(method);
        Tile furnace  = method.equals("gold") ? EDGEVILLE_FURNACE : LUMBRIDGE_FURNACE;

        // Phase A: ensure ores in inventory
        boolean hasOre1 = ore1 != null && Inventory.contains(ore1);
        boolean hasOre2 = ore2 == null || Inventory.contains(ore2);
        if (!hasOre1 || !hasOre2) {
            return withdrawOres(ore1, ore2);
        }

        // Phase B: smelt at furnace
        if (!Inventory.contains(bar)) {
            return smeltAt(furnace, method);
        }

        // Phase C: bank bars + queue GE sell (or smith immediately for XP)
        return bankAndQueue(bar);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private int withdrawOres(String ore1, String ore2) {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            if (ore1 != null && !Inventory.contains(ore1)) Bank.withdrawAll(ore1);
            if (ore2 != null && !Inventory.contains(ore2)) Bank.withdrawAll(ore2);
            Sleep.sleep(Calculations.random(500, 800));
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    private int smeltAt(Tile furnaceTile, String method) {
        if (furnaceTile.distance(Players.localPlayer()) > 6) {
            Walking.walkTo(furnaceTile);
            return Calculations.random(1200, 2000);
        }
        GameObject furnace = GameObjects.closest(g -> g != null
                && g.getName().equals("Furnace")
                && g.hasAction("Smelt"));
        if (furnace == null) {
            Walking.walkTo(furnaceTile);
            return Calculations.random(600, 1000);
        }
        furnace.interact("Smelt");
        Sleep.sleepUntil(() ->
            org.dreambot.api.methods.widget.Widgets.getWidget(270, 0) != null, 3_500);
        // Click the correct bar in the smelting interface
        int childIdx = smeltWidgetChild(method);
        try {
            var widget = org.dreambot.api.methods.widget.Widgets.getWidget(270, childIdx);
            if (widget != null) {
                widget.interact("Smelt");
                Sleep.sleepUntil(() -> !Inventory.contains(ore1For(method)), 30_000);
            }
        } catch (Throwable ignored) { }
        return Calculations.random(500, 800);
    }

    private int bankAndQueue(String bar) {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            int qty = Inventory.count(bar) + Bank.count(bar);
            Bank.depositAll();
            Bank.close();
            // Queue for GE every 200 bars
            if (qty >= 200) {
                GESellTask.queue(bar, qty, 0);
            }
        }
        return Calculations.random(400, 700);
    }

    // ── data maps ─────────────────────────────────────────────────────────────

    private static String ore1For(String m) {
        switch (m) {
            case "bronze":  return "Copper ore";
            case "iron":    return "Iron ore";
            case "steel":   return "Iron ore";
            case "gold":    return "Gold ore";
            case "mithril": return "Mithril ore";
            case "adamant": return "Adamantite ore";
            default: return null;
        }
    }
    private static String ore2For(String m) {
        switch (m) {
            case "bronze": return "Tin ore";
            case "steel":  return "Coal";  // 2 coal per bar; handled by withdrawAll
            default: return null;
        }
    }
    private static String barFor(String m) {
        switch (m) {
            case "bronze":  return "Bronze bar";
            case "iron":    return "Iron bar";
            case "steel":   return "Steel bar";
            case "gold":    return "Gold bar";
            case "mithril": return "Mithril bar";
            case "adamant": return "Adamantite bar";
            default: return "Bronze bar";
        }
    }
    /** Widget child index in the Smelt interface for each bar type. */
    private static int smeltWidgetChild(String m) {
        switch (m) {
            case "bronze":  return 2;
            case "iron":    return 3;
            case "steel":   return 4;
            case "gold":    return 6;
            case "mithril": return 8;
            case "adamant": return 10;
            default: return 2;
        }
    }
}
