package nezz.dreambot.master.profile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ordered list of phases the bot will execute on a fresh account. Mirrors the
 * "account builder" model used by SlugBuilder / SubAccountBuilder / HowF2PAIO:
 * tutorial â†’ starter quests â†’ priority skills â†’ secondary skills â†’ diaries.
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

    /** Full F2P greedy route â€” quests â†’ money â†’ route to 99 everything. */
    public static BuildPlan defaultF2P() {
        BuildPlan p = new BuildPlan();

        // PHASE 0: Tutorial Island
        p.add(new Phase(PhaseType.TUTORIAL, "Tutorial Island", 0));

        // PHASE 1: Zero-req starter quests (8 QP)
        p.add(new Phase(PhaseType.QUEST, "Cook's Assistant",   0));
        p.add(new Phase(PhaseType.QUEST, "Sheep Shearer",      0));
        p.add(new Phase(PhaseType.QUEST, "X Marks the Spot",   0));
        p.add(new Phase(PhaseType.QUEST, "Rune Mysteries",     0));
        p.add(new Phase(PhaseType.QUEST, "Restless Ghost",     0));
        p.add(new Phase(PhaseType.QUEST, "Witch's Potion",     0));
        p.add(new Phase(PhaseType.QUEST, "Ides of Milk",       0));
        p.add(new Phase(PhaseType.QUEST, "Below Ice Mountain", 0));

        // PHASE 2: Basic skill foundation (chickens/goblins)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",       5));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",     5));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",      5));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  15));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   15));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       15));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      15));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      15));

        // PHASE 3: Basic money making -> 25k gp
        p.add(new Phase(PhaseType.MONEY_MAKING, "chicken",    0).opt("gpTarget", 15_000));
        p.add(new Phase(PhaseType.MONEY_MAKING, "cowhide",    0).opt("gpTarget", 25_000));

        // PHASE 4: More starter quests (+17 QP -> 25 QP total)
        p.add(new Phase(PhaseType.QUEST, "Ernest the Chicken", 0));
        p.add(new Phase(PhaseType.QUEST, "Goblin Diplomacy",   0));
        p.add(new Phase(PhaseType.QUEST, "Romeo and Juliet",   0));
        p.add(new Phase(PhaseType.QUEST, "Imp Catcher",        0));
        p.add(new Phase(PhaseType.QUEST, "Doric's Quest",      0));
        p.add(new Phase(PhaseType.QUEST, "Misthalin Mystery",  0));

        // PHASE 5: Skill building (combat + gathering)
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",       20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",       20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",     20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",      20));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  30));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   30));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       30));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      30));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      30));

        // PHASE 6: F2P quest batch 1 (+10 QP -> 35 QP)
        p.add(new Phase(PhaseType.QUEST, "Pirate's Treasure",  0));
        p.add(new Phase(PhaseType.QUEST, "Prince Ali Rescue",  0));
        p.add(new Phase(PhaseType.QUEST, "Vampyre Slayer",     0));
        p.add(new Phase(PhaseType.QUEST, "The Corsair Curse",  0));

        // PHASE 7: Early money making
        p.add(new Phase(PhaseType.MONEY_MAKING, "cowhide",    0).opt("gpTarget", 100_000));
        p.add(new Phase(PhaseType.MONEY_MAKING, "flax_spin",  0).opt("gpTarget", 100_000));

        // PHASE 8: Combat for quests
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",       40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",       40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",     40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",      40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Hitpoints",    45));

        // PHASE 9: F2P quest batch 2 (+8 QP -> 43 QP)
        p.add(new Phase(PhaseType.QUEST, "Black Knights' Fortress", 0));
        p.add(new Phase(PhaseType.QUEST, "Demon Slayer",            0));
        p.add(new Phase(PhaseType.QUEST, "Shield of Arrav",         0));
        p.add(new Phase(PhaseType.QUEST, "The Knight's Sword",      0));

        // PHASE 10: Dragon Slayer I (needs 32 QP, have 43)
        p.add(new Phase(PhaseType.QUEST, "Dragon Slayer I", 0));

        // PHASE 11: Post-quest money making
        p.add(new Phase(PhaseType.MONEY_MAKING, "flax_spin",  0).opt("gpTarget", 300_000));
        p.add(new Phase(PhaseType.MONEY_MAKING, "cowhide",    0).opt("gpTarget", 200_000));

        // PHASE 12: Brutus main grind + Varrock Sewers Moss Giant safe-spot (primary anti-detection trainer)
        // MossGiant module navigates to Varrock Sewers manhole (~3289,3394), cuts cobwebs with knife,
        // safe-spots by the wall in the back room and casts the best available spell until Magic 55.
        // Big bones are buried every kill for free Prayer XP. No damage taken = no eating tells.
        p.add(new Phase(PhaseType.SKILL_LEVEL, "MossGiant", 55));  // Magic 1→55 via safe-spot
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Ranged",    50));  // ranged safe-spot same room
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",    70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",    70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",  70));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",   70));

        // PHASE 13: High skill building
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Smithing",     60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      60));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Crafting",     40));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fletching",    50));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Runecrafting", 44));

        // PHASE 14: Advanced money making
        p.add(new Phase(PhaseType.MONEY_MAKING, "steel_bars", 0).opt("gpTarget", 500_000));

        // PHASE 15: Max skills
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Mining",       99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Smithing",     99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Woodcutting",  99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Firemaking",   99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fishing",      99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Cooking",      99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Crafting",     99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Fletching",    99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Runecrafting", 99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Thieving",     99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Prayer",       43));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Prayer",       99));

        // PHASE 16: Endgame combat
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Brutus",    99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Attack",    99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Strength",  99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Defense",   99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Ranged",    99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Magic",     99));
        p.add(new Phase(PhaseType.SKILL_LEVEL, "Hitpoints", 99));

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
