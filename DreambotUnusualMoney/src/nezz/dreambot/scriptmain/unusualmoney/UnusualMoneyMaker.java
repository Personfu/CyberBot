package nezz.dreambot.scriptmain.unusualmoney;

import nezz.dreambot.unusualmoney.gui.ScriptVars;
import nezz.dreambot.unusualmoney.gui.UnusualMoneyGui;
import nezz.dreambot.unusualmoney.methods.*;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;

import java.awt.*;

@ScriptManifest(
	author = "Nezz",
	name = "Unusual Money Makers",
	version = 1,
	category = Category.MONEYMAKING,
	description = "Runs unusual money making methods: Jade Trading Sticks, Soul Rune buying, Apple Mush, Fire Shades Trekking"
)
public class UnusualMoneyMaker extends AbstractScript {

	private ScriptVars sv = new ScriptVars();
	private MoneyMethod activeMethod;
	private Timer timer;
	private boolean started = false;

	@Override
	public void onStart() {
		UnusualMoneyGui gui = new UnusualMoneyGui(sv);
		gui.setVisible(true);
		while (!sv.started) {
			Sleep.sleep(200);
		}

		switch (sv.method) {
			case JADE_TRADING_STICKS:
				activeMethod = new JadeTradingSticks(sv);
				break;
			case SOUL_RUNES:
				activeMethod = new SoulRuneBuyer(sv);
				break;
			case APPLE_MUSH:
				activeMethod = new AppleMush(sv);
				break;
			case FIRE_SHADES:
				activeMethod = new FireShadesTrekking(sv);
				break;
		}

		timer = new Timer();
		started = true;
		log("Starting method: " + activeMethod.getMethodName());
	}

	@Override
	public int onLoop() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		int result = activeMethod.execute();
		if (result < 0) {
			log("Method signaled stop. Final profit: " + activeMethod.getProfit());
			stop();
			return -1;
		}
		return result;
	}

	@Override
	public void onPaint(Graphics g) {
		if (!started || activeMethod == null)
			return;

		g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(5, 30, 300, 80);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 12));

		g.drawString("Method: " + activeMethod.getMethodName(), 10, 45);
		g.drawString(activeMethod.getStatus(), 10, 60);
		g.drawString("Est. GP/hr: " + timer.getHourlyRate(activeMethod.getProfit()), 10, 75);
		g.drawString("Runtime: " + timer.formatTime(), 10, 90);
	}
}
