package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Steel bar production — mine Iron + Coal, smelt at furnace, sell bars at GE.
 *
 * <h3>Profit</h3>
 * Steel bar ~530 gp, iron ore ~100 gp, coal ~165 gp × 2 = 430 gp cost.
 * Profit: ~100 gp/bar × 200 bars/hr = <b>~20,000 gp/hr</b>
 * Also grants Smithing XP: 17.5 XP × 200 = 3,500 XP/hr (bonus).
 *
 * <h3>Location</h3>
 * Lumbridge mine (south-east, close to furnace entrance).
 * Falador mine for coal, Al-Kharid furnace for smelting.
 */
public final class SteelBarRoute extends MoneyRoute {

    private static final Tile IRON_MINE  = new Tile(3295, 3310, 0);  // Al-Kharid mine
    private static final Tile COAL_MINE  = new Tile(3295, 3308, 0);  // same area, different rocks
    private static final Tile FURNACE    = new Tile(3191, 3255, 0);  // Lumbridge furnace
    private static final Tile BANK       = new Tile(3208, 3220, 0);

    private static final int IRON_NEEDED  = 14;
    private static final int COAL_NEEDED  = 28; // 2 coal per bar × 14
    private static final int BATCH_BARS   = 150;

    private int bankedBars = 0;
    private State state = State.MINE_IRON;

    private enum State { MINE_IRON, MINE_COAL, SMELT, BANKING, SELLING }

    @Override public String id()          { return "steel_bars"; }
    @Override public String description() { return "Mine Iron+Coal → Smelt Steel → GE"; }
    @Override public int estimatedGpHr()  { return 20_000; }

    @Override public Map<Skill, Integer> requirements() {
        Map<Skill, Integer> r = new LinkedHashMap<>();
        r.put(Skill.MINING, 30);
        r.put(Skill.SMITHING, 30);
        return r;
    }

    @Override public int tick() {
        switch (state) {
            case MINE_IRON: return mineOre("Iron rock", "Iron ore", IRON_NEEDED, State.MINE_COAL, IRON_MINE);
            case MINE_COAL: return mineOre("Coal rock", "Coal",     COAL_NEEDED,  State.SMELT,    COAL_MINE);
            case SMELT:     return doSmelt();
            case BANKING:   return doBank();
            case SELLING:   return doSell();
            default:        return 600;
        }
    }

    private int mineOre(String rockName, String oreName, int need, State next, Tile mineArea) {
        if (Inventory.count(oreName) >= need) {
            state = next;
            return 300;
        }
        if (mineArea.distance(Players.getLocal()) > 10) {
            Walking.walk(mineArea);
            return Calculations.random(1200, 1800);
        }
        GameObject rock = GameObjects.closest(g -> g != null
                && g.getName().equals(rockName)
                && g.hasAction("Mine"));
        if (rock == null) {
            Walking.walk(mineArea);
            return Calculations.random(600, 1000);
        }
        if (!Players.getLocal().isAnimating()) {
            rock.interact("Mine");
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 2_000);
        }
        return Calculations.random(1200, 2200);
    }

    private int doSmelt() {
        // Walk to furnace
        if (FURNACE.distance(Players.getLocal()) > 5) {
            Walking.walk(FURNACE);
            return Calculations.random(1200, 1800);
        }
        GameObject furnace = GameObjects.closest(g -> g != null
                && g.getName().equals("Furnace")
                && g.hasAction("Smelt"));
        if (furnace == null) {
            Walking.walk(FURNACE);
            return Calculations.random(600, 1000);
        }
        if (!Inventory.contains("Iron ore") || !Inventory.contains("Coal")) {
            state = State.MINE_IRON;
            return 300;
        }
        furnace.interact("Smelt");
        // Wait for smelting interface (widget 270 for smithing in DB)
        Sleep.sleepUntil(() ->
            org.dreambot.api.methods.widget.Widgets.get(270, 0) != null, 3_000);
        try {
            // Click steel bar icon — child index 4 in standard smelting interface
            org.dreambot.api.methods.widget.Widgets.get(270, 4).interact("Smelt");
            Sleep.sleepUntil(() -> !Inventory.contains("Iron ore"), 30_000);
        } catch (Throwable ignored) { }
        state = State.BANKING;
        return Calculations.random(600, 1000);
    }

    private int doBank() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            bankedBars += Inventory.count("Steel bar");
            Bank.depositAllItems();
            Sleep.sleepUntil(Inventory::isEmpty, 2_000);
            Bank.close();
            state = bankedBars >= BATCH_BARS ? State.SELLING : State.MINE_IRON;
        }
        return Calculations.random(600, 1000);
    }

    private int doSell() {
        GESellTask.queue("Steel bar", bankedBars, 0);
        bankedBars = 0;
        state = State.MINE_IRON;
        return 300;
    }
}

