package nezz.dreambot.aio.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Tabbed configuration GUI for the AIO: module selection + tuning, antiban,
 * webhooks and RuneGuard. Sets {@link Config#started} when the user starts.
 */
public class AIOGui extends JFrame {

	public AIOGui(Config cfg) {
		setTitle("Slug Builder AIO Premium v1.2");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(440, 420));

		JPanel top = new JPanel(new GridLayout(0, 1, 4, 4));
		top.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
		top.add(new JLabel("Activity:"));
		JComboBox<Config.Activity> activity = new JComboBox<>(Config.Activity.values());
		activity.setSelectedItem(cfg.activity);
		activity.addActionListener(e -> cfg.activity = (Config.Activity) activity.getSelectedItem());
		top.add(activity);
		add(top, BorderLayout.NORTH);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Money", buildMoneyTab(cfg));
		tabs.addTab("Bossing", buildBossTab(cfg));
		tabs.addTab("Monsters", buildMonsterTab(cfg));
		tabs.addTab("Antiban", buildAntibanTab(cfg));
		tabs.addTab("Webhooks", buildWebhookTab(cfg));
		tabs.addTab("RuneGuard", buildRuneGuardTab(cfg));
		add(tabs, BorderLayout.CENTER);

		JButton start = new JButton("START");
		start.setBackground(new Color(46, 160, 67));
		start.setForeground(Color.WHITE);
		start.addActionListener(e -> {
			cfg.started = true;
			dispose();
		});
		add(start, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(null);
	}

	private JPanel buildMoneyTab(Config cfg) {
		JPanel p = grid();
		p.add(new JLabel("Money Method:"));
		JComboBox<Config.MoneyModule> box = new JComboBox<>(Config.MoneyModule.values());
		box.setSelectedItem(cfg.module);
		box.addActionListener(e -> cfg.module = (Config.MoneyModule) box.getSelectedItem());
		p.add(box);

		p.add(new JLabel("Jades per world (Jade method):"));
		JSpinner jades = new JSpinner(new SpinnerNumberModel(cfg.jadesPerWorld, 5, 50, 5));
		jades.addChangeListener(e -> cfg.jadesPerWorld = (int) jades.getValue());
		p.add(jades);

		p.add(new JLabel("Soul rune min stock to leave:"));
		JSpinner stock = new JSpinner(new SpinnerNumberModel(cfg.soulRunesMinStock, 0, 250, 10));
		stock.addChangeListener(e -> cfg.soulRunesMinStock = (int) stock.getValue());
		p.add(stock);
		return p;
	}

	private JPanel buildBossTab(Config cfg) {
		JPanel p = grid();
		p.add(new JLabel("Boss:"));
		JComboBox<Config.BossType> box = new JComboBox<>(Config.BossType.values());
		box.setSelectedItem(cfg.boss);
		box.addActionListener(e -> cfg.boss = (Config.BossType) box.getSelectedItem());
		p.add(box);

		p.add(new JLabel("Boss NPC name override (optional):"));
		JTextField nameOverride = new JTextField(cfg.bossNameOverride);
		nameOverride.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.bossNameOverride = nameOverride.getText().trim();
			}
		});
		p.add(nameOverride);

		p.add(new JLabel("Food name:"));
		JTextField food = new JTextField(cfg.foodName);
		food.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.foodName = food.getText().trim();
			}
		});
		p.add(food);

		p.add(new JLabel("Eat at HP %:"));
		JSpinner eat = new JSpinner(new SpinnerNumberModel(cfg.eatAtHpPercent, 10, 90, 5));
		eat.addChangeListener(e -> cfg.eatAtHpPercent = (int) eat.getValue());
		p.add(eat);

		p.add(new JLabel("Min loot value to pick up:"));
		JSpinner loot = new JSpinner(new SpinnerNumberModel(cfg.minLootValue, 0, 1_000_000, 500));
		loot.addChangeListener(e -> cfg.minLootValue = (int) loot.getValue());
		p.add(loot);

		p.add(new JLabel("Gear loadout (comma-separated, optional):"));
		JTextField gear = new JTextField(cfg.gearLoadout);
		gear.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.gearLoadout = gear.getText().trim();
			}
		});
		p.add(gear);

		JCheckBox flick = new JCheckBox("Flick Protect from Melee", cfg.flickProtectMelee);
		flick.addActionListener(e -> cfg.flickProtectMelee = flick.isSelected());
		p.add(flick);
		return p;
	}

	private JPanel buildMonsterTab(Config cfg) {
		JPanel p = grid();
		p.add(new JLabel("Target:"));
		JComboBox<Config.MonsterTarget> box = new JComboBox<>(Config.MonsterTarget.values());
		box.setSelectedItem(cfg.monster);
		box.addActionListener(e -> cfg.monster = (Config.MonsterTarget) box.getSelectedItem());
		p.add(box);

		p.add(new JLabel("NPC name override (optional):"));
		JTextField name = new JTextField(cfg.monsterNameOverride);
		name.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.monsterNameOverride = name.getText().trim();
			}
		});
		p.add(name);

		p.add(new JLabel("Min loot value to pick up:"));
		JSpinner loot = new JSpinner(new SpinnerNumberModel(cfg.monsterLootValue, 0, 1_000_000, 100));
		loot.addChangeListener(e -> cfg.monsterLootValue = (int) loot.getValue());
		p.add(loot);

		JCheckBox safespot = new JCheckBox("Use safespot (ranged/magic targets)", cfg.monsterSafespot);
		safespot.addActionListener(e -> cfg.monsterSafespot = safespot.isSelected());
		p.add(safespot);

		JCheckBox food = new JCheckBox("Bank/eat food", cfg.useFoodForMonsters);
		food.addActionListener(e -> cfg.useFoodForMonsters = food.isSelected());
		p.add(food);
		return p;
	}

	private JPanel buildAntibanTab(Config cfg) {
		JPanel p = grid();
		JCheckBox ab = new JCheckBox("Enable antiban", cfg.antibanEnabled);
		ab.addActionListener(e -> cfg.antibanEnabled = ab.isSelected());
		p.add(ab);
		p.add(new JLabel("Adds human-like idle actions: camera, tab"));
		p.add(new JLabel("switches, mouse moves and reaction pauses."));
		return p;
	}

	private JPanel buildWebhookTab(Config cfg) {
		JPanel p = grid();
		JCheckBox wh = new JCheckBox("Enable Discord webhook", cfg.webhookEnabled);
		wh.addActionListener(e -> cfg.webhookEnabled = wh.isSelected());
		p.add(wh);
		p.add(new JLabel("Webhook URL:"));
		JTextField url = new JTextField(cfg.webhookUrl);
		url.addActionListener(e -> cfg.webhookUrl = url.getText().trim());
		p.add(url);
		p.add(new JLabel("Update interval (minutes):"));
		JSpinner interval = new JSpinner(new SpinnerNumberModel(cfg.webhookIntervalMinutes, 1, 240, 5));
		interval.addChangeListener(e -> cfg.webhookIntervalMinutes = (int) interval.getValue());
		p.add(interval);
		// Capture the URL even if the user doesn't press enter.
		url.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.webhookUrl = url.getText().trim();
			}
		});
		return p;
	}

	private JPanel buildRuneGuardTab(Config cfg) {
		JPanel p = grid();
		JCheckBox rg = new JCheckBox("Enable RuneGuard signing", cfg.runeGuardEnabled);
		rg.addActionListener(e -> cfg.runeGuardEnabled = rg.isSelected());
		p.add(rg);
		p.add(new JLabel("Signing key (PEM):"));
		JTextField key = new JTextField(cfg.runeGuardSigningKey);
		key.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.runeGuardSigningKey = key.getText().trim();
			}
		});
		p.add(key);
		p.add(new JLabel("Script token:"));
		JTextField token = new JTextField(cfg.runeGuardScriptToken);
		token.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(java.awt.event.FocusEvent e) {
				cfg.runeGuardScriptToken = token.getText().trim();
			}
		});
		p.add(token);
		return p;
	}

	private JPanel grid() {
		JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return p;
	}
}
