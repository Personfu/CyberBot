package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;

/**
 * Progressive mining trainer. Picks the highest tier the player can mine
 * (and use a banker for) based on level; powermines tin/copper up to 15,
 * iron 15-40, coal/MLM beyond.
 *
 * <p>Modeled after Pfft's Miner / Sub Mining tier strategies.</p>
 */
public final class MiningModule extends SkillModule {

    @Override public String name() { return "Mining"; }
    @Override public Skill  skill() { return Skill.MINING; }

    @Override public String[] methods() {
        return new String[] { "tin_powermine", "iron_powermine", "iron_bank", "coal_bank", "motherlode" };
    }

    @Override public String pickMethod(int currentLevel, int targetLevel) {
        if (currentLevel < 15) return "tin_powermine";
        if (currentLevel < 30) return "iron_powermine";
        if (currentLevel < 45) return "iron_bank";
        if (currentLevel < 60) return "coal_bank";
        return "motherlode";
    }

    @Override public int tick(String method) {
        switch (method == null ? "" : method) {
            case "tin_powermine":   return mineThenDrop("Tin");
            case "iron_powermine":  return mineThenDrop("Iron");
            case "iron_bank":       return mineToBank("Iron");
            case "coal_bank":       return mineToBank("Coal");
            case "motherlode":      return motherlode();
            default:                return 600;
        }
    }

    private int mineThenDrop(String oreName) {
        if (Inventory.isFull()) {
            Inventory.dropAll(item -> item != null && !item.getName().toLowerCase().contains("pickaxe"));
            return Calculations.random(600, 900);
        }
        GameObject rock = GameObjects.closest(g -> g != null && g.getName() != null
                && g.getName().toLowerCase().contains("rocks")
                && g.hasAction("Mine"));
        if (rock != null && rock.interact("Mine")) {
            return Calculations.random(900, 1800);
        }
        return Calculations.random(400, 800);
    }

    private int mineToBank(String oreName) {
        if (Inventory.isFull()) {
            Walking.walkToBank();
            return Calculations.random(1200, 1800);
        }
        return mineThenDrop(oreName);
    }

    private int motherlode() {
        // Simplified: drop sack-paydirt routine handled by a dedicated MLM
        // sub-task in a future revision. For now, fall back to coal banking.
        return mineToBank("Coal");
    }
}
