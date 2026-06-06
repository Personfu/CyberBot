package nezz.dreambot.aio.boss;

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
 * Generic, subsystem-driven boss fighter. Orchestrates the PRO managers:
 * movement (travel + run/stamina), prayer (overhead protection + offensive),
 * supplies (eat/restore/boost) and combat (target + special attack), plus
 * valuable ground looting and bank restocking.
 *
 * State machine:
 *   RESTOCK -> bank, withdraw food, return
 *   TRAVEL  -> web-walk to the anchor until the named boss is reachable
 *   FIGHT   -> survival -> prayer -> loot -> attack (boss, then adds)
 */
public class BossTask extends Task implements StatsProvider {

	private final Config cfg;
	private final BossConfig boss;

	private final MovementManager movement = new MovementManager();
	private final PrayerManager prayer = new PrayerManager();
	private final CombatManager combat = new CombatManager();
	private final SupplyManager supplies;

	private final Map<String, PriceTracker> priceCache = new HashMap<>();
	private int kills = 0;
	private int lootValue = 0;
	private boolean killCredited = false;
	private State state = State.FIGHT;

	private enum State { RESTOCK, TRAVEL, FIGHT }

	public BossTask(Config cfg) {
		this.cfg = cfg;
		this.boss = BossRegistry.forType(cfg);
		this.supplies = new SupplyManager(cfg.foodName, cfg.eatAtHpPercent);
	}

	@Override
	public String name() {
		return "Boss: " + boss.displayName;
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
			case RESTOCK: return restock();
			case TRAVEL: return travel();
			case FIGHT: return fight();
		}
		return Calculations.random(200, 400);
	}

	private State resolveState() {
		if (!supplies.hasFood()) return State.RESTOCK;
		NPC target = combat.findByName(boss.npcName);
		if (target == null || !target.exists()) return State.TRAVEL;
		return State.FIGHT;
	}

	/* ---------------- RESTOCK ---------------- */

	private int restock() {
		prayer.disableAll();
		if (!Bank.isOpen()) {
			if (movement.walkTo(boss.bankTile, 6)) {
				return Calculations.random(300, 600);
			}
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 5000);
			return Calculations.random(300, 600);
		}
		Bank.depositAllExcept(item -> item != null && isKeepable(item.getName()));
		Sleep.sleep(Calculations.random(150, 300));
		if (!Bank.contains(cfg.foodName)) {
			Logger.log("[Boss] No '" + cfg.foodName + "' in bank - stopping.");
			return -1;
		}
		Bank.withdrawAll(cfg.foodName);
		Sleep.sleepUntil(() -> Inventory.contains(cfg.foodName), 2000);
		Bank.close();
		Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
		return Calculations.random(300, 600);
	}

	/** Keep worn-style gear and potions in inventory when banking. */
	private boolean isKeepable(String n) {
		if (n == null) return false;
		String l = n.toLowerCase();
		if (l.contains("potion") || l.contains("brew") || l.contains("restore")) return true;
		return l.contains("rune") || l.contains("dragon") || l.contains("amulet")
				|| l.contains("ring") || l.contains("cape") || l.contains("boots")
				|| l.contains("gloves") || l.contains("shield") || l.contains("scimitar")
				|| l.contains("whip") || l.contains("crossbow") || l.contains("bow");
	}

	/* ---------------- TRAVEL ---------------- */

	private int travel() {
		if (boss.anchorTile == null) {
			Logger.log("[Boss] No anchor tile for " + boss.displayName);
			return Calculations.random(800, 1200);
		}
		movement.walkTo(boss.anchorTile, 4);
		return Calculations.random(300, 600);
	}

	/* ---------------- FIGHT ---------------- */

	private int fight() {
		// 1) Survival.
		if (supplies.eatIfNeeded()) return Calculations.random(150, 400);
		supplies.restorePrayerIfNeeded(10);
		supplies.boostIfAvailable(boss.offensiveStyle);

		// 2) Prayer: protection (flick-capable) + offensive upkeep.
		if (cfg.flickProtectMelee && prayer.points() > 0) {
			prayer.setProtection(boss.protection);
			prayer.enableOffensive(boss.offensiveStyle);
		}

		// 3) Loot anything valuable between hits.
		if (lootGround()) return Calculations.random(150, 350);

		// 4) Special attack burst when ready.
		combat.useSpecialIfReady(Calculations.random(50, 75));

		// 5) Attack boss, then adds.
		NPC target = combat.findByName(boss.npcName);
		if (target == null && boss.hasAdds) {
			target = combat.find(n -> n != null && n.hasAction("Attack")
					&& n.distance() < 8 && n.getHealthPercent() > 0);
		}
		if (target != null) {
			killCredited = false;
			if (combat.attack(target)) {
				return Calculations.random(400, 800);
			}
			return Calculations.random(300, 600);
		}

		// No target and we were just fighting => a kill likely completed.
		if (!killCredited && !combat.inCombat()) {
			kills++;
			killCredited = true;
		}
		return Calculations.random(300, 600);
	}

	private boolean lootGround() {
		GroundItem loot = GroundItems.closest(gi -> {
			if (gi == null || gi.getName() == null) return false;
			if (gi.distance() > 6) return false;
			return valueOf(gi.getName()) >= cfg.minLootValue || isKeepable(gi.getName());
		});
		if (loot == null) return false;
		if (Inventory.isFull() && !Inventory.contains(loot.getName())) return false;
		String n = loot.getName();
		int amt = Math.max(1, loot.getAmount());
		if (loot.interact("Take")) {
			Sleep.sleep(Calculations.random(300, 600));
			lootValue += valueOf(n) * amt;
			return true;
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
		return "Kills: " + kills + " | Loot: " + lootValue + " | State: " + state;
	}
}
