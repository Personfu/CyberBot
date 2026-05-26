package nezz.dreambot.master.money;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.tasks.Task;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BuildPlan-driven task that runs a {@link MoneyRoute} until a GP target
 * or time limit is reached, then signals completion.
 *
 * <h3>Route selection</h3>
 * If the profile specifies a route ID (via the Phase option key {@code "route"}),
 * that route is used.  Otherwise the registry picks the highest estimated GP/hr
 * route the player currently meets requirements for.
 */
public final class MoneyRouteTask extends Task {

    private final String    targetRouteId;  // null = auto-select
    private final long      gpTarget;       // 0 = unlimited (run until phase ends)
    private final Logger    log;

    private MoneyRoute active;
    private long       startTime = 0;       // ms when route was first selected
    private boolean    done      = false;

    public MoneyRouteTask(String routeId, long gpTarget, Logger log) {
        this.targetRouteId = routeId;
        this.gpTarget      = gpTarget;
        this.log           = log;
    }

    @Override public int  priority()    { return 80; }
    @Override public BotState state()   { return BotState.MONEY_MAKING; }
    @Override public boolean isReady()  { return !done; }
    @Override public boolean isComplete() { return done; }

    /** Estimated GP earned so far based on route's GP/hr and elapsed time. */
    public long gpEstimated() {
        if (active == null || startTime == 0) return 0L;
        long elapsed = System.currentTimeMillis() - startTime;
        return (long)(active.estimatedGpHr() * (elapsed / 3_600_000.0));
    }

    /** GP target for this phase (0 = unlimited). */
    public long gpTarget() { return gpTarget; }

    @Override public int execute() {
        if (active == null) {
            active = selectRoute();
            if (active == null) {
                log.warn("MoneyRouteTask: no suitable route found — skipping.");
                done = true;
                return 200;
            }
            startTime = System.currentTimeMillis();
            log.info("MoneyRoute selected: " + active.id() + " (~" + active.estimatedGpHr() + " gp/hr)");
        }

        if (gpTarget > 0 && gpEstimated() >= gpTarget) {
            log.info("GP target " + gpTarget + " reached (estimated " + gpEstimated() + " earned).");
            done = true;
            return 200;
        }

        int sleep = active.tick();
        return sleep > 0 ? sleep : Calculations.random(450, 750);
    }

    @Override public String label() {
        String gp = gpTarget > 0 ? (gpEstimated() / 1000) + "k/" + (gpTarget / 1000) + "k gp" : "";
        return "money[" + (active == null ? "selecting" : active.id())
                + (gp.isEmpty() ? "" : " " + gp) + "]";
    }

    // ── route selection ──────────────────────────────────────────────────────

    private MoneyRoute selectRoute() {
        if (targetRouteId != null) {
            return MoneyRouteRegistry.byId(targetRouteId);
        }
        // Auto: pick highest GP/hr we qualify for
        List<MoneyRoute> all = new ArrayList<>(MoneyRouteRegistry.all());
        all.sort(Comparator.comparingInt(MoneyRoute::estimatedGpHr).reversed());
        for (MoneyRoute r : all) {
            if (meetsRequirements(r)) return r;
        }
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    private static boolean meetsRequirements(MoneyRoute r) {
        for (Map.Entry<Skill, Integer> e : r.requirements().entrySet()) {
            if (Skills.getRealLevel(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }
}
