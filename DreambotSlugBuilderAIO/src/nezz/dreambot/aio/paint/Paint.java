package nezz.dreambot.aio.paint;

import nezz.dreambot.aio.money.MoneyTask;
import nezz.dreambot.aio.security.RuneGuardClient;
import org.dreambot.api.utilities.Timer;

import java.awt.*;

/**
 * Renders the on-screen status overlay: active module, profit, GP/hr, runtime
 * and RuneGuard status.
 */
public class Paint {

	private final Timer timer;

	public Paint(Timer timer) {
		this.timer = timer;
	}

	public void render(Graphics g, MoneyTask task, RuneGuardClient runeGuard) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(new Color(0, 0, 0, 170));
		g2.fillRoundRect(5, 30, 320, 110, 10, 10);
		g2.setColor(new Color(120, 90, 220));
		g2.drawRoundRect(5, 30, 320, 110, 10, 10);

		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Arial", Font.BOLD, 13));
		g2.drawString("Slug Builder AIO Premium v1.2", 14, 48);

		g2.setFont(new Font("Arial", Font.PLAIN, 12));
		int y = 66;
		if (task != null) {
			g2.drawString("Module: " + task.name(), 14, y); y += 15;
			g2.drawString(task.getStatus(), 14, y); y += 15;
			int profit = task.getProfit();
			g2.drawString("Profit: " + profit + " (" + timer.getHourlyRate(profit) + "/hr)", 14, y); y += 15;
		} else {
			g2.drawString("Module: idle", 14, y); y += 15;
		}
		g2.drawString("Runtime: " + timer.formatTime(), 14, y); y += 15;

		String rg = runeGuard == null || !runeGuard.isEnabled() ? "off"
				: runeGuard.isActive() ? "active" : "error";
		g2.setColor("active".equals(rg) ? new Color(46, 200, 90)
				: "error".equals(rg) ? new Color(220, 70, 70) : Color.LIGHT_GRAY);
		g2.drawString("RuneGuard: " + rg, 220, 48);
	}
}
