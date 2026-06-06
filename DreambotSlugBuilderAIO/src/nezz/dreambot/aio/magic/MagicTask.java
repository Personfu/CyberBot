package nezz.dreambot.aio.magic;

import nezz.dreambot.aio.combat.CombatManager;
import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.movement.MovementManager;
import nezz.dreambot.aio.task.StatsProvider;
import nezz.dreambot.aio.task.Task;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

/**
 * Magic training module. Two modes:
 *   HIGH_ALCH    - repeatedly High Level Alchemy a configured inventory item
 *                  (classic AFK magic XP + GP from item margins).
 *   COMBAT_CAST  - cast a configured combat spell on a named NPC, optionally
 *                  from a safespot tile so nothing melees back.
 *
 * Spell names map onto the DreamBot {@link Normal} spellbook by enum name; the
 * GUI supplies the target item / NPC and the spell to use.
 */
public class MagicTask extends Task implements StatsProvider {

	private final Config cfg;
	private final MovementManager movement = new MovementManager();
	private final CombatManager combat = new CombatManager();
	private final PriceTracker pricer;

	private int castCount = 0;

	public MagicTask(Config cfg) {
		this.cfg = cfg;
		this.pricer = new PriceTracker(cfg.magicAlchItem);
	}

	@Override
	public String name() {
		return cfg.magicMode == Config.MagicMode.HIGH_ALCH
				? "Magic: High Alch " + cfg.magicAlchItem
				: "Magic: Cast " + cfg.magicSpell + " on " + cfg.magicTargetNpc;
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
		if (cfg.magicMode == Config.MagicMode.HIGH_ALCH) {
			return highAlch();
		}
		return combatCast();
	}

	/* ---------------- HIGH ALCH ---------------- */

	private int highAlch() {
		Item target = Inventory.get(cfg.magicAlchItem);
		if (target == null) {
			Logger.log("[Magic] Out of '" + cfg.magicAlchItem + "' to alch - stopping.");
			return -1;
		}
		Spell alch = Normal.HIGH_LEVEL_ALCHEMY;
		if (!Magic.canCast(alch)) {
			Logger.log("[Magic] Cannot cast High Alchemy (runes/level?) - stopping.");
			return -1;
		}
		if (Magic.castSpellOnItem(alch, target)) {
			castCount++;
			// High alch cast is ~5 ticks (3s); humanise slightly.
			return Calculations.random(2900, 3300);
		}
		return Calculations.random(400, 700);
	}

	/* ---------------- COMBAT CAST ---------------- */

	private int combatCast() {
		Spell spell = resolveSpell(cfg.magicSpell);
		if (spell == null) {
			Logger.log("[Magic] Unknown spell '" + cfg.magicSpell + "' - stopping.");
			return -1;
		}
		if (cfg.magicSafespot && cfg.magicSafespotTileSet()
				&& cfg.magicSafespotTile().distance() > 0) {
			movement.walkTo(cfg.magicSafespotTile(), 0);
			return Calculations.random(150, 350);
		}
		// Already attacking something - let it resolve.
		if (combat.inCombat() && combat.currentTarget() != null) {
			return Calculations.random(500, 900);
		}
		NPC target = combat.findByName(cfg.magicTargetNpc);
		if (target == null || !target.exists()) {
			if (cfg.magicAnchorTileSet()) movement.walkTo(cfg.magicAnchorTile(), 4);
			return Calculations.random(400, 700);
		}
		if (Magic.castSpellOn(spell, target)) {
			castCount++;
			final NPC t = target;
			Sleep.sleepUntil(() -> combat.inCombat() || !t.exists(), 2500);
			return Calculations.random(600, 1000);
		}
		return Calculations.random(400, 700);
	}

	/** Maps a free-text spell name onto the Normal spellbook (case/spacing tolerant). */
	private Spell resolveSpell(String name) {
		if (name == null) return null;
		String key = name.trim().toUpperCase().replace(' ', '_').replace("-", "_");
		for (Normal s : Normal.values()) {
			if (s.name().equals(key)) return s;
		}
		return null;
	}

	@Override
	public int getProfit() {
		// Only High Alch has a meaningful GP figure; combat cast reports XP via casts.
		if (cfg.magicMode == Config.MagicMode.HIGH_ALCH) {
			return castCount * pricer.getPrice();
		}
		return 0;
	}

	@Override
	public String getStatus() {
		return "Casts: " + castCount + " | Mode: " + cfg.magicMode;
	}
}
