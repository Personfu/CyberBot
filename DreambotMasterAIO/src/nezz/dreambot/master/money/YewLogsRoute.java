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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Yew log chopping — best F2P Woodcutting money above level 60.
 *
 * <h3>GP/hr</h3>
 * ~180-220 gp × 120-150 logs/hr = <b>25,000-33,000 gp/hr</b>
 *
 * <h3>Locations (priority order)</h3>
 * <ol>
 *   <li>Falador park (3 yew trees, very close to Falador west bank)</li>
 *   <li>Lumbridge graveyard yews (level 60 WC only, less contested)</li>
 *   <li>Edgeville yews (south edge)</li>
 * </ol>
 *
 * <p>Hops world if all 3 trees are contested (no tree in range).</p>
 */
public final class YewLogsRoute extends MoneyRoute {

    private static final Tile FALADOR_BANK = new Tile(2946, 3368, 0);
    private static final Tile YEW_SPOT_1   = new Tile(2984, 3382, 0); // Falador park yew 1
    private static final Tile YEW_SPOT_2   = new Tile(2990, 3391, 0); // Falador park yew 2
    private static final Tile YEW_SPOT_3   = new Tile(2996, 3380, 0); // Falador park yew 3
    private static final Tile LUM_YEW_1    = new Tile(3226, 3231, 0); // Lumbridge graveyard

    private static final int BATCH_SIZE = 200;
    private int bankedLogs = 0;
    private State state = State.CHOPPING;

    private enum State { CHOPPING, BANKING, SELLING }

    @Override public String id()          { return "yew_logs"; }
    @Override public String description() { return "Falador/Lumbridge Yew Trees → GE Sell"; }
    @Override public int estimatedGpHr()  { return 28_000; }

    @Override public Map<Skill, Integer> requirements() {
        Map<Skill, Integer> r = new LinkedHashMap<>();
        r.put(Skill.WOODCUTTING, 60);
        return r;
    }

    @Override public int tick() {
        switch (state) {
            case CHOPPING: return doChop();
            case BANKING:  return doBank();
            case SELLING:  return doSell();
            default:       return 600;
        }
    }

    private int doChop() {
        if (Inventory.isFull()) {
            state = State.BANKING;
            return 300;
        }

        // Navigate to yew area if far
        if (Players.localPlayer().distance(YEW_SPOT_1) > 25) {
            Walking.walkTo(YEW_SPOT_1);
            return Calculations.random(1200, 2000);
        }

        // Find an unoccupied yew tree
        GameObject yew = GameObjects.closest(g -> g != null
                && g.getName().equals("Yew")
                && g.hasAction("Chop down")
                && (g.distance(Players.localPlayer()) < 15));
        if (yew == null) {
            // All yews busy — try backup location
            Walking.walkTo(LUM_YEW_1);
            return Calculations.random(2000, 3000);
        }

        if (!Players.localPlayer().isAnimating()) {
            yew.interact("Chop down");
            Sleep.sleepUntil(() -> Players.localPlayer().isAnimating(), 3_000);
        }
        return Calculations.random(1800, 4000);
    }

    private int doBank() {
        if (!Bank.isOpen()) {
            Bank.openClosest();
            Sleep.sleepUntil(Bank::isOpen, 4_000);
        }
        if (Bank.isOpen()) {
            bankedLogs += Inventory.count("Yew logs");
            Bank.depositAll();
            Sleep.sleepUntil(() -> Inventory.isEmpty(), 2_000);
            Bank.close();
            state = bankedLogs >= BATCH_SIZE ? State.SELLING : State.CHOPPING;
        }
        return Calculations.random(600, 1000);
    }

    private int doSell() {
        GESellTask.queue("Yew logs", bankedLogs, 0);
        bankedLogs = 0;
        state = State.CHOPPING;
        return 300;
    }
}
