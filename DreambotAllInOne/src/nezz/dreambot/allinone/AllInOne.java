package nezz.dreambot.allinone;

import nezz.dreambot.allinone.config.ScriptConfig;
import nezz.dreambot.allinone.gui.AllInOneGui;
import nezz.dreambot.allinone.tasks.BankTask;
import nezz.dreambot.allinone.tasks.CombatTask;
import nezz.dreambot.allinone.tasks.LootTask;
import nezz.dreambot.allinone.util.QuantityFormatter;
import org.dreambot.api.Client;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FLLC All-In-One — Combat, Looting, Banking with per-NPC drop-table config.
 *
 * SDN Submission details (fill in at https://sdn.dreambot.org/scripters/scripts/new):
 *   Script Repo   : https://github.com/Personfu/DreamBot
 *   Script Module : nezz.dreambot.allinone.AllInOne
 *   SDN Params    : (optional) npc=Gargoyle&bank=true
 */
@ScriptManifest(
    name        = "FLLC All-In-One",
    author      = "Personfu",
    version     = 1.1,
    description = "All-in-one combat, looting, and banking script with a full GUI, " +
                  "per-enemy loot config, drop rate simulator, and SDN setup fields.",
    category    = Category.COMBAT
)
public class AllInOne extends AbstractScript {

    // ── State machine ─────────────────────────────────────────────────────────
    private enum State { WAITING, COMBAT, LOOT, BANK }
    private volatile State state = State.WAITING;

    // ── Core components ───────────────────────────────────────────────────────
    private ScriptConfig config;
    private CombatTask   combatTask;
    private LootTask     lootTask;
    private BankTask     bankTask;
    private AllInOneGui  gui;

    private final AtomicBoolean running  = new AtomicBoolean(false);
    private long startTime = System.currentTimeMillis();

    // ── Paint ─────────────────────────────────────────────────────────────────
    private static final Color PAINT_BG  = new Color(10, 10, 10, 170);
    private static final Color PAINT_FG  = new Color(230, 230, 230);
    private static final Color PAINT_VAL = new Color(255, 210, 50);
    private static final Font  PAINT_FNT = new Font("Arial", Font.BOLD, 12);

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onStart() {
        config = new ScriptConfig();
        combatTask = new CombatTask(this, config);
        lootTask   = new LootTask(this, config);
        bankTask   = new BankTask(config);

        gui = new AllInOneGui();
        gui.show(
            config,
            () -> {    // onStart callback
                running.set(true);
                startTime = System.currentTimeMillis();  // reset timer when user clicks START
                SkillTracker.start(Skill.HITPOINTS);
                state = State.COMBAT;
                log("FLLC All-In-One started — targeting: " + config.getTargetNpc());
            },
            () -> {    // onStop callback
                running.set(false);
                state = State.WAITING;
                log("FLLC All-In-One paused.");
            }
        );
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) return 1500;
        if (!running.get())      return 600;

        switch (nextState()) {
            case BANK:
                state = State.BANK;
                return bankTask.execute();

            case LOOT:
                state = State.LOOT;
                return lootTask.execute();

            case COMBAT:
                state = State.COMBAT;
                return combatTask.execute();

            default:
                return 600;
        }
    }

    private State nextState() {
        if (bankTask.shouldExecute())   return State.BANK;
        if (lootTask.shouldExecute())   return State.LOOT;
        if (combatTask.shouldExecute()) return State.COMBAT;
        return State.WAITING;
    }

    @Override
    public void onExit() {
        running.set(false);
        if (gui != null) gui.dispose();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Paint overlay
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 6, y = 40, rowH = 16, padX = 8, padY = 6;
        int rows = 7;
        int w = 210, h = rows * rowH + padY * 2 + 2;

        // Background
        g2.setColor(PAINT_BG);
        g2.fillRoundRect(x - padX, y - rowH - padY, w, h, 8, 8);

        g2.setFont(PAINT_FNT);

        // Header
        g2.setColor(PAINT_VAL);
        g2.drawString("FLLC All-In-One  v1.1", x, y);
        y += rowH;

        // Runtime
        g2.setColor(PAINT_FG);
        g2.drawString("Runtime: ", x, y);
        g2.setColor(PAINT_VAL);
        g2.drawString(formatRuntime(System.currentTimeMillis() - startTime), x + 60, y);
        y += rowH;

        // State
        g2.setColor(PAINT_FG);
        g2.drawString("State:   ", x, y);
        g2.setColor(PAINT_VAL);
        g2.drawString(state.name(), x + 60, y);
        y += rowH;

        // Target NPC
        g2.setColor(PAINT_FG);
        g2.drawString("Target:  ", x, y);
        g2.setColor(PAINT_VAL);
        g2.drawString(config.getTargetNpc(), x + 60, y);
        y += rowH;

        // Kills
        g2.setColor(PAINT_FG);
        g2.drawString("Kills:   ", x, y);
        g2.setColor(PAINT_VAL);
        g2.drawString(String.valueOf(combatTask.getKillCount()), x + 60, y);
        y += rowH;

        // GP looted
        g2.setColor(PAINT_FG);
        g2.drawString("GP Loot: ", x, y);
        g2.setColor(PAINT_VAL);
        g2.drawString(QuantityFormatter.format(lootTask.getTotalGp()), x + 60, y);
        y += rowH;

        // GP/hr
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > 10_000L) {
            long gpHr = (long) (lootTask.getTotalGp() / (elapsed / 3_600_000.0));
            g2.setColor(PAINT_FG);
            g2.drawString("GP/hr:   ", x, y);
            g2.setColor(PAINT_VAL);
            g2.drawString(QuantityFormatter.format(gpHr), x + 60, y);
        }
    }

    private static String formatRuntime(long ms) {
        long s  = ms / 1000;
        long m  = s / 60;
        long hr = m / 60;
        return String.format("%02d:%02d:%02d", hr, m % 60, s % 60);
    }
}
