package com.cyberscape.rsps317.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DropTable {
    private final String npcName;
    private final List<DropEntry> always;
    private final int rolls;
    private final int weightedTotal;
    private final List<DropEntry> entries;

    public DropTable(String npcName,
                     List<DropEntry> always,
                     int rolls,
                     int weightedTotal,
                     List<DropEntry> entries) {
        this.npcName = npcName;
        this.always = Collections.unmodifiableList(new ArrayList<>(always));
        this.rolls = rolls;
        this.weightedTotal = weightedTotal;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public String npcName() { return npcName; }
    public List<DropEntry> always() { return always; }
    public int rolls() { return rolls; }
    public int weightedTotal() { return weightedTotal; }
    public List<DropEntry> entries() { return entries; }
}
