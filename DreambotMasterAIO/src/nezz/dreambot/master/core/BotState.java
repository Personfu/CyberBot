package nezz.dreambot.master.core;

/**
 * Top-level state of the MasterAIO state machine.
 *
 * <p>The bot transitions between these states in {@link MasterAIO#onLoop()} based on
 * task readiness, login status, and break-manager output. Sub-tasks have their own
 * inner states; this enum only captures the outermost lifecycle.</p>
 */
public enum BotState {
    IDLE,
    LOGGING_IN,
    TUTORIAL,
    QUESTING,
    SKILLING,
    COMBAT,
    BANKING,
    WALKING,
    EATING,
    BREAKING,
    MULING,
    MONEY_MAKING,
    STOPPED
}
