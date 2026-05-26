package nezz.dreambot.master.gui;

import nezz.dreambot.master.profile.BuildPlan;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.quests.QuestRegistry;
import nezz.dreambot.master.skills.SkillRegistry;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

/**
 * Cyberpunk-themed Swing GUI for FLLC Master AIO.
 *
 * <p>Threading contract: caller (DreamBot script thread) uses
 * {@code SwingUtilities.invokeLater} to call {@link #configure} then
 * {@link #setVisible}. The {@link CountDownLatch} is counted down when the
 * user clicks START or closes the window, unblocking the script thread.</p>
 */
@SuppressWarnings("serial")
public final class MasterGui extends JFrame {

    // ── Cyberpunk palette ─────────────────────────────────────────────────────
    private static final Color BG       = new Color(  6,   9,  18);
    private static final Color BG2      = new Color( 10,  14,  26);
    private static final Color BG3      = new Color( 14,  20,  36);
    private static final Color FG       = new Color(185, 225, 245);
    private static final Color CYAN     = new Color(  0, 220, 255);
    private static final Color CYAN_DIM = new Color(  0, 130, 180);
    private static final Color ACCENT   = new Color(  0, 255, 195);
    private static final Color YELLOW   = new Color(255, 215,  50);
    private static final Color GREEN    = new Color(  0, 255, 120);
    private static final Color RED_NEON = new Color(255,  60,  80);
    private static final Font  F_HEAD   = new Font("Consolas", Font.BOLD,  15);
    private static final Font  F_LBL    = new Font("Consolas", Font.BOLD,  12);
    private static final Font  F_FLD    = new Font("Consolas", Font.PLAIN, 12);

    private final CountDownLatch latch;
    private Profile  profile;
    private Runnable onStart;
    private Runnable onStop;

    public MasterGui(CountDownLatch latch) {
        super("CYBER.BOT  //  FLLC Master AIO  v2.0");
        this.latch = latch;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (onStop != null) onStop.run();
                dispose();
                latch.countDown();
            }
        });
        setSize(720, 580);
        setResizable(false);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { }
    }

    /** Bind profile + callbacks and populate content. Must be called before setVisible. */
    public void configure(Profile profile, Runnable onStart, Runnable onStop) {
        this.profile = profile;
        this.onStart = onStart;
        this.onStop  = onStop;
        getContentPane().removeAll();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, CYAN),
            new EmptyBorder(10, 16, 10, 16)));

        JLabel title = new JLabel("\u25c8  CYBER.BOT  //  FLLC MASTER AIO  //  v2.0");
        title.setFont(F_HEAD);
        title.setForeground(CYAN);
        p.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("FROM-ZERO F2P ACCOUNT BUILDER");
        sub.setFont(new Font("Consolas", Font.PLAIN, 10));
        sub.setForeground(CYAN_DIM);
        p.add(sub, BorderLayout.EAST);
        return p;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG2);
        tabs.setForeground(CYAN);
        tabs.setFont(F_LBL);
        tabs.addTab("[ ACCOUNT ]", accountTab());
        tabs.addTab("[ PLAN ]",    planTab());
        tabs.addTab("[ QUESTS ]",  questsTab());
        tabs.addTab("[ ANTIBAN ]", antibanTab());
        tabs.addTab("[ NOTIFY ]",  stopTab());
        return tabs;
    }

    // ── Account tab ───────────────────────────────────────────────────────────
    private JComponent accountTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = gbc();
        section(p, c, "// ACCOUNT CREDENTIALS");
        addRow(p, c, "Email",        field(profile.accountEmail,  v -> profile.accountEmail  = v));
        addRow(p, c, "Password",     passField(profile.accountPass, v -> profile.accountPass = v));
        addRow(p, c, "Display name", field(profile.accountName,   v -> profile.accountName   = v));
        section(p, c, "// MULE CONFIG");
        addRow(p, c, "Mule account", field(profile.muleAccount,   v -> profile.muleAccount   = v));
        addRow(p, c, "Mule world",   field(profile.muleWorld,     v -> profile.muleWorld     = v));
        addRow(p, c, "Quickstart",   checkbox(profile.useQuickstart, v -> profile.useQuickstart = v));
        return p;
    }

    // ── Plan tab ──────────────────────────────────────────────────────────────
    private JComponent planTab() {
        JPanel p = panel(new BorderLayout(6, 6));

        DefaultListModel<String> model = new DefaultListModel<>();
        for (BuildPlan.Phase ph : profile.plan.phases()) model.addElement(fmtPhase(ph));

        JList<String> list = new JList<>(model);
        list.setBackground(BG3);
        list.setForeground(FG);
        list.setFont(F_FLD);
        list.setSelectionBackground(new Color(0, 60, 100));
        list.setSelectionForeground(CYAN);
        list.setCellRenderer(new PhaseCellRenderer());
        JScrollPane sp = styledScroll(list);

        JPanel right = panel(new GridLayout(0, 1, 4, 6));
        right.setPreferredSize(new Dimension(155, 0));
        right.add(cyberBtn("F2P DEFAULT", ACCENT, () -> {
            profile.plan = BuildPlan.defaultF2P();
            model.clear();
            for (BuildPlan.Phase ph : profile.plan.phases()) model.addElement(fmtPhase(ph));
        }));
        right.add(cyberBtn("ADD QUEST", CYAN, () -> {
            String q = (String) JOptionPane.showInputDialog(this, "Quest:", "Add Quest",
                JOptionPane.PLAIN_MESSAGE, null,
                QuestRegistry.all().values().stream().map(qst -> qst.name()).toArray(), null);
            if (q != null) {
                profile.plan.add(new BuildPlan.Phase(BuildPlan.PhaseType.QUEST, q, 0));
                model.addElement(fmtPhase(profile.plan.phases().get(profile.plan.phases().size()-1)));
            }
        }));
        right.add(cyberBtn("ADD SKILL", CYAN, () -> {
            String s = (String) JOptionPane.showInputDialog(this, "Skill:", "Add Skill",
                JOptionPane.PLAIN_MESSAGE, null,
                SkillRegistry.all().values().stream().map(m -> m.name()).toArray(), null);
            if (s == null) return;
            String lvlStr = JOptionPane.showInputDialog(this, "Target level:", "60");
            int lvl;
            try { lvl = Integer.parseInt(lvlStr.trim()); } catch (Throwable e) { return; }
            profile.plan.add(new BuildPlan.Phase(BuildPlan.PhaseType.SKILL_LEVEL, s, lvl));
            model.addElement(fmtPhase(profile.plan.phases().get(profile.plan.phases().size()-1)));
        }));
        right.add(cyberBtn("REMOVE", RED_NEON, () -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0 && idx < profile.plan.phases().size()) {
                profile.plan.phases().remove(idx);
                model.remove(idx);
            }
        }));

        p.add(sp, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Quests tab ────────────────────────────────────────────────────────────
    private JComponent questsTab() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 4, 3));
        grid.setBackground(BG2);
        QuestRegistry.all().values().forEach(q -> {
            JLabel l = new JLabel("\u25b6  " + q.name());
            l.setForeground(ACCENT);
            l.setFont(F_FLD);
            grid.add(l);
        });
        JScrollPane sp = styledScroll(grid);
        JPanel p = panel(new BorderLayout());
        JLabel hdr = new JLabel("  // IMPLEMENTED QUESTS (" + QuestRegistry.all().size() + ")", SwingConstants.LEFT);
        hdr.setFont(F_LBL);
        hdr.setForeground(CYAN_DIM);
        hdr.setBorder(new EmptyBorder(4, 6, 4, 0));
        p.add(hdr, BorderLayout.NORTH);
        p.add(sp,  BorderLayout.CENTER);
        return p;
    }

    // ── Antiban tab ───────────────────────────────────────────────────────────
    private JComponent antibanTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = gbc();
        section(p, c, "// HUMANISATION");
        addRow(p, c, "Human mouse",       checkbox(profile.humanMouse,   v -> profile.humanMouse   = v));
        addRow(p, c, "Camera jitter",     checkbox(profile.cameraJitter, v -> profile.cameraJitter = v));
        addRow(p, c, "Random tab opens",  checkbox(profile.randomTabs,   v -> profile.randomTabs   = v));
        addRow(p, c, "AFK drift",         checkbox(profile.afkDrift,     v -> profile.afkDrift     = v));
        section(p, c, "// BREAK SCHEDULE");
        addRow(p, c, "Break every – min (m)", intField(profile.breakEveryMinMin,  v -> profile.breakEveryMinMin  = v));
        addRow(p, c, "Break every – max (m)", intField(profile.breakEveryMaxMin,  v -> profile.breakEveryMaxMin  = v));
        addRow(p, c, "Break for – min (m)",   intField(profile.breakDurationMinM, v -> profile.breakDurationMinM = v));
        addRow(p, c, "Break for – max (m)",   intField(profile.breakDurationMaxM, v -> profile.breakDurationMaxM = v));
        addRow(p, c, "Active hrs / 24h",      intField(profile.fatigueWindowH,    v -> profile.fatigueWindowH    = v));
        return p;
    }

    // ── Stop & Notify tab ─────────────────────────────────────────────────────
    private JComponent stopTab() {
        JPanel p = panel(new GridBagLayout());
        GridBagConstraints c = gbc();
        section(p, c, "// DISCORD NOTIFICATIONS");
        addRow(p, c, "Webhook URL",     field(profile.discordWebhook, v -> profile.discordWebhook = v));
        addRow(p, c, "Notify on ban",   checkbox(profile.notifyOnBan,   v -> profile.notifyOnBan   = v));
        addRow(p, c, "Notify on level", checkbox(profile.notifyOnLevel, v -> profile.notifyOnLevel = v));
        addRow(p, c, "Notify on quest", checkbox(profile.notifyOnQuest, v -> profile.notifyOnQuest = v));
        section(p, c, "// STOP CONDITIONS");
        addRow(p, c, "Stop after (hrs)",  intField(profile.stopAfterHours, v -> profile.stopAfterHours = v));
        addRow(p, c, "Stop on trade req", checkbox(profile.stopOnTradeReq, v -> profile.stopOnTradeReq = v));
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JComponent buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        f.setBackground(BG);
        f.setBorder(new MatteBorder(2, 0, 0, 0, CYAN));

        f.add(cyberBtn("LOAD", CYAN_DIM, () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try { profile = Profile.load(fc.getSelectedFile().toPath()); configure(profile, onStart, onStop); repaint(); }
                catch (Exception e) { JOptionPane.showMessageDialog(this, "Load failed: " + e); }
            }
        }));
        f.add(cyberBtn("SAVE", CYAN_DIM, () -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try { profile.save(fc.getSelectedFile().toPath()); }
                catch (Exception e) { JOptionPane.showMessageDialog(this, "Save failed: " + e); }
            }
        }));
        f.add(cyberBtn("STOP", RED_NEON, () -> {
            if (onStop != null) onStop.run();
            dispose();
            latch.countDown();
        }));

        JButton start = cyberBtn("\u25b6  START", GREEN, () -> {
            if (onStart != null) onStart.run();
            dispose();
            latch.countDown();
        });
        start.setFont(new Font("Consolas", Font.BOLD, 13));
        start.setPreferredSize(new Dimension(118, 32));
        f.add(start);
        return f;
    }

    // ── Component helpers ─────────────────────────────────────────────────────
    private JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG2);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 4, 3, 4);
        c.gridx = 0; c.gridy = 0; c.weightx = 1.0;
        return c;
    }

    private void section(JPanel p, GridBagConstraints c, String text) {
        c.gridx = 0; c.gridwidth = 2;
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Consolas", Font.BOLD, 11));
        l.setForeground(CYAN_DIM);
        l.setBorder(new MatteBorder(0, 0, 1, 0, new Color(0, 80, 120)));
        p.add(l, c);
        c.gridy++; c.gridwidth = 1;
    }

    private void addRow(JPanel p, GridBagConstraints c, String label, JComponent comp) {
        c.gridx = 0; c.weightx = 0.35;
        JLabel l = new JLabel(label);
        l.setFont(F_LBL); l.setForeground(FG);
        p.add(l, c);
        c.gridx = 1; c.weightx = 0.65;
        p.add(comp, c);
        c.gridy++;
    }

    private JTextField field(String initial, java.util.function.Consumer<String> setter) {
        JTextField tf = new JTextField(initial == null ? "" : initial, 24);
        tf.setBackground(BG3); tf.setForeground(CYAN); tf.setCaretColor(ACCENT); tf.setFont(F_FLD);
        tf.setBorder(new CompoundBorder(new LineBorder(CYAN_DIM, 1), new EmptyBorder(2, 4, 2, 4)));
        tf.getDocument().addDocumentListener(new SimpleDocListener(() -> setter.accept(tf.getText())));
        return tf;
    }

    private JTextField intField(int initial, java.util.function.IntConsumer setter) {
        return field(String.valueOf(initial), v -> {
            try { setter.accept(Integer.parseInt(v.trim())); } catch (NumberFormatException ignored) { }
        });
    }

    private JTextField passField(String initial, java.util.function.Consumer<String> setter) {
        JPasswordField pf = new JPasswordField(initial == null ? "" : initial, 24);
        pf.setBackground(BG3); pf.setForeground(CYAN); pf.setCaretColor(ACCENT); pf.setFont(F_FLD);
        pf.setBorder(new CompoundBorder(new LineBorder(CYAN_DIM, 1), new EmptyBorder(2, 4, 2, 4)));
        pf.getDocument().addDocumentListener(new SimpleDocListener(() -> setter.accept(new String(pf.getPassword()))));
        return pf;
    }

    private JCheckBox checkbox(boolean initial, java.util.function.Consumer<Boolean> setter) {
        JCheckBox cb = new JCheckBox("", initial);
        cb.setBackground(BG2); cb.setForeground(CYAN);
        cb.addActionListener(e -> setter.accept(cb.isSelected()));
        return cb;
    }

    private JButton cyberBtn(String text, Color color, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFont(F_LBL);
        b.setBackground(BG);
        b.setForeground(color);
        b.setBorder(new CompoundBorder(new LineBorder(color, 1), new EmptyBorder(4, 10, 4, 10)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(
                    Math.min(color.getRed()   / 5, 50),
                    Math.min(color.getGreen() / 5, 50),
                    Math.min(color.getBlue()  / 5, 50)));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setBackground(BG); }
        });
        b.addActionListener(e -> onClick.run());
        return b;
    }

    private JScrollPane styledScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(CYAN_DIM, 1));
        sp.getViewport().setBackground(BG3);
        sp.getVerticalScrollBar().setBackground(BG2);
        sp.getHorizontalScrollBar().setBackground(BG2);
        return sp;
    }

    private static String fmtPhase(BuildPlan.Phase ph) {
        String val = ph.value > 0 ? " \u2192 " + ph.value : "";
        return "[" + ph.type.name() + "]  " + ph.target + val;
    }

    /** Colour-codes plan rows by phase type. */
    private static final class PhaseCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean hasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            l.setBackground(isSelected ? new Color(0, 50, 90) : new Color(10, 14, 26));
            l.setFont(new Font("Consolas", Font.PLAIN, 12));
            String s = value == null ? "" : value.toString();
            if      (s.contains("QUEST"))   l.setForeground(new Color(  0, 200, 255));
            else if (s.contains("SKILL"))   l.setForeground(new Color(  0, 255, 140));
            else if (s.contains("TUTORIAL"))l.setForeground(new Color(255, 215,  50));
            else if (s.contains("MONEY"))   l.setForeground(new Color(255, 165,   0));
            else                            l.setForeground(new Color(185, 225, 245));
            return l;
        }
    }

    private static final class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        public void insertUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e)  { r.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
