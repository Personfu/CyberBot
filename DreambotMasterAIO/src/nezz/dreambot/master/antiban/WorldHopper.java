package nezz.dreambot.master.antiban;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.world.World;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Hops F2P worlds on a randomised schedule (8-25 min) and on demand
 * (e.g. when a resource spot is contested or a player has been watching).
 *
 * <p>Keeps the account off a single world long enough to sidestep the
 * "player X always at this rock" pattern that flags manual reviewers.</p>
 */
public final class WorldHopper {

    private static final int MIN_HOP_MS = 8  * 60 * 1_000;
    private static final int MAX_HOP_MS = 28 * 60 * 1_000;

    private long nextHopAt;

    public WorldHopper() { scheduleNext(); }

    /** True when the scheduled hop window has arrived. */
    public boolean shouldHop() { return System.currentTimeMillis() >= nextHopAt; }

    /** Force a hop on the next shouldHop() check. */
    public void flagContested() { nextHopAt = 0; }

    /**
     * Hop to a random non-members, non-pvp F2P world.
     * @return true if hop was initiated
     */
    public boolean hop() {
        try {
            World current = Worlds.getCurrent();
            List<World> candidates = Worlds.all().stream()
                .filter(w -> w != null
                    && !w.isMembers()
                    && !w.isPVP()
                    && !w.isDeadmanMode()
                    && !w.isHighRisk()
                    && w.getPopulation() < 1_200
                    && (current == null || w.getRealId() != current.getRealId()))
                .collect(Collectors.toList());

            if (candidates.isEmpty()) return false;

            World target = candidates.get(Calculations.random(0, candidates.size() - 1));
            org.dreambot.api.methods.worldhopper.WorldHopper.hopWorld(target.getRealId());
            scheduleNext();
            return true;
        } catch (Throwable ignored) {
            scheduleNext();
            return false;
        }
    }

    private void scheduleNext() {
        nextHopAt = System.currentTimeMillis()
                + Calculations.random(MIN_HOP_MS, MAX_HOP_MS);
    }
}


