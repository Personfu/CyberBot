package nezz.dreambot.allinone.gui;

import nezz.dreambot.allinone.config.LootEntry;
import nezz.dreambot.allinone.config.NpcLootProfile;
import nezz.dreambot.allinone.config.ScriptConfig;
import nezz.dreambot.allinone.tasks.LootTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Full Swing GUI for the All-In-One DreamBot script.
 * Four tabs: Combat | Loot Config | Rates Simulator | SDN Setup.
 * Call {@link #show(ScriptConfig, Runnable, Runnable)} to open the window.
 * The start/stop runnables are invoked on the Swing EDT — callers must be thread-safe.
 */
public class AllInOneGui {

    // ── Dark OSRS colour palette ──────────────────────────────────────────────
    private static final Color BG       = new Color(30,  30,  30);
    private static final Color PANEL_BG = new Color(40,  40,  40);
    private static final Color FIELD_BG = new Color(50,  50,  50);
    private static final Color FG       = new Color(220, 220, 220);
    private static final Color ACCENT   = new Color(255, 200,  30);  // gold
    private static final Color GREEN    = new Color( 80, 200,  80);
    private static final Color RED_COL  = new Color(220,  60,  60);
    private static final Font  FONT     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  BOLD     = new Font("Segoe UI", Font.BOLD,  13);

    private JFrame  frame;
    private boolean running = false;
    private JButton startStopBtn;

    // ── Per-tab refs ──────────────────────────────────────────────────────────
    // Combat tab
    private JComboBox<String> npcCombo;
    private JTextField        npcIdField;
    private JRadioButton      rbMelee, rbRanged, rbMagic;
    private JComboBox<String> foodCombo;
    private JSpinner          eatAtHpSpinner;
    private JSpinner          minFoodSpinner;
    private JCheckBox         bankWhenFullChk;
    private JSpinner          bankDistSpinner;
    private JSpinner          lootRadiusSpinner;

    // Loot Config tab
    private LootTableModel    lootTableModel;

    // Rates Simulator tab
    private JComboBox<String> simNpcCombo;
    private JSpinner          simKillsSpinner;
    private JSpinner          simDropRateSpinner;
    private JSpinner          simRareSpinner;
    private JTextArea         simResultArea;

    // SDN tab
    private JTextField sdnRepoField, sdnModuleField, sdnParamsField;

    // ─────────────────────────────────────────────────────────────────────────

    public void show(ScriptConfig config, Runnable onStart, Runnable onStop) {
        SwingUtilities.invokeLater(() -> build(config, onStart, onStop));
    }

    private void build(ScriptConfig config, Runnable onStart, Runnable onStop) {
        frame = new JFrame("FLLC All-In-One  v1.1  —  by Personfu");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(true);
        frame.getContentPane().setBackground(BG);
        frame.setLayout(new BorderLayout(0, 4));

        // ── Header ────────────────────────────────────────────────────────────
        JLabel header = new JLabel("FLLC ALL-IN-ONE", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(ACCENT);
        header.setBackground(new Color(20, 20, 20));
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        frame.add(header, BorderLayout.NORTH);

        // ── Tabs ──────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        styleTabs(tabs);

        tabs.addTab("⚔  Combat",       buildCombatTab(config));
        tabs.addTab("🎒  Loot Config",  buildLootTab(config));
        tabs.addTab("📊  Rates Sim",    buildSimTab(config));
        tabs.addTab("🔗  SDN Setup",    buildSdnTab(config));

        frame.add(tabs, BorderLayout.CENTER);

        // ── Start / Stop ──────────────────────────────────────────────────────
        startStopBtn = new JButton("▶  START");
        startStopBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startStopBtn.setBackground(GREEN);
        startStopBtn.setForeground(Color.BLACK);
        startStopBtn.setFocusPainted(false);
        startStopBtn.setBorder(new EmptyBorder(12, 0, 12, 0));
        startStopBtn.addActionListener(e -> {
            if (!running) {
                applyToConfig(config);
                running = true;
                startStopBtn.setText("⏹  STOP");
                startStopBtn.setBackground(RED_COL);
                startStopBtn.setForeground(Color.WHITE);
                onStart.run();
            } else {
                running = false;
                startStopBtn.setText("▶  START");
                startStopBtn.setBackground(GREEN);
                startStopBtn.setForeground(Color.BLACK);
                onStop.run();
            }
        });
        frame.add(startStopBtn, BorderLayout.SOUTH);

        frame.pack();
        frame.setMinimumSize(new Dimension(560, 520));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 1 — COMBAT
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildCombatTab(ScriptConfig config) {
        JPanel root = darkPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // NPC name
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        root.add(label("Target NPC:"), g);
        npcCombo = new JComboBox<>(NpcLootProfile.KNOWN_NPCS);
        npcCombo.setEditable(true);
        npcCombo.setSelectedItem(config.getTargetNpc());
        styleCombo(npcCombo);
        // When the NPC changes, reload the loot table
        npcCombo.addActionListener(e -> {
            String sel = (String) npcCombo.getSelectedItem();
            if (sel != null && !sel.isEmpty()) {
                config.setTargetNpc(sel);
                config.resetLootEntriesToDefaults();
                if (lootTableModel != null) {
                    lootTableModel.setEntries(config.getLootEntries());
                }
                // Auto-fill NPC ID
                int id = NpcIdMap.getOrDefault(sel, 0);
                npcIdField.setText(String.valueOf(id));
                config.setTargetNpcId(id);
            }
        });
        g.gridx = 1; g.weightx = 1;
        root.add(npcCombo, g);

        // NPC ID
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("NPC ID (optional):"), g);
        npcIdField = styleField(new JTextField(String.valueOf(config.getTargetNpcId()), 8));
        g.gridx = 1; g.weightx = 1;
        root.add(npcIdField, g);

        // Attack style
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Attack Style:"), g);
        JPanel stylePanel = darkPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ButtonGroup bg = new ButtonGroup();
        rbMelee  = styleRadio("Melee",  "MELEE".equals(config.getAttackStyle()));
        rbRanged = styleRadio("Ranged", "RANGED".equals(config.getAttackStyle()));
        rbMagic  = styleRadio("Magic",  "MAGIC".equals(config.getAttackStyle()));
        bg.add(rbMelee); bg.add(rbRanged); bg.add(rbMagic);
        stylePanel.add(rbMelee); stylePanel.add(rbRanged); stylePanel.add(rbMagic);
        g.gridx = 1; g.weightx = 1;
        root.add(stylePanel, g);

        // Food
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Food:"), g);
        foodCombo = new JComboBox<>(new String[]{"None","Lobster","Swordfish","Shark","Monkfish","Anglerfish","Saradomin brew","Prayer potion (4)"});
        foodCombo.setSelectedItem(config.getFoodName());
        styleCombo(foodCombo);
        g.gridx = 1; g.weightx = 1;
        root.add(foodCombo, g);

        // Eat at HP %
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Eat below HP %:"), g);
        eatAtHpSpinner = new JSpinner(new SpinnerNumberModel(config.getEatAtHpPercent(), 10, 80, 5));
        styleSpinner(eatAtHpSpinner);
        g.gridx = 1; g.weightx = 1;
        root.add(eatAtHpSpinner, g);

        // Min food count
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Min food before bank:"), g);
        minFoodSpinner = new JSpinner(new SpinnerNumberModel(config.getMinFoodCount(), 0, 28, 1));
        styleSpinner(minFoodSpinner);
        g.gridx = 1; g.weightx = 1;
        root.add(minFoodSpinner, g);

        // Bank when full
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Bank when full:"), g);
        bankWhenFullChk = new JCheckBox("", config.isBankWhenFull());
        bankWhenFullChk.setBackground(PANEL_BG);
        bankWhenFullChk.setForeground(FG);
        g.gridx = 1; g.weightx = 1;
        root.add(bankWhenFullChk, g);

        // Bank distance
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Max bank dist (tiles):"), g);
        bankDistSpinner = new JSpinner(new SpinnerNumberModel(config.getMaxBankDistance(), 1, 500, 5));
        styleSpinner(bankDistSpinner);
        g.gridx = 1; g.weightx = 1;
        root.add(bankDistSpinner, g);

        // Loot radius
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Loot radius (tiles):"), g);
        lootRadiusSpinner = new JSpinner(new SpinnerNumberModel(config.getLootRadiusTiles(), 1, 20, 1));
        styleSpinner(lootRadiusSpinner);
        g.gridx = 1; g.weightx = 1;
        root.add(lootRadiusSpinner, g);

        // Filler
        g.gridx = 0; g.gridy++; g.gridwidth = 2; g.weighty = 1;
        root.add(new JPanel() {{ setBackground(PANEL_BG); }}, g);

        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 2 — LOOT CONFIG  (per-enemy drop table)
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildLootTab(ScriptConfig config) {
        config.resetLootEntriesToDefaults();

        JPanel root = darkPanel(new BorderLayout(4, 4));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Instruction label
        JLabel info = label("Configure which items to pick up for the selected NPC. Edit Min Qty and Min GP Value columns directly.");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(new Color(180, 180, 180));
        root.add(info, BorderLayout.NORTH);

        // Drop table
        lootTableModel = new LootTableModel(config.getLootEntries());
        JTable table = new JTable(lootTableModel);
        table.setBackground(FIELD_BG);
        table.setForeground(FG);
        table.setGridColor(new Color(70, 70, 70));
        table.setFont(FONT);
        table.setRowHeight(22);
        table.getTableHeader().setBackground(new Color(60, 60, 60));
        table.getTableHeader().setForeground(ACCENT);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(40);   // enabled
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // name
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // rate
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // qty
        table.getColumnModel().getColumn(4).setPreferredWidth(70);  // min qty
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // min gp

        root.add(new JScrollPane(table) {{
            getViewport().setBackground(FIELD_BG);
        }}, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = darkPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton chkAll   = accentBtn("Check All");
        JButton unchkAll = accentBtn("Uncheck All");
        JButton highVal  = accentBtn("Valuables Only (≥50k)");
        chkAll.addActionListener   (e -> lootTableModel.setAllEnabled(true));
        unchkAll.addActionListener (e -> lootTableModel.setAllEnabled(false));
        highVal.addActionListener  (e -> lootTableModel.applyValueFilter(50_000L, LootTask.approxPriceStatic));
        btnPanel.add(chkAll); btnPanel.add(unchkAll); btnPanel.add(highVal);
        root.add(btnPanel, BorderLayout.SOUTH);

        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 3 — RATES SIMULATOR
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildSimTab(ScriptConfig config) {
        JPanel root = darkPanel(new BorderLayout(4, 4));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ── Controls ─────────────────────────────────────────────────────────
        JPanel ctrl = darkPanel(new GridBagLayout());
        ctrl.setBorder(titledBorder("Simulation Settings"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        ctrl.add(label("NPC:"), g);
        simNpcCombo = new JComboBox<>(NpcLootProfile.KNOWN_NPCS);
        simNpcCombo.setSelectedItem(config.getSimNpcName());
        styleCombo(simNpcCombo);
        g.gridx = 1; g.weightx = 1;
        ctrl.add(simNpcCombo, g);

        g.gridx = 0; g.gridy++; g.weightx = 0;
        ctrl.add(label("Simulated Kills:"), g);
        simKillsSpinner = new JSpinner(new SpinnerNumberModel(config.getSimKills(), 1, 1_000_000, 1000));
        styleSpinner(simKillsSpinner);
        g.gridx = 1; g.weightx = 1;
        ctrl.add(simKillsSpinner, g);

        g.gridx = 0; g.gridy++; g.weightx = 0;
        ctrl.add(label("Drop Rate Multiplier:"), g);
        simDropRateSpinner = new JSpinner(new SpinnerNumberModel(config.getSimDropRateMulti(), 0.01, 100.0, 0.5));
        styleSpinner(simDropRateSpinner);
        ((JSpinner.NumberEditor) simDropRateSpinner.getEditor()).getFormat().setMaximumFractionDigits(2);
        g.gridx = 1; g.weightx = 1;
        ctrl.add(simDropRateSpinner, g);

        g.gridx = 0; g.gridy++; g.weightx = 0;
        ctrl.add(label("Rare Rate Multiplier:"), g);
        simRareSpinner = new JSpinner(new SpinnerNumberModel(config.getSimRareMulti(), 0.01, 100.0, 0.5));
        styleSpinner(simRareSpinner);
        ((JSpinner.NumberEditor) simRareSpinner.getEditor()).getFormat().setMaximumFractionDigits(2);
        g.gridx = 1; g.weightx = 1;
        ctrl.add(simRareSpinner, g);

        // Run button
        JButton runBtn = new JButton("▶  Run Simulation");
        runBtn.setFont(BOLD);
        runBtn.setBackground(ACCENT);
        runBtn.setForeground(Color.BLACK);
        runBtn.setFocusPainted(false);
        runBtn.addActionListener(e -> runSimulation(config));
        g.gridx = 0; g.gridy++; g.gridwidth = 2; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER;
        ctrl.add(runBtn, g);

        root.add(ctrl, BorderLayout.NORTH);

        // ── Results ───────────────────────────────────────────────────────────
        simResultArea = new JTextArea(
            "Click \"Run Simulation\" to simulate kills and see a drop histogram.\n" +
            "Drop Rate Multiplier = 2.0 doubles the chance of every drop.\n" +
            "Rare Rate Multiplier applies an additional boost to rare items.");
        simResultArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        simResultArea.setBackground(new Color(20, 20, 20));
        simResultArea.setForeground(new Color(200, 255, 180));
        simResultArea.setEditable(false);
        simResultArea.setBorder(new EmptyBorder(6, 6, 6, 6));
        root.add(new JScrollPane(simResultArea) {{
            getViewport().setBackground(new Color(20, 20, 20));
        }}, BorderLayout.CENTER);

        return root;
    }

    private void runSimulation(ScriptConfig config) {
        String  npcName     = (String) simNpcCombo.getSelectedItem();
        int     kills       = (int)    simKillsSpinner.getValue();
        double  dropMulti   = (double) simDropRateSpinner.getValue();
        double  rareMulti   = (double) simRareSpinner.getValue();

        config.setSimNpcName(npcName);
        config.setSimKills(kills);
        config.setSimDropRateMulti(dropMulti);
        config.setSimRareMulti(rareMulti);

        simResultArea.setText("Simulating " + kills + " kills vs " + npcName + "...");

        // Run in background to keep UI responsive
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() {
                return buildSimReport(npcName, kills, dropMulti, rareMulti);
            }
            @Override protected void done() {
                try { simResultArea.setText(get()); simResultArea.setCaretPosition(0); }
                catch (Exception ex) { simResultArea.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }

    private String buildSimReport(String npcName, int kills, double dropMulti, double rareMulti) {
        List<LootEntry> table = NpcLootProfile.getDropTable(npcName);
        if (table.isEmpty()) {
            return "No drop table found for: " + npcName + "\n(The drop table may not be configured yet.)";
        }

        java.util.Map<String, long[]> results = new java.util.LinkedHashMap<>();
        java.util.Random rng = new java.util.Random();

        for (int k = 0; k < kills; k++) {
            for (LootEntry e : table) {
                double chance;
                if (e.getRateDenominator() <= 1) {
                    chance = 1.0;
                } else {
                    // Apply multipliers; clamp to 1.0
                    double base = (double) e.getRateNumerator() / e.getRateDenominator();
                    // Determine if rare: rate ≤ 1/128 treated as rare
                    boolean isRare = e.getRateDenominator() >= 128 && e.getRateNumerator() == 1;
                    double multi = isRare ? (dropMulti * rareMulti) : dropMulti;
                    chance = Math.min(1.0, base * multi);
                }
                if (rng.nextDouble() < chance) {
                    int qty = e.getMinQty() + (e.getMaxQty() > e.getMinQty()
                        ? rng.nextInt(e.getMaxQty() - e.getMinQty() + 1) : 0);
                    long[] data = results.computeIfAbsent(e.getItemName(), x -> new long[2]);
                    data[0]++;      // count of drop events
                    data[1] += qty; // total quantity
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Simulation Results ===\n");
        sb.append(String.format("NPC: %-20s  Kills: %,d\n", npcName, kills));
        sb.append(String.format("Drop Multi: %.2f×   Rare Multi: %.2f×\n\n", dropMulti, rareMulti));
        sb.append(String.format("%-28s %9s %10s %12s\n", "Item", "Drops", "Qty", "Est GP"));
        sb.append("─".repeat(65) + "\n");

        // Sort by estimated GP value descending
        results.entrySet().stream()
            .sorted((a, b) -> {
                long gpA = LootTask.approxPriceStatic.applyAsLong(a.getKey()) * a.getValue()[1];
                long gpB = LootTask.approxPriceStatic.applyAsLong(b.getKey()) * b.getValue()[1];
                return Long.compare(gpB, gpA);
            })
            .forEach(en -> {
                long price  = LootTask.approxPriceStatic.applyAsLong(en.getKey());
                long totalGp = price * en.getValue()[1];
                sb.append(String.format("%-28s %,9d %,10d %12s\n",
                    en.getKey(), en.getValue()[0], en.getValue()[1],
                    price > 0 ? nezz.dreambot.allinone.util.QuantityFormatter.format(totalGp) : "—"));
            });

        if (results.isEmpty()) sb.append("  (nothing dropped — check multipliers)\n");

        return sb.toString();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TAB 4 — SDN SETUP
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel buildSdnTab(ScriptConfig config) {
        JPanel root = darkPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Heading
        JLabel heading = new JLabel("SDN Submission Fields", SwingConstants.LEFT);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(ACCENT);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 3;
        root.add(heading, g);

        JLabel sub = new JLabel("Copy these values when submitting to https://sdn.dreambot.org/scripters/scripts/new");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(new Color(180, 180, 180));
        g.gridy++;
        root.add(sub, g);

        g.gridwidth = 1;

        // Script Repo
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Script Repo:"), g);
        sdnRepoField = styleField(new JTextField("https://github.com/Personfu/CyberBot/"));
        sdnRepoField.setEditable(false);
        g.gridx = 1; g.weightx = 1;
        root.add(sdnRepoField, g);
        g.gridx = 2; g.weightx = 0;
        root.add(copyBtn(sdnRepoField), g);

        // Script Module
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("Script Module:"), g);
        sdnModuleField = styleField(new JTextField("nezz.dreambot.allinone.AllInOne"));
        sdnModuleField.setEditable(false);
        g.gridx = 1; g.weightx = 1;
        root.add(sdnModuleField, g);
        g.gridx = 2; g.weightx = 0;
        root.add(copyBtn(sdnModuleField), g);

        // SDN Parameters
        g.gridx = 0; g.gridy++; g.weightx = 0;
        root.add(label("SDN Parameters:"), g);
        sdnParamsField = styleField(new JTextField(config.getSdnParameters()));
        sdnParamsField.setToolTipText("Optional. Example: npc=Gargoyle&bank=true");
        g.gridx = 1; g.weightx = 1;
        root.add(sdnParamsField, g);
        g.gridx = 2; g.weightx = 0;
        root.add(copyBtn(sdnParamsField), g);

        // Open SDN button
        g.gridx = 0; g.gridy++; g.gridwidth = 3; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.CENTER;
        JButton openSdn = new JButton("🔗  Open SDN Submission Page");
        openSdn.setFont(BOLD);
        openSdn.setBackground(new Color(0, 120, 210));
        openSdn.setForeground(Color.WHITE);
        openSdn.setFocusPainted(false);
        openSdn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI("https://sdn.dreambot.org/scripters/scripts/new"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    "https://sdn.dreambot.org/scripters/scripts/new", "SDN URL", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        root.add(openSdn, g);

        // Notes box
        g.gridy++; g.fill = GridBagConstraints.HORIZONTAL;
        JTextArea notes = new JTextArea(
            "SUBMISSION STEPS:\n" +
            "1. Push your compiled .jar to GitHub at the repo URL above.\n" +
            "2. Open the SDN submission page (button above).\n" +
            "3. Paste Script Repo and Script Module from this tab.\n" +
            "4. SDN Parameters is optional — leave blank or enter defaults.\n" +
            "5. Set Category to 'Combat', fill description, submit.\n\n" +
            "NOTE: Make sure the GitHub repo is PUBLIC and the JAR is at:\n" +
            "  DreambotAllInOne/out/artifacts/DreambotAllInOne.jar");
        notes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        notes.setBackground(new Color(20, 20, 20));
        notes.setForeground(new Color(180, 200, 180));
        notes.setEditable(false);
        notes.setBorder(new EmptyBorder(6, 8, 6, 8));
        root.add(notes, g);

        // Filler
        g.gridy++; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        root.add(darkPanel(null), g);

        return root;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Apply GUI values → ScriptConfig
    // ═════════════════════════════════════════════════════════════════════════

    private void applyToConfig(ScriptConfig config) {
        String npc = (String) npcCombo.getSelectedItem();
        if (npc != null && !npc.isBlank()) config.setTargetNpc(npc.trim());

        try { config.setTargetNpcId(Integer.parseInt(npcIdField.getText().trim())); }
        catch (NumberFormatException ignored) {}

        if (rbMelee.isSelected())       config.setAttackStyle("MELEE");
        else if (rbRanged.isSelected()) config.setAttackStyle("RANGED");
        else                            config.setAttackStyle("MAGIC");

        Object food = foodCombo.getSelectedItem();
        if (food != null) config.setFoodName(food.toString());

        config.setEatAtHpPercent((int)   eatAtHpSpinner.getValue());
        config.setMinFoodCount((int)     minFoodSpinner.getValue());
        config.setBankWhenFull(          bankWhenFullChk.isSelected());
        config.setMaxBankDistance((int)  bankDistSpinner.getValue());
        config.setLootRadiusTiles((int)  lootRadiusSpinner.getValue());

        config.setSdnParameters(sdnParamsField.getText().trim());

        // Loot entries already live-edited in the table model
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Swing style helpers
    // ═════════════════════════════════════════════════════════════════════════

    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = lm != null ? new JPanel(lm) : new JPanel();
        p.setBackground(PANEL_BG);
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT);
        l.setForeground(FG);
        return l;
    }

    private <T extends JTextField> T styleField(T f) {
        f.setBackground(FIELD_BG);
        f.setForeground(FG);
        f.setCaretColor(FG);
        f.setFont(FONT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            new EmptyBorder(3, 5, 3, 5)));
        return f;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(FIELD_BG);
        cb.setForeground(FG);
        cb.setFont(FONT);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(FIELD_BG);
        sp.getEditor().getComponent(0).setBackground(FIELD_BG);
        ((JComponent) sp.getEditor().getComponent(0)).setForeground(FG);
        sp.setFont(FONT);
    }

    private JRadioButton styleRadio(String text, boolean selected) {
        JRadioButton rb = new JRadioButton(text, selected);
        rb.setBackground(PANEL_BG);
        rb.setForeground(FG);
        rb.setFont(FONT);
        return rb;
    }

    private void styleTabs(JTabbedPane t) {
        t.setBackground(BG);
        t.setForeground(FG);
        t.setFont(BOLD);
        UIManager.put("TabbedPane.selected",    PANEL_BG);
        UIManager.put("TabbedPane.background",  BG);
        UIManager.put("TabbedPane.foreground",  FG);
    }

    private JButton accentBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(60, 60, 60));
        b.setForeground(ACCENT);
        b.setFont(FONT);
        b.setFocusPainted(false);
        return b;
    }

    private JButton copyBtn(JTextField source) {
        JButton b = new JButton("Copy");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setBackground(new Color(60, 60, 60));
        b.setForeground(FG);
        b.setFocusPainted(false);
        b.addActionListener(e -> {
            StringSelection sel = new StringSelection(source.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            b.setText("✓");
            Timer t = new Timer(1500, ev -> b.setText("Copy"));
            t.setRepeats(false); t.start();
        });
        return b;
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder b = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)), title);
        b.setTitleFont(BOLD);
        b.setTitleColor(ACCENT);
        return b;
    }

    public void dispose() {
        if (frame != null) { frame.setVisible(false); frame.dispose(); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Loot table model
    // ═════════════════════════════════════════════════════════════════════════

    static final class LootTableModel extends AbstractTableModel {
        private static final String[] COLS = {"✓", "Item Name", "Drop Rate", "Qty Range", "Min Pick Qty", "Min GP Value"};
        private List<LootEntry> entries;

        LootTableModel(List<LootEntry> entries) { this.entries = entries; }

        void setEntries(List<LootEntry> e) { this.entries = e; fireTableDataChanged(); }

        @Override public int getRowCount()    { return entries.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override public Class<?> getColumnClass(int c) {
            if (c == 0) return Boolean.class;
            if (c == 4 || c == 5) return Integer.class;
            return String.class;
        }
        @Override public boolean isCellEditable(int r, int c) { return c == 0 || c == 4 || c == 5; }

        @Override public Object getValueAt(int r, int c) {
            LootEntry e = entries.get(r);
            switch (c) {
                case 0: return e.isEnabled();
                case 1: return e.getItemName();
                case 2: return e.rateString();
                case 3: return e.getMinQty() == e.getMaxQty()
                    ? String.valueOf(e.getMinQty())
                    : e.getMinQty() + "–" + e.getMaxQty();
                case 4: return e.getMinPickupQty();
                case 5: return (int) e.getMinPickupGeValue();
                default: return "";
            }
        }

        @Override public void setValueAt(Object val, int r, int c) {
            LootEntry e = entries.get(r);
            switch (c) {
                case 0: e.setEnabled((Boolean) val); break;
                case 4: try { e.setMinPickupQty(Integer.parseInt(val.toString())); } catch (NumberFormatException ignored) {} break;
                case 5: try { e.setMinPickupGeValue(Long.parseLong(val.toString())); } catch (NumberFormatException ignored) {} break;
            }
            fireTableCellUpdated(r, c);
        }

        void setAllEnabled(boolean v) {
            entries.forEach(e -> e.setEnabled(v));
            fireTableDataChanged();
        }

        /** Enable only entries whose approx GP value is ≥ threshold. */
        void applyValueFilter(long threshold, java.util.function.ToLongFunction<String> priceFunc) {
            for (LootEntry e : entries) {
                long price = priceFunc.applyAsLong(e.getItemName());
                // Always-drop items pass through
                boolean always = e.getRateDenominator() <= 1;
                e.setEnabled(always || price >= threshold);
            }
            fireTableDataChanged();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // NPC → ID quick-lookup
    // ═════════════════════════════════════════════════════════════════════════

    private static final java.util.Map<String, Integer> NpcIdMap = new java.util.HashMap<>();
    static {
        NpcIdMap.put("Chicken",           41);
        NpcIdMap.put("Cow",               81);
        NpcIdMap.put("Goblin",            125);
        NpcIdMap.put("Hill Giant",        2099);
        NpcIdMap.put("Moss Giant",        2090);
        NpcIdMap.put("Lesser Demon",      79);
        NpcIdMap.put("Greater Demon",     2025);
        NpcIdMap.put("Black Demon",       172);
        NpcIdMap.put("Abyssal Demon",     415);
        NpcIdMap.put("Gargoyle",          1543);
        NpcIdMap.put("Dagannoth Rex",     2262);
        NpcIdMap.put("Dagannoth Supreme", 2263);
        NpcIdMap.put("Dagannoth Prime",   2264);
        NpcIdMap.put("General Graardor",  2215);
        NpcIdMap.put("Cerberus",          5862);
        NpcIdMap.put("King Black Dragon", 50);
        NpcIdMap.put("Vorkath",           8059);
        NpcIdMap.put("Zulrah",            2042);
    }
}
