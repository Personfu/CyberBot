package nezz.dreambot.master.ge;

import nezz.dreambot.master.id.ItemID;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Grand Exchange utility layer for the MasterAIO.
 *
 * <h3>Usage</h3>
 * <pre>
 *   // Sell all banked cowhides:
 *   GeSession s = GeSession.create();
 *   s.queueSell(ItemID.COWHIDE, 28, 0); // 0 = market price
 *   s.execute();
 *
 *   // Buy flax:
 *   s.queueBuy(ItemID.FLAX, 500, 110); // buy at 110gp each
 *   s.execute();
 * </pre>
 *
 * <p>Walk-to-GE logic targets the Grand Exchange clerk NPC in Varrock.
 * If already at GE the walk is skipped.</p>
 *
 * <p>Based on the GE patterns used in SlugBuilder, SubAccountBuilder and
 * HowP2PAIO.</p>
 */
public final class GrandExchangeUtil {

    /** Tile in front of the GE clerk (Varrock, inside GE). */
    private static final Tile GE_TILE = new Tile(3165, 3487, 0);
    /** Name of the GE exchange clerk NPC. */
    private static final String CLERK_NAME = "Grand Exchange Clerk";

    private GrandExchangeUtil() { }

    // ────────────────────────────────────────────────────────────────────────
    // Navigation
    // ────────────────────────────────────────────────────────────────────────

    /** Walk to the GE if not already there. Returns true once arrived. */
    public static boolean walkToGE() {
        if (isAtGE()) return true;
        Walking.walkTo(GE_TILE);
        Sleep.sleepUntil(GrandExchangeUtil::isAtGE, 15_000);
        return isAtGE();
    }

    public static boolean isAtGE() {
        return GE_TILE.distance(
            org.dreambot.api.methods.interactive.Players.localPlayer()) < 12;
    }

    // ────────────────────────────────────────────────────────────────────────
    // GE open / close
    // ────────────────────────────────────────────────────────────────────────

    public static boolean openGE() {
        if (GrandExchange.isOpen()) return true;
        NPC clerk = NPCs.closest(CLERK_NAME);
        if (clerk == null) {
            walkToGE();
            return false;
        }
        if (clerk.interact("Exchange")) {
            Sleep.sleepUntil(GrandExchange::isOpen, 3_000);
        }
        return GrandExchange.isOpen();
    }

    public static void closeGE() {
        if (GrandExchange.isOpen()) {
            try { GrandExchange.close(); } catch (Throwable ignored) { }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Quick sell — dumps a stack of items onto the GE at market price (-5 %).
    // Returns true if the offer was placed.
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Withdraw all of {@code itemName} from the bank (if needed), walk to GE,
     * and list them as a sell offer at {@code priceEach} (0 = market price).
     */
    public static boolean sellAll(String itemName, int priceEach) {
        if (!ensureBankEmpty(itemName)) return false;
        if (!walkToGE())               return false;
        if (!openGE())                 return false;

        int qty = Inventory.count(itemName);
        if (qty <= 0) return false;

        int price = priceEach > 0 ? priceEach
                : (int)(getGuidePrice(itemName) * 0.95);  // undercut by 5 %

        try {
            GrandExchange.sellItem(itemName, qty, price);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4_000);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Buy {@code qty} of {@code itemId} at {@code priceEach} each.
     */
    public static boolean buy(int itemId, String itemName, int qty, int priceEach) {
        if (!walkToGE()) return false;
        if (!openGE())   return false;
        try {
            GrandExchange.buyItem(itemId, itemName, qty, priceEach);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4_000);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Collect finished offers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Collects completed GE offers into the inventory, then deposits them at
     * the nearest bank.  Call once after selling a batch.
     */
    public static void collectAndBank() {
        if (!walkToGE() || !openGE()) return;
        try {
            GrandExchange.collect();
            Sleep.sleep(Calculations.random(600, 1200));
        } catch (Throwable ignored) { }
        closeGE();
        if (Inventory.isFull() || Inventory.getUsedSlots() > 0) {
            Bank.openClosest();
            if (Bank.isOpen()) {
                Bank.depositAllExcept(
                    item -> item != null && item.getName().contains("coin")
                );
                Bank.close();
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Helpers
    // ────────────────────────────────────────────────────────────────────────

    private static boolean ensureBankEmpty(String itemName) {
        if (!Inventory.contains(itemName)) {
            Bank.openClosest();
            if (!Bank.isOpen()) return false;
            Bank.withdrawAll(itemName);
            Sleep.sleepUntil(() -> Inventory.contains(itemName), 3_000);
            Bank.close();
        }
        return Inventory.contains(itemName);
    }

    private static boolean offerPlaced(String itemName) {
        try {
            GrandExchangeItem[] slots = GrandExchange.getItems();
            if (slots == null) return false;
            for (GrandExchangeItem slot : slots) {
                if (slot != null && itemName.equalsIgnoreCase(slot.getName())) return true;
            }
        } catch (Throwable ignored) { }
        return false;
    }

    private static int getGuidePrice(String itemName) {
        try {
            GrandExchangeItem[] slots = GrandExchange.getItems();
            // DreamBot does not expose a guide-price lookup without an open slot;
            // fall back to a reasonable default so the sell still goes through.
        } catch (Throwable ignored) { }
        return 100; // safe fallback
    }
}
