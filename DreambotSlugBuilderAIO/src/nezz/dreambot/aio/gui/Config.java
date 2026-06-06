package nezz.dreambot.aio.gui;

/**
 * Mutable configuration shared between the GUI and the running script.
 */
public class Config {

	public volatile boolean started = false;

	/** Selected money-making module. */
	public MoneyModule module = MoneyModule.SOUL_RUNES;

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
