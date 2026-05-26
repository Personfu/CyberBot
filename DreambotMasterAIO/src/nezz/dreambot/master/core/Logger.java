package nezz.dreambot.master.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Minimal in-memory + file logger. The console UI tab can drain {@link #drain()}
 * to render recent lines; the file sink is opt-in via {@link #enableFile(Path)}.
 *
 * <p>Not a replacement for SLF4J/Log4j — this is a script-side scratch logger
 * the AIO uses without pulling external deps.</p>
 */
public final class Logger {

    public enum Level { TRACE, DEBUG, INFO, WARN, ERROR }

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ConcurrentLinkedDeque<String> ring = new ConcurrentLinkedDeque<>();
    private final int ringCap;
    private volatile Path file;
    private volatile Level minLevel = Level.INFO;

    public Logger() { this(500); }
    public Logger(int ringCap) { this.ringCap = ringCap; }

    public void enableFile(Path path) {
        this.file = path;
        try {
            Files.createDirectories(path.getParent() == null ? Paths.get(".") : path.getParent());
        } catch (IOException ignored) { }
    }

    public void setMinLevel(Level l) { this.minLevel = l; }

    public void trace(String msg) { log(Level.TRACE, msg); }
    public void debug(String msg) { log(Level.DEBUG, msg); }
    public void info (String msg) { log(Level.INFO,  msg); }
    public void warn (String msg) { log(Level.WARN,  msg); }
    public void error(String msg) { log(Level.ERROR, msg); }

    public void log(Level lvl, String msg) {
        if (lvl.ordinal() < minLevel.ordinal()) return;
        String line = "[" + LocalDateTime.now().format(TS) + "][" + lvl + "] " + msg;
        ring.add(line);
        while (ring.size() > ringCap) ring.pollFirst();
        if (file != null) {
            try {
                Files.write(file, (line + System.lineSeparator()).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ignored) { }
        }
    }

    /** Snapshot of buffered lines without clearing. */
    public String[] snapshot() {
        return ring.toArray(new String[0]);
    }

    /** Pop and return all buffered lines. */
    public String[] drain() {
        String[] out = ring.toArray(new String[0]);
        ring.clear();
        return out;
    }
}
