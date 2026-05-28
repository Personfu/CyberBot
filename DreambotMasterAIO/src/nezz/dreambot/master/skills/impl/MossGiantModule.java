package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.id.ItemID;
import nezz.dreambot.master.id.NpcID;
import nezz.dreambot.master.id.ObjectID;
import nezz.dreambot.master.skills.SkillModule;
import nezz.dreambot.master.util.CombatUtil;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

/**
 * MossGiantModule — MAIN anti-detection training method.
 *
 * <h3>Location</h3>
 * Varrock Sewers, accessed via the manhole south-east of Varrock (~Tile 3289, 3394).
 * A knife is required to slash the cobwebs that block the path to the Moss Giant
 * back room. A wall safe-spot at the end of the corridor lets the bot stand still
 * and cast/shoot indefinitely without the Moss Giants being able to reach it.
 *
 * <h3>Why this beats splashing / AFK alching for detection</h3>
 * <ul>
 *   <li>Bot moves through the dungeon each banking trip — varied walk path.</li>
 *   <li>Loot pickup randomises timing between casts.</li>
 *   <li>Bone-burying adds natural idle breaks between kill cycles.</li>
 *   <li>Safe-spot means the bot never takes damage → no food-click tells.</li>
 *   <li>Camera angle is naturally varied by DreamBot Antiban between kills.</li>
 * </ul>
 *
 * <h3>Training goal</h3>
 * Magic 1 → 55 in a single session (roughly 8-12 hrs depending on spell tier).
 * Secondary benefit: Prayer XP from burying Big bones every kill.
 *
 * <h3>Requirements</h3>
 * <ul>
 *   <li>Knife in bank (to slash cobwebs)</li>
 *   <li>Staff + runes appropriate for current Magic level, OR bow + arrows for Ranged</li>
 *   <li>Food in bank (lobster / swordfish preferred; trout accepted)</li>
 * </ul>
 */
public final class MossGiantModule extends SkillModule {

    // ── Surface entrance ─────────────────────────────────────────────────────
    /** Manhole lid, south-east Varrock (user-confirmed x≈3289). */
    private static final Tile MANHOLE_SURFACE  = new Tile(3289, 3394, 0);
    private static final Area MANHOLE_AREA     = new Area(3284, 3389, 3294, 3399, 0);

    // ── Varrock East bank (for restocking) ───────────────────────────────────
    private static final Tile VARROCK_EAST_BANK = new Tile(3253, 3420, 0);
    private static final Area BANK_AREA         = new Area(3249, 3416, 3257, 3424, 0);

    // ── Underground — Varrock Sewers ─────────────────────────────────────────
    /**
     * The whole sewer floor (underground coords). If the player's tile falls
     * inside this area we know we're underground.
     */
    private static final Area SEWER_AREA       = new Area(3140, 9790, 3310, 9950, 0);

    /**
     * The back room where Moss Giants spawn.
     * Confirmed via in-game DreamBot tile overlay: player tile (3165, 9886, 0).
     * Area covers the full corridor + giant room so traverseSewer() knows when
     * we've arrived.
     */
    private static final Area MOSS_ROOM        = new Area(3148, 9870, 3185, 9910, 0);

    /**
     * Wall-corner safe-spot confirmed from screenshot (Player Tile 3165, 9886, 0).
     * Standing here the Moss Giants cannot melee-path to the player; Magic and
     * Ranged reach them freely. The east wall + south wall form the corner pocket.
     */
    private static final Tile SAFE_SPOT        = new Tile(3165, 9886, 0);

    /** Approximate spawn centre — giants cluster west/NW of the safe-spot. */
    private static final Tile TARGET_TILE      = new Tile(3157, 9882, 0);

    /** Max distance from SAFE_SPOT to still consider a giant attackable. */
    private static final int  ATTACK_RADIUS    = 10;

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final int  EAT_AT_PCT       = 55;
    /** Rune stack to withdraw per banking trip for each spell tier. */
    private static final int  RUNE_WITHDRAW    = 250;

    // ── Loot priority list (high → low value) ────────────────────────────────
    private static final String[] LOOT = {
        "Mossy key", "Nature rune", "Chaos rune", "Death rune", "Mithril ore",
        "Limpwurt root", "Herb seed", "Coins", "Big bones"
    };

    // ── State ─────────────────────────────────────────────────────────────────
    /** Whether we are currently on a banking run. */
    private boolean banking = false;
    /** Hop-fallback click helper for ranged attacks on moving NPCs. */
    private final CombatUtil.NpcClickHelper clickHelper = new CombatUtil.NpcClickHelper();

    // ─────────────────────────────────────────────────────────────────────────

    @Override public String name()  { return "MossGiant"; }
    /** Primary skill; tracks Magic level to trigger banking for rune upgrades. */
    @Override public Skill  skill() { return Skill.MAGIC; }

    @Override public String[] methods() {
        return new String[] { "moss_giant_magic", "moss_giant_ranged" };
    }

    @Override public String pickMethod(int curr, int tgt) {
        return "moss_giant_magic"; // default; caller can pass "moss_giant_ranged"
    }

    @Override
    public String[] requiredItems(String method) {
        if ("moss_giant_ranged".equals(method)) {
            return new String[] { "Knife", "Bronze arrow" };
        }
        return new String[] { "Knife", "Air rune", "Mind rune" };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main tick
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public int tick(String method) {

        // ── 1. Always eat first if low ────────────────────────────────────────
        if (shouldEat()) {
            eatFood();
            return Calculations.random(400, 800);
        }

        // ── 2. Banking loop ───────────────────────────────────────────────────
        if (banking || needsBank(method)) {
            banking = true;
            int result = handleBank(method);
            if (result < 0) {
                banking = false; // bank done — head back
            }
            return Math.abs(result);
        }

        // ── 3. Navigate to sewer if not already underground ───────────────────
        if (!SEWER_AREA.contains(Players.getLocal())) {
            return descend();
        }

        // ── 4. Reach the Moss Giant back room through cobwebs ─────────────────
        if (!MOSS_ROOM.contains(Players.getLocal())) {
            return traverseSewer();
        }

        // ── 5. Settle on the safe-spot tile ───────────────────────────────────
        if (!onSafeSpot()) {
            Walking.walk(SAFE_SPOT);
            return Calculations.random(800, 1400);
        }

        // ── 6. Bury Big bones while not in combat ─────────────────────────────
        if (!inCombat() && Inventory.contains("Big bones")) {
            return buryBones();
        }

        // ── 7. Pick up nearby loot while not in combat ────────────────────────
        if (!inCombat()) {
            int looted = lootGround();
            if (looted > 0) return looted;
        }

        // ── 8. Inventory nearly full — quick bank run ─────────────────────────
        if (Inventory.isFull()) {
            banking = true;
            return Calculations.random(400, 600);
        }

        // ── 9. Attack ─────────────────────────────────────────────────────────
        return attack(method);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    /** Find the manhole and climb down into Varrock Sewers. */
    private int descend() {
        // If not near the manhole, walk there first
        if (!MANHOLE_AREA.contains(Players.getLocal())) {
            Walking.walk(MANHOLE_SURFACE);
            return Calculations.random(1800, 2800);
        }
        // Open the manhole lid if it exists, then climb down
        GameObject lid = GameObjects.closest(g -> g != null
                && g.getTile().distance(MANHOLE_SURFACE) <= 3
                && (g.getName().equalsIgnoreCase("Manhole")
                 || g.getName().equalsIgnoreCase("Manhole cover")
                 || g.getID() == ObjectID.MANHOLE_VARROCK_SEWER_LID));
        if (lid != null) {
            // Some manholes need to be opened first, then climbed
            if (lid.hasAction("Open")) {
                lid.interact("Open");
                return Calculations.random(1200, 1800);
            }
            if (lid.hasAction("Climb-down") || lid.hasAction("Enter")) {
                lid.interact(lid.hasAction("Climb-down") ? "Climb-down" : "Enter");
                return Calculations.random(2000, 3200);
            }
        }
        // Fallback: search for any nearby ladder/trapdoor going down
        GameObject down = GameObjects.closest(g -> g != null
                && g.getTile().distance(MANHOLE_SURFACE) <= 4
                && (g.hasAction("Climb-down") || g.hasAction("Enter") || g.hasAction("Climb-in")));
        if (down != null) {
            down.interact(down.hasAction("Climb-down") ? "Climb-down"
                        : down.hasAction("Enter") ? "Enter" : "Climb-in");
            return Calculations.random(2000, 3200);
        }
        // Still walking toward it
        Walking.walk(MANHOLE_SURFACE);
        return Calculations.random(1400, 2000);
    }

    /**
     * Walk from the sewer entrance through to the Moss Giant back room.
     * Cuts any cobwebs blocking the path with the knife in inventory.
     */
    private int traverseSewer() {
        // Check for cobweb in our path (within 8 tiles)
        GameObject cobweb = GameObjects.closest(g -> g != null
                && (g.getID() == ObjectID.COBWEB_VARROCK_SEWER
                 || g.getName().equalsIgnoreCase("Cobweb"))
                && g.getTile().distance(Players.getLocal()) <= 8);

        if (cobweb != null) {
            // Use knife on cobweb (slash interaction)
            if (Inventory.contains("Knife")) {
                Item knife = Inventory.get("Knife");
                if (knife != null) {
                    knife.interact("Use");
                    if (Inventory.isItemSelected()) {
                        cobweb.interact("Use");
                        return Calculations.random(1400, 2200);
                    }
                }
            }
            // Fallback: attack the cobweb (any slash weapon works)
            if (cobweb.interact("Attack")) {
                return Calculations.random(1800, 2600);
            }
        }

        // No cobweb blocking — walk toward Moss Giant room
        Walking.walk(MOSS_ROOM.getCenter());
        return Calculations.random(1600, 2400);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Combat
    // ─────────────────────────────────────────────────────────────────────────

    private int attack(String method) {
        // Already fighting — let it tick
        if (inCombat()) {
            return Calculations.random(1200, 2400);
        }

        NPC giant = NPCs.closest(n -> n != null
                && n.getID() == NpcID.MOSS_GIANT
                && n.getHealthPercent() > 0
                && !n.isInCombat()
                && GIANT_AREA.contains(n.getTile())
                && n.hasAction("Attack"));

        if (giant == null) {
            // All giants in combat or dead — wait for respawn tick
            return Calculations.random(2400, 5000);
        }

        if ("moss_giant_ranged".equals(method)) {
            return attackRanged(giant);
        }
        return attackMagic(giant);
    }

    private int attackMagic(NPC giant) {
        int lvl = Skills.getRealLevel(Skill.MAGIC);
        Normal spell = spellForLevel(lvl);
        if (Magic.castSpellOn(spell, giant)) {
            return Calculations.random(2400, 3800);
        }
        return Calculations.random(700, 1200);
    }

    private int attackRanged(NPC giant) {
        if (Combat.getCombatStyle() != CombatStyle.RANGED_RAPID) {
            Combat.setCombatStyle(CombatStyle.RANGED_RAPID);
            return Calculations.random(400, 700);
        }
        if (clickHelper.tryClick(giant, "Attack")) {
            return Calculations.random(1800, 3000);
        }
        return Calculations.random(700, 1200);
    }

    /**
     * Returns the best attack spell the player can currently cast.
     * Prioritises bolt spells (better XP/rune) once available.
     */
    private static Normal spellForLevel(int lvl) {
        if (lvl >= 45) return Normal.FIRE_BOLT;
        if (lvl >= 35) return Normal.EARTH_BOLT;
        if (lvl >= 29) return Normal.WATER_BOLT;
        if (lvl >= 23) return Normal.WIND_BOLT;
        if (lvl >= 13) return Normal.FIRE_STRIKE;
        if (lvl >= 9)  return Normal.EARTH_STRIKE;
        if (lvl >= 5)  return Normal.WATER_STRIKE;
        return Normal.WIND_STRIKE;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Loot + bones
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns ms slept if an item was taken, else 0. */
    private int lootGround() {
        for (String name : LOOT) {
            GroundItem item = GroundItems.closest(gi -> gi != null
                    && gi.getName() != null
                    && gi.getName().equalsIgnoreCase(name)
                    && gi.getTile().distance(SAFE_SPOT) <= 5);
            if (item != null && item.interact("Take")) {
                return Calculations.random(800, 1600);
            }
        }
        return 0;
    }

    private int buryBones() {
        Item bones = Inventory.get("Big bones");
        if (bones != null && bones.interact("Bury")) {
            return Calculations.random(1600, 2000); // bury animation is ~1.8s
        }
        return Calculations.random(400, 600);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Banking
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @return positive ms to wait (still banking), negative ms (banking done, return to spot).
     */
    private int handleBank(String method) {
        // If still underground, climb out first
        if (SEWER_AREA.contains(Players.getLocal())) {
            return climbOut();
        }

        // Walk to Varrock East bank
        if (!BANK_AREA.contains(Players.getLocal())) {
            Walking.walk(VARROCK_EAST_BANK);
            return Calculations.random(1800, 2800);
        }

        // Open bank
        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(1200, 2000);
        }

        // Deposit loot (keep knife, weapons, armor)
        Bank.depositAllExcept(i -> i != null && i.getName() != null && (
                i.getName().equalsIgnoreCase("Knife")
             || i.getName().toLowerCase().contains("staff")
             || i.getName().toLowerCase().contains("bow")
             || i.getName().toLowerCase().contains("robe")
             || i.getName().toLowerCase().contains("body")
             || i.getName().toLowerCase().contains("chaps")
             || i.getName().toLowerCase().contains("vambraces")));

        // Ensure knife in inventory
        if (!Inventory.contains("Knife") && Bank.contains("Knife")) {
            Bank.withdraw("Knife", 1);
        }

        // Withdraw food
        boolean gotFood = false;
        for (String food : new String[] { "Swordfish", "Lobster", "Tuna", "Salmon", "Trout" }) {
            if (Bank.contains(food)) {
                Bank.withdraw(food, 8);
                gotFood = true;
                break;
            }
        }
        if (!gotFood) {
            // No food in bank — withdraw cooked meat as last resort
            if (Bank.contains("Cooked meat")) Bank.withdraw("Cooked meat", 10);
        }

        // Withdraw runes / ammo
        if ("moss_giant_ranged".equals(method)) {
            withdrawAmmo();
        } else {
            withdrawRunes(Skills.getRealLevel(Skill.MAGIC));
        }

        Bank.close();
        return -(Calculations.random(1200, 2000)); // negative signals "done"
    }

    private void withdrawRunes(int magicLvl) {
        if (magicLvl >= 45) {
            // Fire bolt: fire rune + chaos rune + air rune
            if (Bank.contains("Fire rune"))  Bank.withdraw("Fire rune",  RUNE_WITHDRAW);
            if (Bank.contains("Chaos rune")) Bank.withdraw("Chaos rune", RUNE_WITHDRAW / 3);
            if (Bank.contains("Air rune"))   Bank.withdraw("Air rune",   RUNE_WITHDRAW / 2);
        } else if (magicLvl >= 13) {
            // Fire strike: fire + air + mind
            if (Bank.contains("Fire rune")) Bank.withdraw("Fire rune",  RUNE_WITHDRAW);
            if (Bank.contains("Air rune"))  Bank.withdraw("Air rune",   RUNE_WITHDRAW);
            if (Bank.contains("Mind rune")) Bank.withdraw("Mind rune",  RUNE_WITHDRAW);
        } else {
            // Wind strike: air + mind
            if (Bank.contains("Air rune"))  Bank.withdraw("Air rune",  RUNE_WITHDRAW);
            if (Bank.contains("Mind rune")) Bank.withdraw("Mind rune", RUNE_WITHDRAW);
        }
    }

    private void withdrawAmmo() {
        for (String arrow : new String[] { "Rune arrow", "Adamant arrow", "Mithril arrow",
                                           "Steel arrow", "Iron arrow", "Bronze arrow" }) {
            if (Bank.contains(arrow)) {
                Bank.withdraw(arrow, RUNE_WITHDRAW);
                break;
            }
        }
    }

    /** Find ladder/trapdoor going back up to surface. */
    private int climbOut() {
        GameObject up = GameObjects.closest(g -> g != null
                && (g.getName().equalsIgnoreCase("Ladder")
                 || g.getName().equalsIgnoreCase("Manhole")
                 || g.getName().equalsIgnoreCase("Trapdoor"))
                && (g.hasAction("Climb-up") || g.hasAction("Leave")));
        if (up != null && (up.interact("Climb-up") || up.interact("Leave"))) {
            return Calculations.random(2000, 3200);
        }
        // Walk toward approximate exit location
        Walking.walk(new Tile(3237, 9857, 0));
        return Calculations.random(1800, 2800);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private boolean needsBank(String method) {
        if (!hasFood()) return true;
        if ("moss_giant_ranged".equals(method)) {
            return !Inventory.contains(i -> i != null && i.getName() != null
                    && i.getName().toLowerCase().contains("arrow"));
        }
        // Need at least one type of rune
        return !Inventory.contains(i -> i != null && i.getName() != null
                && i.getName().toLowerCase().contains("rune"));
    }

    private boolean hasFood() {
        return Inventory.contains(i -> i != null && i.getName() != null && (
                i.getName().equalsIgnoreCase("Swordfish")
             || i.getName().equalsIgnoreCase("Lobster")
             || i.getName().equalsIgnoreCase("Tuna")
             || i.getName().equalsIgnoreCase("Salmon")
             || i.getName().equalsIgnoreCase("Trout")
             || i.getName().equalsIgnoreCase("Cooked meat")
             || i.getName().equalsIgnoreCase("Bread")));
    }

    private boolean shouldEat() {
        int max = Skills.getRealLevel(Skill.HITPOINTS);
        int cur = Skills.getBoostedLevel(Skill.HITPOINTS);
        return cur < max * EAT_AT_PCT / 100;
    }

    private void eatFood() {
        Item food = Inventory.get(i -> i != null && i.getName() != null && (
                i.getName().equalsIgnoreCase("Swordfish")
             || i.getName().equalsIgnoreCase("Lobster")
             || i.getName().equalsIgnoreCase("Tuna")
             || i.getName().equalsIgnoreCase("Salmon")
             || i.getName().equalsIgnoreCase("Trout")
             || i.getName().equalsIgnoreCase("Cooked meat")
             || i.getName().equalsIgnoreCase("Bread")));
        if (food != null) food.interact("Eat");
    }

    private boolean onSafeSpot() {
        Tile t = Players.getLocal().getTile();
        return t != null && t.distance(SAFE_SPOT) <= 1;
    }

    private boolean inCombat() {
        return Players.getLocal().isInCombat()
                || Players.getLocal().getInteractingCharacter() != null;
    }
}
