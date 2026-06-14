package nezz.dreambot.aio.gui;

/**
 * Mutable configuration shared between the GUI and the running script.
 */
public class Config {

	public volatile boolean started = false;

	/** Top-level activity category. */
	public Activity activity = Activity.MONEY;

	/** Selected money-making module. */
	public MoneyModule module = MoneyModule.SOUL_RUNES;

	/* ---- Bossing ---- */
	public BossType boss = BossType.BRUTUS;
	public String bossNameOverride = "";   // optional: override the NPC name to target
	public String foodName = "Shark";
	public int eatAtHpPercent = 50;
	/** Comma-separated equipment item names to keep equipped during the fight. */
	public String gearLoadout = "";
	public boolean flickProtectMelee = true;
	public int minLootValue = 1000;        // only pick up ground loot worth >= this

	public enum Activity {
		MONEY("Money Making"),
		BOSS("Bossing"),
		MONSTERS("Monsters (Training / F2P Money)"),
		COMBAT_TRAINER("Combat Trainer (Auto-Progression)"),
		MAGIC("Magic (High Alch / Combat Cast)");

		private final String label;
		Activity(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	public enum MonsterTarget {
		HILL_GIANTS("Hill Giants (big bones, limpwurt)"),
		CHAOS_DRUIDS("Chaos Druids (grimy herbs)"),
		GREEN_DRAGONS("Green Dragons (bones, hides)"),
		MOSS_GIANTS("Moss Giants (big bones, keys)"),
		LESSER_DEMONS("Lesser Demons (magic safespot)");

		private final String label;
		MonsterTarget(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	public enum BossType {
		BRUTUS("Brutus"),
		SEWER_AXEMAN("Varrock Sewers (axe adds)"),
		KING_BLACK_DRAGON("King Black Dragon"),
		GIANT_MOLE("Giant Mole"),
		CHAOS_ELEMENTAL("Chaos Elemental"),
		IVAR_KING_OF_BONES("Ivar, King of Bones"),
		SCURRIUS("Scurrius"),
		OBOR("Obor"),
		BRYOPHYTA("Bryophyta"),
		DERANGED_ARCHAEOLOGIST("Deranged Archaeologist");

		private final String label;
		BossType(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	/* ---- Module tuning ---- */
	public int jadesPerWorld = 20;
	public int soulRunesMinStock = 0;
	public boolean membersWorldsOnly = true;

	/* ---- Monsters (training / F2P money) ---- */
	public MonsterTarget monster = MonsterTarget.HILL_GIANTS;
	public String monsterNameOverride = "";
	public boolean monsterSafespot = true;
	public int monsterLootValue = 200;
	public boolean useFoodForMonsters = true;

	/* ---- Combat Trainer ---- */
	public boolean useFoodForCombatTrainer = true;
	public boolean combatTrainerSafespot = false;
	public int combatTrainerLootValue = 100;

	/* ---- Magic ---- */
	public MagicMode magicMode = MagicMode.HIGH_ALCH;
	public String magicAlchItem = "Yew longbow";
	public String magicSpell = "FIRE_STRIKE";
	public String magicTargetNpc = "Chicken";
	public boolean magicSafespot = false;
	// Optional tiles encoded as "x,y,z"; blank = unset.
	public String magicSafespotTileStr = "";
	public String magicAnchorTileStr = "";

	public enum MagicMode {
		HIGH_ALCH("High Alchemy (item)"),
		COMBAT_CAST("Combat Cast (NPC)");
		private final String label;
		MagicMode(String label) { this.label = label; }
		@Override public String toString() { return label; }
	}

	public boolean magicSafespotTileSet() { return parseTile(magicSafespotTileStr) != null; }
	public boolean magicAnchorTileSet() { return parseTile(magicAnchorTileStr) != null; }
	public org.dreambot.api.methods.map.Tile magicSafespotTile() { return parseTile(magicSafespotTileStr); }
	public org.dreambot.api.methods.map.Tile magicAnchorTile() { return parseTile(magicAnchorTileStr); }

	private static org.dreambot.api.methods.map.Tile parseTile(String s) {
		if (s == null) return null;
		String[] p = s.trim().split("\\s*,\\s*");
		if (p.length < 2) return null;
		try {
			int x = Integer.parseInt(p[0]);
			int y = Integer.parseInt(p[1]);
			int z = p.length >= 3 ? Integer.parseInt(p[2]) : 0;
			return new org.dreambot.api.methods.map.Tile(x, y, z);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/* ---- Antiban ---- */
	public boolean antibanEnabled = true;

	/* ---- Webhooks ---- */
	public boolean webhookEnabled = false;
	public String webhookUrl = "";
	public int webhookIntervalMinutes = 15;

	/* ---- RuneGuard ---- */
	public boolean runeGuardEnabled = false;
	public String runeGuardSigningKey = "";
	public String runeGuardScriptToken = "";
	public String scriptName = "Slug Builder AIO";
	public String scriptVersion = "1.2";

	public enum MoneyModule {
		JADE_TRADING_STICKS("Jade -> Trading Sticks (Tai Bwo Wannai)"),
		SOUL_RUNES("Soul Runes (Shop Buyer)"),
		APPLE_MUSH("Apple Mush (Apple Press)"),
		FIRE_SHADES("Fire Shades (Temple Trekking)");

		private final String label;

		MoneyModule(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
