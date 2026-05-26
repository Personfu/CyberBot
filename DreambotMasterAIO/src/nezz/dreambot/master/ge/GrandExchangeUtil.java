package nezz.dreambot.master.ge;

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

public final class GrandExchangeUtil {

    private static final Tile GE_TILE = new Tile(3165, 3487, 0);
    private static final String CLERK_NAME = "Grand Exchange Clerk";

    private GrandExchangeUtil() {}

    public static boolean isAtGE() {
        return GE_TILE.distance(
            org.dreambot.api.methods.interactive.Players.getLocal()) < 12;
    }

    public static boolean walkToGE() {
        if (isAtGE()) return true;
        Walking.walk(GE_TILE);
        Sleep.sleepUntil(GrandExchangeUtil::isAtGE, 15000L);
        return isAtGE();
    }

    public static boolean openGE() {
        if (GrandExchange.isOpen()) return true;
        NPC clerk = NPCs.closest(CLERK_NAME);
        if (clerk == null) {
            walkToGE();
            return false;
        }
        if (clerk.interact("Exchange")) {
            Sleep.sleepUntil(GrandExchange::isOpen, 3000L);
        }
        return GrandExchange.isOpen();
    }

    public static void closeGE() {
        if (GrandExchange.isOpen()) {
            try { GrandExchange.close(); } catch (Throwable ignored) {}
        }
    }

    public static boolean sellAll(String itemName, int priceEach) {
        if (!ensureBankEmpty(itemName)) return false;
        if (!walkToGE()) return false;
        if (!openGE()) return false;
        int qty = Inventory.count(itemName);
        if (qty <= 0) return false;
        int price = (priceEach > 0) ? priceEach : 100;
        try {
            GrandExchange.sellItem(itemName, qty, price);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean buy(int itemId, String itemName, int qty, int priceEach) {
        if (!walkToGE()) return false;
        if (!openGE()) return false;
        try {
            GrandExchange.buyItem(itemName, qty, priceEach);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void collectAndBank() {
        if (!walkToGE() || !openGE()) return;
        try {
            GrandExchange.collect();
            Sleep.sleep((long) Calculations.random(600, 1200));
        } catch (Throwable ignored) {}
        closeGE();
        if (!Inventory.isEmpty()) {
            Bank.open();
            if (Bank.isOpen()) {
                Bank.depositAllItems();
                Bank.close();
            }
        }
    }

    private static boolean ensureBankEmpty(String itemName) {
        if (!Inventory.contains(itemName)) {
            Bank.open();
            if (!Bank.isOpen()) return false;
            Bank.withdrawAll(itemName);
            Sleep.sleepUntil(() -> Inventory.contains(itemName), 3000L);
            Bank.close();
        }
        return Inventory.contains(itemName);
    }

    private static boolean offerPlaced(String itemName) {
        try {
            GrandExchangeItem[] slots = GrandExchange.getItems();
            if (slots == null) return false;
            for (GrandExchangeItem slot : slots) {
                if (slot != null && itemName.equalsIgnoreCase(slot.getName())) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}