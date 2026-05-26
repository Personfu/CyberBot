package nezz.dreambot.master.skills.impl;

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
import org.dreambot.api.wrappers.items.Item;

/**
 * Firemaking trainer — progressive log burning, route to 99 (F2P).
 *
 * <pre>
 *  1-15  : Regular logs (buy from GE or keep from Woodcutting)
 * 15-30  : Oak logs
 * 30-45  : Willow logs
 * 45-60  : Teak logs (P2P) / Willow fallback for F2P
 * 60-75  : Maple logs (P2P) / Yew fallback for F2P
 * 75-99  : Yew logs (F2P) / Magic (P2P)
 * </pre>
 *
 * Burns in a bank-line pattern: withdraw 27 logs + tinderbox, burn
 * east→west along the bank row, bank, repeat.
 */
public final class FiremakingModule extends SkillModule {

    private static final Tile GE_BANK_TILE = new Tile(3167, 3487, 0);

    @Override public String name()  { return "Firemaking"; }
    @Override public Skill  skill() { return Skill.FIREMAKING; }

    @Override public String[] methods() {
        return new String[] { "logs", "oak", "willow", "yew", "magic" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 15)  return "logs";
        if (curr < 30)  return "oak";
        if (curr < 60)  return "willow";
        if (curr < 75)  return "yew";
        return "magic";
    }

    @Override public int tick(String method) {
        String logName = logFor(method);

        // Ensure tinderbox in inventory
        if (!Inventory.contains("Tinderbox")) {
            withdrawLogsAndBox(logName);
            return Calculations.random(500, 800);
        }

        // If no logs, withdraw from bank
        if (!Inventory.contains(logName)) {
            withdrawLogsAndBox(logName);
            return Calculations.random(500, 800);
        }

        // Burn a log: use tinderbox on log
        Item tinderbox = Inventory.get("Tinderbox");
        Item log       = Inventory.get(logName);
        if (tinderbox == null || log == null) return 300;

        tinderbox.useOn(log);
        Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 2_000);
        Sleep.sleepUntil(() -> !Players.localPlayer().isAnimating(), 8_000);
        return Calculations.random(300, 600);
    }

    private void withdrawLogsAndBox(String logName) {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAll();
            Bank.withdraw("Tinderbox", 1);
            Bank.withdraw(logName, 27);
            Sleep.sleepUntil(() -> Inventory.contains(logName), 2_000);
            Bank.close();
        }
    }

    private static String logFor(String method) {
        switch (method == null ? "" : method) {
            case "oak":    return "Oak logs";
            case "willow": return "Willow logs";
            case "yew":    return "Yew logs";
            case "magic":  return "Magic logs";
            default:       return "Logs";
        }
    }
}
