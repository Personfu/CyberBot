package com.cyberscape.rsps317.model;

public class Rates {
    public final double dropRateMultiplier;
    public final double rareDropMultiplier;
    public final double gpMultiplier;
    public final double xpMultiplier;
    public final boolean pityEnabled;
    public final double pityThreshold;

    public Rates(double dropRateMultiplier,
                 double rareDropMultiplier,
                 double gpMultiplier,
                 double xpMultiplier,
                 boolean pityEnabled,
                 double pityThreshold) {
        this.dropRateMultiplier = dropRateMultiplier;
        this.rareDropMultiplier = rareDropMultiplier;
        this.gpMultiplier = gpMultiplier;
        this.xpMultiplier = xpMultiplier;
        this.pityEnabled = pityEnabled;
        this.pityThreshold = pityThreshold;
    }

    public static Rates defaults() {
        return new Rates(1.0, 1.0, 1.0, 1.0, false, 3.0);
    }
}
