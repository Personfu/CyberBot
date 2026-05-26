package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chicken farm money route.
 *
 * <h3>Drops collected</h3>
 * <ul>
 *   <li><b>Feathers</b> — stack in inventory; sell at GE (2-4 gp each).</li>
 *   <li><b>Raw chicken</b> — 50-80 gp each.</li>
 *   <li><b>Bones</b> — bury immediately for Prayer XP.</li>
 *   <li><b>Eggs</b> — picked up opportunistically (50-100 gp each).</li>
 * </ul>
 *
 * <h3>GP/hr</h3>
 * ~5,000-10,000 gp/hr (feathers dominate due to stacking).
 * Pairs well with early-game XP since chickens are 1HP and grant combat levels.
 *
 * <h3>Location</h3>
 * Lumbridge chicken pen (south-west of Lumbridge castle) — the standard
 * starter-level F2P farm spot.
 */
public final class ChickenRoute extends MoneyRoute {

    private static final Tile CHICKEN_TILE = new Tile(3230, 3294, 0);
    private static final Tile BANK_TILE    = new Tile(3208, 3220, 0);
    private static final int  BATCH_SIZE   = 1_500; // stack feathers

    private int feathersCollected = 0;
    private State state = State.KILLING;

    private enum State { KILLING, BANKING, SELLING }

    @Override public String id()          { return "chicken"; }
    @Override public String description() { return "Lumbridge Chickens → Feathers + Raw Chicken → GE"; }
    @Override public int estimatedGpHr()  { return 7_000; }

    @Override public Map<Skill, Integer> requirements() {
        return new LinkedHashMap<>();  // no requirements
    }

    @Override public int tick() {
        switch (state) {
            case KILLING:  return doKill();
            case BANKING:  return doBank();
            case SELLING:  return doSell();
            default:       return 600;
        }
    }

    private int doKill() {
        // Bank non-stacking items when inv is full (feathers stack so only raw chicken fills it)
        boolean invFull = Inventory.isFull();
        if (invFull) {
            state = State.BANKING;
            return 300;
        }

        // Bury bones immediately
        while (Inventory.contains("Bones")) {
            org.dreambot.api.wrappers.items.Item bone = Inventory.get("Bones");
            if (bone != null) bone.interact("Bury");
            Sleep.sleep(Calculations.random(300, 600));
        }

        // Walk to chicken pen
        if (CHICKEN_TILE.distance(Players.getLocal()) > 15) {
            Walking.walk(CHICKEN_TILE);
            return Calculations.random(1200, 1800);
        }

        // Grab feathers from ground first
        GroundItem feathers = GroundItems.closest("Feather");
        if (feathers != null && feathers.distance(Players.getLocal()) < 5) {
            feathers.interact("Take");
            Sleep.sleep(Calculations.random(300, 500));
            return Calculations.random(200, 400);
        }

        // Grab raw chicken
        GroundItem rawChicken = GroundItems.closest("Raw chicken");
        if (rawChicken != null && rawChicken.distance(Players.getLocal()) < 4) {
            rawChicken.interact("Take");
            Sleep.sleep(Calculations.random(300, 500));
            return Calculations.random(200, 400);
        }

        // Attack chicken
        NPC chicken = NPCs.closest(n -> n != null && n.getName().equals("Chicken")
                && !n.isInCombat());
        if (chicken != null) {
            chicken.interact("Attack");
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 2000);
        }
        return Calculations.random(600, 1200);
    }

    private int doBank() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            feathersCollected += Inventory.count("Feather");
            Bank.depositAllItems();
            Sleep.sleepUntil(() -> Inventory.isEmpty(), 2_000);
            Bank.close();
            state = feathersCollected >= BATCH_SIZE ? State.SELLING : State.KILLING;
        }
        return Calculations.random(600, 1000);
    }

    private int doSell() {
        GESellTask.queue("Feather",       feathersCollected, 0);
        GESellTask.queue("Raw chicken",   Integer.MAX_VALUE, 0);
        GESellTask.queue("Egg",           Integer.MAX_VALUE, 0);
        feathersCollected = 0;
        state = State.KILLING;
        return 300;
    }
}

