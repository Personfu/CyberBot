package nezz.dreambot.master.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.function.BooleanSupplier;

/**
 * Single step in a quest. Steps know how to execute themselves (one tick),
 * report when they're done, and provide a short label for the paint overlay.
 *
 * <p>Quest-Helper exposes a rich step hierarchy
 * ({@code NpcStep}, {@code ObjectStep}, {@code DetailedQuestStep}, ...);
 * here we keep it pragmatic with a small set of builder helpers — quest
 * authors compose them in the {@link Quest} subclass.</p>
 */
public abstract class QuestStep {
    public abstract String label();
    public abstract boolean isDone();
    public abstract void tick();

    // ── builders ─────────────────────────────────────────────────────────────

    public static QuestStep talkTo(String npcName, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return "talk-to:" + npcName; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() {
                if (Dialogues.canContinue()) { Dialogues.clickContinue(); return; }
                NPC npc = NPCs.closest(npcName);
                if (npc == null) return;
                if (!npc.isOnScreen()) { Walking.walk(npc); return; }
                if (npc.interact("Talk-to")) {
                    Sleep.sleepUntil(Dialogues::canContinue, 1500);
                }
            }
        };
    }

    public static QuestStep selectOption(String option, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return "select:" + option; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() {
                if (Dialogues.areOptionsAvailable()) {
                    Dialogues.clickOption(option);
                } else if (Dialogues.canContinue()) {
                    Dialogues.clickContinue();
                }
            }
        };
    }

    public static QuestStep walkTo(Tile dest, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return "walk:" + dest; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() { Walking.walk(dest); }
        };
    }

    public static QuestStep interactObject(String name, String action, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return action + ":" + name; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() {
                GameObject g = GameObjects.closest(name);
                if (g == null) return;
                if (!g.isOnScreen()) { Walking.walk(g.getTile()); return; }
                g.interact(action);
            }
        };
    }

    public static QuestStep useItemOnObject(String item, String object, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return "use:" + item + "->" + object; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() {
                if (!Inventory.isItemSelected()) {
                    Inventory.interact(item, "Use");
                    return;
                }
                GameObject g = GameObjects.closest(object);
                if (g != null) g.interact("Use");
            }
        };
    }

    public static QuestStep useItemOnNpc(String item, String npc, BooleanSupplier doneCheck) {
        return new QuestStep() {
            @Override public String label() { return "use:" + item + "->" + npc; }
            @Override public boolean isDone() { return doneCheck.getAsBoolean(); }
            @Override public void tick() {
                if (!Inventory.isItemSelected()) {
                    Inventory.interact(item, "Use");
                    return;
                }
                NPC n = NPCs.closest(npc);
                if (n != null) n.interact("Use");
            }
        };
    }

    public static QuestStep noop(String label) {
        return new QuestStep() {
            @Override public String label() { return "noop:" + label; }
            @Override public boolean isDone() { return true; }
            @Override public void tick() { }
        };
    }
}
