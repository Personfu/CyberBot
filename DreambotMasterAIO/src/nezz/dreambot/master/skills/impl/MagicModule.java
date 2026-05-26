package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Magic trainer:
 * <pre>
 *   1-13:  Wind Strike on chickens (or splashing on monk)
 *  13-25:  Water Strike → Earth Strike → Fire Strike on cows
 *  25-55:  Splashing or Curse on guards (AFK route)
 *  55+:    High Alch / Camelot Teleport / String Jewellery
 * </pre>
 */
public final class MagicModule extends SkillModule {

    @Override public String name() { return "Magic"; }
    @Override public Skill  skill() { return Skill.MAGIC; }

    @Override public String[] methods() {
        return new String[] { "wind_strike", "fire_strike", "splash", "high_alch" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        if (curr < 13) return "wind_strike";
        if (curr < 25) return "fire_strike";
        if (curr < 55) return "splash";
        return "high_alch";
    }

    @Override public int tick(String method) {
        switch (method == null ? "" : method) {
            case "fire_strike":  return cast(Normal.FIRE_STRIKE, "Cow");
            case "splash":       return splash();
            case "high_alch":    return highAlch();
            default:             return cast(Normal.WIND_STRIKE, "Chicken");
        }
    }

    private int cast(Normal spell, String npcName) {
        NPC target = NPCs.closest(n -> n != null && npcName.equalsIgnoreCase(n.getName())
                && !n.isInCombat() && n.getHealthPercent() > 0);
        if (target != null && Magic.castSpellOn(spell, target)) {
            return Calculations.random(2400, 3600);
        }
        return Calculations.random(700, 1100);
    }

    private int splash() {
        // Find any low-level NPC that will not retaliate enough to break us
        // (typically a level-2 Monk in Edgeville monastery with -65 mage).
        NPC target = NPCs.closest("Monk");
        if (target != null && Magic.castSpellOn(Normal.CURSE, target)) {
            return Calculations.random(2400, 3600);
        }
        return Calculations.random(700, 1100);
    }

    private int highAlch() {
        if (!Inventory.isItemSelected()) {
            if (Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
                return Calculations.random(700, 900);
            }
            return Calculations.random(450, 700);
        }
        // Click an alchable item — anything except runes / coins.
        Inventory.interact(it -> it != null
                && !it.getName().toLowerCase().contains("rune")
                && !it.getName().equalsIgnoreCase("Coins"), "Cast");
        return Calculations.random(1200, 1800);
    }
}
