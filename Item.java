package com.cyberscape.rsps317.model;

public class Item {
    private final String name;
    private final int id;
    private final long gePrice;
    private final boolean stackable;
    private final boolean untradeable;

    public Item(String name, int id, long gePrice, boolean stackable, boolean untradeable) {
        this.name = name;
        this.id = id;
        this.gePrice = gePrice;
        this.stackable = stackable;
        this.untradeable = untradeable;
    }

    public String name() { return name; }
    public int id() { return id; }
    public long gePrice() { return gePrice; }
    public boolean stackable() { return stackable; }
    public boolean untradeable() { return untradeable; }
}
