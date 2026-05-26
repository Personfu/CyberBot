package nezz.dreambot.master.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Runtime configuration for the MasterAIO. Holds account creds, the
 * {@link BuildPlan}, antiban flags, GE mule address, and the discord webhook.
 *
 * <p>Persisted as flat properties to keep the binary footprint small — no
 * Jackson/Gson dependency. Lists are stored as comma-separated strings.</p>
 */
public final class Profile {

    // ── Account / login ──────────────────────────────────────────────────────
    public String accountEmail   = "";
    public String accountPass    = "";
    public String accountName    = "";
    public String accountAge     = "1990-01-01";
    public boolean useQuickstart = false;

    // ── Plan ─────────────────────────────────────────────────────────────────
    public BuildPlan plan = BuildPlan.defaultF2P();

    // ── Antiban ──────────────────────────────────────────────────────────────
    public boolean humanMouse        = true;
    public boolean cameraJitter      = true;
    public boolean randomTabs        = true;
    public boolean afkDrift          = true;
    public int     breakEveryMinMin  = 45;   // break after at least this many minutes
    public int     breakEveryMaxMin  = 90;
    public int     breakDurationMinM = 5;
    public int     breakDurationMaxM = 20;
    public int     fatigueWindowH    = 6;    // hours played per 24h

    // ── Money / mule / GE ────────────────────────────────────────────────────
    public String muleAccount   = "";
    public String muleWorld     = "301";
    public Path   muleLocation  = null;
    public boolean useGeRestock = true;

    // ── Notification ─────────────────────────────────────────────────────────
    public String  discordWebhook = "";
    public boolean notifyOnBan    = true;
    public boolean notifyOnLevel  = true;
    public boolean notifyOnQuest  = true;

    // ── Night sleep (24/7 anti-ban) ──────────────────────────────────────────
    public boolean enableNightSleep    = true;
    public int     nightSleepStartHour = 0;    // 00:00 local time
    public int     nightSleepEndHour   = 7;    // 07:00 local time

    // ── World hopping ────────────────────────────────────────────────────────
    public boolean enableWorldHop = true;

    // ── Stop conditions ──────────────────────────────────────────────────────
    public int  stopAfterHours    = 0;       // 0 = never
    public long stopAtTotalXp     = 0L;
    public boolean stopOnTradeReq = true;

    // ────────────────────────────────────────────────────────────────────────
    // Persistence
    // ────────────────────────────────────────────────────────────────────────

    public void save(Path path) throws IOException {
        Properties p = new Properties();
        p.setProperty("accountEmail",      accountEmail);
        p.setProperty("accountPass",       accountPass);
        p.setProperty("accountName",       accountName);
        p.setProperty("accountAge",        accountAge);
        p.setProperty("useQuickstart",     String.valueOf(useQuickstart));
        p.setProperty("humanMouse",        String.valueOf(humanMouse));
        p.setProperty("cameraJitter",      String.valueOf(cameraJitter));
        p.setProperty("randomTabs",        String.valueOf(randomTabs));
        p.setProperty("afkDrift",          String.valueOf(afkDrift));
        p.setProperty("breakEveryMinMin",  String.valueOf(breakEveryMinMin));
        p.setProperty("breakEveryMaxMin",  String.valueOf(breakEveryMaxMin));
        p.setProperty("breakDurationMinM", String.valueOf(breakDurationMinM));
        p.setProperty("breakDurationMaxM", String.valueOf(breakDurationMaxM));
        p.setProperty("fatigueWindowH",    String.valueOf(fatigueWindowH));
        p.setProperty("muleAccount",       muleAccount);
        p.setProperty("muleWorld",         muleWorld);
        p.setProperty("useGeRestock",      String.valueOf(useGeRestock));
        p.setProperty("discordWebhook",    discordWebhook);
        p.setProperty("notifyOnBan",       String.valueOf(notifyOnBan));
        p.setProperty("notifyOnLevel",     String.valueOf(notifyOnLevel));
        p.setProperty("notifyOnQuest",     String.valueOf(notifyOnQuest));
        p.setProperty("stopAfterHours",    String.valueOf(stopAfterHours));
        p.setProperty("stopAtTotalXp",     String.valueOf(stopAtTotalXp));
        p.setProperty("stopOnTradeReq",    String.valueOf(stopOnTradeReq));
        try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            p.store(w, "MasterAIO profile");
        }
    }

    public static Profile load(Path path) throws IOException {
        Profile out = new Profile();
        Properties p = new Properties();
        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(r);
        }
        out.accountEmail      = p.getProperty("accountEmail", out.accountEmail);
        out.accountPass       = p.getProperty("accountPass",  out.accountPass);
        out.accountName       = p.getProperty("accountName",  out.accountName);
        out.accountAge        = p.getProperty("accountAge",   out.accountAge);
        out.useQuickstart     = parseB(p, "useQuickstart",    out.useQuickstart);
        out.humanMouse        = parseB(p, "humanMouse",       out.humanMouse);
        out.cameraJitter      = parseB(p, "cameraJitter",     out.cameraJitter);
        out.randomTabs        = parseB(p, "randomTabs",       out.randomTabs);
        out.afkDrift          = parseB(p, "afkDrift",         out.afkDrift);
        out.breakEveryMinMin  = parseI(p, "breakEveryMinMin", out.breakEveryMinMin);
        out.breakEveryMaxMin  = parseI(p, "breakEveryMaxMin", out.breakEveryMaxMin);
        out.breakDurationMinM = parseI(p, "breakDurationMinM",out.breakDurationMinM);
        out.breakDurationMaxM = parseI(p, "breakDurationMaxM",out.breakDurationMaxM);
        out.fatigueWindowH    = parseI(p, "fatigueWindowH",   out.fatigueWindowH);
        out.muleAccount       = p.getProperty("muleAccount",  out.muleAccount);
        out.muleWorld         = p.getProperty("muleWorld",    out.muleWorld);
        out.useGeRestock      = parseB(p, "useGeRestock",     out.useGeRestock);
        out.discordWebhook    = p.getProperty("discordWebhook", out.discordWebhook);
        out.notifyOnBan       = parseB(p, "notifyOnBan",      out.notifyOnBan);
        out.notifyOnLevel     = parseB(p, "notifyOnLevel",    out.notifyOnLevel);
        out.notifyOnQuest     = parseB(p, "notifyOnQuest",    out.notifyOnQuest);
        out.stopAfterHours    = parseI(p, "stopAfterHours",   out.stopAfterHours);
        out.stopAtTotalXp     = parseL(p, "stopAtTotalXp",    out.stopAtTotalXp);
        out.stopOnTradeReq    = parseB(p, "stopOnTradeReq",   out.stopOnTradeReq);
        return out;
    }

    private static boolean parseB(Properties p, String k, boolean d) {
        String v = p.getProperty(k);
        return v == null ? d : Boolean.parseBoolean(v);
    }
    private static int parseI(Properties p, String k, int d) {
        try { return Integer.parseInt(p.getProperty(k, String.valueOf(d))); }
        catch (NumberFormatException e) { return d; }
    }
    private static long parseL(Properties p, String k, long d) {
        try { return Long.parseLong(p.getProperty(k, String.valueOf(d))); }
        catch (NumberFormatException e) { return d; }
    }
}
