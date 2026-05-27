package nezz.dreambot.master.util;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.List;
import java.util.stream.Collectors;

/** Combat utility methods used across skill modules. */
public final class CombatUtil {

    private CombatUtil() {}

    // ─────────────────────────────────────────────────────────────────────────
    // Combat level calculator (Kaze / SlugHub)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the approximate combat level contributed by the given style.
     * Uses the official OSRS formula:
     * <pre>
     *   base   = 0.25 * (defence + hitpoints + floor(prayer / 2))
     *   melee  = 0.325 * (attack + strength)
     *   ranged = 0.325 * floor(ranged * 1.5)
     *   magic  = 0.325 * floor(magic  * 1.5)
     * </pre>
     *
     * @param style "melee", "ranged", or "magic" (case-insensitive)
     * @return combat level estimate, or -1 for unknown style
     */
    public static int getRoughCombatLevel(String style) {
        int attack    = Skills.getRealLevel(Skill.ATTACK);
        int strength  = Skills.getRealLevel(Skill.STRENGTH);
        int defence   = Skills.getRealLevel(Skill.DEFENCE);
        int hitpoints = Skills.getRealLevel(Skill.HITPOINTS);
        int prayer    = Skills.getRealLevel(Skill.PRAYER);
        int ranged    = Skills.getRealLevel(Skill.RANGED);
        int magic     = Skills.getRealLevel(Skill.MAGIC);

        double base = 0.25 * (defence + hitpoints + Math.floor(prayer / 2.0));
        double combat;
        switch (style.toLowerCase()) {
            case "melee":
                combat = 0.325 * (attack + strength);
                break;
            case "ranged":
                combat = 0.325 * Math.floor(ranged * 1.5);
                break;
            case "magic":
                combat = 0.325 * Math.floor(magic * 1.5);
                break;
            default:
                return -1;
        }
        return (int) Math.floor(base + combat);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Moving-NPC click helper (Slug / SlugHub)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Per-module helper that tracks how long an NPC click attempt has been failing.
     * If the window (5–9 s, randomised per attempt) elapses without a successful click,
     * the helper hops a random F2P world — interrupting the failed click attempt in a
     * way that looks natural to a human player.
     *
     * <p>Instantiate one instance per combat module (i.e. as a class field).</p>
     *
     * <pre>
     *   // In your module:
     *   private final CombatUtil.NpcClickHelper clicker = new CombatUtil.NpcClickHelper();
     *
     *   // In your attack method:
     *   if (clicker.tryClick(giant, "Attack")) {
     *       return ATTACK_WAIT;
     *   }
     *   return SHORT_WAIT;
     * </pre>
     */
    public static final class NpcClickHelper {

        private long lastSuccessMs = 0;
        private int  hopWindowMs;

        public NpcClickHelper() { resetWindow(); }

        /**
         * Attempt to interact with {@code npc} using {@code action}.
         * Hops if we've been failing to click beyond the random window.
         *
         * @return true if the click was registered
         */
        public boolean tryClick(NPC npc, String action) {
            if (npc == null) return false;

            if (npc.interact(action)) {
                lastSuccessMs = System.currentTimeMillis();
                resetWindow();
                return true;
            }

            // Haven't clicked yet — check if we've exceeded our patience window
            if (lastSuccessMs > 0
                    && System.currentTimeMillis() - lastSuccessMs > hopWindowMs) {
                hopF2P();
                lastSuccessMs = System.currentTimeMillis();
                resetWindow();
            }
            return false;
        }

        private void resetWindow() {
            hopWindowMs = Calculations.random(5_000, 9_000);
        }

        private void hopF2P() {
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

                if (!candidates.isEmpty()) {
                    World target = candidates.get(
                        Calculations.random(0, candidates.size() - 1));
                    org.dreambot.api.methods.worldhopper.WorldHopper.hopWorld(target.getRealId());
                }
            } catch (Throwable ignored) {}
        }
    }
}
