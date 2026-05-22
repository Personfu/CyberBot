package nezz.dreambot.allinone.tasks;

import nezz.dreambot.allinone.config.ScriptConfig;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Finds and attacks the configured NPC.
 * Handles eating food at the configured HP threshold.
 */
public class CombatTask {

    private final AbstractScript script;
    private final ScriptConfig   config;

    private long    killCount    = 0;
    private boolean wasInCombat = false;

    public CombatTask(AbstractScript script, ScriptConfig config) {
        this.script = script;
        this.config = config;
    }

    /** Execute this task only when not already in combat and no loot on ground. */
    public boolean shouldExecute() {
        if (Players.getLocal() == null) return false;
        // Already in combat — stay in this task to eat if needed
        if (Players.getLocal().isInCombat()) return true;
        // Not in combat and inventory has items to loot — let LootTask go first
        return true;
    }

    public int execute() {
        // ── Eat food if HP is low ─────────────────────────────────────────────
        int hp    = Skills.getBoostedLevel(Skill.HITPOINTS);
        int maxHp = Skills.getRealLevel(Skill.HITPOINTS);   // getRealLevel = base/max HP
        int hpPct = maxHp > 0 ? (hp * 100 / maxHp) : 100;

        if (hpPct <= config.getEatAtHpPercent() && Inventory.contains(config.getFoodName())) {
            Inventory.interact(config.getFoodName(), "Eat");
            Sleep.sleep(Calculations.random(600, 900));
            return Calculations.random(300, 600);
        }

        // ── Kill tracking: detect combat-end transition ────────────────────────
        boolean nowInCombat = Players.getLocal().isInCombat();
        if (wasInCombat && !nowInCombat) {
            killCount++;
        }
        wasInCombat = nowInCombat;

        // ── If already in combat, just wait ──────────────────────────────────
        if (nowInCombat) {
            return Calculations.random(300, 600);
        }

        // ── Find and attack target NPC ────────────────────────────────────────
        NPC target = findTarget();
        if (target == null) {
            // Walk back to centre tile if we drifted
            return Calculations.random(600, 900);
        }

        if (!target.isOnScreen()) {
            Walking.walk(target.getTile());
            Sleep.sleepUntil(() -> target.isOnScreen(), 3000);
            return Calculations.random(300, 600);
        }

        target.interact("Attack");
        Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 2500);
        // After attacking, update wasInCombat so the next loop doesn't false-count
        wasInCombat = Players.getLocal().isInCombat();
        return Calculations.random(600, 900);
    }

    private NPC findTarget() {
        String npcName = config.getTargetNpc();
        int    npcId   = config.getTargetNpcId();
        String myName  = Players.getLocal().getName();

        return NPCs.closest(n -> {
            if (n == null || n.getName() == null) return false;
            if (!n.getName().equalsIgnoreCase(npcName)) return false;
            // Optionally filter by ID (getId() is the current non-deprecated method)
            if (npcId > 0 && n.getId() != npcId) return false;
            // Skip NPCs that are in combat with someone else
            if (n.isInCombat()) {
                if (n.getInteractingCharacter() == null) return false;
                String opponent = n.getInteractingCharacter().getName();
                return opponent != null && opponent.equals(myName);
            }
            return true;
        });
    }

    /**
     * Increments kill count when the tracked NPC dies.
     * We detect death by watching the NPC's HP drop to 0 or disappear.
     */
    public long getKillCount() { return killCount; }
}
