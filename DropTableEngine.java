package com.cyberscape.rsps317;

import com.cyberscape.rsps317.model.DropEntry;
import com.cyberscape.rsps317.model.DropTable;
import com.cyberscape.rsps317.model.Item;
import com.cyberscape.rsps317.model.Npc;
import com.cyberscape.rsps317.model.QuantityRange;
import com.cyberscape.rsps317.model.Rates;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads drop tables, items, NPCs, and rates from YAML. Provides one core method:
 *   rollLoot(npcName, dryStreak) -> ordered list of (itemName, quantity)
 *
 * Demonstrates the actual server-side mechanic that decides what drops. In a full
 * Apollo-based RSPS this exact engine is invoked from the combat module after an
 * NPC reaches 0 HP.
 */
public class DropTableEngine {

    private final Map<String, DropTable> dropTables;
    private final Map<String, Item> items;
    private final Map<String, Npc> npcs;
    private final Rates rates;
    private final Map<String, Long> dryStreaks = new HashMap<>();

    public DropTableEngine(Path dataDir) throws IOException {
        this(dataDir, null);
    }

    public DropTableEngine(Path dataDir, Rates runtimeRatesOverride) throws IOException {
        Yaml yaml = new Yaml();
        Rates loadedRates = loadRates(yaml, dataDir.resolve("rates.yml"));
        this.rates = runtimeRatesOverride != null ? runtimeRatesOverride : loadedRates;
        this.items = loadItems(yaml, dataDir.resolve("item_definitions.yml"));
        this.npcs = loadNpcs(yaml, dataDir.resolve("npc_definitions.yml"));
        this.dropTables = loadDropTables(yaml, dataDir.resolve("drop_tables.yml"));
    }

    public Rates rates() { return rates; }
    public Map<String, Item> items() { return items; }
    public Map<String, Npc> npcs() { return npcs; }
    public Map<String, DropTable> dropTables() { return dropTables; }

    /**
     * Roll a single kill against the named NPC's drop table.
     * Returns an ordered list — `always` drops first, then weighted-table rolls.
     */
    public List<DropResult> rollLoot(String npcName) {
        DropTable table = dropTables.get(npcName);
        if (table == null) {
            throw new IllegalArgumentException("No drop table for NPC: " + npcName);
        }
        List<DropResult> results = new ArrayList<>();

        for (DropEntry always : table.always()) {
            results.add(rollEntry(always));
        }

        if (table.weightedTotal() <= 0 || table.entries().isEmpty()) {
            return results;
        }

        for (int i = 0; i < table.rolls(); i++) {
            DropEntry rolled = rollWeightedTable(table);
            if (rolled != null) {
                results.add(rollEntry(rolled));
                if (rolled.rare()) dryStreaks.put(npcName, 0L);
                else dryStreaks.merge(npcName, 1L, Long::sum);
            }
        }
        return results;
    }

    private DropEntry rollWeightedTable(DropTable table) {
        double effectiveTotal = 0;
        for (DropEntry e : table.entries()) {
            effectiveTotal += effectiveWeight(e);
        }
        if (effectiveTotal <= 0) return null;

        double r = ThreadLocalRandom.current().nextDouble() * effectiveTotal;
        double accum = 0;
        for (DropEntry e : table.entries()) {
            accum += effectiveWeight(e);
            if (r < accum) return e;
        }
        return table.entries().get(table.entries().size() - 1);
    }

    /**
     * Apply the global multipliers. A rare entry with weight 1 and rare_drop_multiplier=5
     * becomes effectively weight 5 — the rare comes up 5x more often relative to the common
     * pool. Same idea for drop_rate_multiplier on the table as a whole.
     */
    private double effectiveWeight(DropEntry e) {
        double w = e.weight() * rates.dropRateMultiplier;
        if (e.rare()) w *= rates.rareDropMultiplier;
        return w;
    }

    private DropResult rollEntry(DropEntry entry) {
        int qty = entry.quantity().roll();
        if ("Coins".equalsIgnoreCase(entry.itemName())) {
            qty = (int) Math.round(qty * rates.gpMultiplier);
        }
        return new DropResult(entry.itemName(), qty);
    }

    @SuppressWarnings("unchecked")
    private Map<String, DropTable> loadDropTables(Yaml yaml, Path path) throws IOException {
        Map<String, Object> raw;
        try (var in = Files.newInputStream(path)) {
            raw = yaml.load(in);
        }
        Map<String, DropTable> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            Map<String, Object> tableRaw = (Map<String, Object>) e.getValue();
            List<DropEntry> always = parseEntries((List<Map<String, Object>>) tableRaw.getOrDefault("always", new ArrayList<>()));
            int rolls = ((Number) tableRaw.getOrDefault("rolls", 0)).intValue();
            int weightedTotal = ((Number) tableRaw.getOrDefault("weighted_total", 0)).intValue();
            List<DropEntry> entries = parseEntries((List<Map<String, Object>>) tableRaw.getOrDefault("entries", new ArrayList<>()));
            out.put(e.getKey(), new DropTable(e.getKey(), always, rolls, weightedTotal, entries));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<DropEntry> parseEntries(List<Map<String, Object>> rawEntries) {
        List<DropEntry> out = new ArrayList<>();
        for (Map<String, Object> raw : rawEntries) {
            String item = (String) raw.get("item");
            QuantityRange qty;
            if (raw.containsKey("quantity_range")) {
                List<Number> range = (List<Number>) raw.get("quantity_range");
                qty = QuantityRange.of(range.get(0).intValue(), range.get(1).intValue());
            } else {
                int n = ((Number) raw.getOrDefault("quantity", 1)).intValue();
                qty = QuantityRange.exact(n);
            }
            int weight = ((Number) raw.getOrDefault("weight", 0)).intValue();
            boolean rare = Boolean.TRUE.equals(raw.get("rare"));
            out.add(new DropEntry(item, qty, weight, rare));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Item> loadItems(Yaml yaml, Path path) throws IOException {
        Map<String, Object> raw;
        try (var in = Files.newInputStream(path)) {
            raw = yaml.load(in);
        }
        Map<String, Item> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) e.getValue();
            int id = ((Number) def.get("id")).intValue();
            long gePrice = ((Number) def.getOrDefault("ge_price", 0)).longValue();
            boolean stackable = Boolean.TRUE.equals(def.get("stackable"));
            boolean untradeable = Boolean.TRUE.equals(def.get("untradeable"));
            out.put(e.getKey(), new Item(e.getKey(), id, gePrice, stackable, untradeable));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Npc> loadNpcs(Yaml yaml, Path path) throws IOException {
        Map<String, Object> raw;
        try (var in = Files.newInputStream(path)) {
            raw = yaml.load(in);
        }
        Map<String, Npc> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) e.getValue();
            int id = ((Number) def.get("id")).intValue();
            int cb = ((Number) def.getOrDefault("combat_level", 1)).intValue();
            int hp = ((Number) def.getOrDefault("hitpoints", 1)).intValue();
            int kt = ((Number) def.getOrDefault("kill_time_seconds", 1)).intValue();
            out.put(e.getKey(), new Npc(e.getKey(), id, cb, hp, kt));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Rates loadRates(Yaml yaml, Path path) throws IOException {
        Map<String, Object> raw;
        try (var in = Files.newInputStream(path)) {
            raw = yaml.load(in);
        }
        double drop = ((Number) raw.getOrDefault("drop_rate_multiplier", 1.0)).doubleValue();
        double rare = ((Number) raw.getOrDefault("rare_drop_multiplier", 1.0)).doubleValue();
        double gp   = ((Number) raw.getOrDefault("gp_multiplier", 1.0)).doubleValue();
        double xp   = ((Number) raw.getOrDefault("xp_multiplier", 1.0)).doubleValue();
        boolean pity = Boolean.TRUE.equals(raw.getOrDefault("pity_enabled", false));
        double pityThreshold = ((Number) raw.getOrDefault("pity_threshold", 3.0)).doubleValue();
        return new Rates(drop, rare, gp, xp, pity, pityThreshold);
    }

    public static class DropResult {
        public final String itemName;
        public final int quantity;
        public DropResult(String itemName, int quantity) {
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }
}
