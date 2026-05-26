package nezz.dreambot.master.skills;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.tasks.Task;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

/**
 * Runs a {@link SkillModule} until the configured target level is reached.
 * Picks an appropriate sub-method per current level via
 * {@link SkillModule#pickMethod(int, int)} so progression chains (Tin→Iron→
 * Coal as Mining levels up) "just work".
 */
public final class SkillTask extends Task {

    private final SkillModule module;
    private final int targetLevel;
    private final Profile profile;
    private final Logger log;
    private String currentMethod;
    private boolean done;
    private int lastLogged = -1;

    public SkillTask(SkillModule module, int targetLevel, Profile profile, Logger log) {
        this.module = module;
        this.targetLevel = targetLevel;
        this.profile = profile;
        this.log = log;
    }

    @Override public int priority() { return 80; }
    @Override public BotState state() { return BotState.SKILLING; }
    @Override public boolean isReady() { return !done && Client.isLoggedIn(); }
    @Override public boolean isComplete() { return done; }

    @Override public int execute() {
        int current = Skills.getRealLevel(module.skill());
        if (current != lastLogged) {
            log.info("[" + module.name() + "] level " + current + " / " + targetLevel);
            lastLogged = current;
        }
        if (current >= targetLevel) {
            log.info("[" + module.name() + "] target reached.");
            done = true;
            return 600;
        }
        String method = module.pickMethod(current, targetLevel);
        if (method != null && !method.equals(currentMethod)) {
            log.info("[" + module.name() + "] switching to method: " + method);
            currentMethod = method;
        }
        int sleep = module.tick(currentMethod);
        return Math.max(200, sleep);
    }

    @Override public String label() {
        return "skill:" + module.name() + ":" + (currentMethod == null ? "?" : currentMethod);
    }

    public Skill skill() { return module.skill(); }
}
