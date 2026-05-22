package nezz.dreambot.allinone.util;

/**
 * Formats large numbers for on-screen paint overlays.
 * Mirrors the style of RuneLite's QuantityFormatter (9000→9k, 14100000→14.1m).
 */
public final class QuantityFormatter {

    private QuantityFormatter() {}

    public static String format(long n) {
        if (n < 0)               return "-" + format(-n);
        if (n >= 1_000_000_000L) return String.format("%.1fb", n / 1_000_000_000.0);
        if (n >= 1_000_000L)     return String.format("%.1fm", n / 1_000_000.0);
        if (n >= 1_000L)         return String.format("%.1fk", n / 1_000.0);
        return Long.toString(n);
    }

    /** "Always", "1/128", "12/256" — used in the drop table GUI column. */
    public static String formatRate(int numerator, int denominator) {
        if (denominator <= 1) return "Always";
        if (numerator == 1)   return "1/" + denominator;
        return numerator + "/" + denominator;
    }

    /** Returns the approximate number of kills between drops, for the Rates column. */
    public static String formatRateVerbose(int numerator, int denominator) {
        if (denominator <= 1) return "Always (1/1)";
        double rate = (double) denominator / numerator;
        if (rate < 2) return "Very common";
        if (rate < 10) return String.format("~1 per %.0f kills", rate);
        return String.format("1/%d", (int) rate);
    }
}
