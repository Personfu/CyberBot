package nezz.dreambot.master.antiban;

import nezz.dreambot.master.profile.Profile;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.mouse.algorithm.MouseAlgorithm;
import org.dreambot.api.input.mouse.destination.AbstractMouseDestination;
import org.dreambot.api.methods.Calculations;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Human-like mouse algorithm using a cubic Bezier path with overshoot,
 * tremor and per-segment speed variance. Modelled after WindMouse / a few
 * DreamBot community algorithms (Pandemic's Mouse, Slug's "FluidMouse").
 *
 * <p>Install with {@link #install(Profile)}; this swaps DreamBot's default
 * algorithm for ours via {@code Mouse.setMouseAlgorithm(...)}.</p>
 *
 * <p>Tuning knobs are conservative defaults; you can adjust:
 * <ul>
 *   <li>{@link #overshootChance} — fraction of moves that overshoot the target.</li>
 *   <li>{@link #tremor} — gaussian jitter per segment in pixels.</li>
 *   <li>{@link #baseSpeed} / {@link #speedVar} — segment dwell µs ranges.</li>
 * </ul></p>
 */
public final class HumanMouse implements MouseAlgorithm {

    public double overshootChance = 0.18;
    public double tremor          = 1.4;
    public int    baseSpeed       = 6;     // ms per segment
    public int    speedVar        = 5;

    public static void install(Profile profile) {
        if (!profile.humanMouse) return;
        try {
            Mouse.setMouseAlgorithm(new HumanMouse());
        } catch (Throwable ignored) {
            // DreamBot versions before mouse-algorithm API: silently skip.
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean handleMovement(AbstractMouseDestination destination) {
        Point start = Mouse.getPosition();
        Point end   = destination.getSuitablePoint();
        if (end == null) return false;
        if (start.distance(end) < 1) return Mouse.move(end);

        // Build a cubic Bezier with two control points pulled off the
        // direct line by a curved offset proportional to distance.
        double d = start.distance(end);
        double curve = Math.min(120, d * (0.18 + Math.random() * 0.22));
        // Perpendicular vector to (start->end).
        double dx = end.x - start.x, dy = end.y - start.y;
        double len = Math.max(1, Math.sqrt(dx * dx + dy * dy));
        double nx = -dy / len, ny = dx / len;
        double sign = Math.random() < 0.5 ? 1 : -1;

        Point c1 = new Point(
                (int) (start.x + dx * 0.30 + nx * curve * sign),
                (int) (start.y + dy * 0.30 + ny * curve * sign));
        Point c2 = new Point(
                (int) (start.x + dx * 0.70 + nx * curve * sign * 0.6),
                (int) (start.y + dy * 0.70 + ny * curve * sign * 0.6));

        // Sample count proportional to distance (longer paths get more steps).
        int steps = (int) Math.max(15, Math.min(80, d / 7));
        List<Point> path = sampleBezier(start, c1, c2, end, steps);

        // Maybe overshoot.
        if (Math.random() < overshootChance) {
            int ox = (int) (dx / len * Calculations.random(6, 18));
            int oy = (int) (dy / len * Calculations.random(6, 18));
            path.add(new Point(end.x + ox, end.y + oy));
            path.add(end);
        }

        // Drive the mouse along the path.
        for (Point p : path) {
            int jx = (int) Math.round(p.x + gauss() * tremor);
            int jy = (int) Math.round(p.y + gauss() * tremor);
            Mouse.hop(jx, jy);
            sleepNoExc(baseSpeed + (int) (Math.random() * speedVar));
        }
        // Final settle at exact target.
        Mouse.hop(end);
        return true;
    }

    @Override
    public boolean handleClick(org.dreambot.api.input.event.impl.mouse.MouseButton button) {
        // Defer to DreamBot's default click handling.
        return false;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static List<Point> sampleBezier(Point p0, Point p1, Point p2, Point p3, int n) {
        List<Point> out = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            double t  = i / (double) n;
            double u  = 1 - t;
            double b0 = u * u * u;
            double b1 = 3 * u * u * t;
            double b2 = 3 * u * t * t;
            double b3 = t * t * t;
            int x = (int) Math.round(b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x);
            int y = (int) Math.round(b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y);
            out.add(new Point(x, y));
        }
        return out;
    }

    private static double gauss() {
        // Box-Muller without storing the spare — cheap enough for per-step.
        double u1 = 1 - Math.random();
        double u2 = 1 - Math.random();
        return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }

    private static void sleepNoExc(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}


