package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Air rune crafting — steady early F2P Runecrafting income with zero upfront cost.
 *
 * <h3>GP/hr (approximate)</h3>
 * <ul>
 *   <li>Level  1-9:  1 rune/ess  × 28/trip × ~30 trips/hr =  840 runes = ~4,200 gp/hr</li>
 *   <li>Level 11-21: 2 runes/ess → ~8,400 gp/hr</li>
 *   <li>Level 44+:   uses all pouches → ~12,000 gp/hr</li>
 * </ul>
 *
 * <h3>Route</h3>
 * Lumbridge bank → Falador park south → Air Altar → bank repeat.
 */
public final class AirRuneRoute extends MoneyRoute {

    private static final Tile AIR_ALTAR_ENTER = new Tile(2983, 3292, 0);  // Mysterious ruin south of Falador
    private static final Tile FALADOR_BANK    = new Tile(2946, 3368, 0);
    private static final String AIR_RUIN      = "Mysterious ruins";
    private static final String ALTAR         = "Altar";
    private static final int BATCH_RUNES      = 3_000;

    private int bankedRunes = 0;
    private State state = State.WITHDRAW;

    private enum State { WITHDRAW, WALK_TO_ALTAR, ENTER_ALTAR, CRAFT, BANKING, SELLING }

    @Override public String id()          { return "air_runes"; }
    @Override public String description() { return "Rune Ess → Air Runes → GE"; }
    @Override public int estimatedGpHr()  { return 8_000; }

    @Override public Map<Skill, Integer> requirements() {
        Map<Skill, Integer> r = new LinkedHashMap<>();
        r.put(Skill.RUNECRAFTING, 1);
        return r;
    }

    @Override public int tick() {
        switch (state) {
            case WITHDRAW:      return doWithdraw();
            case WALK_TO_ALTAR: return doWalkToAltar();
            case ENTER_ALTAR:   return doEnterAltar();
            case CRAFT:         return doCraft();
            case BANKING:       return doBank();
            case SELLING:       return doSell();
            default:            return 600;
        }
    }

    private int doWithdraw() {
        if (Inventory.contains("Rune essence") || Inventory.contains("Pure essence")) {
            state = State.WALK_TO_ALTAR;
            return 300;
        }
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.withdrawAll("Pure essence");
            if (!Inventory.contains("Pure essence")) Bank.withdrawAll("Rune essence");
            Sleep.sleepUntil(() ->
                Inventory.contains("Pure essence") || Inventory.contains("Rune essence"), 2_000);
            Bank.close();
            state = State.WALK_TO_ALTAR;
        }
        return Calculations.random(600, 1000);
    }

    private int doWalkToAltar() {
        if (AIR_ALTAR_ENTER.distance(Players.getLocal()) < 8) {
            state = State.ENTER_ALTAR;
            return 300;
        }
        Walking.walk(AIR_ALTAR_ENTER);
        return Calculations.random(1200, 2000);
    }

    private int doEnterAltar() {
        GameObject ruin = GameObjects.closest(g -> g != null
                && g.getName().equals(AIR_RUIN)
                && g.hasAction("Enter"));
        if (ruin == null) {
            Walking.walk(AIR_ALTAR_ENTER);
            return Calculations.random(800, 1200);
        }
        ruin.interact("Enter");
        Sleep.sleepUntil(() ->
            GameObjects.closest(ALTAR) != null, 3_000);
        state = State.CRAFT;
        return Calculations.random(600, 1000);
    }

    private int doCraft() {
        GameObject altar = GameObjects.closest(ALTAR);
        if (altar == null) {
            // Fell outside altar — re-enter
            state = State.WALK_TO_ALTAR;
            return 600;
        }
        altar.interact("Craft-rune");
        Sleep.sleepUntil(() ->
            !Inventory.contains("Rune essence") && !Inventory.contains("Pure essence"), 5_000);
        // Exit altar via portal
        GameObject portal = GameObjects.closest(g -> g != null && g.hasAction("Use"));
        if (portal != null) portal.interact("Use");
        Sleep.sleep(Calculations.random(800, 1200));
        state = State.BANKING;
        return Calculations.random(600, 1000);
    }

    private int doBank() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 4_000);
        }
        if (Bank.isOpen()) {
            bankedRunes += Inventory.count("Air rune");
            Bank.depositAllItems();
            Sleep.sleepUntil(Inventory::isEmpty, 2_000);
            Bank.close();
            state = bankedRunes >= BATCH_RUNES ? State.SELLING : State.WITHDRAW;
        }
        return Calculations.random(600, 1000);
    }

    private int doSell() {
        GESellTask.queue("Air rune", bankedRunes, 0);
        bankedRunes = 0;
        state = State.WITHDRAW;
        return 300;
    }
}
