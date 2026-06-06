package nezz.dreambot.aio;

import nezz.dreambot.aio.antiban.Antiban;
import nezz.dreambot.aio.boss.BossTask;
import nezz.dreambot.aio.gui.AIOGui;
import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.money.*;
import nezz.dreambot.aio.paint.Paint;
import nezz.dreambot.aio.security.RuneGuardClient;
import nezz.dreambot.aio.task.StatsProvider;
import nezz.dreambot.aio.task.Task;
import nezz.dreambot.aio.task.TaskManager;
import nezz.dreambot.aio.webhook.WebhookManager;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;

import java.awt.*;

/**
 * Slug Builder AIO Premium v1.2 - task-based all-in-one engine.
 *
 * The engine drives a {@link TaskManager}; modules are plain {@link Task}s, so
 * new skills/money methods can be added without touching this class. Ships with
 * four unusual money-making modules, antiban, Discord webhooks and optional
 * RuneGuard runtime signing.
 */
@ScriptManifest(
		author = "Nezz",
		name = "Slug Builder AIO",
		version = 1.2,
		category = Category.MONEYMAKING,
		description = "All-in-one money maker: Jade sticks, Soul runes, Apple mush, Fire shades. Antiban, webhooks, RuneGuard."
)
public class AIOScript extends AbstractScript {

	private final Config cfg = new Config();
	private final TaskManager tasks = new TaskManager();

	private Timer timer;
	private Paint paint;
	private Antiban antiban;
	private WebhookManager webhook;
	private RuneGuardClient runeGuard;
	private Task activeTask;
	private StatsProvider stats;
	private boolean started = false;

	@Override
	public void onStart() {
		AIOGui gui = new AIOGui(cfg);
		gui.setVisible(true);
		while (!cfg.started) {
			Sleep.sleep(200);
		}

		activeTask = buildModule();
		stats = (StatsProvider) activeTask;
		tasks.add(activeTask);

		timer = new Timer();
		paint = new Paint(timer);
		antiban = new Antiban();

		webhook = new WebhookManager(cfg.webhookEnabled, cfg.webhookUrl, cfg.webhookIntervalMinutes);

		runeGuard = new RuneGuardClient(
				cfg.runeGuardEnabled ? cfg.runeGuardSigningKey : null,
				cfg.runeGuardScriptToken,
				cfg.scriptName,
				cfg.scriptVersion,
				Logger::log);
		if (runeGuard.isEnabled()) {
			runeGuard.start();
		}

		webhook.send("Slug Builder AIO started", "Module: " + activeTask.name());
		started = true;
		Logger.log("[AIO] Started module: " + activeTask.name());
	}

	private Task buildModule() {
		if (cfg.activity == Config.Activity.BOSS) {
			return new BossTask(cfg);
		}
		switch (cfg.module) {
			case JADE_TRADING_STICKS: return new JadeTradingSticksTask(cfg);
			case APPLE_MUSH: return new AppleMushTask(cfg);
			case FIRE_SHADES: return new FireShadesTask(cfg);
			case SOUL_RUNES:
			default: return new SoulRuneBuyerTask(cfg);
		}
	}

	@Override
	public int onLoop() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		// Don't act mid-walk to a far destination.
		if (Players.getLocal() != null && Players.getLocal().isMoving()
				&& Client.getDestination() != null
				&& Client.getDestination().distance(Players.getLocal().getTile()) > 4) {
			return Calculations.random(200, 300);
		}

		if (cfg.antibanEnabled && antiban.tick()) {
			return Calculations.random(100, 250);
		}

		int result = tasks.loop();

		if (stats != null) {
			webhook.maybeSend("Slug Builder AIO progress",
					activeTask.name() + "\n" + stats.getStatus()
							+ "\nProfit/hr: " + timer.getHourlyRate(stats.getProfit()));
		}

		if (result < 0) {
			Logger.log("[AIO] Module requested stop.");
			stop();
			return -1;
		}
		return result;
	}

	@Override
	public void onPaint(Graphics g) {
		if (started && paint != null) {
			paint.render(g, activeTask, stats, runeGuard);
		}
	}

	@Override
	public void onExit() {
		tasks.stopAll();
		if (webhook != null && stats != null) {
			webhook.send("Slug Builder AIO stopped",
					activeTask.name() + " | Final profit: " + stats.getProfit());
		}
		if (runeGuard != null) {
			runeGuard.close();
		}
	}
}
