package nezz.dreambot.master.antiban;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.tasks.Task;
import nezz.dreambot.master.tasks.TaskScheduler;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.awt.*;
import java.util.Random;

/**
 * Humanisation layer — fires realistic idle events between bot actions.
 *
 * <h3>Event pool (weighted):</h3>
 * <ol>
 *   <li><b>Camera</b> — smooth yaw + pitch adjustments (not instant snaps).</li>
 *   <li><b>Tab browse</b> — open Skills/Quest/Equipment/Prayer/Magic tabs,
 *       hover XP tooltips, re-open Inventory.</li>
 *   <li><b>Mouse idle drift</b> — hover minimap, examine nearby objects,
 *       right-click → Examine menu → press Esc (cancel).</li>
 *   <li><b>Short AFK</b> — sleep 1.5-8 s to simulate looking away from screen.</li>
 *   <li><b>Long AFK</b> — 12-40 s idle to simulate phone/drink break.</li>
 *   <li><b>Hover skill tooltip</b> — open stats tab, hover a skill, then close.</li>
 *   <li><b>Random nearby walk</b> — take 1-3 steps and return.</li>
 *   <li><b>Examine NPC/object</b> — right-click → Examine on nearby entity.</li>
 * </ol>
 *
 * <p>Event frequency is modulated by {@link FatigueModel} — later in the
 * session events fire more often and idle pauses grow longer, just like a
 * tired human.</p>
 *
 * <p>Based on community patterns from Pandemic's Script, Dreamy AIO, Slug AIO.</p>
 */
public final class Antiban {

    private final Profile      profile;
    private final Logger       log;
    private final FatigueModel fatigue;
    /**
     * Per-account seeded RNG — all behavioral variation for this account
     * (reaction speed, AFK tendency, mouse bias) is derived from this one
     * seed so the same username always produces the same bot personality.
     */
    private final Random rng;
    /** Mouse movement speed multiplier for this account (0.7–1.4). */
    private final double mouseSpeedBias;
    /** Reaction time multiplier — scales all event interval delays (0.75–1.35). */
    private final double reactionBias;
    /** AFK duration multiplier — how idle-prone this account is (0.6–1.6). */
    private final double afkTendency;

    private long lastEvent = System.currentTimeMillis();

    // Separate timing for each category so they don't block each other
    private long nextCameraEvent = 0;
    private long nextTabEvent    = 0;
    private long nextHoverEvent  = 0;
    private long nextAfkEvent    = 0;

    public Antiban(Profile profile, Logger log) {
        this.profile = profile;
        this.log = log;
        this.fatigue = new FatigueModel();
        // Seed per-account RNG from username hash — same account = same personality
        this.rng = new Random(profile.accountSeed());
        // Derive stable behavioral biases in [min,max] using the seeded RNG
        this.mouseSpeedBias = 0.70 + rng.nextDouble() * 0.70; // 0.70 – 1.40
        this.reactionBias   = 0.75 + rng.nextDouble() * 0.60; // 0.75 – 1.35
        this.afkTendency    = 0.60 + rng.nextDouble() * 1.00; // 0.60 – 1.60
        log.info(String.format("Antiban seeds: mouse=%.2f reaction=%.2f afk=%.2f",
                mouseSpeedBias, reactionBias, afkTendency));
        scheduleAll();
    }

    public FatigueModel fatigue() { return fatigue; }

    public void register(TaskScheduler s) {
        s.add(new EventTask());
    }

    // ── event scheduler ──────────────────────────────────────────────────────

    private void scheduleAll() {
        long now = System.currentTimeMillis();
        // Apply reactionBias so naturally fast/slow accounts have different cadence
        nextCameraEvent = now + (long)(Calculations.random(12_000, 35_000) * reactionBias);
        nextTabEvent    = now + (long)(Calculations.random(25_000, 75_000) * reactionBias);
        nextHoverEvent  = now + (long)(Calculations.random(18_000, 55_000) * reactionBias);
        nextAfkEvent    = now + (long)(Calculations.random(30_000, 90_000) * afkTendency);
    }

    // ── inner event task ─────────────────────────────────────────────────────

    private final class EventTask extends Task {
        @Override public int priority() { return 20; }
        @Override public BotState state() { return BotState.SKILLING; }

        @Override public boolean isReady() {
            if (!profile.cameraJitter && !profile.randomTabs && !profile.afkDrift) return false;
            long now = System.currentTimeMillis();
            return now >= nextCameraEvent || now >= nextTabEvent
                    || now >= nextHoverEvent || now >= nextAfkEvent;
        }

        @Override public int execute() {
            fatigue.tick();
            long now = System.currentTimeMillis();
            try {
                if (profile.cameraJitter && now >= nextCameraEvent) {
                    doCameraEvent();
                    nextCameraEvent = now + (long)(fatigue.fatigueDelay(Calculations.random(14_000, 40_000)) * reactionBias);
                }
                if (profile.randomTabs && now >= nextTabEvent) {
                    doTabEvent();
                    nextTabEvent = now + (long)(fatigue.fatigueDelay(Calculations.random(30_000, 90_000)) * reactionBias);
                }
                if (now >= nextHoverEvent) {
                    doHoverEvent();
                    nextHoverEvent = now + (long)(fatigue.fatigueDelay(Calculations.random(20_000, 60_000)) * reactionBias);
                }
                if (profile.afkDrift && now >= nextAfkEvent) {
                    doAfkEvent();
                    nextAfkEvent = now + (long)(fatigue.fatigueDelay(Calculations.random(35_000, 120_000)) * afkTendency);
                }
            } catch (Throwable t) {
                log.warn("antiban event errored: " + t);
            }
            return 200;
        }

        @Override public String label() { return "antiban[fatigue=" + fatigue.fatiguePercent() + "%]"; }
    }

    // ── individual event implementations ─────────────────────────────────────

    /** Smooth camera rotation + occasional pitch adjustment. */
    private void doCameraEvent() {
        int roll = Calculations.random(0, 99);
        if (roll < 60) {
            // Smooth yaw: rotate in steps, not instant snap
            int targetYaw = Calculations.random(0, 359);
            smoothRotateYaw(targetYaw);
        } else if (roll < 85) {
            // Pitch: look up or down slightly
            int pitch = Calculations.random(200, 383);   // 128=straight, 383=straight up
            try { Camera.rotateToPitch(pitch); } catch (Throwable ignored) { }
        } else {
            // Zoom in/out (scroll wheel)
            int scrolls = Calculations.random(2, 5);
            boolean in = Math.random() < 0.5;
            for (int i = 0; i < scrolls; i++) {
                Mouse.scroll(in, 1, () -> true);
                sleepNoExc(Calculations.random(60, 120));
            }
        }
        log.trace("antiban: camera event");
    }

    private void smoothRotateYaw(int targetYaw) {
        try {
            int current = Camera.getYaw();
            int diff = targetYaw - current;
            // Wrap diff to [-180, 180]
            while (diff > 180)  diff -= 360;
            while (diff < -180) diff += 360;
            int steps = Calculations.random(3, 8);
            for (int i = 0; i < steps; i++) {
                int intermediate = current + (diff * (i + 1)) / steps;
                Camera.rotateToYaw(((intermediate % 360) + 360) % 360);
                sleepNoExc(Calculations.random(40, 90));
            }
        } catch (Throwable ignored) {
            try { Camera.rotateToYaw(targetYaw); } catch (Throwable ignored2) { }
        }
    }

    /** Open a random tab, possibly hover a skill for XP tooltip, then restore inventory. */
    private void doTabEvent() {
        Tab[] tabs = {
            Tab.SKILLS, Tab.QUEST, Tab.EQUIPMENT,
            Tab.PRAYER, Tab.MAGIC, Tab.INVENTORY
        };
        Tab pick = tabs[Calculations.random(0, tabs.length - 1)];
        try { Tabs.open(pick); } catch (Throwable ignored) { }
        log.trace("antiban: tab=" + pick);

        if (pick == Tab.SKILLS) {
            // Hover a skill for its XP tooltip
            sleepNoExc(Calculations.random(400, 900));
            Skill[] skills = Skill.values();
            Skill s = skills[Calculations.random(0, skills.length - 1)];
            try {
                // Attempt to hover the skill widget (just move mouse near it)
                // Skills tab layout: 6 columns × 4 rows starting at ~(558, 271)
                int idx = s.ordinal();
                int col = idx % 3, row = idx / 3;
                int wx = 558 + col * 54;
                int wy = 271 + row * 38;
                Mouse.move(new Point(wx + Calculations.random(-8, 8),
                                     wy + Calculations.random(-8, 8)));
                sleepNoExc(Calculations.random(600, 1600));
            } catch (Throwable ignored) { }
        }

        // Always restore inventory tab after a delay
        sleepNoExc(Calculations.random(700, 2200));
        try { Tabs.open(Tab.INVENTORY); } catch (Throwable ignored) { }
    }

    /** Move mouse to examine an object, NPC or minimap area. */
    private void doHoverEvent() {
        int roll = Calculations.random(0, 99);
        if (roll < 40) {
            hoverNearbyEntity();
        } else if (roll < 70) {
            hoverMinimap();
        } else {
            hoverChatbox();
        }
    }

    private void hoverNearbyEntity() {
        try {
            NPC npc = NPCs.closest(n -> n != null && n.isOnScreen()
                    && Players.getLocal() != null
                    && n.distance(Players.getLocal()) < 8);
            if (npc != null) {
                Mouse.move(npc.getClickablePoint());
                log.trace("antiban: hover npc=" + npc.getName());
                sleepNoExc(Calculations.random(300, 900));
                // Occasionally right-click → examine
                if (rng.nextDouble() < 0.25) {
                    Mouse.click(false); // right-click
                    sleepNoExc(Calculations.random(400, 700));
                    // Press Esc to dismiss without selecting
                    try {
                        java.awt.Robot robot = new java.awt.Robot();
                        robot.keyPress(java.awt.event.KeyEvent.VK_ESCAPE);
                        robot.keyRelease(java.awt.event.KeyEvent.VK_ESCAPE);
                    } catch (Throwable ignored) { }
                }
                return;
            }
            // Fall back to nearest game object
            GameObject obj = GameObjects.closest(g -> g != null && g.isOnScreen()
                    && Players.getLocal() != null
                    && g.distance(Players.getLocal()) < 6);
            if (obj != null) {
                Mouse.move(obj.getClickablePoint());
                sleepNoExc(Calculations.random(200, 700));
            }
        } catch (Throwable ignored) { }
    }

    private void hoverMinimap() {
        // Minimap is roughly centered at (629, 86) in fixed mode
        int mx = 629 + Calculations.random(-30, 30);
        int my = 86  + Calculations.random(-25, 25);
        try {
            Mouse.move(new Point(mx, my));
            sleepNoExc(Calculations.random(400, 1200));
        } catch (Throwable ignored) { }
        log.trace("antiban: hover minimap");
    }

    private void hoverChatbox() {
        // Chatbox region: roughly x=7..506, y=439..503
        int cx = 7   + Calculations.random(0, 499);
        int cy = 439 + Calculations.random(0, 64);
        try {
            Mouse.move(new Point(cx, cy));
            sleepNoExc(Calculations.random(300, 1000));
        } catch (Throwable ignored) { }
    }

    /** AFK pause — short (attention lapse) or long (tabbed out / drink break). */
    private void doAfkEvent() {
        int roll = Calculations.random(0, 99);
        int ms;
        if (roll < 60) {
            ms = fatigue.fatigueDelay(Calculations.random(1_500, 6_000));
        } else if (roll < 85) {
            ms = fatigue.fatigueDelay(Calculations.random(6_000, 18_000));
        } else {
            ms = fatigue.fatigueDelay(Calculations.random(18_000, 45_000));
        }
        // Scale AFK duration by this account's idleness tendency
        ms = (int)(ms * afkTendency);
        // Scale AFK duration by this account's idleness tendency
        ms = (int)(ms * afkTendency);
        log.trace("antiban: afk " + (ms / 1000) + "s");
        sleepNoExc(ms);
    }

    // ── utility ──────────────────────────────────────────────────────────────

    private static void sleepNoExc(int ms) {
        try { Thread.sleep(Math.max(0, ms)); }
        catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
