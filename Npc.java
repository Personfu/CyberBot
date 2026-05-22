package com.cyberscape.rsps317.model;

public class Npc {
    private final String name;
    private final int id;
    private final int combatLevel;
    private final int hitpoints;
    private final int killTimeSeconds;

    public Npc(String name, int id, int combatLevel, int hitpoints, int killTimeSeconds) {
        this.name = name;
        this.id = id;
        this.combatLevel = combatLevel;
        this.hitpoints = hitpoints;
        this.killTimeSeconds = killTimeSeconds;
    }

    public String name() { return name; }
    public int id() { return id; }
    public int combatLevel() { return combatLevel; }
    public int hitpoints() { return hitpoints; }
    public int killTimeSeconds() { return killTimeSeconds; }
}
