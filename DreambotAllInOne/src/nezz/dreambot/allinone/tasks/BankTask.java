package nezz.dreambot.allinone.tasks;

import nezz.dreambot.allinone.config.ScriptConfig;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;

/**
 * Deposits loot in the nearest bank, keeping food in inventory.
 */
public class BankTask {

    private final ScriptConfig config;

    public BankTask(ScriptConfig config) {
        this.config = config;
    }

    /**
     * Execute banking if inventory is full OR food count has dropped critically.
     */
    public boolean shouldExecute() {
        if (!config.isBankWhenFull()) return false;
        if (Players.getLocal() == null) return false;
        if (Players.getLocal().isInCombat()) return false;

        if (Inventory.isFull()) return true;

        // Also bank if we've run out of food
        String food = config.getFoodName();
        if (!food.isEmpty() && !food.equalsIgnoreCase("None")) {
            int foodCount = Inventory.count(food);
            if (foodCount < config.getMinFoodCount()) return true;
        }
        return false;
    }

    public int execute() {
        if (!Bank.isOpen()) {
            Bank.open();   // opens nearest bank; no openClosest() in this API version
            Sleep.sleepUntil(Bank::isOpen, 4000);
        }

        if (!Bank.isOpen()) {
            // Bank didn't open — try again next tick
            return Calculations.random(600, 900);
        }

        // Deposit everything except food — depositAllExcept takes a Filter, no String overload
        String food = config.getFoodName();
        if (!food.isEmpty() && !food.equalsIgnoreCase("None")) {
            Bank.depositAllExcept(item -> item != null
                && item.getName() != null
                && item.getName().equalsIgnoreCase(food));
        } else {
            Bank.depositAllItems();
        }
        Sleep.sleep(Calculations.random(400, 700));

        // Withdraw food if needed
        if (!food.isEmpty() && !food.equalsIgnoreCase("None")) {
            int currentFood = Inventory.count(food);
            int desiredFood = 8; // keep 8 pieces; tune if needed
            if (currentFood < desiredFood) {
                int toWithdraw = desiredFood - currentFood;
                Bank.withdraw(food, toWithdraw);
                Sleep.sleep(Calculations.random(300, 600));
            }
        }

        Bank.close();
        Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
        return Calculations.random(600, 900);
    }
}
