package nezz.dreambot.master.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Port of {@code net.runelite.client.util.QuantityFormatter} —
 * formats integer quantities the way OSRS does in the GE / inventory tooltips:
 * <pre>
 *   12        -> "12"
 *   9_999     -> "9,999"
 *   14_100    -> "14.1K"
 *   1_500_000 -> "1.5M"
 *   2_100_000_000 -> "2.1B"
 * </pre>
 *
 * <p>This is the helper class linked in the Varbits / NPC tutorial — keep it
 * in {@code util.QuantityFormatter} so existing FLLC scripts can swap in by
 * import without changing code.</p>
 */
public final class QuantityFormatter {

    private QuantityFormatter() { }

    private static final DecimalFormat NUMBER = new DecimalFormat("#,##0",
            new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat PRECISE = new DecimalFormat("#,##0.000",
            new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat SHORT_ONE = new DecimalFormat("#0.0",
            new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat SHORT_INT = new DecimalFormat("#0",
            new DecimalFormatSymbols(Locale.US));

    /** "12" / "9,999" / "14.1K" / "1.5M" / "2.1B". */
    public static String format(long quantity) {
        long abs = Math.abs(quantity);
        if (abs < 10_000) {
            return NUMBER.format(quantity);
        }
        String suffix;
        double scale;
        if (abs < 10_000_000L)        { suffix = "K"; scale = 1_000.0; }
        else if (abs < 10_000_000_000L){ suffix = "M"; scale = 1_000_000.0; }
        else                          { suffix = "B"; scale = 1_000_000_000.0; }

        double v = quantity / scale;
        DecimalFormat f = (Math.abs(v) < 10.0 ? SHORT_ONE : SHORT_INT);
        return f.format(v) + suffix;
    }

    /** Always inserts thousands separators: 14100000 -> "14,100,000". */
    public static String formatNumber(long n) {
        return NUMBER.format(n);
    }

    /** Three-decimal precise format: 14100000 -> "14,100,000.000". */
    public static String formatNumberPrecise(double d) {
        return PRECISE.format(d);
    }
}
