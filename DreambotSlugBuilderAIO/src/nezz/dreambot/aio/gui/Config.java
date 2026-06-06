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
		BOSS("Bossing");

		private final String label;
		Activity(String label) { this.label = label; }
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
