package nezz.dreambot.master.tasks;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.money.MoneyRoute;
import nezz.dreambot.master.money.MoneyRouteRegistry;
import nezz.dreambot.master.skills.SkillModule;
import nezz.dreambot.master.skills.SkillRegistry;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skills;

/**
 * Runs a single activity (SkillModule or MoneyRoute) indefinitely until the
 * user manually stops the script. Used by the GUI [ ACTIVITY ] tab.
 *
 * <p>Unlike {@link SkillTask} or {@link MoneyRouteTask}, this task never
 * signals completion — it loops forever so the user can choose when to stop.</p>
 */
public final class ActivityRunner extends Task {

    private final SkillModule module;
    private final MoneyRoute  route;
    private final Logger      log;
    private String            method;

    private ActivityRunner(SkillModule m, MoneyRoute r, String method, Logger log) {
        this.module = m;
        this.route  = r;
        this.method = (method == null || method.isBlank()) ? null : method;
        this.log    = log;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /** Run a skill module indefinitely (auto-picks best method per current level). */
    public static ActivityRunner forSkill(SkillModule module, String methodOverride, Logger log) {
        return new ActivityRunner(module, null, methodOverride, log);
    }

    /** Run a money route indefinitely. */
    public static ActivityRunner forRoute(MoneyRoute route, Logger log) {
        return new ActivityRunner(null, route, null, log);
    }

    /**
     * Resolve an activity by ID — tries the skill registry first, then the
     * money route registry. Returns null if the ID is unknown.
     */
    public static ActivityRunner byId(String id, String methodOverride, Logger log) {
        if (id == null || id.isBlank()) return null;
        SkillModule m = SkillRegistry.byName(id).orElse(null);
        if (m != null) {
            log.info("Activity mode → skill module: " + m.name());
            return forSkill(m, methodOverride, log);
        }
        MoneyRoute r = MoneyRouteRegistry.byId(id);
        if (r != null) {
            log.info("Activity mode → money route: " + r.id());
            return forRoute(r, log);
        }
        log.warn("ActivityRunner: unknown activity id '" + id + "' — nothing to run.");
        return null;
    }

    // ── Task contract ─────────────────────────────────────────────────────────

    @Override public int      priority()    { return 80; }
    @Override public BotState state()       { return BotState.SKILLING; }
    @Override public boolean  isReady()     { return Client.isLoggedIn(); }
    /** Never completes — runs until the user clicks Stop in DreamBot. */
    @Override public boolean  isComplete()  { return false; }

    @Override public String label() {
        if (module != null) return "activity:" + module.name() + "[" + (method == null ? "auto" : method) + "]";
        if (route  != null) return "activity:route:" + route.id();
        return "activity:none";
    }

    @Override
    public int execute() {
        if (module != null) {
            int current = Skills.getRealLevel(module.skill());
            // Auto-select or reuse the method
            if (method == null) {
                method = module.pickMethod(current, 99);
            }
            return Math.max(200, module.tick(method));
        }
        if (route != null) {
            int sleep = route.tick();
            return sleep > 0 ? sleep : Calculations.random(450, 750);
        }
        return 1000;
    }
}
