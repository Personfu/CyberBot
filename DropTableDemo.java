package com.cyberscape.rsps317;

import com.cyberscape.rsps317.DropTableEngine.DropResult;
import com.cyberscape.rsps317.model.DropEntry;
import com.cyberscape.rsps317.model.DropTable;
import com.cyberscape.rsps317.model.Item;
import com.cyberscape.rsps317.model.Npc;
import com.cyberscape.rsps317.model.Rates;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private static final int GUI_TEXT_AREA_ROWS = 22;
    private static final int GUI_TEXT_AREA_COLUMNS = 110;

    public static void main(String[] args) throws Exception {
        CliOptions options = parseArgs(args);

        Path dataDir = resolveDataDir(options.dataDirArg);
        if (dataDir == null) {
            throw new IllegalArgumentException("Could not find data files. Use --data-dir=<path> with rates.yml, item_definitions.yml, npc_definitions.yml, and drop_tables.yml.");
        }

        if (options.gui) {
            launchGui(dataDir, options);
            return;
        }

        Rates baseRates = DropTableEngine.loadRatesFile(dataDir.resolve("rates.yml"));
        Rates runRates = new Rates(
            options.dropRateMultiplier != null ? options.dropRateMultiplier : baseRates.dropRateMultiplier,
            options.rareDropMultiplier != null ? options.rareDropMultiplier : baseRates.rareDropMultiplier,
            options.gpMultiplier != null ? options.gpMultiplier : baseRates.gpMultiplier,
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
            throw new IllegalArgumentException("Unknown NPC: " + npcName + ". Available NPCs (showing up to 10): " + sampleNpcNames(engine, 10));
        }

        System.out.print(buildSimulationReport(engine, npcName, kills));
        System.out.println("Edit rates.yml or use CLI multipliers and re-run to see the effect.");
    }

    private static String buildSimulationReport(DropTableEngine engine, String npcName, long kills) {
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

        StringBuilder out = new StringBuilder();
        out.append(System.lineSeparator());
        out.append("=== Drop simulation: ").append(npcName).append(" x ").append(kills).append(" kills ===").append(System.lineSeparator());
        out.append("Rates: drop=").append(engine.rates().dropRateMultiplier)
            .append(" rare=").append(engine.rates().rareDropMultiplier)
            .append(" gp=").append(engine.rates().gpMultiplier)
            .append(System.lineSeparator());
        out.append("Engine time: ").append(elapsedMs).append("ms").append(System.lineSeparator());
        out.append(System.lineSeparator());
        out.append(String.format("%-30s %12s %12s %14s%n", "Item", "Drops", "Total qty", "Total GP"));
        out.append("-".repeat(72)).append(System.lineSeparator());
        histogram.entrySet().stream()
            .sorted((a, b) -> Long.compare(gpFor(engine, b.getKey(), totalQty),
                                           gpFor(engine, a.getKey(), totalQty)))
            .forEach(e -> {
                long qty = totalQty.getOrDefault(e.getKey(), 0L);
                Item it = engine.items().get(e.getKey());
                long gp = it != null ? it.gePrice() * qty : 0;
                out.append(String.format("%-30s %12s %12s %14s%n",
                    truncate(e.getKey(), 30),
                    formatN(e.getValue()),
                    formatN(qty),
                    formatN(gp)));
            });
        out.append("-".repeat(72)).append(System.lineSeparator());
        out.append(String.format("%-30s %12s %12s %14s%n", "TOTAL", "", "", formatN(totalGp)));

        if (npc != null && npc.killTimeSeconds() > 0) {
            double killsPerHour = 3600.0 / npc.killTimeSeconds();
            double gpPerKill = (double) totalGp / kills;
            long projectedGpPerHour = (long) (killsPerHour * gpPerKill);
            out.append(System.lineSeparator());
            out.append("Projected at ").append((int) killsPerHour).append(" kills/hr: ")
                .append(formatN(projectedGpPerHour)).append(" gp/hr").append(System.lineSeparator());
        }
        out.append(System.lineSeparator());
        return out.toString();
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

    private static String sampleNpcNames(DropTableEngine engine, int maxCount) {
        List<String> names = new ArrayList<>(engine.dropTables().keySet());
        int end = Math.min(maxCount, names.size());
        return String.join(", ", names.subList(0, end));
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
            } else if (arg.equals("--gui")) {
                options.gui = true;
            } else if (arg.startsWith("--gui-screenshot=")) {
                options.guiScreenshot = arg.substring("--gui-screenshot=".length());
            } else if (arg.startsWith("--rare-drop-multiplier=")) {
                options.rareDropMultiplier = Double.parseDouble(arg.substring("--rare-drop-multiplier=".length()));
            } else if (arg.startsWith("--drop-rate-multiplier=")) {
                options.dropRateMultiplier = Double.parseDouble(arg.substring("--drop-rate-multiplier=".length()));
            } else if (arg.startsWith("--gp-multiplier=")) {
                options.gpMultiplier = Double.parseDouble(arg.substring("--gp-multiplier=".length()));
            } else if (arg.startsWith("--script-repo=")) {
                options.scriptRepo = arg.substring("--script-repo=".length());
            } else if (arg.startsWith("--script-module=")) {
                options.scriptModule = arg.substring("--script-module=".length());
            } else if (arg.startsWith("--sdn-parameters=")) {
                options.sdnParameters = arg.substring("--sdn-parameters=".length());
            } else {
                options.positional.add(arg);
            }
        }
        return options;
    }

    private static void launchGui(Path dataDir, CliOptions options) throws Exception {
        Rates baseRates = DropTableEngine.loadRatesFile(dataDir.resolve("rates.yml"));
        DropTableEngine baseEngine = new DropTableEngine(dataDir, baseRates);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CyberBot All-in-One Drop Config");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

            JTextField dataDirField = new JTextField(dataDir.toString());
            JComboBox<String> npcBox = new JComboBox<>(baseEngine.dropTables().keySet().toArray(String[]::new));
            JTextField killsField = new JTextField("1000");
            JTextField dropMultiplierField = new JTextField(String.valueOf(options.dropRateMultiplier != null ? options.dropRateMultiplier : baseRates.dropRateMultiplier));
            JTextField rareMultiplierField = new JTextField(String.valueOf(options.rareDropMultiplier != null ? options.rareDropMultiplier : baseRates.rareDropMultiplier));
            JTextField gpMultiplierField = new JTextField(String.valueOf(options.gpMultiplier != null ? options.gpMultiplier : baseRates.gpMultiplier));
            JTextField scriptRepoField = new JTextField(options.scriptRepo != null ? options.scriptRepo : "");
            JTextField scriptModuleField = new JTextField(options.scriptModule != null ? options.scriptModule : "");
            JTextField sdnParamsField = new JTextField(options.sdnParameters != null ? options.sdnParameters : "");

            form.add(new JLabel("Data Directory"));
            form.add(dataDirField);
            form.add(new JLabel("NPC"));
            form.add(npcBox);
            form.add(new JLabel("Kills"));
            form.add(killsField);
            form.add(new JLabel("Drop Rate Multiplier"));
            form.add(dropMultiplierField);
            form.add(new JLabel("Rare Drop Multiplier"));
            form.add(rareMultiplierField);
            form.add(new JLabel("GP Multiplier"));
            form.add(gpMultiplierField);
            form.add(new JLabel("Script Repo"));
            form.add(scriptRepoField);
            form.add(new JLabel("Script Module"));
            form.add(scriptModuleField);
            form.add(new JLabel("SDN Parameters (Optional)"));
            form.add(sdnParamsField);

            JTextArea output = new JTextArea(GUI_TEXT_AREA_ROWS, GUI_TEXT_AREA_COLUMNS);
            output.setEditable(false);

            JButton runButton = new JButton("Run Simulation");
            runButton.addActionListener(ev -> {
                try {
                    Path selectedDataDir = Paths.get(dataDirField.getText().trim()).toAbsolutePath().normalize();
                    Rates selectedBase = DropTableEngine.loadRatesFile(selectedDataDir.resolve("rates.yml"));
                    Rates runRates = new Rates(
                        Double.parseDouble(dropMultiplierField.getText().trim()),
                        Double.parseDouble(rareMultiplierField.getText().trim()),
                        Double.parseDouble(gpMultiplierField.getText().trim()),
                        selectedBase.xpMultiplier,
                        selectedBase.pityEnabled,
                        selectedBase.pityThreshold
                    );
                    DropTableEngine runEngine = new DropTableEngine(selectedDataDir, runRates);
                    String npc = String.valueOf(npcBox.getSelectedItem());
                    long kills = Long.parseLong(killsField.getText().trim());
                    String report = kills > 0
                        ? buildSimulationReport(runEngine, npc, kills)
                        : "Simulation skipped (Kills <= 0)." + System.lineSeparator() + System.lineSeparator();
                    output.setText(buildGuiOutput(
                        scriptRepoField.getText().trim(),
                        scriptModuleField.getText().trim(),
                        sdnParamsField.getText().trim(),
                        runRates,
                        buildItemsPerEnemyReport(runEngine),
                        report
                    ));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Simulation Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            frame.getContentPane().setLayout(new BorderLayout(8, 8));
            frame.add(form, BorderLayout.NORTH);
            frame.add(runButton, BorderLayout.CENTER);
            frame.add(new JScrollPane(output), BorderLayout.SOUTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            if (options.guiScreenshot != null && !options.guiScreenshot.isBlank()) {
                try {
                    saveComponentScreenshot(frame, Paths.get(options.guiScreenshot).toAbsolutePath().normalize());
                    frame.dispose();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private static void saveComponentScreenshot(JFrame frame, Path outputPath) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        frame.paint(image.getGraphics());
        ImageIO.write(image, "png", outputPath.toFile());
    }

    private static String buildGuiOutput(
        String scriptRepo,
        String scriptModule,
        String sdnParameters,
        Rates rates,
        String itemsPerEnemyReport,
        String simulationReport
    ) {
        StringBuilder out = new StringBuilder();
        out.append("SDN submission setup").append('\n')
            .append("Script Repo: ").append(scriptRepo).append('\n')
            .append("Script Module: ").append(scriptModule).append('\n')
            .append("SDN Parameters (Optional): ").append(sdnParameters).append('\n')
            .append('\n')
            .append("Drop Rate Changer").append('\n')
            .append("Drop Rate Multiplier: ").append(rates.dropRateMultiplier).append('\n')
            .append("Rare Drop Multiplier: ").append(rates.rareDropMultiplier).append('\n')
            .append("GP Multiplier: ").append(rates.gpMultiplier).append('\n')
            .append('\n')
            .append(itemsPerEnemyReport)
            .append(simulationReport);
        return out.toString();
    }

    private static String buildItemsPerEnemyReport(DropTableEngine engine) {
        StringBuilder out = new StringBuilder();
        out.append("Items per enemy (configured drop table, not simulated)").append('\n');
        for (Map.Entry<String, DropTable> e : engine.dropTables().entrySet()) {
            DropTable table = e.getValue();
            out.append("NPC: ").append(e.getKey()).append('\n');
            if (!table.always().isEmpty()) {
                out.append("  Always drops:").append('\n');
                for (DropEntry alwaysEntry : table.always()) {
                    out.append("    - ")
                        .append(alwaysEntry.itemName())
                        .append(" x")
                        .append(alwaysEntry.quantity())
                        .append('\n');
                }
            }
            if (!table.entries().isEmpty()) {
                out.append("  Weighted drops (rolls=")
                    .append(table.rolls())
                    .append(", weighted_total=")
                    .append(table.weightedTotal())
                    .append("):")
                    .append('\n');
                for (DropEntry weightedEntry : table.entries()) {
                    out.append("    - ")
                        .append(weightedEntry.itemName())
                        .append(" x")
                        .append(weightedEntry.quantity())
                        .append(", weight=")
                        .append(weightedEntry.weight());
                    if (weightedEntry.rare()) {
                        out.append(", rare");
                    }
                    out.append('\n');
                }
            }
            out.append('\n');
        }
        return out.toString();
    }

    private static class CliOptions {
        private String dataDirArg;
        private boolean gui;
        private String guiScreenshot;
        private Double rareDropMultiplier;
        private Double dropRateMultiplier;
        private Double gpMultiplier;
        private String scriptRepo;
        private String scriptModule;
        private String sdnParameters;
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
