package nezz.dreambot.master.money;

/**
 * Abstract base for all F2P money-making routes.
 *
 * <h3>Contract</h3>
 * Each route:
 * <ul>
 *   <li>Declares the minimum skill levels needed to run it.</li>
 *   <li>Estimates GP/hr at those levels.</li>
 *   <li>Exposes a {@link #tick()} that the {@link MoneyRouteTask} calls each loop.</li>
 *   <li>Queues completed stacks to {@link nezz.dreambot.master.ge.GESellTask}.</li>
 * </ul>
 *
 * <p>Inspired by the dedicated money-making phases in SlugBuilder, SubAccountBuilder,
 * HowF2PAIO and their GP/hr notes on Dreambot forums.</p>
 */
public abstract class MoneyRoute {

    /** Unique identifier used in the BuildPlan. */
    public abstract String id();

    /** Human-readable description for the HUD. */
    public abstract String description();

    /** Estimated GP/hr at minimum level. Used for route ranking. */
    public abstract int estimatedGpHr();

    /**
     * Minimum skill levels required.  Map entries: Skill → level.
     * Routes without requirements return an empty map.
     */
    public abstract java.util.Map<org.dreambot.api.methods.skills.Skill, Integer> requirements();

    /**
     * Called each bot loop while this route is active.
     * @return suggested sleep in ms before next tick
     */
    public abstract int tick();

    /** True when the route has built up a sellable batch and queued it at GE. */
    public boolean isBatchReady() { return false; }
}
