package nezz.dreambot.master.skills;

import org.dreambot.api.methods.skills.Skill;

/**
 * Contract for a single skill's training logic. Each module owns:
 * <ul>
 *   <li>which DreamBot {@link Skill} it advances,</li>
 *   <li>a list of training "methods" (e.g. Mining: Tin/Iron/Coal/MLM),</li>
 *   <li>logic to pick the best method for a given current/target level,</li>
 *   <li>a single tick of training execution.</li>
 * </ul>
 *
 * <p>Concrete implementations live in {@code skills.impl} and are registered
 * in {@link SkillRegistry}.</p>
 */
public abstract class SkillModule {

    public abstract String name();
    public abstract Skill  skill();

    /** Names of supported training methods, in suggested order by level. */
    public abstract String[] methods();

    /** Pick the best method for current level vs. target. */
    public abstract String pickMethod(int currentLevel, int targetLevel);

    /** Do one tick of training; return the sleep value. */
    public abstract int tick(String method);

    /** Optional: items the player needs in inventory/bank to train this method. */
    public String[] requiredItems(String method) { return new String[0]; }
}
