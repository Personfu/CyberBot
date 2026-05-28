package nezz.dreambot.master.skills.impl;

import nezz.dreambot.master.ge.GESellTask;
import nezz.dreambot.master.ge.GrandExchangeUtil;
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
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
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
import org.dreambot.api.utilities.Sleep;
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
    private static final Area GIANT_AREA       = new Area(3148, 9870, 3185, 9910, 0);

    /** Max distance from SAFE_SPOT to still consider a giant attackable. */
    private static final int  ATTACK_RADIUS    = 10;

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final int  EAT_AT_PCT       = 55;
    /** Rune stack to withdraw per banking trip for each spell tier. */
    private static final int  RUNE_WITHDRAW    = 250;
    /** Minimum coins in bank before attempting to restock runes at GE. */
    private static final int  GE_REFILL_COINS  = 15_000;
    private static final String[] GE_SELL_ITEMS = {
        "Nature rune", "Chaos rune", "Death rune", "Mithril ore", "Limpwurt root"
    };

    // ── Loot priority list (high → low value) ────────────────────────────────
    private static final String[] LOOT = {
        "Mossy key", "Nature rune", "Chaos rune", "Death rune", "Mithril ore",
        "Limpwurt root", "Herb seed", "Coins", "Big bones"
    };

    private static final String BOSS_KEY = "Mossy key";
    private static final Tile BOSS_DOOR_CENTER = new Tile(3163, 9880, 0);
    private static final Area BOSS_DOOR_AREA = new Area(3156, 9872, 3175, 9892, 0);
    private static final Area BOSS_FIGHT_AREA = new Area(3150, 9860, 3185, 9895, 0);
    private static final int RETREAT_HP_PCT = 55;
    private static final String[] BOSS_ARMOR_CHEST = { "Steel platebody", "Iron platebody" };
    private static final String[] BOSS_ARMOR_LEGS  = { "Steel platelegs", "Iron platelegs" };
    private static final String[] BOSS_ARMOR_HEAD  = { "Steel full helm", "Iron full helm" };
    private static final String[] BOSS_ARMOR_SHIELD = { "Steel kiteshield", "Iron kiteshield" };
    private static final String[] BOSS_WEAPONS = { "Iron axe", "Steel axe" };
    private static final int BOSS_GEAR_MAX_BUY_PRICE = 5_000;
    private static final int BOSS_GEAR_GE_BUFF_COINS = 10_000;

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
        if (hasRangedWeapon() && hasRangedAmmo()
                && Skills.getRealLevel(Skill.RANGED) >= Skills.getRealLevel(Skill.MAGIC)) {
            return "moss_giant_ranged";
        }
        if (canCastEffectiveSpell(Skills.getRealLevel(Skill.MAGIC))) {
            return "moss_giant_magic";
        }
        if (hasRangedWeapon() && hasRangedAmmo()) {
            return "moss_giant_ranged";
        }
        return "moss_giant_magic";
    }

    @Override
    public String[] requiredItems(String method) {
        if ("moss_giant_ranged".equals(method)) {
            return new String[] { "Knife", "Arrow", "Bow" };
        }
        return new String[] { "Knife", "Air rune", "Mind rune" };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main tick
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public int tick(String method) {
        method = selectBestCombatMethod(method);

        // ── 1. Always eat first if low ────────────────────────────────────────
        if (shouldEat()) {
            eatFood();
            return Calculations.random(400, 800);
        }

        // ── 2. Boss key / banking loop ─────────────────────────────────────────
        if (hasBossKey()) {
            if (shouldRetreat()) {
                banking = true;
                int result = handleBank(method);
                if (result < 0) banking = false;
                return Math.abs(result);
            }
            if (banking || needsBank(method)) {
                banking = true;
                int result = handleBank(method);
                if (result < 0) {
                    banking = false; // bank done — head back
                }
                return Math.abs(result);
            }
        }

        // ── 3. Navigate to sewer if not already underground ───────────────────
        if (!SEWER_AREA.contains(Players.getLocal())) {
            return descend();
        }

        if (hasBossKey()) {
            int bossAction = handleBossKeyPath();
            if (bossAction > 0) return bossAction;
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
        if (hasBossKey() && BOSS_DOOR_AREA.contains(Players.getLocal())) {
            return attackBoss();
        }
        return attack(method);
    }

    private String selectBestCombatMethod(String method) {
        if (method == null || method.isEmpty()) {
            method = "moss_giant_magic";
        }

        int magicLvl = Skills.getRealLevel(Skill.MAGIC);
        int rangedLvl = Skills.getRealLevel(Skill.RANGED);

        boolean canMagic = canCastEffectiveSpell(magicLvl);
        boolean canRange = hasRangedAmmo() && hasRangedWeapon();

        if (canRange && rangedLvl > magicLvl) {
            return "moss_giant_ranged";
        }
        if (canMagic) {
            return "moss_giant_magic";
        }
        if (canRange) {
            return "moss_giant_ranged";
        }
        return method;
    }

    private boolean hasRangedAmmo() {
        return Inventory.contains(i -> i != null && i.getName() != null
                && i.getName().toLowerCase().contains("arrow"));
    }

    private boolean hasRangedWeapon() {
        return Inventory.contains(i -> i != null && i.getName() != null && (
                i.getName().toLowerCase().contains("bow")
             || i.getName().toLowerCase().contains("ark")
             || i.getName().toLowerCase().contains("shortbow")
             || i.getName().toLowerCase().contains("longbow")));
    }

    private boolean hasMagicRunes() {
        return canCastEffectiveSpell(Skills.getRealLevel(Skill.MAGIC));
    }

    private boolean canCastEffectiveSpell(int magicLvl) {
        if (magicLvl >= 45) {
            return hasRunes("Fire rune", "Air rune", "Chaos rune");
        }
        if (magicLvl >= 35) {
            return hasRunes("Earth rune", "Air rune");
        }
        if (magicLvl >= 29) {
            return hasRunes("Water rune", "Air rune");
        }
        if (magicLvl >= 23) {
            return hasRunes("Wind rune", "Air rune");
        }
        if (magicLvl >= 13) {
            return hasRunes("Fire rune", "Air rune", "Mind rune");
        }
        if (magicLvl >= 5) {
            return hasRunes("Water rune", "Air rune", "Mind rune");
        }
        return hasRunes("Wind rune", "Air rune", "Mind rune");
    }

    private boolean hasRunes(String... runes) {
        for (String rune : runes) {
            if (!Inventory.contains(rune)) {
                return false;
            }
        }
        return true;
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
        // Ensure we always pick up mossy keys from the room if they drop away from the safe spot.
        GroundItem key = GroundItems.closest(gi -> gi != null
                && BOSS_KEY.equalsIgnoreCase(gi.getName())
                && MOSS_ROOM.contains(gi.getTile()));
        if (key != null && key.interact("Take")) {
            return Calculations.random(800, 1600);
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

        // Deposit loot (keep knife, weapons, armor, and boss key)
        Bank.depositAllExcept(i -> i != null && i.getName() != null && (
                i.getName().equalsIgnoreCase("Knife")
             || i.getName().equalsIgnoreCase(BOSS_KEY)
             || i.getName().equalsIgnoreCase("Iron axe")
             || i.getName().toLowerCase().contains("staff")
             || i.getName().toLowerCase().contains("bow")
             || i.getName().toLowerCase().contains("robe")
             || i.getName().toLowerCase().contains("body")
             || i.getName().toLowerCase().contains("chaps")
             || i.getName().toLowerCase().contains("vambraces")));

        queueValueLoot();
        maybeBuyRunesFromGE(method);
        prepareBossLoadout();
        if (!hasBossLoadout()) {
            maybeBuyBossGearFromGE();
            prepareBossLoadout();
        }

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

    private void queueValueLoot() {
        for (String item : GE_SELL_ITEMS) {
            int qty = Bank.count(item);
            if (qty > 0) {
                GESellTask.queue(item, qty, 0);
            }
        }
    }

    private void maybeBuyRunesFromGE(String method) {
        int coins = Bank.contains("Coins") ? Bank.count("Coins") : 0;
        if (coins < GE_REFILL_COINS || !GrandExchangeUtil.isTradeUnrestricted()) {
            return;
        }
        if (!needsGERefill(method)) {
            return;
        }

        if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
        }

        if ("moss_giant_ranged".equals(method)) {
            for (String arrow : new String[] { "Rune arrow", "Adamant arrow", "Mithril arrow",
                                               "Steel arrow", "Iron arrow", "Bronze arrow" }) {
                if (Bank.count(arrow) < RUNE_WITHDRAW) {
                    GrandExchangeUtil.buyChecked(ItemID.RUNE_ARROW, "Rune arrow", RUNE_WITHDRAW, 0);
                    break;
                }
            }
        } else {
            int magicLvl = Skills.getRealLevel(Skill.MAGIC);
            if (magicLvl >= 45) {
                if (Bank.count("Fire rune") < RUNE_WITHDRAW)  GrandExchangeUtil.buyChecked(ItemID.FIRE_RUNE,  "Fire rune",  RUNE_WITHDRAW, 0);
                if (Bank.count("Chaos rune") < RUNE_WITHDRAW / 3) GrandExchangeUtil.buyChecked(ItemID.CHAOS_RUNE, "Chaos rune", RUNE_WITHDRAW / 3, 0);
                if (Bank.count("Air rune") < RUNE_WITHDRAW / 2)   GrandExchangeUtil.buyChecked(ItemID.AIR_RUNE,   "Air rune",   RUNE_WITHDRAW / 2, 0);
            } else if (magicLvl >= 13) {
                if (Bank.count("Fire rune") < RUNE_WITHDRAW) GrandExchangeUtil.buyChecked(ItemID.FIRE_RUNE, "Fire rune",  RUNE_WITHDRAW, 0);
                if (Bank.count("Air rune") < RUNE_WITHDRAW)  GrandExchangeUtil.buyChecked(ItemID.AIR_RUNE,  "Air rune",   RUNE_WITHDRAW, 0);
                if (Bank.count("Mind rune") < RUNE_WITHDRAW) GrandExchangeUtil.buyChecked(ItemID.MIND_RUNE, "Mind rune", RUNE_WITHDRAW, 0);
            } else {
                if (Bank.count("Air rune") < RUNE_WITHDRAW)  GrandExchangeUtil.buyChecked(ItemID.AIR_RUNE,  "Air rune",  RUNE_WITHDRAW, 0);
                if (Bank.count("Mind rune") < RUNE_WITHDRAW) GrandExchangeUtil.buyChecked(ItemID.MIND_RUNE, "Mind rune", RUNE_WITHDRAW, 0);
            }
        }
        GrandExchangeUtil.collectAndBank();
    }

    private boolean needsGERefill(String method) {
        if ("moss_giant_ranged".equals(method)) {
            for (String arrow : new String[] { "Rune arrow", "Adamant arrow", "Mithril arrow",
                                               "Steel arrow", "Iron arrow", "Bronze arrow" }) {
                if (Bank.count(arrow) < RUNE_WITHDRAW) return true;
            }
            return false;
        }
        int magicLvl = Skills.getRealLevel(Skill.MAGIC);
        if (magicLvl >= 45) {
            return Bank.count("Fire rune") < RUNE_WITHDRAW
                || Bank.count("Chaos rune") < RUNE_WITHDRAW / 3
                || Bank.count("Air rune") < RUNE_WITHDRAW / 2;
        }
        if (magicLvl >= 13) {
            return Bank.count("Fire rune") < RUNE_WITHDRAW
                || Bank.count("Air rune") < RUNE_WITHDRAW
                || Bank.count("Mind rune") < RUNE_WITHDRAW;
        }
        return Bank.count("Air rune") < RUNE_WITHDRAW
            || Bank.count("Mind rune") < RUNE_WITHDRAW;
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
        if (hasBossKey() && !hasBossLoadout()) return true;
        if (!hasFood()) return true;
        if (!hasKnife()) return true;
        if ("moss_giant_ranged".equals(method)) {
            return !Inventory.contains(i -> i != null && i.getName() != null
                    && i.getName().toLowerCase().contains("arrow"));
        }
        // Need at least one type of rune
        return !Inventory.contains(i -> i != null && i.getName() != null
                && i.getName().toLowerCase().contains("rune"));
    }

    private boolean hasBossKey() {
        return Inventory.contains(BOSS_KEY);
    }

    private boolean hasBossLoadout() {
        return Inventory.contains(i -> i != null && i.getName() != null
                && (i.getName().equalsIgnoreCase("Iron axe") || i.getName().equalsIgnoreCase("Steel axe")))
            && hasAnyArmorForBoss()
            && hasFood();
    }

    private boolean hasAnyArmorForBoss() {
        return Inventory.contains(i -> i != null && i.getName() != null && (
                matchesAny(i.getName(), BOSS_ARMOR_CHEST)
             || matchesAny(i.getName(), BOSS_ARMOR_LEGS)
             || matchesAny(i.getName(), BOSS_ARMOR_HEAD)
             || matchesAny(i.getName(), BOSS_ARMOR_SHIELD)));
    }

    private boolean matchesAny(String name, String[] options) {
        for (String option : options) {
            if (option.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private boolean hasKnife() {
        return Inventory.contains(i -> i != null && i.getName() != null
                && i.getName().equalsIgnoreCase("Knife"));
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

    private boolean shouldRetreat() {
        int hp = Players.getLocal().getHealthPercent();
        return hp > 0 && hp < RETREAT_HP_PCT;
    }

    private int handleBossKeyPath() {
        GameObject bossDoor = findBossDoor();
        if (bossDoor != null) {
            if (useKeyOnDoor(bossDoor)) {
                return Calculations.random(1800, 2400);
            }
        }

        if (!BOSS_DOOR_AREA.contains(Players.getLocal())) {
            Walking.walk(BOSS_DOOR_CENTER);
            return Calculations.random(1400, 2200);
        }

        if (!onSafeSpot()) {
            Walking.walk(SAFE_SPOT);
            return Calculations.random(1200, 1800);
        }

        return attackBoss();
    }

    private GameObject findBossDoor() {
        return GameObjects.closest(g -> g != null
                && BOSS_DOOR_AREA.contains(g.getTile())
                && ("Door".equalsIgnoreCase(g.getName())
                 || "Gate".equalsIgnoreCase(g.getName())
                 || "Mossy door".equalsIgnoreCase(g.getName()))
                && (g.hasAction("Unlock") || g.hasAction("Open") || g.hasAction("Use")));
    }

    private boolean useKeyOnDoor(GameObject door) {
        Item key = Inventory.get(BOSS_KEY);
        if (key == null) return false;
        if (key.interact("Use")) {
            return door.interact(door.hasAction("Unlock") ? "Unlock"
                    : door.hasAction("Open") ? "Open" : "Use");
        }
        return false;
    }

    private int attackBoss() {
        if (shouldRetreat()) {
            return retreatFromBoss();
        }

        if (hasWeaponEquipped("Iron axe") || hasWeaponEquipped("Steel axe")) {
            ensureBossMeleeStyle();
        }

        if (!onSafeSpot()) {
            Walking.walk(SAFE_SPOT);
            return Calculations.random(1200, 1800);
        }

        NPC ad = findBossTarget(false);
        if (ad != null) {
            if (clickHelper.tryClick(ad, "Attack")) {
                return Calculations.random(1800, 2600);
            }
            return Calculations.random(700, 1100);
        }

        NPC boss = findBossTarget(true);
        if (boss != null) {
            if (clickHelper.tryClick(boss, "Attack")) {
                return Calculations.random(1800, 2600);
            }
            return Calculations.random(700, 1100);
        }
        return Calculations.random(700, 1200);
    }

    private NPC findBossTarget(boolean priorityBoss) {
        return NPCs.closest(n -> n != null
                && n.getName() != null
                && BOSS_FIGHT_AREA.contains(n.getTile())
                && n.getHealthPercent() > 0
                && !n.isInCombat()
                && (priorityBoss ? isBossName(n.getName()) : isAdName(n.getName())));
    }

    private boolean isBossName(String name) {
        String normalized = name.toLowerCase();
        return normalized.contains("moss giant") || normalized.contains("moss giant boss");
    }

    private boolean isAdName(String name) {
        String normalized = name.toLowerCase();
        return (normalized.contains("spider")
             || normalized.contains("skeleton")
             || normalized.contains("zombie")
             || normalized.contains("rat")
             || normalized.contains("crawler")
             || normalized.contains("imp"));
    }

    private int retreatFromBoss() {
        if (!BANK_AREA.contains(Players.getLocal())) {
            Walking.walk(VARROCK_EAST_BANK);
            return Calculations.random(1800, 2600);
        }
        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(1200, 2000);
        }
        Bank.depositAllItems();
        Bank.depositAllEquipment();
        return -(Calculations.random(1200, 2000));
    }

    private void ensureBossMeleeStyle() {
        if (Combat.getCombatStyle() != CombatStyle.STRENGTH) {
            Combat.setCombatStyle(CombatStyle.STRENGTH);
        }
    }

    private boolean hasWeaponEquipped(String name) {
        if (Equipment.getItemInSlot(EquipmentSlot.WEAPON.getSlot()) == null) return false;
        return Equipment.getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().equalsIgnoreCase(name);
    }

    private void prepareBossLoadout() {
        withdrawBossGear();
        equipBossGear();
    }

    private void withdrawBossGear() {
        if (!Inventory.contains("Iron axe") && Bank.contains("Iron axe")) {
            Bank.withdraw("Iron axe", 1);
        } else {
            for (String weapon : BOSS_WEAPONS) {
                if (!Inventory.contains(weapon) && Bank.contains(weapon)) {
                    Bank.withdraw(weapon, 1);
                    break;
                }
            }
        }
        withdrawArmorSet(BOSS_ARMOR_CHEST);
        withdrawArmorSet(BOSS_ARMOR_LEGS);
        withdrawArmorSet(BOSS_ARMOR_HEAD);
        withdrawArmorSet(BOSS_ARMOR_SHIELD);
    }

    private void withdrawArmorSet(String[] options) {
        for (String item : options) {
            if (!Inventory.contains(item) && Bank.contains(item)) {
                Bank.withdraw(item, 1);
                return;
            }
        }
    }

    private void maybeBuyBossGearFromGE() {
        if (!GrandExchangeUtil.isTradeUnrestricted()) {
            return;
        }
        int coins = Bank.contains("Coins") ? Bank.count("Coins") : 0;
        if (coins < BOSS_GEAR_GE_BUFF_COINS) {
            return;
        }

        if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
        }

        if (!GrandExchangeUtil.buyChecked(ItemID.IRON_AXE, "Iron axe", 1, BOSS_GEAR_MAX_BUY_PRICE)) {
            GrandExchangeUtil.buyChecked(ItemID.STEEL_AXE, "Steel axe", 1, BOSS_GEAR_MAX_BUY_PRICE);
        }

        // TODO: Add armor buy by item ID if item IDs are available.
        GrandExchangeUtil.collectAndBank();
    }

    private void equipBossGear() {
        if (!hasWeaponEquipped("Iron axe") && Inventory.contains("Iron axe")) {
            Inventory.interact("Iron axe", "Wield");
        }
        for (String item : BOSS_ARMOR_HEAD) {
            if (Equipment.isSlotEmpty(EquipmentSlot.HEAD.getSlot()) && Inventory.contains(item)) {
                Inventory.interact(item, "Wear");
                return;
            }
        }
        for (String item : BOSS_ARMOR_CHEST) {
            if (Equipment.isSlotEmpty(EquipmentSlot.CHEST.getSlot()) && Inventory.contains(item)) {
                Inventory.interact(item, "Wear");
                return;
            }
        }
        for (String item : BOSS_ARMOR_LEGS) {
            if (Equipment.isSlotEmpty(EquipmentSlot.LEGS.getSlot()) && Inventory.contains(item)) {
                Inventory.interact(item, "Wear");
                return;
            }
        }
        for (String item : BOSS_ARMOR_SHIELD) {
            if (Equipment.isSlotEmpty(EquipmentSlot.SHIELD.getSlot()) && Inventory.contains(item)) {
                Inventory.interact(item, "Wield");
                return;
            }
        }
    }

    private boolean shouldEat() {
        int max = Skills.getRealLevel(Skill.HITPOINTS);
        int cur = Skills.getBoostedLevel(Skill.HITPOINTS);
        return max > 0 && cur < max * EAT_AT_PCT / 100;
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
