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

/**
 * Runecrafting trainer — F2P progressive route to 99.
 *
 * <pre>
 *  1-14  : Air runes  → Air Altar (south of Falador)   — 5 gp each
 * 14-27  : Mind runes → Mind Altar (north of Falador)
 * 27-35  : Water runes → Water Altar (east of Draynor)
 * 35-44  : Earth runes → Earth Altar (Varrock east)
 * 44-54  : Fire runes  → Fire Altar (Al-Kharid area)
 * 54-99  : Body runes  → Body Altar (SE of GE)  — most trips per hr, best F2P
 * </pre>
 *
 * <p>Uses Pure Essence (bought from GE or from Rune Mysteries quest reward).</p>
 */
public final class RunecraftingModule extends SkillModule {

    // Altar entrance ruins
    private static final Tile AIR_RUIN   = new Tile(2983, 3292, 0);
    private static final Tile MIND_RUIN  = new Tile(2793, 3825, 0);
    private static final Tile WATER_RUIN = new Tile(3184, 3162, 0);
    private static final Tile EARTH_RUIN = new Tile(3308, 3472, 0);
    private static final Tile FIRE_RUIN  = new Tile(3312, 3255, 0);
    private static final Tile BODY_RUIN  = new Tile(3057, 3445, 0);

    private static final Tile FALADOR_BANK = new Tile(2946, 3368, 0);
    private static final Tile VARROCK_BANK = new Tile(3185, 3436, 0);
    private static final Tile GE_BANK      = new Tile(3167, 3487, 0);

    @Override public String name()  { return "Runecrafting"; }
    @Override public Skill  skill() { return Skill.RUNECRAFTING; }

    @Override public String[] methods() {
        return new String[] { "air", "mind", "water", "earth", "fire", "body" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 14)  return "air";
        if (curr < 27)  return "mind";
        if (curr < 35)  return "water";
        if (curr < 44)  return "earth";
        if (curr < 54)  return "fire";
        return "body";
    }

    @Override public int tick(String method) {
        Tile altarRuin = ruinFor(method);
        Tile bankTile  = bankFor(method);

        // Phase 1: withdraw pure essence
        if (!Inventory.contains("Pure essence") && !Inventory.contains("Rune essence")) {
            return doWithdraw(bankTile);
        }

        // Phase 2: walk to altar ruin
        if (altarRuin.distance(Players.getLocal()) > 8) {
            Walking.walk(altarRuin);
            return Calculations.random(1200, 2000);
        }

        // Phase 3: enter ruin
        if (GameObjects.closest("Altar") == null) {
            GameObject ruin = GameObjects.closest(g -> g != null
                    && g.getName().equals("Mysterious ruins")
                    && g.hasAction("Enter"));
            if (ruin == null) {
                Walking.walk(altarRuin);
                return Calculations.random(800, 1200);
            }
            ruin.interact("Enter");
            Sleep.sleepUntil(() -> GameObjects.closest("Altar") != null, 3_000);
            return Calculations.random(500, 800);
        }

        // Phase 4: craft runes
        GameObject altar = GameObjects.closest("Altar");
        if (altar != null) {
            altar.interact("Craft-rune");
            Sleep.sleepUntil(() ->
                !Inventory.contains("Pure essence") && !Inventory.contains("Rune essence"),
                8_000);
        }

        // Phase 5: exit via portal
        GameObject portal = GameObjects.closest(g -> g != null && g.hasAction("Use"));
        if (portal != null) portal.interact("Use");
        Sleep.sleep(Calculations.random(800, 1200));

        // Phase 6: bank
        return doBank(bankTile);
    }

    private int doWithdraw(Tile bankTile) {
        if (bankTile.distance(Players.getLocal()) > 10) {
            Walking.walk(bankTile);
            return Calculations.random(1200, 2000);
        }
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3_000);
        }
        if (Bank.isOpen()) {
            Bank.withdrawAll("Pure essence");
            if (!Inventory.contains("Pure essence")) Bank.withdrawAll("Rune essence");
            Bank.close();
        }
        return Calculations.random(500, 800);
    }

    private int doBank(Tile bankTile) {
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 4_000);
        }
        if (Bank.isOpen()) {
            Bank.depositAllItems();
            Bank.withdrawAll("Pure essence");
            if (!Inventory.contains("Pure essence")) Bank.withdrawAll("Rune essence");
            Bank.close();
        }
        return Calculations.random(400, 700);
    }

    private static Tile ruinFor(String m) {
        switch (m) {
            case "mind":  return MIND_RUIN;
            case "water": return WATER_RUIN;
            case "earth": return EARTH_RUIN;
            case "fire":  return FIRE_RUIN;
            case "body":  return BODY_RUIN;
            default:      return AIR_RUIN;
        }
    }
    private static Tile bankFor(String m) {
        switch (m) {
            case "air":
            case "mind":  return FALADOR_BANK;
            case "water": return new Tile(3093, 3243, 0); // Draynor bank
            case "earth":
            case "body":  return VARROCK_BANK;
            case "fire":  return new Tile(3269, 3167, 0); // Al-Kharid bank
            default:      return GE_BANK;
        }
    }
}
