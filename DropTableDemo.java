package com.cyberscape.rsps317;

import com.cyberscape.rsps317.DropTableEngine.DropResult;
import com.cyberscape.rsps317.model.Item;
import com.cyberscape.rsps317.model.Npc;
import com.cyberscape.rsps317.model.Rates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * CLI demo:
 *   gradle run --args="<NpcName> <numKills>"
 * Example:
 *   gradle run --args="Vorkath 10000"
 *
 * Prints a loot histogram for that NPC over N simulated kills, with total GP value
 * and projected GP/hour using the NPC's kill_time_seconds field.
 *
 * Then edit data/rates.yml -> drop_rate_multiplier: 50.0 and re-run. Same drop
 * table, same engine, vastly different output. THAT is the server-admin power.
 */
public class DropTableDemo {

    public static void main(String[] args) throws Exception {
        CliOptions options = parseArgs(args);

        Path dataDir = resolveDataDir(options.dataDirArg);
        if (dataDir == null) {
            throw new IllegalArgumentException("Could not find data files. Use --data-dir=<path> with rates.yml, item_definitions.yml, npc_definitions.yml, and drop_tables.yml.");
        }

        DropTableEngine baseEngine = new DropTableEngine(dataDir);
        Rates baseRates = baseEngine.rates();
        Rates runRates = new Rates(
            options.dropRateMultiplier != null ? options.dropRateMultiplier : baseRates.dropRateMultiplier,
            options.rareDropMultiplier != null ? options.rareDropMultiplier : baseRates.rareDropMultiplier,
            baseRates.gpMultiplier,
            baseRates.xpMultiplier,
            baseRates.pityEnabled,
            baseRates.pityThreshold
        );
        DropTableEngine engine = new DropTableEngine(dataDir, runRates);

        String npcName;
        long kills;
        if (options.positional.size() >= 2) {
            npcName = options.positional.get(0);
            kills = Long.parseLong(options.positional.get(1));
        } else {
            try (Scanner sc = new Scanner(System.in)) {
                System.out.print("NPC name: ");
                npcName = sc.nextLine().trim();
                System.out.print("Number of kills to simulate: ");
                kills = Long.parseLong(sc.nextLine().trim());
            }
        }

        if (!engine.dropTables().containsKey(npcName)) {
            System.err.println("Unknown NPC: " + npcName);
            System.err.println("Known NPCs: " + engine.dropTables().keySet());
            System.exit(1);
        }

        Npc npc = engine.npcs().get(npcName);
        Map<String, Long> histogram = new LinkedHashMap<>();
        Map<String, Long> totalQty = new LinkedHashMap<>();
        long startNs = System.nanoTime();

        for (long i = 0; i < kills; i++) {
            List<DropResult> rolled = engine.rollLoot(npcName);
            for (DropResult r : rolled) {
                histogram.merge(r.itemName, 1L, Long::sum);
                totalQty.merge(r.itemName, (long) r.quantity, Long::sum);
            }
        }
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;

        long totalGp = 0;
        for (Map.Entry<String, Long> e : totalQty.entrySet()) {
            Item item = engine.items().get(e.getKey());
            if (item != null) totalGp += item.gePrice() * e.getValue();
        }

        System.out.println();
        System.out.println("=== Drop simulation: " + npcName + " x " + kills + " kills ===");
        System.out.println("Rates: drop=" + engine.rates().dropRateMultiplier
            + " rare=" + engine.rates().rareDropMultiplier
            + " gp=" + engine.rates().gpMultiplier);
        System.out.println("Engine time: " + elapsedMs + "ms");
        System.out.println();
        System.out.printf("%-30s %12s %12s %14s%n", "Item", "Drops", "Total qty", "Total GP");
        System.out.println("-".repeat(72));
        histogram.entrySet().stream()
            .sorted((a, b) -> Long.compare(gpFor(engine, b.getKey(), totalQty),
                                           gpFor(engine, a.getKey(), totalQty)))
            .forEach(e -> {
                long qty = totalQty.getOrDefault(e.getKey(), 0L);
                Item it = engine.items().get(e.getKey());
                long gp = it != null ? it.gePrice() * qty : 0;
                System.out.printf("%-30s %12s %12s %14s%n",
                    truncate(e.getKey(), 30),
                    formatN(e.getValue()),
                    formatN(qty),
                    formatN(gp));
            });
        System.out.println("-".repeat(72));
        System.out.printf("%-30s %12s %12s %14s%n", "TOTAL", "", "", formatN(totalGp));

        if (npc != null && npc.killTimeSeconds() > 0) {
            double killsPerHour = 3600.0 / npc.killTimeSeconds();
            double gpPerKill = (double) totalGp / kills;
            long projectedGpPerHour = (long) (killsPerHour * gpPerKill);
            System.out.println();
            System.out.println("Projected at " + (int) killsPerHour + " kills/hr: "
                + formatN(projectedGpPerHour) + " gp/hr");
        }

        System.out.println();
        System.out.println("Edit data/rates.yml -> drop_rate_multiplier and re-run to see the effect.");
    }

    private static Path resolveDataDir(String dataDirArg) {
        List<Path> candidates = new ArrayList<>();
        if (dataDirArg != null && !dataDirArg.isBlank()) {
            candidates.add(Paths.get(dataDirArg).toAbsolutePath().normalize());
        }
        candidates.add(Paths.get("data").toAbsolutePath().normalize());
        candidates.add(Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize());
        candidates.add(Paths.get(System.getProperty("user.dir"), "Cyberscape317", "data").toAbsolutePath().normalize());

        for (Path candidate : candidates) {
            if (hasRequiredDataFiles(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hasRequiredDataFiles(Path dir) {
        return Files.exists(dir.resolve("rates.yml"))
            && Files.exists(dir.resolve("item_definitions.yml"))
            && Files.exists(dir.resolve("npc_definitions.yml"))
            && Files.exists(dir.resolve("drop_tables.yml"));
    }

    private static CliOptions parseArgs(String[] args) {
        CliOptions options = new CliOptions();
        for (String arg : args) {
            if (arg.startsWith("--data-dir=")) {
                options.dataDirArg = arg.substring("--data-dir=".length());
            } else if (arg.startsWith("--rare-drop-multiplier=")) {
                options.rareDropMultiplier = Double.parseDouble(arg.substring("--rare-drop-multiplier=".length()));
            } else if (arg.startsWith("--drop-rate-multiplier=")) {
                options.dropRateMultiplier = Double.parseDouble(arg.substring("--drop-rate-multiplier=".length()));
            } else {
                options.positional.add(arg);
            }
        }
        return options;
    }

    private static class CliOptions {
        private String dataDirArg;
        private Double rareDropMultiplier;
        private Double dropRateMultiplier;
        private final List<String> positional = new ArrayList<>();
    }

    private static long gpFor(DropTableEngine engine, String name, Map<String, Long> totals) {
        Item it = engine.items().get(name);
        if (it == null) return 0;
        return it.gePrice() * totals.getOrDefault(name, 0L);
    }

    private static String formatN(long n) {
        return NumberFormat.getNumberInstance(Locale.US).format(n);
    }

    private static String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }
}
