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

    /** Exact match first; then case-insensitive; then simple class-name prefix (e.g. "ChickenRoute" → "chicken"). */
    public static MoneyRoute byId(String id) {
        if (id == null) return null;
        MoneyRoute exact = ROUTES.get(id);
        if (exact != null) return exact;
        // Case-insensitive fallback
        String lower = id.toLowerCase();
        for (Map.Entry<String, MoneyRoute> e : ROUTES.entrySet()) {
            if (e.getKey().equalsIgnoreCase(id)) return e.getValue();
            // Class-name prefix: "ChickenRoute" → starts with "chicken" (id)
            if (lower.startsWith(e.getKey().toLowerCase())) return e.getValue();
        }
        return null;
    }

    public static List<MoneyRoute> all() { return new ArrayList<>(ROUTES.values()); }
}
