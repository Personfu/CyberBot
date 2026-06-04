package nezz.dreambot.unusualmoney.gui;

public class ScriptVars {
	public boolean started = false;
	public MoneyMethod method = MoneyMethod.SOUL_RUNES;
	public int jadesPerWorld = 20;
	public int soulRunesMinStock = 0;

	public enum MoneyMethod {
		JADE_TRADING_STICKS("Jade -> Trading Sticks (Tai Bwo Wannai)"),
		SOUL_RUNES("Soul Runes (Shop Buyer)"),
		APPLE_MUSH("Apple Mush (Apple Press)"),
		FIRE_SHADES("Fire Shades (Temple Trekking)");

		private final String description;

		MoneyMethod(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
