package nezz.dreambot.master.core;

import nezz.dreambot.master.antiban.Antiban;
import nezz.dreambot.master.antiban.BreakManager;
import nezz.dreambot.master.antiban.HumanMouse;
import nezz.dreambot.master.gui.MasterGui;
import nezz.dreambot.master.profile.BuildPlan;
import nezz.dreambot.master.profile.Profile;
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

import java.awt.*;
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
public class MasterAIO extends AbstractScript {

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
        profile   = new Profile();
        scheduler = new TaskScheduler();
        antiban   = new Antiban(profile, log);
        breaks    = new BreakManager(profile, log, antiban.fatigue());

        gui = new MasterGui();
        gui.show(profile, () -> {
            running.set(true);
            startTime = System.currentTimeMillis();
            HumanMouse.install(profile);
            SkillTracker.start();
            scheduler.clear();
            // High-level driver task that consumes BuildPlan phases.
            scheduler.add(new BuildPlanTask(profile, scheduler, log));
            // Cross-cutting tasks (antiban events, breaks) registered last so
            // their lower priority numbers run first.
            antiban.register(scheduler);
            breaks.register(scheduler);
            log.info("MasterAIO started — plan: " + profile.plan.phases().size() + " phases");
        }, () -> {
            running.set(false);
            scheduler.clear();
            log.info("MasterAIO paused.");
        });
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

    // ────────────────────────────────────────────────────────────────────────
    // Paint
    // ────────────────────────────────────────────────────────────────────────

    private static final Color BG  = new Color(8, 10, 14, 200);
    private static final Color FG  = new Color(230, 230, 230);
    private static final Color VAL = new Color(255, 210, 50);
    private static final Color ACC = new Color(0, 220, 130);
    private static final Font  FNT = new Font("Consolas", Font.BOLD, 12);

    @Override
    public void onPaint(Graphics g) {
        if (profile == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(FNT);

        int x = 6, y = 36, rowH = 14, padX = 8, padY = 6;
        int w = 260, h = 9 * rowH + padY * 2 + 4;

        g2.setColor(BG);
        g2.fillRoundRect(x - padX, y - rowH - padY, w, h, 10, 10);
        g2.setColor(ACC);
        g2.drawString("FLLC Master AIO  v2.0", x, y); y += rowH;

        BuildPlan.Phase phase = profile.plan.current();
        kv(g2, "Phase",   phase == null ? "complete" : phase.toString(), x, y); y += rowH;
        kv(g2, "Task",    scheduler.active() == null ? "idle" : scheduler.active().label(), x, y); y += rowH;
        kv(g2, "State",   state.name(), x, y); y += rowH;
        kv(g2, "Runtime", fmtRt(System.currentTimeMillis() - startTime), x, y); y += rowH;

        long totalXp = 0;
        try {
            for (Skill s : Skill.values()) totalXp += Skills.getExperience(s);
        } catch (Throwable ignored) { }
        kv(g2, "Total XP", QuantityFormatter.format(totalXp), x, y); y += rowH;
        kv(g2, "Antiban",  profile.humanMouse ? "ON" : "off", x, y); y += rowH;
        kv(g2, "Breaks",   breaks == null ? "-" : breaks.status(), x, y); y += rowH;
    }

    private void kv(Graphics2D g2, String k, String v, int x, int y) {
        g2.setColor(FG);
        g2.drawString(k + ":", x, y);
        g2.setColor(VAL);
        g2.drawString(v, x + 70, y);
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
