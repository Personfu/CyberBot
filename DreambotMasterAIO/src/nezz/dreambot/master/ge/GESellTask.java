package nezz.dreambot.master.ge;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.tasks.Task;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scheduled task that walks to the GE, lists accumulated sellable goods,
 * and collects gold from completed offers.
 *
 * <h3>Sell queue</h3>
 * Call {@code GESellTask.queue(itemName, qty, price)} from any money-making
 * module to stage an item for sale. The task fires whenever the queue is
 * non-empty AND a minimum bank threshold is met (so we don't waste walk time
 * for 10 cowhides).
 */
public final class GESellTask extends Task {

    /** Global sell queue — item name → (qty, price). */
    private static final Map<String, int[]> SELL_QUEUE = new LinkedHashMap<>();
    private static final int MIN_STACK = 20; // don't bother unless we have ≥N of an item

    private final Logger log;
    private boolean done = false;

    public GESellTask(Logger log) { this.log = log; }

    /**
     * Queue an item for sale.
     * @param itemName  exact in-game item name
     * @param qty       number of items to sell (use {@code Integer.MAX_VALUE} for "all in bank")
     * @param priceEach price in gp (0 = let GE util guess market price)
     */
    public static void queue(String itemName, int qty, int priceEach) {
        SELL_QUEUE.merge(itemName, new int[]{qty, priceEach}, (old, neo) ->
                new int[]{old[0] + neo[0], neo[1]});
    }

    public static void clearQueue() { SELL_QUEUE.clear(); }

    @Override public int  priority()  { return 60; }
    @Override public BotState state() { return BotState.MONEY_MAKING; }
    @Override public boolean isComplete() { return done; }

    @Override public boolean isReady() {
        // Ready when queue has something big enough to bother with
        if (SELL_QUEUE.isEmpty()) return false;
        for (int[] v : SELL_QUEUE.values()) {
            if (v[0] >= MIN_STACK) return true;
        }
        return false;
    }

    @Override public int execute() {
        if (!GrandExchangeUtil.walkToGE()) return Calculations.random(1200, 1800);

        // First: collect any completed offers
        if (GrandExchangeUtil.openGE()) {
            try { GrandExchange.collect(); } catch (Throwable ignored) { }
            GrandExchangeUtil.closeGE();
        }

        // Then bank everything we just collected
        Bank.open();
        if (Bank.isOpen()) {
            Bank.depositAllItems();
            Bank.close();
        }

        // Now list each queued item
        for (Map.Entry<String, int[]> entry : new LinkedHashMap<>(SELL_QUEUE).entrySet()) {
            String name  = entry.getKey();
            int    qty   = entry.getValue()[0];
            int    price = entry.getValue()[1];
            boolean placed = GrandExchangeUtil.sellAll(name, price);
            if (placed) {
                log.info("GE: listed " + qty + "x " + name + " @ " + price + " gp");
                SELL_QUEUE.remove(name);
            }
        }

        done = true;
        return Calculations.random(600, 1000);
    }

    @Override public String label() { return "GE-sell[" + SELL_QUEUE.size() + " items queued]"; }
}

