package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cow-killing money route — the classic F2P income source.
 *
 * <h3>Loop</h3>
 * <ol>
 *   <li>Kill cows in the Lumbridge cow field (or Edgeville).</li>
 *   <li>Collect cowhide (priority) and raw beef.</li>
 *   <li>When inventory full → bank at Lumbridge.</li>
 *   <li>Every {@code BATCH_SIZE} hides → queue sell to GE
 *       (tan to hard leather first if high enough Crafting).</li>
 * </ol>
 *
 * <h3>GP/hr estimates</h3>
 * <ul>
 *   <li>Raw cowhide: ~90-130 gp each × 200/hr = ~20,000-26,000 gp/hr</li>
 *   <li>Hard leather (tan at Al-Kharid): ~225 gp × 150/hr ≈ 33,000 gp/hr</li>
 * </ul>
 */
public final class CowhideRoute extends MoneyRoute {

    // Lumbridge cow field
    private static final Tile COW_FIELD   = new Tile(3252, 3265, 0);
    // Lumbridge bank
    private static final Tile LUM_BANK    = new Tile(3208, 3220, 0);
    // Al-Kharid tanner (Ellis)
    private static final Tile TANNER_TILE = new Tile(3271, 3190, 0);

    private static final String COWHIDE    = "Cowhide";
    private static final String RAW_BEEF   = "Raw beef";
    private static final String HARD_LEATH = "Hard leather";
    private static final int    BATCH_SIZE = 280; // ~10 full inventories

    private int bankedHides = 0;
    private boolean tanningMode = false;
    private State state = State.KILLING;

    private enum State { KILLING, BANKING, TANNING, SELLING }

    @Override public String id()          { return "cowhide"; }
    @Override public String description() { return "Cow Field → Cowhide/Hard Leather → GE"; }
    @Override public int estimatedGpHr()  { return 24_000; }

    @Override public Map<Skill, Integer> requirements() {
        Map<Skill, Integer> r = new LinkedHashMap<>();
        r.put(Skill.ATTACK, 1);
        return r;
    }

    @Override public int tick() {
        switch (state) {
            case KILLING:  return doKilling();
            case BANKING:  return doBanking();
            case TANNING:  return doTanning();
            case SELLING:  return doSelling();
            default:       return 600;
        }
    }

    // ── phase implementations ─────────────────────────────────────────────────

    private int doKilling() {
        if (Inventory.isFull()) {
            state = State.BANKING;
            return 300;
        }

        // Walk to cow field if far
        if (COW_FIELD.distance(Players.getLocal()) > 20) {
            Walking.walk(COW_FIELD);
            return Calculations.random(1200, 1800);
        }

        // Pick up cowhide on the ground first
        org.dreambot.api.wrappers.items.GroundItem hide =
            org.dreambot.api.methods.item.GroundItems.closest(COWHIDE);
        if (hide != null && hide.distance(Players.getLocal()) < 5) {
            hide.interact("Take");
            Sleep.sleepUntil(() -> Inventory.contains(COWHIDE), 1500);
            return Calculations.random(300, 600);
        }

        // Attack the nearest cow
        if (Combat.getHealthPercent() < 20) {
            // Low HP — eat if we have food; otherwise walk to safe spot briefly
            org.dreambot.api.wrappers.items.Item food =
                Inventory.get(i -> i != null && i.hasAction("Eat"));
            if (food != null) food.interact("Eat");
            return Calculations.random(600, 1000);
        }

        NPC cow = NPCs.closest(n -> n != null
                && (n.getName().equals("Cow") || n.getName().equals("Cow calf"))
                && !n.isInCombat());
        if (cow == null) {
            // Hop world if all cows contested
            Walking.walk(COW_FIELD);
            return Calculations.random(1000, 1500);
        }
        if (!cow.isInCombat() || cow.isInteractedWith()) {
            cow.interact("Attack");
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 2000);
        }
        return Calculations.random(600, 1200);
    }

    private int doBanking() {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3000);
        }
        if (Bank.isOpen()) {
            // Count BEFORE depositing — depositAllItems empties the inventory
            bankedHides += Inventory.count(COWHIDE);
            Bank.depositAllItems();
            Sleep.sleepUntil(() -> !Inventory.contains(COWHIDE), 1500);
            Bank.close();
            // Check if we have enough to sell
            if (bankedHides >= BATCH_SIZE) {
                state = tanningMode ? State.TANNING : State.SELLING;
            } else {
                state = State.KILLING;
            }
        }
        return Calculations.random(800, 1200);
    }

    private int doTanning() {
        // Walk to Ellis the tanner in Al-Kharid
        if (TANNER_TILE.distance(Players.getLocal()) > 5) {
            Walking.walk(TANNER_TILE);
            return Calculations.random(1200, 1800);
        }
        // Withdraw cowhides from bank (use Al-Kharid bank)
        if (!Inventory.contains(COWHIDE)) {
            Bank.open();
            if (Bank.isOpen()) {
                Bank.withdrawAll(COWHIDE);
                Bank.close();
            }
            return Calculations.random(800, 1200);
        }
        // Interact with Ellis
        NPC ellis = NPCs.closest("Ellis");
        if (ellis != null) {
            ellis.interact("Trade");
            Sleep.sleepUntil(() ->
                org.dreambot.api.methods.widget.Widgets.get(324, 0) != null, 3000);
            // Click "Tan All" → hard leather (interface child depends on client version)
            try {
                org.dreambot.api.methods.widget.Widgets.get(324, 14).interact("Tan");
                Sleep.sleepUntil(() -> !Inventory.contains(COWHIDE), 10_000);
            } catch (Throwable ignored) { }
        }
        state = State.SELLING;
        return Calculations.random(600, 1000);
    }

    private int doSelling() {
        String item = tanningMode ? HARD_LEATH : COWHIDE;
        GESellTask.queue(item, bankedHides, 0);
        bankedHides = 0;
        state = State.KILLING;
        return 300;
    }

    public void enableTanning(boolean on) { this.tanningMode = on; }
}


