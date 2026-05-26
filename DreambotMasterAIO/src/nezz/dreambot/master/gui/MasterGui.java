package nezz.dreambot.master.gui;

import nezz.dreambot.master.profile.BuildPlan;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.quests.QuestRegistry;
import nezz.dreambot.master.skills.SkillRegistry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Swing GUI for the MasterAIO. Five tabs:
 * <ol>
 *   <li><b>Account</b> — username, password, age, mule details.</li>
 *   <li><b>Plan</b> — ordered list of phases (drag-and-drop intended).</li>
 *   <li><b>Quests</b> — checklist of implemented quests.</li>
 *   <li><b>Antiban</b> — mouse, breaks, fatigue, AFK drift.</li>
 *   <li><b>Stop / Notify</b> — Discord webhook, stop conditions.</li>
 * </ol>
 *
 * <p>Visual style matches the existing FLLC All-In-One GUI: dark background,
 * yellow header, simple sectioning. No external L&F dependency.</p>
 */
public final class MasterGui extends JFrame {

    private static final Color BG  = new Color(15, 18, 22);
    private static final Color BG2 = new Color(22, 26, 32);
    private static final Color FG  = new Color(220, 220, 220);
    private static final Color ACC = new Color(255, 210, 50);
    private static final Color GRN = new Color(0, 220, 130);

    private Profile profile;
    private Runnable onStart;
    private Runnable onStop;

    public MasterGui() {
        super("FLLC Master AIO — Config");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(700, 560);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
    }

    public void show(Profile profile, Runnable onStart, Runnable onStop) {
        this.profile = profile;
        this.onStart = onStart;
        this.onStop  = onStop;

        getContentPane().removeAll();
        add(header(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG2);
        tabs.setForeground(FG);
        tabs.addTab("Account",  accountTab());
        tabs.addTab("Plan",     planTab());
        tabs.addTab("Quests",   questsTab());
        tabs.addTab("Antiban",  antibanTab());
        tabs.addTab("Stop & Notify", stopTab());
        add(tabs, BorderLayout.CENTER);

        add(footer(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JComponent header() {
        JLabel h = new JLabel("FLLC Master AIO — From-Zero Account Builder", SwingConstants.CENTER);
        h.setFont(new Font("Consolas", Font.BOLD, 18));
        h.setForeground(ACC);
        h.setOpaque(true);
        h.setBackground(BG);
        h.setBorder(new EmptyBorder(10, 12, 10, 12));
        return h;
    }

    // ── Account ──────────────────────────────────────────────────────────────
    private JComponent accountTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = grid();
        addRow(p, c, "Email",    field(profile.accountEmail,  v -> profile.accountEmail = v));
        addRow(p, c, "Password", passField(profile.accountPass, v -> profile.accountPass = v));
        addRow(p, c, "Name (display)", field(profile.accountName, v -> profile.accountName = v));
        addRow(p, c, "Age",      field(profile.accountAge, v -> profile.accountAge = v));
        addRow(p, c, "Mule account",   field(profile.muleAccount, v -> profile.muleAccount = v));
        addRow(p, c, "Mule world",     field(profile.muleWorld,   v -> profile.muleWorld   = v));
        addRow(p, c, "Use quickstart", checkbox(profile.useQuickstart, v -> profile.useQuickstart = v));
        return p;
    }

    // ── Plan ─────────────────────────────────────────────────────────────────
    private JComponent planTab() {
        JPanel p = panel(new BorderLayout(6, 6));
        DefaultListModel<String> model = new DefaultListModel<>();
        for (BuildPlan.Phase ph : profile.plan.phases()) model.addElement(ph.toString());
        JList<String> list = new JList<>(model);
        list.setBackground(BG2);
        list.setForeground(FG);
        list.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(list);
        sp.setBackground(BG2);

        JPanel right = panel(new GridLayout(0, 1, 4, 4));
        right.add(button("Use F2P default", () -> {
            profile.plan = BuildPlan.defaultF2P();
            model.clear();
            for (BuildPlan.Phase ph : profile.plan.phases()) model.addElement(ph.toString());
        }));
        right.add(button("Add Quest…", () -> {
            String q = (String) JOptionPane.showInputDialog(this, "Quest:", "Add Quest",
                    JOptionPane.PLAIN_MESSAGE, null,
                    QuestRegistry.all().values().stream().map(quest -> quest.name()).toArray(),
                    null);
            if (q != null) {
                profile.plan.add(new BuildPlan.Phase(BuildPlan.PhaseType.QUEST, q, 0));
                model.addElement("QUEST " + q);
            }
        }));
        right.add(button("Add Skill…", () -> {
            String s = (String) JOptionPane.showInputDialog(this, "Skill:", "Add Skill",
                    JOptionPane.PLAIN_MESSAGE, null,
                    SkillRegistry.all().values().stream().map(m -> m.name()).toArray(),
                    null);
            if (s == null) return;
            String lvlStr = JOptionPane.showInputDialog(this, "Target level:", "20");
            int lvl;
            try { lvl = Integer.parseInt(lvlStr.trim()); }
            catch (Throwable e) { return; }
            profile.plan.add(new BuildPlan.Phase(BuildPlan.PhaseType.SKILL_LEVEL, s, lvl));
            model.addElement("SKILL_LEVEL " + s + " (" + lvl + ")");
        }));
        right.add(button("Remove selected", () -> {
            int idx = list.getSelectedIndex();
            if (idx < 0 || idx >= profile.plan.phases().size()) return;
            profile.plan.phases().remove(idx);
            model.remove(idx);
        }));

        p.add(sp, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Quests ───────────────────────────────────────────────────────────────
    private JComponent questsTab() {
        JPanel p = panel(new GridLayout(0, 2, 4, 4));
        QuestRegistry.all().values().forEach(q -> {
            JLabel l = new JLabel(" • " + q.name());
            l.setForeground(GRN);
            p.add(l);
        });
        return p;
    }

    // ── Antiban ──────────────────────────────────────────────────────────────
    private JComponent antibanTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = grid();
        addRow(p, c, "Human mouse",     checkbox(profile.humanMouse,    v -> profile.humanMouse    = v));
        addRow(p, c, "Camera jitter",   checkbox(profile.cameraJitter,  v -> profile.cameraJitter  = v));
        addRow(p, c, "Random tab opens",checkbox(profile.randomTabs,    v -> profile.randomTabs    = v));
        addRow(p, c, "AFK drift",       checkbox(profile.afkDrift,      v -> profile.afkDrift      = v));
        addRow(p, c, "Break every (min)", intField(profile.breakEveryMinMin, v -> profile.breakEveryMinMin = v));
        addRow(p, c, "Break every (max)", intField(profile.breakEveryMaxMin, v -> profile.breakEveryMaxMin = v));
        addRow(p, c, "Break duration (min)", intField(profile.breakDurationMinM, v -> profile.breakDurationMinM = v));
        addRow(p, c, "Break duration (max)", intField(profile.breakDurationMaxM, v -> profile.breakDurationMaxM = v));
        addRow(p, c, "Fatigue window (h/24h)", intField(profile.fatigueWindowH, v -> profile.fatigueWindowH = v));
        return p;
    }

    // ── Stop & Notify ────────────────────────────────────────────────────────
    private JComponent stopTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = grid();
        addRow(p, c, "Discord webhook", field(profile.discordWebhook, v -> profile.discordWebhook = v));
        addRow(p, c, "Notify on ban",   checkbox(profile.notifyOnBan,   v -> profile.notifyOnBan   = v));
        addRow(p, c, "Notify on level", checkbox(profile.notifyOnLevel, v -> profile.notifyOnLevel = v));
        addRow(p, c, "Notify on quest",  checkbox(profile.notifyOnQuest, v -> profile.notifyOnQuest = v));
        addRow(p, c, "Stop after (hrs)", intField(profile.stopAfterHours, v -> profile.stopAfterHours = v));
        addRow(p, c, "Stop on trade req", checkbox(profile.stopOnTradeReq, v -> profile.stopOnTradeReq = v));
        return p;
    }

    // ── footer ───────────────────────────────────────────────────────────────
    private JComponent footer() {
        JPanel f = panel(new FlowLayout(FlowLayout.RIGHT));
        f.add(button("Load…", () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try { profile = Profile.load(fc.getSelectedFile().toPath()); rebuild(); }
                catch (Exception e) { JOptionPane.showMessageDialog(this, "Load failed: " + e); }
            }
        }));
        f.add(button("Save…", () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try { profile.save(fc.getSelectedFile().toPath()); }
                catch (Exception e) { JOptionPane.showMessageDialog(this, "Save failed: " + e); }
            }
        }));
        JButton start = button("Start", () -> { if (onStart != null) onStart.run(); setVisible(false); });
        start.setBackground(GRN);
        f.add(start);
        JButton stop = button("Stop", () -> { if (onStop != null) onStop.run(); });
        f.add(stop);
        return f;
    }

    private void rebuild() {
        setVisible(false);
        show(profile, onStart, onStop);
    }

    // ── component helpers ────────────────────────────────────────────────────

    private JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG2);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private GridBagConstraints grid() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 3, 3, 3);
        c.gridx = 0; c.gridy = 0;
        c.weightx = 1.0;
        return c;
    }

    private void addRow(JPanel p, GridBagConstraints c, String label, JComponent comp) {
        JLabel l = new JLabel(label);
        l.setForeground(FG);
        c.gridx = 0; p.add(l, c);
        c.gridx = 1; p.add(comp, c);
        c.gridy++;
    }

    private JTextField field(String initial, java.util.function.Consumer<String> setter) {
        JTextField tf = new JTextField(initial == null ? "" : initial, 22);
        tf.setBackground(BG);
        tf.setForeground(FG);
        tf.setCaretColor(FG);
        tf.getDocument().addDocumentListener(new SimpleDocListener(() -> setter.accept(tf.getText())));
        return tf;
    }

    private JTextField intField(int initial, java.util.function.IntConsumer setter) {
        JTextField tf = field(String.valueOf(initial), v -> {
            try { setter.accept(Integer.parseInt(v.trim())); }
            catch (NumberFormatException ignored) { }
        });
        return tf;
    }

    private JTextField passField(String initial, java.util.function.Consumer<String> setter) {
        JPasswordField pf = new JPasswordField(initial == null ? "" : initial, 22);
        pf.setBackground(BG);
        pf.setForeground(FG);
        pf.setCaretColor(FG);
        pf.getDocument().addDocumentListener(new SimpleDocListener(() -> setter.accept(new String(pf.getPassword()))));
        return pf;
    }

    private JCheckBox checkbox(boolean initial, java.util.function.Consumer<Boolean> setter) {
        JCheckBox cb = new JCheckBox("", initial);
        cb.setBackground(BG2);
        cb.setForeground(FG);
        cb.addActionListener(e -> setter.accept(cb.isSelected()));
        return cb;
    }

    private JButton button(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setBackground(BG);
        b.setForeground(FG);
        b.setFocusPainted(false);
        b.addActionListener(e -> onClick.run());
        return b;
    }

    private static final class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
