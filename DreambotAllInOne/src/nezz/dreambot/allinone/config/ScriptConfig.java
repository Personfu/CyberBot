package nezz.dreambot.allinone.config;

import java.util.ArrayList;
import java.util.List;

/**
 * All user-configurable settings for the All-In-One script.
 * Populated by the GUI before the script starts, then read by the task classes.
 */
public class ScriptConfig {

    // ── Combat ────────────────────────────────────────────────────────────────
    private String  targetNpc         = "Gargoyle";
    private int     targetNpcId       = 1543;        // OSRS NPC ID (optional filter)
    private String  attackStyle       = "MELEE";     // "MELEE", "RANGED", "MAGIC"
    private String  foodName          = "Shark";
    private int     eatAtHpPercent    = 40;          // eat when HP% drops below this
    private int     minFoodCount      = 4;           // bank when food drops below this

    // ── Loot ─────────────────────────────────────────────────────────────────
    private boolean lootEnabled       = true;
    private int     lootRadiusTiles   = 8;           // only loot within N tiles of kill spot
    /** Live loot configuration — entries are cloned from NpcLootProfile and edited by user. */
    private final List<LootEntry> lootEntries = new ArrayList<>();

    // ── Banking ───────────────────────────────────────────────────────────────
    private boolean bankWhenFull      = true;
    private int     maxBankDistance   = 60;          // max tiles to walk to bank

    // ── Drop rate simulator (Rates tab) ──────────────────────────────────────
    private String  simNpcName        = "Gargoyle";
    private int     simKills          = 1000;
    private double  simDropRateMulti  = 1.0;         // 1.0 = OSRS authentic, 5.0 = 5× rates
    private double  simRareMulti      = 1.0;

    // ── SDN submission fields (read-only display in SDN tab) ─────────────────
    private String  sdnScriptRepo     = "https://github.com/Personfu/CyberBot/";
    private String  sdnScriptModule   = "nezz.dreambot.allinone.AllInOne";
    private String  sdnParameters     = "";

    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    public String  getTargetNpc()          { return targetNpc; }
    public void    setTargetNpc(String v)  { this.targetNpc = v; }

    public int     getTargetNpcId()        { return targetNpcId; }
    public void    setTargetNpcId(int v)   { this.targetNpcId = v; }

    public String  getAttackStyle()        { return attackStyle; }
    public void    setAttackStyle(String v){ this.attackStyle = v; }

    public String  getFoodName()           { return foodName; }
    public void    setFoodName(String v)   { this.foodName = v; }

    public int     getEatAtHpPercent()     { return eatAtHpPercent; }
    public void    setEatAtHpPercent(int v){ this.eatAtHpPercent = Math.max(10, Math.min(80, v)); }

    public int     getMinFoodCount()       { return minFoodCount; }
    public void    setMinFoodCount(int v)  { this.minFoodCount = Math.max(0, v); }

    public boolean isLootEnabled()         { return lootEnabled; }
    public void    setLootEnabled(boolean v){ this.lootEnabled = v; }

    public int     getLootRadiusTiles()    { return lootRadiusTiles; }
    public void    setLootRadiusTiles(int v){ this.lootRadiusTiles = Math.max(1, v); }

    public List<LootEntry> getLootEntries(){ return lootEntries; }

    /**
     * Resets loot entries to the default drop table for the current target NPC.
     * Called whenever the user changes the target NPC in the GUI.
     */
    public void resetLootEntriesToDefaults() {
        lootEntries.clear();
        for (LootEntry e : NpcLootProfile.getDropTable(targetNpc)) {
            lootEntries.add(new LootEntry(
                e.getItemName(), e.getItemId(),
                e.getMinQty(), e.getMaxQty(),
                e.getRateNumerator(), e.getRateDenominator()
            ));
        }
    }

    public boolean isBankWhenFull()          { return bankWhenFull; }
    public void    setBankWhenFull(boolean v){ this.bankWhenFull = v; }

    public int     getMaxBankDistance()      { return maxBankDistance; }
    public void    setMaxBankDistance(int v) { this.maxBankDistance = Math.max(1, v); }

    public String  getSimNpcName()           { return simNpcName; }
    public void    setSimNpcName(String v)   { this.simNpcName = v; }

    public int     getSimKills()             { return simKills; }
    public void    setSimKills(int v)        { this.simKills = Math.max(1, Math.min(1_000_000, v)); }

    public double  getSimDropRateMulti()     { return simDropRateMulti; }
    public void    setSimDropRateMulti(double v){ this.simDropRateMulti = Math.max(0.01, v); }

    public double  getSimRareMulti()         { return simRareMulti; }
    public void    setSimRareMulti(double v) { this.simRareMulti = Math.max(0.01, v); }

    public String  getSdnScriptRepo()        { return sdnScriptRepo; }
    public void    setSdnScriptRepo(String v){ this.sdnScriptRepo = v; }

    public String  getSdnScriptModule()      { return sdnScriptModule; }
    public void    setSdnScriptModule(String v){ this.sdnScriptModule = v; }

    public String  getSdnParameters()        { return sdnParameters; }
    public void    setSdnParameters(String v){ this.sdnParameters = v; }
}
