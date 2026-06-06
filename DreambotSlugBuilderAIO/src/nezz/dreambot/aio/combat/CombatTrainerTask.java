package nezz.dreambot.aio.combat;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.movement.MovementManager;
import nezz.dreambot.aio.supplies.SupplyManager;
import nezz.dreambot.aio.task.StatsProvider;
import nezz.dreambot.aio.task.Task;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GroundItems;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Progression-aware combat trainer. Automatically picks the best training
 * target for the player's combat level and upgrades as you level up.
 * Banks for food when needed, loots valuable drops.
 */
public class CombatTrainerTask extends Task implements StatsProvider {

	private final Config cfg;
	private final MovementManager movement = new MovementManager();
	private final CombatManager combat = new CombatManager();
	private final SupplyManager supplies;
	private final Map<String, PriceTracker> priceCache = new HashMap<>();

	private TrainingTarget current;
	private int lootValue = 0;
	private int lastCombatLevel = -1;
	private State state = State.FIGHT;

	private enum State { BANK, TRAVEL, FIGHT }

	public CombatTrainerTask(Config cfg) {
		this.cfg = cfg;
		this.supplies = new SupplyManager(cfg.foodName, cfg.eatAtHpPercent);
	}

	@Override
	public String name() {
		String target = current != null ? current.displayName : "scanning";
		return "Combat Trainer: " + target;
	}

	@Override
	public boolean accept() {
		return true;
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		refreshTarget();
		movement.tickEnergy();
		state = resolveState();
		switch (state) {
			case BANK:   return bank();
			case TRAVEL: return travel();
			case FIGHT:  return fight();
		}
		return Calculations.random(200, 400);
	}

	private void refreshTarget() {
		if (Players.getLocal() == null) return;
		int cb = Players.getLocal().getCombatLevel();
		if (cb != lastCombatLevel) {
			TrainingTarget next = TrainingLadder.forLevel(cb);
			if (current == null || !next.npcName.equals(current.npcName)) {
				Logger.log("[CombatTrainer] Level " + cb + " -> switching to " + next.displayName);
				current = next;
			}
			lastCombatLevel = cb;
		}
		if (current == null) {
			current = TrainingLadder.forLevel(3);
		}
	}

	private State resolveState() {
		boolean needFood = cfg.useFoodForCombatTrainer && !supplies.hasFood();
		boolean full = Inventory.isFull();
		if (needFood || full) return State.BANK;
		NPC target = combat.findByName(current.npcName);
		if (target == null || !target.exists()) return State.TRAVEL;
		return State.FIGHT;
	}

	private int bank() {
		if (!Bank.isOpen()) {
			if (movement.walkTo(current.bankTile, 6))
				return Calculations.random(300, 600);
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 5000);
			return Calculations.random(300, 600);
		}
		Bank.depositAllExcept(item -> item != null && keepItem(item.getName()));
		Sleep.sleep(Calculations.random(150, 300));
		if (cfg.useFoodForCombatTrainer && !Inventory.contains(cfg.foodName)) {
			if (!Bank.contains(cfg.foodName)) {
				Logger.log("[CombatTrainer] No '" + cfg.foodName + "' in bank - stopping.");
				return -1;
			}
			Bank.withdrawAll(cfg.foodName);
			Sleep.sleepUntil(() -> Inventory.contains(cfg.foodName), 2000);
		}
		Bank.close();
		Sleep.sleepUntil(() -> !Bank.isOpen(), 1500);
		return Calculations.random(300, 600);
	}

	private boolean keepItem(String n) {
		if (n == null) return false;
		if (cfg.foodName.equalsIgnoreCase(n)) return true;
		String l = n.toLowerCase();
		return l.contains("potion") || l.contains("rune") || l.contains("amulet")
				|| l.contains("ring") || l.contains("cape") || l.contains("boots")
				|| l.contains("gloves") || l.contains("shield") || l.contains("scimitar")
				|| l.contains("whip") || l.contains("bow") || l.contains("arrow");
	}

	private int travel() {
		if (current.hasSafespot() && cfg.combatTrainerSafespot) {
			movement.walkTo(current.safespotTile, 0);
		} else {
			movement.walkTo(current.anchorTile, 4);
		}
		return Calculations.random(300, 600);
	}

	private int fight() {
		if (supplies.eatIfNeeded()) return Calculations.random(150, 400);

		if (current.hasSafespot() && cfg.combatTrainerSafespot
				&& current.safespotTile.distance() > 0) {
			movement.walkTo(current.safespotTile, 0);
			return Calculations.random(150, 350);
		}

		if (lootGround()) return Calculations.random(150, 350);

		NPC target = combat.findByName(current.npcName);
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
			return inLootList(gi.getName()) || valueOf(gi.getName()) >= cfg.combatTrainerLootValue;
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
		if (current.lootNames == null) return false;
		for (String l : current.lootNames) {
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
		String target = current != null ? current.displayName : "?";
		return "Training: " + target + " (cb " + lastCombatLevel + ") | Loot: " + lootValue + " | " + state;
	}
}
