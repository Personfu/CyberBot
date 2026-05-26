package nezz.dreambot.master.antiban;

import org.dreambot.api.methods.Calculations;

/**
 * Models human-like session fatigue. As hours accumulate:
 * <ul>
 *   <li>Reaction times increase (click latency multiplier goes up).</li>
 *   <li>Random idle pauses become longer.</li>
 *   <li>Camera-move frequency drops.</li>
 * </ul>
 *
 * <p>Resets when a break is taken via {@link #onBreakTaken()}.</p>
 */
public final class FatigueModel {

    /** Session play-time tracked here (ms). Resets on break. */
    private long sessionMs = 0;
    private long lastTick  = System.currentTimeMillis();

    /** Fatigue in [0.0, 1.0]. 0 = fresh, 1 = exhausted. */
    public float fatigue() {
        // Reaches 0.8 after ~4 h, maxes out at ~6 h.
        float h = sessionMs / 3_600_000f;
        return Math.min(1f, h / 6f);
    }

    /**
     * Returns a randomised sleep duration adjusted for fatigue.
     * At low fatigue returns {@code base}; at high fatigue up to 2.5×.
     */
    public int fatigueDelay(int baseMs) {
        float mult = 1f + fatigue() * 1.5f;
        int noiseMs = Calculations.random(0, (int)(baseMs * 0.4f));
        return (int)(baseMs * mult) + noiseMs;
    }

    /** Call once per bot loop to accumulate session time. */
    public void tick() {
        long now = System.currentTimeMillis();
        sessionMs += now - lastTick;
        lastTick = now;
    }

    /** Call when the BreakManager takes a real break. */
    public void onBreakTaken() {
        sessionMs = Math.max(0, sessionMs - 1_800_000L); // recoup 30 min per break
        lastTick  = System.currentTimeMillis();
    }

    /** 0-100 int for HUD display. */
    public int fatiguePercent() { return (int)(fatigue() * 100); }
}
