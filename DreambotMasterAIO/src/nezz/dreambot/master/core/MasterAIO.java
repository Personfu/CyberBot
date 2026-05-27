package nezz.dreambot.master.core;

import nezz.dreambot.master.antiban.Antiban;
import nezz.dreambot.master.antiban.BreakManager;
import nezz.dreambot.master.antiban.HumanMouse;
import nezz.dreambot.master.ge.GESellTask;
import nezz.dreambot.master.gui.MasterGui;
import nezz.dreambot.master.tasks.ActivityRunner;
import nezz.dreambot.master.profile.BuildPlan;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.money.MoneyRouteTask;
import nezz.dreambot.master.tasks.BuildPlanTask;
import nezz.dreambot.master.tasks.Task;
import nezz.dreambot.master.tasks.TaskScheduler;
import nezz.dreambot.master.util.QuantityFormatter;
import org.dreambot.api.Client;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import nezz.dreambot.master.util.WebhookManager;
import org.dreambot.api.script.listener.LoginListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Entry point for the FLLC Master AIO — a from-zero account builder modelled
 * after SlugBuilder / HowP2PAIO / SubAccountBuilder.
 *
 * <h3>Architecture</h3>
 * <ul>
 *   <li>{@link Profile} — JSON-free flat-properties config (accounts, plan, antiban).</li>
 *   <li>{@link BuildPlan} — ordered phases: tutorial → quests → skills → diaries.</li>
 *   <li>{@link TaskScheduler} — priority-ordered Task list, run one ready Task per tick.</li>
 *   <li>{@link Antiban} / {@link BreakManager} / {@link HumanMouse} — humanization.</li>
 *   <li>{@link MasterGui} — Swing GUI for plan/antiban/account config.</li>
 * </ul>
 *
 * @author Personfu
 */
@ScriptManifest(
    name        = "FLLC Master AIO",
    author      = "Personfu",
    version     = 2.0,
    description = "From-zero account builder — Tutorial Island + 10 F2P quests + 9 priority skills. " +
                  "Modelled after SlugBuilder / HowP2PAIO. Drop-in profile loader, antiban, mule support.",
    category    = Category.MISC
)
public class MasterAIO extends AbstractScript implements LoginListener {

    private Profile          profile;
    private TaskScheduler    scheduler;
    private Antiban          antiban;
    private BreakManager     breaks;
    private MasterGui        gui;
    private final Logger     log = new Logger();
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile BotState state = BotState.IDLE;
    private long startTime = System.currentTimeMillis();

    // ────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void onStart() {
        profile = new Profile();
        CountDownLatch latch = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            gui = new MasterGui(latch);
            gui.configure(profile,
                () -> running.set(true),
                () -> running.set(false)
            );
            gui.setVisible(true);
            gui.toFront();
            gui.requestFocus();
        });

        // Block the script thread until the user clicks Start or closes the GUI.
        try { latch.await(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        if (!running.get()) {
            stop();
            return;
        }

        scheduler = new TaskScheduler();
        antiban   = new Antiban(profile, log);
        breaks    = new BreakManager(profile, log, antiban.fatigue());
        startTime = System.currentTimeMillis();
        HumanMouse.install(profile);
        SkillTracker.start();

        if (profile.activityMode && !profile.activityId.isEmpty()) {
            // ── Activity / quick-start mode ──────────────────────────────────
            ActivityRunner runner = ActivityRunner.byId(profile.activityId, profile.activityMethod, log);
            if (runner != null) {
                scheduler.add(runner);
                log.info("MasterAIO [ACTIVITY] → " + runner.label());
            } else {
                log.warn("Unknown activity '" + profile.activityId + "' — falling back to Build Plan.");
                scheduler.add(new BuildPlanTask(profile, scheduler, log));
            }
        } else {
            // ── Full build-plan mode ─────────────────────────────────────────
            scheduler.add(new BuildPlanTask(profile, scheduler, log));
            log.info("MasterAIO [PLAN] started — " + profile.plan.phases().size() + " phases");
        }

        scheduler.add(new GESellTask(log));   // permanent sell-queue processor
        antiban.register(scheduler);
        breaks.register(scheduler);
    }

    @Override
    public int onLoop() {
        if (!running.get()) return 600;
        if (!Client.isLoggedIn()) {
            state = BotState.LOGGING_IN;
            return 1500;
        }
        int sleep = scheduler.tick();
        state = scheduler.lastState();
        return sleep;
    }

    @Override
    public void onExit() {
        running.set(false);
        if (gui != null) gui.dispose();
    }

    /**
     * DreamBot LoginListener callback. Response code 4 = account banned.
     * Stops the script immediately and logs the event so Discord webhook can fire.
     */
    @Override
    public void onLoginResponse(int response) {
        if (response == 4) {
            String player = "unknown";
            try {
                if (org.dreambot.api.methods.interactive.Players.getLocal() != null) {
                    player = org.dreambot.api.methods.interactive.Players.getLocal().getName();
                }
            } catch (Throwable ignored) {}
            log.info("[!!!] Login response 4 \u2014 account may be BANNED. Stopping script.");
            WebhookManager.sendBanNotification(player);
            running.set(false);
            stop();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Paint — cyberpunk overlay
    // ────────────────────────────────────────────────────────────────────────

    // Neon cyan/electric blue palette
    private static final Color C_BG       = new Color(3,   7,  18, 220);
    private static final Color C_BORDER   = new Color(0,  200, 255, 200);
    private static final Color C_ACCENT   = new Color(0,  255, 200, 230);
    private static final Color C_HEADER   = new Color(0,  230, 255);
    private static final Color C_KEY      = new Color(0,  160, 210);
    private static final Color C_VAL      = new Color(195, 235, 255);
    private static final Color C_YELLOW   = new Color(255, 215, 50);
    private static final Color C_SCAN     = new Color(0,   0,   0,  22);
    private static final Color C_SEP      = new Color(0,  150, 200, 80);
    private static final Font  F_HEAD     = new Font("Consolas", Font.BOLD,  12);
    private static final Font  F_KEY      = new Font("Consolas", Font.BOLD,  11);
    private static final Font  F_VAL      = new Font("Consolas", Font.PLAIN, 11);

    @Override
    public void onPaint(Graphics g) {
        if (profile == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int PX = 7, PY = 32, PW = 286, PH = 162, CUT = 11;

        // ── Clipped panel background ──────────────────────────────────────
        int[] xs = {PX+CUT, PX+PW, PX+PW, PX,    PX    };
        int[] ys = {PY,     PY,    PY+PH, PY+PH, PY+CUT};
        Polygon panel = new Polygon(xs, ys, 5);
        g2.setColor(C_BG);
        g2.fillPolygon(panel);

        // Scanlines
        g2.setColor(C_SCAN);
        for (int ly = PY; ly < PY + PH; ly += 2) g2.drawLine(PX, ly, PX+PW, ly);

        // Thin inner glow fill
        g2.setColor(new Color(0, 120, 200, 12));
        g2.fillRect(PX+1, PY+1, PW-2, 16);

        // ── Border ────────────────────────────────────────────────────────
        g2.setColor(C_BORDER);
        g2.setStroke(new BasicStroke(1.1f));
        g2.drawPolygon(panel);

        // Corner tick marks (circuit-board style)
        g2.setColor(C_ACCENT);
        g2.setStroke(new BasicStroke(1.8f));
        // top-right
        g2.drawLine(PX+PW-14, PY,    PX+PW,    PY   );
        g2.drawLine(PX+PW,    PY,    PX+PW,    PY+14);
        // bottom-left
        g2.drawLine(PX,       PY+PH, PX+14,    PY+PH);
        g2.drawLine(PX,       PY+PH, PX,       PY+PH-14);
        // top-left cut corner dot
        g2.setColor(new Color(0, 255, 200, 160));
        g2.fillRect(PX+CUT-2, PY, 3, 3);

        // ── Header row ────────────────────────────────────────────────────
        int tx = PX + 10;
        int ty = PY + 13;

        // Blinking active indicator
        boolean blink = (System.currentTimeMillis() / 550) % 2 == 0;
        g2.setColor(running.get() && blink ? new Color(0, 255, 128) : new Color(0, 80, 50));
        g2.fillOval(tx, ty - 8, 7, 7);
        g2.setColor(C_BORDER);
        g2.setStroke(new BasicStroke(0.6f));
        g2.drawOval(tx, ty - 8, 7, 7);

        g2.setFont(F_HEAD);
        g2.setColor(C_HEADER);
        g2.drawString("CYBER.BOT  v2.0", tx + 11, ty);

        // Runtime — right-aligned
        String rt = fmtRt(System.currentTimeMillis() - startTime);
        g2.setColor(C_YELLOW);
        g2.drawString(rt, PX + PW - 58, ty);

        // Header separator
        g2.setColor(C_SEP);
        g2.setStroke(new BasicStroke(0.7f));
        g2.drawLine(PX + 2, ty + 4, PX + PW - 2, ty + 4);
        ty += 16;

        // ── Phase / task info ─────────────────────────────────────────────
        String taskLbl = (scheduler != null && scheduler.active() != null)
                ? scheduler.active().label() : "idle";
        String tradeStatus = nezz.dreambot.master.ge.GrandExchangeUtil.isTradeUnrestricted()
                ? "UNRESTRICTED" : "RESTRICTED";

        if (profile.activityMode) {
            row(g2, tx, ty, PW - 16, "MODE",   "ACTIVITY: " + profile.activityId); ty += 13;
        } else {
            BuildPlan.Phase phase = profile.plan.current();
            int cursor     = profile.plan.cursor() + 1;
            int total      = profile.plan.phases().size();
            String phType  = phase == null ? "DONE" : phase.type.name();
            String phTgt   = phase == null ? "--"   : phase.target + (phase.value > 0 ? " → lvl " + phase.value : "");
            row(g2, tx, ty, PW - 16, "PHASE",  "[" + phType + "] " + phTgt); ty += 13;
            row(g2, tx, ty, PW - 16, "PROG",   cursor + " / " + total);      ty += 13;
        }
        row(g2, tx, ty, PW - 16, "TASK",   taskLbl);                         ty += 13;

        // State + trade status with color
        g2.setFont(F_KEY);
        g2.setColor(C_KEY);
        g2.drawString("STATUS", tx, ty);
        g2.setFont(F_VAL);
        g2.setColor(stateColor(state));
        g2.drawString(":: " + state.name(), tx + 56, ty);
        ty += 13;

        // Trade restriction line
        boolean tradable = nezz.dreambot.master.ge.GrandExchangeUtil.isTradeUnrestricted();
        g2.setFont(F_KEY); g2.setColor(C_KEY);
        g2.drawString("TRADE", tx, ty);
        g2.setFont(F_VAL);
        g2.setColor(tradable ? new Color(0, 255, 128) : new Color(255, 90, 60));
        g2.drawString(":: " + (tradable ? "UNRESTRICTED" : "RESTRICTED (<100ttl/10qp/20hr)"), tx + 56, ty);
        ty += 13;

        // Separator
        g2.setColor(C_SEP);
        g2.setStroke(new BasicStroke(0.7f));
        g2.drawLine(PX + 2, ty + 2, PX + PW - 2, ty + 2);
        ty += 10;

        // ── Stats ─────────────────────────────────────────────────────────
        long totalXp = 0;
        try { for (Skill s : Skill.values()) totalXp += Skills.getExperience(s); }
        catch (Throwable ignored) { }

        row(g2, tx, ty, PW - 16, "TOTAL XP", QuantityFormatter.format(totalXp)); ty += 13;

        // XP gain this session, OR GP progress when money-making
        Task activeTask = scheduler != null ? scheduler.active() : null;
        if (activeTask instanceof MoneyRouteTask) {
            MoneyRouteTask mrt = (MoneyRouteTask) activeTask;
            long tgt = mrt.gpTarget();
            String gpLine = tgt > 0
                    ? QuantityFormatter.format(mrt.gpEstimated()) + " / " + QuantityFormatter.format(tgt) + " gp"
                    : QuantityFormatter.format(mrt.gpEstimated()) + " gp est.";
            row(g2, tx, ty, PW - 16, "GP EARNED", gpLine);
        } else {
            long gainedXp = 0;
            try { for (Skill s : Skill.values()) gainedXp += SkillTracker.getGainedExperience(s); }
            catch (Throwable ignored) { }
            row(g2, tx, ty, PW - 16, "XP GAINED", QuantityFormatter.format(gainedXp));
        }
        ty += 13;

        row(g2, tx, ty, PW - 16, "ANTIBAN",  profile.humanMouse ? "ACTIVE" : "OFF"); ty += 13;
        row(g2, tx, ty, PW - 16, "BREAKS",   breaks == null ? "--" : breaks.status());

        // ── Bottom label ──────────────────────────────────────────────────
        g2.setFont(new Font("Consolas", Font.PLAIN, 9));
        g2.setColor(new Color(0, 100, 140, 160));
        g2.drawString("FLLC.SYSTEMS // PERSONFU", PX + 6, PY + PH - 3);
    }

    private void row(Graphics2D g2, int x, int y, int maxW, String key, String val) {
        g2.setFont(F_KEY);
        g2.setColor(C_KEY);
        g2.drawString(key, x, y);
        g2.setFont(F_VAL);
        g2.setColor(C_VAL);
        FontMetrics fm = g2.getFontMetrics();
        String disp = val == null ? "--" : val;
        while (disp.length() > 3 && 56 + fm.stringWidth(":: " + disp) > maxW) {
            disp = disp.substring(0, disp.length() - 1);
        }
        g2.drawString(":: " + disp, x + 56, y);
    }

    private static Color stateColor(BotState s) {
        if (s == null) return new Color(100, 100, 100);
        switch (s) {
            case SKILLING:     return new Color(0,  255, 128);
            case QUESTING:     return new Color(0,  200, 255);
            case TUTORIAL:     return new Color(255, 215, 50);
            case MONEY_MAKING: return new Color(255, 160, 0);
            case COMBAT:       return new Color(255, 80,  80);
            case BANKING:      return new Color(180, 100, 255);
            case BREAKING:     return new Color(255, 100, 160);
            case LOGGING_IN:   return new Color(160, 160, 255);
            default:           return new Color(140, 140, 140);
        }
    }

    private static String fmtRt(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        return String.format("%02d:%02d:%02d", h, m % 60, s % 60);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Accessors (for tasks to log into the shared logger)
    // ────────────────────────────────────────────────────────────────────────

    public Logger logger() { return log; }
    public Profile profile() { return profile; }
}
