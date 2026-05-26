package nezz.dreambot.master.antiban;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.tasks.Task;
import nezz.dreambot.master.tasks.TaskScheduler;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Human day/night break scheduler for 24/7 bot operation.
 *
 * <h3>Model</h3>
 * <pre>
 *   Day cycle (UTC-adjusted):
 *     • Play sessions: 2-4 h each, separated by 20-60 min micro-breaks.
 *     • Fatigue-driven long break after 8-10 h cumulative play.
 *     • Night window (00:00-07:00 local): 5-8 h offline "sleep" — bot
 *       logs out and stays idle, resuming in the morning.
 *
 *   Short break  : 3-25 min  (tab-out, food, phone)
 *   Medium break : 25-75 min (away from desk)
 *   Long break   : 60-180 min (session end)
 *   Night sleep  : 5-9 h    (logged out completely)
 * </pre>
 *
 * <p>All durations are randomised with ±20 % jitter to avoid
 * bot-detectable periodicity.</p>
 */
public final class BreakManager {

    private final Profile      profile;
    private final Logger       log;
    private final FatigueModel fatigue;

    // 24-h play-time accounting
    private long sessionPlayMs  = 0;
    private long windowStart    = System.currentTimeMillis();

    // Scheduling
    private long nextBreakAt    = 0;
    private long breakUntil     = 0;

    // Night-sleep tracking
    private long nightSleepUntil = 0;

    public BreakManager(Profile profile, Logger log, FatigueModel fatigue) {
        this.profile = profile;
        this.log     = log;
        this.fatigue = fatigue;
        scheduleNext();
    }

    public void register(TaskScheduler s) { s.add(new BreakTask()); }

    /** Human-readable status line for the HUD paint. */
    public String status() {
        long now = System.currentTimeMillis();
        if (now < nightSleepUntil) {
            long h = (nightSleepUntil - now) / 3_600_000;
            long m = ((nightSleepUntil - now) % 3_600_000) / 60_000;
            return "NIGHT " + h + "h" + m + "m left";
        }
        if (now < breakUntil) {
            long left = (breakUntil - now) / 1_000;
            return "BREAK " + (left / 60) + "m" + (left % 60) + "s left";
        }
        long until = (nextBreakAt - now) / 1_000;
        if (until < 0) return "break due";
        return "next " + (until / 60) + "m" + (until % 60) + "s";
    }

    // ── scheduling helpers ────────────────────────────────────────────────────

    private void scheduleNext() {
        // Next break is proportional to how tired we are — fresh player plays
        // longer stretches, tired player takes breaks more often.
        float fatigueRatio = fatigue.fatigue();                        // 0..1
        int minMins = Math.max(15, profile.breakEveryMinMin);
        int maxMins = Math.max(minMins + 5, profile.breakEveryMaxMin);
        // Fatigued: shorten play window toward the low end
        int rangeMins = maxMins - minMins;
        int playMins  = minMins + (int)((1 - fatigueRatio) * rangeMins);
        playMins += Calculations.random(-5, 5);                        // jitter
        playMins  = Math.max(5, playMins);
        nextBreakAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(playMins);
    }

    private long pickBreakDuration() {
        float f = fatigue.fatigue();
        int roll = Calculations.random(0, 99);
        long ms;
        if (f > 0.8f || roll < 10) {
            // Long / session-ending break
            ms = TimeUnit.MINUTES.toMillis(Calculations.random(60, 180));
        } else if (f > 0.5f || roll < 40) {
            // Medium break
            ms = TimeUnit.MINUTES.toMillis(Calculations.random(25, 75));
        } else {
            // Short break
            ms = TimeUnit.MINUTES.toMillis(Calculations.random(
                    Math.max(1, profile.breakDurationMinM),
                    Math.max(profile.breakDurationMinM + 1, profile.breakDurationMaxM)));
        }
        // ±10 % jitter
        ms += (long)(ms * (Math.random() * 0.2 - 0.1));
        return ms;
    }

    /** True if local time is in the night window and we should simulate sleep. */
    private boolean isNightWindow() {
        if (!profile.enableNightSleep) return false;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        // Configurable night start/end; defaults 00:00-07:00
        int nightStart = profile.nightSleepStartHour;
        int nightEnd   = profile.nightSleepEndHour;
        if (nightStart <= nightEnd) {
            return hour >= nightStart && hour < nightEnd;
        } else {
            return hour >= nightStart || hour < nightEnd;
        }
    }

    private long nightSleepDuration() {
        int base = Calculations.random(5, 9); // hours
        return TimeUnit.HOURS.toMillis(base) + TimeUnit.MINUTES.toMillis(Calculations.random(0, 45));
    }

    // ── break task ────────────────────────────────────────────────────────────

    private final class BreakTask extends Task {
        private long lastTick = System.currentTimeMillis();

        @Override public int priority() { return 5; }
        @Override public BotState state() { return BotState.BREAKING; }

        @Override public boolean isReady() {
            long now = System.currentTimeMillis();
            sessionPlayMs += now - lastTick;
            lastTick = now;
            fatigue.tick();
            if (now < breakUntil || now < nightSleepUntil) return true;
            if (isNightWindow() && nightSleepUntil == 0) return true;
            return now >= nextBreakAt;
        }

        @Override public int execute() {
            long now = System.currentTimeMillis();

            // Night-sleep: log out for several hours
            if (isNightWindow() && nightSleepUntil <= now) {
                nightSleepUntil = now + nightSleepDuration();
                log.info("Night sleep until " + new java.util.Date(nightSleepUntil));
                logout();
                return 30_000;
            }
            if (now < nightSleepUntil) {
                // Still in night window — stay logged out
                return 60_000;
            }

            // Normal break
            if (breakUntil == 0 || breakUntil <= now) {
                breakUntil = now + pickBreakDuration();
                log.info("Break until " + new java.util.Date(breakUntil));
                fatigue.onBreakTaken();
                logout();
            }
            if (now < breakUntil) return 5_000;

            // Break over
            log.info("Break finished — resuming.");
            breakUntil = 0;
            scheduleNext();
            return 600;
        }

        @Override public String label() { return "break[" + BreakManager.this.status() + "]"; }

        private void logout() {
            try { Tabs.open(Tab.LOGOUT); } catch (Throwable ignored) { }
        }
    }
}
