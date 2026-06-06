package nezz.dreambot.aio.monster;

import nezz.dreambot.aio.combat.CombatManager;
import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.movement.MovementManager;
import nezz.dreambot.aio.prayer.PrayerManager;
import nezz.dreambot.aio.supplies.SupplyManager;
import nezz.dreambot.aio.task.StatsProvider;
import nezz.dreambot.aio.task.Task;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GroundItems;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Data-driven regular-monster grind for training and F2P money making. Reuses
 * the same subsystems as the boss engine: movement, prayer, supplies, combat.
 *
 * State machine:
 *   BANK   -> deposit junk, keep food/gear/loot-stackables, restock food
 *   TRAVEL -> walk to the spot (or safespot tile)
 *   FIGHT  -> survival -> prayer -> loot -> attack the named monster
 *
 * Safespot targets attack from a fixed tile (relying on the player's configured
 * autocast for magic), so the monster can't melee back.
 */
public class MonsterFarmTask extends Task implements StatsProvider {

	private final Config cfg;
	private final MonsterConfig monster;

	private final MovementManager movement = new MovementManager();
	private final PrayerManager prayer = new PrayerManager();
	private final CombatManager combat = new CombatManager();
	private final SupplyManager supplies;

	private final Map<String, PriceTracker> priceCache = new HashMap<>();
	private int lootValue = 0;
	private State state = State.FIGHT;

	private enum State { BANK, TRAVEL, FIGHT }

	public MonsterFarmTask(Config cfg) {
		this.cfg = cfg;
		this.monster = MonsterRegistry.forType(cfg);
		this.supplies = new SupplyManager(cfg.foodName, cfg.eatAtHpPercent);
	}

	@Override
	public String name() {
		return "Monsters: " + monster.displayName;
	}

	@Override
	public boolean accept() {
		return true;
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		movement.tickEnergy();
		state = resolveState();
		switch (state) {
			case BANK: return bank();
			case TRAVEL: return travel();
			case FIGHT: return fight();
		}
		return Calculations.random(200, 400);
	}

	private State resolveState() {
		boolean needFood = cfg.useFoodForMonsters && !supplies.hasFood();
		boolean full = Inventory.isFull();
		if (needFood || full) return State.BANK;
		NPC target = combat.findByName(monster.npcName);
		if (target == null || !target.exists()) return State.TRAVEL;
		return State.FIGHT;
	}

	/* ---------------- BANK ---------------- */

	private int bank() {
		prayer.disableAll();
		if (!Bank.isOpen()) {
			if (movement.walkTo(monster.bankTile, 6)) return Calculations.random(300, 600);
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 5000);
			return Calculations.random(300, 600);
		}
		Bank.depositAllExcept(item -> item != null && keepInBank(item.getName()));
		Sleep.sleep(Calculations.random(150, 300));
		if (cfg.useFoodForMonsters && !Inventory.contains(cfg.foodName)) {
			if (!Bank.contains(cfg.foodName)) {
				Logger.log("[Monsters] No '" + cfg.foodName + "' in bank - stopping.");
				return -1;
			}
			Bank.withdrawAll(cfg.foodName);
			Sleep.sleepUntil(() -> Inventory.contains(cfg.foodName), 2000);
		}
		Bank.close();
		Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
		return Calculations.random(300, 600);
	}

	/** Items kept in inventory when depositing junk: food, gear, stackable loot. */
	private boolean keepInBank(String n) {
		if (n == null) return false;
		if (cfg.foodName.equalsIgnoreCase(n)) return true;
		String l = n.toLowerCase();
		if (l.contains("potion") || l.contains("rune") || l.contains("dragon")
				|| l.contains("amulet") || l.contains("ring") || l.contains("cape")
				|| l.contains("boots") || l.contains("gloves") || l.contains("shield")
				|| l.contains("scimitar") || l.contains("whip") || l.contains("bow")) {
			return true;
		}
		return false;
	}

	/* ---------------- TRAVEL ---------------- */

	private int travel() {
		if (monster.hasSafespot() && cfg.monsterSafespot) {
			movement.walkTo(monster.safespotTile, 0);
			return Calculations.random(300, 600);
		}
		if (monster.anchorTile != null) {
			movement.walkTo(monster.anchorTile, 4);
		}
		return Calculations.random(300, 600);
	}

	/* ---------------- FIGHT ---------------- */

	private int fight() {
		if (supplies.eatIfNeeded()) return Calculations.random(150, 400);
		if (monster.protection != PrayerManager.Protect.NONE && prayer.points() > 0) {
			prayer.setProtection(monster.protection);
		}

		// Keep to the safespot tile so we never get meleed.
		if (monster.hasSafespot() && cfg.monsterSafespot
				&& monster.safespotTile.distance() > 0) {
			movement.walkTo(monster.safespotTile, 0);
			return Calculations.random(150, 350);
		}

		if (lootGround()) return Calculations.random(150, 350);

		NPC target = combat.findByName(monster.npcName);
		if (target != null) {
			if (combat.attack(target)) return Calculations.random(400, 800);
			return Calculations.random(300, 600);
		}
		return Calculations.random(300, 600);
	}

	private boolean lootGround() {
		GroundItem loot = GroundItems.closest(gi -> {
			if (gi == null || gi.getName() == null) return false;
			if (gi.distance() > 6) return false;
			return inLootList(gi.getName()) || valueOf(gi.getName()) >= cfg.monsterLootValue;
		});
		if (loot == null) return false;
		if (Inventory.isFull() && !Inventory.contains(loot.getName())) return false;
		String n = loot.getName();
		int amt = Math.max(1, loot.getAmount());
		if (loot.interact("Take")) {
			Sleep.sleep(Calculations.random(250, 500));
			lootValue += valueOf(n) * amt;
			return true;
		}
		return false;
	}

	private boolean inLootList(String name) {
		for (String l : monster.lootNames) {
			if (l.equalsIgnoreCase(name)) return true;
		}
		return false;
	}

	private int valueOf(String itemName) {
		return priceCache.computeIfAbsent(itemName, PriceTracker::new).getPrice();
	}

	@Override
	public int getProfit() {
		return lootValue;
	}

	@Override
	public String getStatus() {
		return "Loot value: " + lootValue + " | State: " + state;
	}
}
