package com.cyberscape.rsps317.model;

public class DropEntry {
    private final String itemName;
    private final QuantityRange quantity;
    private final int weight;
    private final boolean rare;

    public DropEntry(String itemName, QuantityRange quantity, int weight, boolean rare) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.weight = weight;
        this.rare = rare;
    }

    public String itemName() { return itemName; }
    public QuantityRange quantity() { return quantity; }
    public int weight() { return weight; }
    public boolean rare() { return rare; }
}
