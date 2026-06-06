package nezz.dreambot.aio.gear;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Equips and switches gear loadouts by item name. A loadout is just a list of
 * item names; {@link #equip(GearSet)} wields anything from the set that's in the
 * inventory but not already worn. This supports tribrid gear switches (melee /
 * range / mage) for bosses that demand them.
 */
public class GearManager {

	/** A named set of equipment items (by name). */
	public static class GearSet {
		public final String name;
		public final List<String> items;

		public GearSet(String name, List<String> items) {
			this.name = name;
			this.items = items;
		}
	}

	/** Wields every item of the set that's present in the inventory. */
	public boolean equip(GearSet set) {
		if (set == null) return false;
		boolean changed = false;
		for (String itemName : set.items) {
			if (Equipment.contains(itemName)) continue;
			Item inv = Inventory.get(itemName);
			if (inv == null) continue;
			if (inv.interact("Wield") || inv.interact("Wear") || inv.interact("Equip")) {
				Sleep.sleep(Calculations.random(120, 260));
				changed = true;
			}
		}
		return changed;
	}

	public boolean isFullyEquipped(GearSet set) {
		if (set == null) return true;
		for (String itemName : set.items) {
			if (!Equipment.contains(itemName)) return false;
		}
		return true;
	}

	public static GearSet of(String name, String... items) {
		List<String> list = new ArrayList<>();
		for (String i : items) list.add(i);
		return new GearSet(name, list);
	}
}
