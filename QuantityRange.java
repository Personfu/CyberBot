package com.cyberscape.rsps317.model;

import java.util.concurrent.ThreadLocalRandom;

public class QuantityRange {
    private final int min;
    private final int max;

    private QuantityRange(int min, int max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public static QuantityRange exact(int n) { return new QuantityRange(n, n); }
    public static QuantityRange of(int min, int max) { return new QuantityRange(min, max); }

    public int roll() {
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public int min() { return min; }
    public int max() { return max; }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : (min + "-" + max);
    }
}
