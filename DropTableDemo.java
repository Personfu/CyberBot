package com.cyberscape.rsps317;

import com.cyberscape.rsps317.DropTableEngine.DropResult;
import com.cyberscape.rsps317.model.Item;
import com.cyberscape.rsps317.model.Npc;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
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
        Path dataDir = Paths.get("data").toAbsolutePath();
        if (!dataDir.resolve("rates.yml").toFile().exists()) {
            Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
            if (currentDir.resolve("rates.yml").toFile().exists()) {
                dataDir = currentDir;
            } else {
                dataDir = currentDir.resolve(Paths.get("Cyberscape317", "data")).toAbsolutePath();
            }
        }
        DropTableEngine engine = new DropTableEngine(dataDir);

        String npcName;
        long kills;
        if (args.length >= 2) {
            npcName = args[0];
            kills = Long.parseLong(args[1]);
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
