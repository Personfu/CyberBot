package nezz.dreambot.master.profile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered list of phases the bot will execute on a fresh account. Mirrors the
 * "account builder" model used by SlugBuilder / SubAccountBuilder / HowF2PAIO:
 * tutorial → starter quests → priority skills → secondary skills → diaries.
 *
 * <p>Phases are processed by {@link nezz.dreambot.master.tasks.BuildPlanTask}
 * which converts the next phase into a runnable Task and enqueues it.</p>
 */
public final class BuildPlan {

    public enum PhaseType {
        TUTORIAL,            // Tutorial Island
        QUEST,               // single quest
        QUEST_BATCH,         // multiple quests
        SKILL_LEVEL,         // train skill to target level
        SKILL_XP,            // train skill to target xp
        MONEY_MAKING,        // run a MoneyRoute until GP target reached
        MINIGAME,            // run minigame N times
        MULE,                // mule items to alt
        BOND,                // buy bond
        DIARY,               // diary task block
        WAIT                 // hold until external signal
    }

    public static final class Phase {
        public final PhaseType type;
        public final String  target;     // skill name, quest name, etc.
        public final int     value;      // level / xp / count
        public final Map<String, Object> options = new LinkedHashMap<>();
        public boolean done;

        public Phase(PhaseType type, String target, int value) {
            this.type = type;
            this.target = target;
            this.value = value;
        }

        public Phase opt(String k, Object v) { options.put(k, v); return this; }

        @Override public String toString() {
            return type + (target.isEmpty() ? "" : " " + target)
                    + (value > 0 ? " (" + value + ")" : "")
                    + (done ? " [DONE]" : "");
        }
    }

    private final List<Phase> phases = new ArrayList<>();
    private int cursor = 0;

    public BuildPlan add(Phase p) { phases.add(p); return this; }

    /** Full F2P greedy route — quests → money → route to 99 everything. */
    public static BuildPlan defaultF2P() {
        BuildPlan p = new BuildPlan();

        // ── 0. Tutorial Island ─────────────────────────────────────────────
        p.add(new Phase(PhaseType.TUTORIAL, "Tutorial Island", 0));

        // ── 1. Starter quests (unlock skills, QP gating) ──────────────────
        p.add(new Phase(PhaseType.QUEST, "Cook's Assistant",    0)); //  1 QP
        p.add(new Phase(PhaseType.QUEST, "Sheep Shearer",       0)); //  1 QP
        p.add(new Phase(PhaseType.QUEST, "X Marks the Spot",    0)); //  1 QP (unlock Zeah, easy)
        p.add(new Phase(PhaseType.QUEST, "Rune Mysteries",      0)); //  1 QP (unlock RC)
        p.add(new Phase(PhaseType.QUEST, "Restless Ghost",      0)); //  1 QP (Prayer XP)
        p.add(new Phase(PhaseType.QUEST, "Witch's Potion",      0)); //  1 QP
        p.add(new Phase(PhaseType.QUEST, "Imp Catcher",         0)); //  1 QP (Magic XP)
        p.add(new Phase(PhaseType.QUEST, "Ides of Milk",        0)); //  1 QP (unlock Brutus)
        p.add(new Phase(PhaseType.QUEST, "Below Ice Mountain",  0)); //  1 QP (Camdozaal access)
        p.add(new Phase(PhaseType.QUEST, "Romeo and Juliet",    0)); //  5 QP
        p.add(new Phase(PhaseType.QUEST, "Ernest the Chicken",  0)); //  4 QP
        p.add(new Phase(PhaseType.QUEST, "Goblin Diplomacy",    0)); //  5 QP
        p.add(new Phase(PhaseType.QUEST, "Vampyre Slayer",      0)); //  3 QP
        p.add(new Phase(PhaseType.QUEST, "The Corsair Curse",   0)); //  2 QP (easy)
        p.add(new Phase(PhaseType.QUEST, "Misthalin Mystery",   0)); //  1 QP

        // ── 2. Early money: chickens → bank feathers → sell ───────────────
        p.add(new Phase(PhaseType.MONEY_MAKING, "ChickenRoute", 10_000).opt("gpTarget", 10_000));

        // ── 3. Brutus combat training (fast, free food, cowhide money) ───────
        // Brutus unlocked by Ides of Milk quest above. Regular cows as fallback.
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus", 20));  // gets Attack to ~20 via Brutus
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",   20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength", 20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",  20));

        // ── 4. Doric's Quest (free pickaxe + Mining XP) ───────────────────
        p.add(new Phase(PhaseType.QUEST, "Doric's Quest",    0)); //  1 QP

        // ── 5. Early money: cowhide + sell at GE ──────────────────────────
        p.add(new Phase(PhaseType.MONEY_MAKING, "CowhideRoute", 50_000).opt("gpTarget", 50_000));

        // ── 6. Mid quests (need ~12 QP for BKF) ───────────────────────────
        p.add(new Phase(PhaseType.QUEST, "Pirate's Treasure",       0)); //  2 QP
        p.add(new Phase(PhaseType.QUEST, "Prince Ali Rescue",       0)); //  3 QP
        p.add(new Phase(PhaseType.QUEST, "Black Knights' Fortress", 0)); //  3 QP  (needs 12 QP)

        // ── 7. Knight's Sword — massive Smithing XP jump to 29 ──────────
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",   15)); // need 10 Mining for KS
        p.add(new Phase(PhaseType.QUEST, "The Knight's Sword", 0)); // 1 QP, 12,725 Smithing XP

        // ── 8. Flax spin — best F2P passive money (~112k gp/hr) ──────────
        p.add(new Phase(PhaseType.MONEY_MAKING, "FlaxSpinRoute", 200_000).opt("gpTarget", 200_000));

        // ── 9. Build combat to wear rune armour + fight Elvarg ────────────
        // Use Brutus for efficient F2P combat XP + cowhide money
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",  40));  // Brutus trains all combat
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",     40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",   40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",    40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Hitpoints",  50));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Prayer",     31));  // protect from magic

        // ── 10. Demon Slayer + Shield of Arrav ────────────────────────────
        p.add(new Phase(PhaseType.QUEST, "Demon Slayer",    0)); //  3 QP
        p.add(new Phase(PhaseType.QUEST, "Shield of Arrav", 0)); //  1 QP (co-op — stub)

        // ── 11. Dragon Slayer I — end-game F2P quest ─────────────────────
        p.add(new Phase(PhaseType.QUEST, "Dragon Slayer I", 0)); //  2 QP → rune platebody

        // ── 12. Route to 99 — gather skills (using processing loops) ──────
        // Mining → coal + iron (feed into SmithingModule)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       30));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Smithing",     30)); // KS boost helps
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Smithing",     60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Smithing",     99));

        // Woodcutting + Firemaking (burn logs while waiting)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   99));

        // Fishing → Cooking (cook every fish batch)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      99));

        // Yew log money route once 60 WC
        p.add(new Phase(PhaseType.MONEY_MAKING, "YewLogsRoute", 500_000).opt("gpTarget", 500_000));

        // Crafting — leather → gems
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Crafting",     40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Crafting",     99));

        // Fletching
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fletching",    50));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fletching",    99));

        // Runecrafting (unlock via Rune Mysteries earlier)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Runecrafting", 44));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Runecrafting", 99));

        // Thieving
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Thieving",     40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Thieving",     99));

        // Combat to 99 — use Brutus for efficient F2P training
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",   70));  // grind Brutus to 70 combat
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",       70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",     70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",      70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Ranged",       70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Magic",        70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",   99));  // final Brutus grind
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",       99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",     99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",      99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Ranged",       99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Magic",        99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Prayer",       99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Hitpoints",    99));

        return p;
    }

    public Phase current() {
        while (cursor < phases.size() && phases.get(cursor).done) cursor++;
        return cursor < phases.size() ? phases.get(cursor) : null;
    }

    public void advance() {
        if (cursor < phases.size()) {
            phases.get(cursor).done = true;
            cursor++;
        }
    }

    public List<Phase> phases() { return phases; }
    public int cursor() { return cursor; }
    public boolean isComplete() { return current() == null; }
}
