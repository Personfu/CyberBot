package nezz.dreambot.aio.movement;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

/**
 * Movement helper: keeps run enabled when energy allows, sips stamina potions,
 * and walks toward a destination using DreamBot's web walker. Designed to be
 * called once per loop before combat logic.
 */
public class MovementManager {

	private int runThreshold = Calculations.random(25, 45);

	/** Enables run / drinks stamina as appropriate. Call every loop. */
	public void tickEnergy() {
		int energy = Walking.getRunEnergy();
		if (!Walking.isRunEnabled() && energy >= runThreshold) {
			Walking.toggleRun();
			runThreshold = Calculations.random(25, 45);
		}
		if (energy < 25) {
			Item stam = Inventory.get(i -> i != null && i.getName() != null
					&& i.getName().contains("Stamina potion"));
			if (stam != null) {
				stam.interact("Drink");
				Sleep.sleep(Calculations.random(250, 500));
			}
		}
	}

	/** Walks toward the tile if further than {@code within}. @return true if walking. */
	public boolean walkTo(Tile dest, int within) {
		if (dest == null) return false;
		if (dest.distance() <= within) return false;
		tickEnergy();
		Walking.walk(dest);
		final Tile d = dest;
		Sleep.sleepUntil(() -> d.distance() <= within
				|| (Players.getLocal() != null && !Players.getLocal().isMoving()), 5000);
		return true;
	}

	public boolean atOrNear(Tile dest, int within) {
		return dest != null && dest.distance() <= within;
	}
}
