package nezz.dreambot.aio.boss;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.task.StatsProvider;
import nezz.dreambot.aio.task.Task;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.methods.interactive.GroundItems;
import org.dreambot.api.wrappers.items.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic, server-agnostic boss fighter. Drives a small state machine:
 *
 *   RESTOCK  -> bank, withdraw food, return to the arena
 *   TRAVEL   -> walk to the boss anchor tile until the named boss is reachable
 *   FIGHT    -> manage HP (eat), prayer (optional protect-from-melee flick),
 *               attack the boss and any adds, then loot valuable ground items
 *
 * Bosses are targeted by name via {@link BossConfig}, so no server-specific
 * numeric NPC IDs are needed. Loot is valued live off the GE for profit stats.
 */
public class BossTask extends Task implements StatsProvider {

	private final Config cfg;
	private final BossConfig boss;

	private int kills = 0;
	private int lootValue = 0;
	private final Map<String, PriceTracker> priceCache = new HashMap<>();

	private State state = State.FIGHT;

	private enum State { RESTOCK, TRAVEL, FIGHT }

	public BossTask(Config cfg) {
		this.cfg = cfg;
		this.boss = BossRegistry.forType(cfg);
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

		state = resolveState();
		switch (state) {
			case RESTOCK: return restock();
			case TRAVEL: return travel();
			case FIGHT: return fight();
		}
		return Calculations.random(200, 400);
	}

	private State resolveState() {
		if (outOfFood()) {
			return State.RESTOCK;
		}
		NPC target = NPCs.closest(boss.npcName);
		if (target == null || !target.exists()) {
			return State.TRAVEL;
		}
		return State.FIGHT;
	}

	private boolean outOfFood() {
		return !Inventory.contains(cfg.foodName);
	}

	/* ---------------- RESTOCK ---------------- */

	private int restock() {
		if (Prayers.isActive(Prayer.PROTECT_FROM_MELEE)) {
			Prayers.toggle(false, Prayer.PROTECT_FROM_MELEE);
		}
		if (!Bank.isOpen()) {
			if (boss.bankTile != null && boss.bankTile.distance() > 6) {
				Walking.walk(boss.bankTile);
				Sleep.sleepUntil(() -> boss.bankTile.distance() < 6 || Bank.isOpen(), 6000);
				return Calculations.random(300, 600);
			}
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 5000);
			return Calculations.random(300, 600);
		}
		Bank.depositAllExcept(item -> item != null && isGear(item.getName()));
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

	/** Keep worn-style items if they somehow end up in inventory; food handled separately. */
	private boolean isGear(String n) {
		if (n == null) return false;
		String l = n.toLowerCase();
		return l.contains("rune") || l.contains("dragon") || l.contains("amulet")
				|| l.contains("ring") || l.contains("cape") || l.contains("boots")
				|| l.contains("gloves") || l.contains("shield") || l.contains("scimitar")
				|| l.contains("whip") || l.contains("crossbow") || l.contains("bow");
	}

	/* ---------------- TRAVEL ---------------- */

	private int travel() {
		if (boss.anchorTile == null) {
			Logger.log("[Boss] No anchor tile configured for " + boss.displayName);
			return Calculations.random(800, 1200);
		}
		if (boss.anchorTile.distance() > 4) {
			Walking.walk(boss.anchorTile);
			Sleep.sleepUntil(() -> boss.anchorTile.distance() < 5
					|| NPCs.closest(boss.npcName) != null, 6000);
		}
		return Calculations.random(300, 600);
	}

	/* ---------------- FIGHT ---------------- */

	private int fight() {
		// 1) Survival first.
		if (shouldEat()) {
			eat();
			return Calculations.random(150, 400);
		}
		// 2) Prayer flick (optional).
		if (cfg.flickProtectMelee && Prayers.getPoints() > 0
				&& !Prayers.isActive(Prayer.PROTECT_FROM_MELEE)) {
			Prayers.toggle(true, Prayer.PROTECT_FROM_MELEE);
		}
		// 3) Loot anything valuable lying around between hits.
		if (lootGround()) {
			return Calculations.random(150, 350);
		}
		// 4) Attack the boss, falling back to adds.
		NPC target = NPCs.closest(n -> n != null && boss.npcName.equals(n.getName())
				&& n.hasAction("Attack"));
		if (target == null) {
			// Adds (e.g. axe-throwers) - attack the nearest attackable NPC in the arena.
			target = NPCs.closest(n -> n != null && n.hasAction("Attack")
					&& n.distance() < 8 && n.getHealthPercent() > 0);
		}
		if (target != null) {
			if (!Players.getLocal().isInCombat()) {
				final NPC t = target;
				if (target.interact("Attack")) {
					Sleep.sleepUntil(() -> Players.getLocal().isInCombat() || !t.exists(), 2500);
				}
			} else if (target.getHealthPercent() <= 0 || !target.exists()) {
				kills++;
			}
			return Calculations.random(400, 800);
		}
		return Calculations.random(300, 600);
	}

	private boolean shouldEat() {
		int cur = Skills.getBoostedLevel(Skill.HITPOINTS);
		int max = Skills.getRealLevel(Skill.HITPOINTS);
		if (max <= 0) return false;
		int pct = (cur * 100) / max;
		return pct <= cfg.eatAtHpPercent && Inventory.contains(cfg.foodName);
	}

	private void eat() {
		Item food = Inventory.get(cfg.foodName);
		if (food != null) {
			food.interact("Eat");
			Sleep.sleep(Calculations.random(300, 600));
		}
	}

	private boolean lootGround() {
		GroundItem loot = GroundItems.closest(gi -> {
			if (gi == null || gi.getName() == null) return false;
			if (gi.distance() > 6) return false;
			return valueOf(gi.getName()) >= cfg.minLootValue || isGear(gi.getName());
		});
		if (loot == null) return false;
		if (Inventory.isFull() && !Inventory.contains(loot.getName())) {
			return false; // no room for a new stack
		}
		String n = loot.getName();
		int amt = loot.getAmount();
		if (loot.interact("Take")) {
			Sleep.sleep(Calculations.random(300, 600));
			lootValue += valueOf(n) * Math.max(1, amt);
			return true;
		}
		return false;
	}

	private int valueOf(String itemName) {
		PriceTracker pt = priceCache.computeIfAbsent(itemName, PriceTracker::new);
		return pt.getPrice();
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
