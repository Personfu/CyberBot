package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.skills.SkillModule;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

/**
 * BrutusKillerModule — trains Attack, Strength, Defence, and Hitpoints by
 * farming Brutus, the giant cow boss from the "Ides of Milk" quest (F2P).
 *
 * <h3>Strategy</h3>
 * <ol>
 *   <li>Walk to Brutus' spawn in the Lumbridge cow field.</li>
 *   <li>Attack Brutus. While on cooldown, attack regular cows for XP.</li>
 *   <li>Loot Cowhide and Raw beef from kills.</li>
 *   <li>Cook Raw beef on a nearby fire (light one with logs if needed).</li>
 *   <li>Eat cooked beef when HP falls below 45%.</li>
 *   <li>Bank Cowhide at Lumbridge bank when inventory is nearly full.</li>
 * </ol>
 *
 * <h3>Profit</h3>
 * Cowhide ~180gp each → ~100 kills/hr @ 3 hides/kill = ~54,000gp/hr free food
 * included. Excellent F2P gold/XP ratio for new accounts.
 *
 * <h3>Requirements</h3>
 * "Ides of Milk" quest complete to unlock Brutus rechallenge. Regular cows
 * work as fallback if Brutus is unavailable.
 */
public final class BrutusKillerModule extends SkillModule {

    // ── Location constants ────────────────────────────────────────────────────
    /** Centre of the Lumbridge cow field (Brutus' main roam area). */
    private static final Tile BRUTUS_SPAWN = new Tile(3259, 3267, 0);
    /** Bounding box of the cow field east of Lumbridge. */
    private static final Area COW_FIELD    = new Area(3244, 3254, 3273, 3282, 0);
    /** Lumbridge castle bank (floor 2). */
    private static final Tile BANK_TILE    = new Tile(3208, 3220, 2);
    /** Bank area check (must be upstairs). */
    private static final Area BANK_AREA    = new Area(3203, 3215, 3213, 3228, 2);

    // ── Tuning ────────────────────────────────────────────────────────────────
    /** HP percentage below which the bot will eat food. */
    private static final int  EAT_AT_PCT          = 45;
    /** Number of Cowhide to hold before banking. */
    private static final int  BANK_COWHIDE_AT     = 18;

    // ─────────────────────────────────────────────────────────────────────────

    @Override public String name()  { return "Brutus"; }
    @Override public Skill  skill() { return Skill.ATTACK; }

    @Override public String[] methods() {
        return new String[] { "brutus", "cows" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        // Always prefer Brutus — more XP per kill, scales with account level.
        return "brutus";
    }

    @Override
    public int tick(String method) {

        // 1. Heal if low HP
        if (shouldEat()) {
            eatBestFood();
            return Calculations.random(400, 700);
        }

        // 2. Bank cowhide when we have too many
        if (Inventory.count("Cowhide") >= BANK_COWHIDE_AT) {
            return handleBanking();
        }

        // 3. Cook any raw beef if a fire is nearby and we're not in combat
        if (Inventory.contains("Raw beef") && !isInCombat()) {
            int cookResult = tryCookBeef();
            if (cookResult > 0) return cookResult;
            // No fire available — drop raw beef to free inventory space
            Item rawBeef = Inventory.get("Raw beef");
            if (rawBeef != null) rawBeef.interact("Drop");
            return Calculations.random(400, 600);
        }

        // 4. Navigate to the cow field if we're outside it
        if (!COW_FIELD.contains(Players.getLocal())) {
            Walking.walk(BRUTUS_SPAWN);
            return Calculations.random(1200, 2000);
        }

        // 5. Set combat style (Accurate → funnels XP to Attack)
        ensureAttackStyle();

        // 6. Already fighting something — wait
        if (isInCombat()) {
            return Calculations.random(800, 1400);
        }

        // 7. Attack Brutus first; fall back to regular cows while he respawns
        NPC brutus = NPCs.closest(n -> n != null
                && n.getName() != null
                && n.getName().contains("Brutus")
                && n.getHealthPercent() > 0
                && !n.isInCombat()
                && COW_FIELD.contains(n));
        if (brutus != null) {
            brutus.interact("Attack");
            return Calculations.random(1200, 2200);
        }

        NPC cow = NPCs.closest(n -> n != null
                && "Cow".equals(n.getName())
                && n.getHealthPercent() > 0
                && !n.isInCombat()
                && COW_FIELD.contains(n));
        if (cow != null) {
            cow.interact("Attack");
            return Calculations.random(1200, 2000);
        }

        return Calculations.random(500, 900);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isInCombat() {
        return Players.getLocal().isInCombat()
                || Players.getLocal().getInteractingCharacter() != null;
    }

    private static boolean shouldEat() {
        int current = Skills.getBoostedLevel(Skill.HITPOINTS);
        int max     = Skills.getLevel(Skill.HITPOINTS);
        return max > 0 && (current * 100 / max) < EAT_AT_PCT;
    }

    private static void eatBestFood() {
        Item food = Inventory.get(i -> i != null && i.getName() != null
                && (i.getName().contains("beef")
                 || i.getName().contains("Trout")
                 || i.getName().contains("Salmon")
                 || i.getName().contains("Pike")
                 || i.getName().contains("Shrimp")
                 || i.getName().contains("Sardine")
                 || i.getName().contains("Herring")
                 || i.getName().contains("Bread")));
        if (food != null) food.interact("Eat");
    }

    private static void ensureAttackStyle() {
        try {
            if (Combat.getFightMode() != CombatStyle.ACCURATE) {
                Combat.toggleAttackStyle(CombatStyle.ACCURATE);
            }
        } catch (Exception ignored) {
            // Best-effort — some weapon types may not support ACCURATE
        }
    }

    /**
     * Attempt to cook Raw beef on a nearby fire.
     * Returns sleep ms on success, 0 if no fire found.
     */
    private static int tryCookBeef() {
        GameObject fire = GameObjects.closest(o -> o != null
                && "Fire".equalsIgnoreCase(o.getName())
                && COW_FIELD.contains(o));
        if (fire == null) return 0;

        Item beef = Inventory.get("Raw beef");
        if (beef == null) return 0;

        beef.interact("Use");
        fire.interact("Use");
        return Calculations.random(1000, 2000);
    }

    /** Walk to Lumbridge bank and deposit all Cowhide. */
    private static int handleBanking() {
        if (!BANK_AREA.contains(Players.getLocal())) {
            Walking.walk(BANK_TILE);
            return Calculations.random(1400, 2400);
        }
        Bank.open();
        if (Bank.isOpen()) {
            Bank.depositAll("Cowhide");
            Bank.close();
        }
        return Calculations.random(800, 1400);
    }
}
