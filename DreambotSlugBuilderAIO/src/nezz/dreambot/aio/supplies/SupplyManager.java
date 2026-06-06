package nezz.dreambot.aio.supplies;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.Arrays;
import java.util.List;

/**
 * Manages consumables: eating food at an HP threshold and sipping combat /
 * restore / prayer potions. All thresholds are percentage-based so the same
 * config works across account levels.
 */
public class SupplyManager {

	private final String foodName;
	private final int eatAtHpPercent;

	/** Common potion action keywords we recognise on inventory items. */
	private static final List<String> POTION_NAMES = Arrays.asList(
			"Saradomin brew", "Super restore", "Prayer potion", "Super combat",
			"Ranging potion", "Magic potion", "Stamina potion");

	public SupplyManager(String foodName, int eatAtHpPercent) {
		this.foodName = foodName;
		this.eatAtHpPercent = eatAtHpPercent;
	}

	public boolean hasFood() {
		return Inventory.contains(foodName);
	}

	public int hpPercent() {
		int max = Skills.getRealLevel(Skill.HITPOINTS);
		if (max <= 0) return 100;
		return (Skills.getBoostedLevel(Skill.HITPOINTS) * 100) / max;
	}

	public boolean shouldEat() {
		return hpPercent() <= eatAtHpPercent && hasFood();
	}

	/** Eats one food if below the threshold. @return true if it ate. */
	public boolean eatIfNeeded() {
		if (!shouldEat()) return false;
		Item food = Inventory.get(foodName);
		if (food != null && food.interact("Eat")) {
			Sleep.sleep(Calculations.random(250, 550));
			return true;
		}
		return false;
	}

	/** Restores prayer if below the given absolute points using any prayer potion. */
	public boolean restorePrayerIfNeeded(int belowPoints) {
		if (Skills.getBoostedLevel(Skill.PRAYER) > belowPoints) return false;
		Item pot = Inventory.get(i -> i != null && i.getName() != null
				&& (i.getName().contains("Prayer potion") || i.getName().contains("Super restore")));
		if (pot != null && pot.interact("Drink")) {
			Sleep.sleep(Calculations.random(250, 550));
			return true;
		}
		return false;
	}

	/** Drinks a boost potion (e.g. Super combat) if one is present and not yet boosted. */
	public boolean boostIfAvailable(Skill skill) {
		if (Skills.getBoostedLevel(skill) > Skills.getRealLevel(skill)) return false;
		Item pot = Inventory.get(i -> i != null && i.getName() != null && isBoostFor(i.getName(), skill));
		if (pot != null && pot.interact("Drink")) {
			Sleep.sleep(Calculations.random(250, 550));
			return true;
		}
		return false;
	}

	private boolean isBoostFor(String name, Skill skill) {
		switch (skill) {
			case RANGED: return name.contains("Ranging") || name.contains("Bastion") || name.contains("Super ranging");
			case MAGIC: return name.contains("Magic potion") || name.contains("Battlemage") || name.contains("Imbued heart");
			case ATTACK:
			case STRENGTH:
			case DEFENCE: return name.contains("Super combat") || name.contains("Combat potion")
					|| name.contains("Super attack") || name.contains("Super strength") || name.contains("Super defence");
			default: return false;
		}
	}

	public String getFoodName() {
		return foodName;
	}
}
