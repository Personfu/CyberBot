package nezz.dreambot.aio.combat;

import org.dreambot.api.methods.map.Tile;

import java.util.Arrays;
import java.util.List;

/**
 * Ordered progression ladder of training targets from level 3 through 70+.
 * {@link #forLevel(int)} returns the best target for the player's combat level.
 */
public final class TrainingLadder {

	private TrainingLadder() {}

	private static final Tile LUMBRIDGE_BANK = new Tile(3208, 3220, 2);
	private static final Tile EDGEVILLE_BANK = new Tile(3094, 3491, 0);
	private static final Tile FALADOR_BANK = new Tile(2946, 3368, 0);

	private static final List<TrainingTarget> LADDER = Arrays.asList(
			new TrainingTarget("Chickens", "Chicken",
					3, 10,
					new Tile(3235, 3295, 0), LUMBRIDGE_BANK, null,
					"Bones", "Raw chicken", "Feather"),

			new TrainingTarget("Cows", "Cow",
					10, 20,
					new Tile(3253, 3267, 0), LUMBRIDGE_BANK, null,
					"Bones", "Raw beef", "Cowhide"),

			new TrainingTarget("Goblins", "Goblin",
					3, 15,
					new Tile(3247, 3236, 0), LUMBRIDGE_BANK, null,
					"Bones", "Coins"),

			new TrainingTarget("Al Kharid Warriors", "Al-Kharid warrior",
					15, 30,
					new Tile(3293, 3174, 0), new Tile(3269, 3167, 0), null,
					"Bones", "Coins"),

			new TrainingTarget("Barbarians", "Barbarian",
					15, 30,
					new Tile(3081, 3421, 0), EDGEVILLE_BANK, null,
					"Bones", "Coins"),

			new TrainingTarget("Hill Giants", "Hill giant",
					30, 50,
					new Tile(3117, 9852, 0), EDGEVILLE_BANK, null,
					"Big bones", "Limpwurt root", "Giant key"),

			new TrainingTarget("Moss Giants", "Moss giant",
					40, 60,
					new Tile(3157, 9906, 0), EDGEVILLE_BANK, null,
					"Big bones", "Mossy key", "Limpwurt root"),

			new TrainingTarget("Lesser Demons", "Lesser demon",
					50, 70,
					new Tile(2843, 9573, 0), EDGEVILLE_BANK,
					new Tile(2841, 9569, 0),
					"Rune medium helm"),

			new TrainingTarget("Greater Demons", "Greater demon",
					65, 999,
					new Tile(2843, 9573, 0), EDGEVILLE_BANK, null,
					"Rune full helm"),

			new TrainingTarget("Giant Spiders", "Giant spider",
					20, 40,
					new Tile(3168, 9896, 0), EDGEVILLE_BANK, null)
	);

	public static TrainingTarget forLevel(int combatLevel) {
		TrainingTarget best = LADDER.get(0);
		for (TrainingTarget t : LADDER) {
			if (combatLevel >= t.minCombatLevel && combatLevel <= t.maxCombatLevel) {
				best = t;
			}
		}
		return best;
	}

	public static List<TrainingTarget> all() {
		return LADDER;
	}
}
