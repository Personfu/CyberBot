package nezz.dreambot.aio.antiban;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.mouse.MouseSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.interactive.Players;

import java.awt.*;

/**
 * Lightweight, probability-gated antiban actions. Each call to {@link #tick()}
 * has a small chance of performing one human-like idle behaviour. Designed to
 * be cheap and safe to call from the main loop at any time.
 */
public class Antiban {

	private long lastActionTime = 0;
	private long nextActionDelay = randomDelay();

	private long randomDelay() {
		// 20s - 90s between antiban actions
		return Calculations.random(20000, 90000);
	}

	/**
	 * Possibly performs one antiban behaviour. Returns true if an action ran.
	 */
	public boolean tick() {
		long now = System.currentTimeMillis();
		if (now - lastActionTime < nextActionDelay) {
			return false;
		}
		lastActionTime = now;
		nextActionDelay = randomDelay();

		int roll = Calculations.random(0, 100);
		if (roll < 35) {
			return examineSkill();
		} else if (roll < 60) {
			return moveMouseOffScreen();
		} else if (roll < 80) {
			return openRandomTab();
		} else {
			return shortReactionPause();
		}
	}

	private boolean examineSkill() {
		Skill[] skills = Skill.values();
		Skill skill = skills[Calculations.random(0, skills.length - 1)];
		if (!Tabs.isOpen(Tab.SKILLS)) {
			Tabs.open(Tab.SKILLS);
			Sleep.sleep(Calculations.random(200, 500));
		}
		// Hovering experience is enough to look human; we just read the value.
		Skills.getExperience(skill);
		Sleep.sleep(Calculations.random(300, 900));
		return true;
	}

	private boolean moveMouseOffScreen() {
		Point p = new Point(Calculations.random(0, 765), Calculations.random(0, 503));
		Mouse.move(p);
		Sleep.sleep(Calculations.random(150, 600));
		return true;
	}

	private boolean openRandomTab() {
		Tab[] options = {Tab.INVENTORY, Tab.SKILLS, Tab.EQUIPMENT, Tab.STATS};
		Tab tab = options[Calculations.random(0, options.length - 1)];
		Tabs.open(tab);
		Sleep.sleep(Calculations.random(200, 800));
		if (tab != Tab.INVENTORY) {
			Tabs.open(Tab.INVENTORY);
		}
		return true;
	}

	private boolean shortReactionPause() {
		Player local = Players.getLocal();
		// Slightly longer pause as if the player looked away.
		Sleep.sleep(Calculations.random(800, 2500));
		return true;
	}
}
