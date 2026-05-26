package nezz.dreambot.master.tasks;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.money.MoneyRouteRegistry;
import nezz.dreambot.master.money.MoneyRouteTask;
import nezz.dreambot.master.profile.BuildPlan;
import nezz.dreambot.master.profile.Profile;
import nezz.dreambot.master.quests.QuestRegistry;
import nezz.dreambot.master.quests.QuestTask;
import nezz.dreambot.master.skills.SkillRegistry;
import nezz.dreambot.master.skills.SkillTask;

/**
 * High-level driver: walks the {@link BuildPlan} and, for each phase, materializes
 * a runnable {@link Task} (TutorialIslandTask, QuestTask, SkillTask, ...) and adds
 * it to the scheduler. Self-removes when the plan completes.
 */
public final class BuildPlanTask extends Task {

    private final Profile profile;
    private final TaskScheduler scheduler;
    private final Logger log;
    private Task currentSubtask;

    public BuildPlanTask(Profile profile, TaskScheduler scheduler, Logger log) {
        this.profile = profile;
        this.scheduler = scheduler;
        this.log = log;
    }

    @Override public int priority() { return 100; }
    @Override public BotState state() {
        if (currentSubtask != null) return currentSubtask.state();
        BuildPlan.Phase p = profile.plan.current();
        if (p == null) return BotState.IDLE;
        switch (p.type) {
            case TUTORIAL:    return BotState.TUTORIAL;
            case QUEST:
            case QUEST_BATCH: return BotState.QUESTING;
            case SKILL_LEVEL:
            case SKILL_XP:    return BotState.SKILLING;
            default:          return BotState.IDLE;
        }
    }

    @Override public boolean isReady() {
        return !profile.plan.isComplete();
    }

    @Override public int execute() {
        // If we already spawned a subtask, let it finish.
        if (currentSubtask != null) {
            if (currentSubtask.isComplete()) {
                log.info("Phase complete: " + profile.plan.current());
                profile.plan.advance();
                scheduler.remove(currentSubtask);
                currentSubtask = null;
            }
            return 500;
        }

        BuildPlan.Phase p = profile.plan.current();
        if (p == null) return 600;

        log.info("Starting phase: " + p);
        switch (p.type) {
            case TUTORIAL:
                currentSubtask = new TutorialTask(profile, log);
                break;
            case QUEST:
                currentSubtask = QuestRegistry.byName(p.target)
                        .map(q -> (Task) new QuestTask(q, log))
                        .orElseGet(() -> new NoopTask("quest:" + p.target));
                break;
            case SKILL_LEVEL:
                currentSubtask = SkillRegistry.byName(p.target)
                        .map(s -> (Task) new SkillTask(s, p.value, profile, log))
                        .orElseGet(() -> new NoopTask("skill:" + p.target));
                break;
            case MONEY_MAKING: {
                String routeId = p.target; // e.g. "ChickenRoute"
                long gpTarget = 0;
                Object gpOpt = p.options.get("gpTarget");
                if (gpOpt instanceof Number) gpTarget = ((Number) gpOpt).longValue();
                currentSubtask = new MoneyRouteTask(routeId, gpTarget, log);
                break;
            }
            default:
                log.warn("Phase type " + p.type + " not yet implemented, skipping.");
                profile.plan.advance();
                return 200;
        }
        scheduler.add(currentSubtask);
        return 200;
    }

    @Override public String label() {
        BuildPlan.Phase p = profile.plan.current();
        return "BuildPlan: " + (p == null ? "DONE" : p.toString());
    }

    /** Placeholder for not-yet-implemented phases — completes immediately. */
    private static final class NoopTask extends Task {
        private final String name; private boolean done;
        NoopTask(String name) { this.name = name; }
        @Override public int priority() { return 90; }
        @Override public BotState state() { return BotState.IDLE; }
        @Override public boolean isReady() { return !done; }
        @Override public int execute() { done = true; return 200; }
        @Override public boolean isComplete() { return done; }
        @Override public String label() { return "noop:" + name; }
    }
}
