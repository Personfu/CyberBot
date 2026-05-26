package nezz.dreambot.master.money;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all {@link MoneyRoute} implementations. Lookup by ID or list all.
 */
public final class MoneyRouteRegistry {

    private static final Map<String, MoneyRoute> ROUTES = new LinkedHashMap<>();

    static {
        register(new ChickenRoute());    // no requirements, always available
        register(new CowhideRoute());    // no requirements, solid GP
        register(new FlaxSpinRoute());   // no skill req, best non-combat F2P
        register(new SteelBarRoute());   // requires 30 Mining / Smithing
        register(new AirRuneRoute());    // requires 1 RC
        register(new YewLogsRoute());    // requires 60 WC
    }

    private MoneyRouteRegistry() { }

    public static void register(MoneyRoute r) { ROUTES.put(r.id(), r); }

    public static MoneyRoute byId(String id) { return ROUTES.get(id); }

    public static List<MoneyRoute> all() { return new ArrayList<>(ROUTES.values()); }
}
