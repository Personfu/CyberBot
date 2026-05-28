package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import nezz.dreambot.master.ge.GrandExchangeUtil;
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
 * Flax spinning — one of the top-tier F2P money methods.
 *
 * <h3>Loop</h3>
 * <ol>
 *   <li>Buy flax at GE (~90-100 gp each).</li>
 *   <li>Walk to Lumbridge castle 2nd floor spinning wheel.</li>
 *   <li>Spin all flax → bow strings (0 Crafting required).</li>
 *   <li>Bank bow strings at Lumbridge bank.</li>
 *   <li>Sell bow strings at GE (~175 gp each).</li>
 * </ol>
 *
 * <h3>GP/hr</h3>
 * ~75-85 gp profit × 1,500 strings/hr = <b>~112,000 gp/hr</b> — the best
 * non-combat F2P method that needs only GP to start.
 */
public final class FlaxSpinRoute extends MoneyRoute {

    private static final Tile SPINNING_WHEEL = new Tile(3209, 3213, 1); // Lumbridge castle 1st floor (0-indexed)
    private static final Tile LUM_BANK       = new Tile(3208, 3220, 2); // Lumbridge bank (top floor)
    private static final Tile LUM_BANK_F0    = new Tile(3208, 3220, 0); // ground-floor bank alternative
    private static final int  FLAX_BUY_QTY  = 1_000;
    private static final int  FLAX_PRICE    = 110;  // buy slightly above guide price

    private int boughtFlax = 0;
    private State state = State.BUY_FLAX;

    private enum State { BUY_FLAX, WITHDRAW_FLAX, SPINNING, BANKING, SELLING }

    @Override public String id()          { return "flax_spin"; }
    @Override public String description() { return "Buy Flax → Spin at Lumbridge → Sell Bow Strings"; }
    @Override public int estimatedGpHr()  { return 112_000; }

    @Override public Map<Skill, Integer> requirements() {
        return new LinkedHashMap<>(); // no skill requirements
    }

    @Override public int tick() {
        switch (state) {
            case BUY_FLAX:      return doBuyFlax();
            case WITHDRAW_FLAX: return doWithdraw();
            case SPINNING:      return doSpin();
            case BANKING:       return doBank();
            case SELLING:       return doSell();
            default:            return 600;
        }
    }

    private int doBuyFlax() {
        // Check if we already have flax banked
        Bank.open();
        if (Bank.isOpen()) {
            int inBank = Bank.count("Flax");
            if (inBank >= 28) {
                Bank.close();
                state = State.WITHDRAW_FLAX;
                return 300;
            }
            Bank.close();
        }
        // Buy from GE
        GrandExchangeUtil.walkToGE();
        if (GrandExchangeUtil.openGE()) {
            // Try to collect any finished offers first
            try { org.dreambot.api.methods.grandexchange.GrandExchange.collect(); }
            catch (Throwable ignored) { }
            GrandExchangeUtil.buy(
                nezz.dreambot.master.id.ItemID.FLAX, "Flax",
                FLAX_BUY_QTY, FLAX_PRICE);
            GrandExchangeUtil.closeGE();
            boughtFlax += FLAX_BUY_QTY;
        }
        // Bank the flax and transition
        Bank.open();
        if (Bank.isOpen()) {
            Bank.depositAllItems();
            Bank.close();
        }
        state = State.WITHDRAW_FLAX;
        return Calculations.random(800, 1200);
    }

    private int doWithdraw() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            if (!Inventory.contains("Flax")) {
                Bank.withdrawAll("Flax");
                Sleep.sleepUntil(() -> Inventory.contains("Flax"), 2_000);
            }
            Bank.close();
            state = State.SPINNING;
        }
        return Calculations.random(600, 900);
    }

    private int doSpin() {
        if (!Inventory.contains("Flax")) {
            state = Inventory.contains("Bow string") ? State.BANKING : State.WITHDRAW_FLAX;
            return 300;
        }

        // Navigate to spinning wheel
        if (SPINNING_WHEEL.distance(Players.getLocal()) > 5) {
            Walking.walk(SPINNING_WHEEL);
            return Calculations.random(1200, 2000);
        }

        GameObject wheel = GameObjects.closest(g -> g != null
                && g.getName().equals("Spinning wheel")
                && g.hasAction("Spin"));
        if (wheel == null) {
            Walking.walk(SPINNING_WHEEL);
            return Calculations.random(800, 1200);
        }

        Item flax = Inventory.get("Flax");
        if (flax != null) {
            flax.useOn(wheel);
            // Wait for "Make All" widget (interface 459 child 5 in modern clients)
            Sleep.sleepUntil(() ->
                org.dreambot.api.methods.widget.Widgets.get(459, 5) != null, 2_500);
            try {
                org.dreambot.api.methods.widget.Widgets.get(459, 5).interact("Make");
                Sleep.sleepUntil(() -> !Inventory.contains("Flax"), 60_000);
            } catch (Throwable ignored) { }
        }

        state = State.BANKING;
        return Calculations.random(500, 800);
    }

    private int doBank() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAllItems();
            Sleep.sleepUntil(Inventory::isEmpty, 2_000);
            int totalStrings = Bank.count("Bow string");
            Bank.close();
            if (totalStrings >= 500) {
                state = State.SELLING;
            } else {
                state = State.WITHDRAW_FLAX;
            }
        }
        return Calculations.random(600, 1000);
    }

    private int doSell() {
        GESellTask.queue("Bow string", Integer.MAX_VALUE, 0);
        state = State.BUY_FLAX;
        return 300;
    }
}

