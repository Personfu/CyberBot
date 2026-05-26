package nezz.dreambot.master.tasks;

import nezz.dreambot.master.core.BotState;
import nezz.dreambot.master.core.Logger;
import nezz.dreambot.master.id.NpcID;
import nezz.dreambot.master.id.ObjectID;
import nezz.dreambot.master.id.Varbits;
import nezz.dreambot.master.profile.Profile;
import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.awt.Rectangle;
import java.util.List;

/**
 * Tutorial Island as a MasterAIO Task. Drives the player through all
 * 12 sections by reading the tutorial progress varp (281) and selecting the
 * appropriate interaction for each value.
 *
 * <p>This refactors the standalone {@code TutorialIsland} script from
 * DreambotTutIsland into a scheduler-friendly Task that signals completion
 * via {@link #isComplete()} once the varp reaches the post-tutorial sentinel
 * (1000). The interaction code paths are kept faithful to the original since
 * they're battle-tested.</p>
 */
public final class TutorialTask extends Task {

    private final Profile profile;
    private final Logger  log;
    private boolean appearanceDone;
    private boolean completed;
    private long started = System.currentTimeMillis();

    public TutorialTask(Profile profile, Logger log) {
        this.profile = profile;
        this.log = log;
    }

    @Override public int priority() { return 80; }
    @Override public BotState state() { return BotState.TUTORIAL; }
    @Override public boolean isReady() { return !completed; }
    @Override public boolean isComplete() { return completed; }
    @Override public String label() { return "tutorial:varp=" + PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS); }

    @Override
    public int execute() {
        if (!Client.isLoggedIn()) return 1200;

        // Fast-path: tutorial already completed on this account.
        int prog = PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS);
        if (prog >= 1000) {
            log.info("Tutorial Island already complete — skipping phase.");
            completed = true;
            return 600;
        }

        // Click-through any "Click to continue" widget that bleeds across sections.
        eatClickToContinue();

        // If we're at the appearance interface and haven't finished, randomize and accept.
        if (!appearanceDone) {
            if (handleAppearance()) appearanceDone = true;
            return Calculations.random(180, 320);
        }

        // If a sidebar tab is being asked for, open it.
        int tabConfig = PlayerSettings.getConfig(Varbits.TUTORIAL_TAB_CONFIG);
        if (tabConfig > 0) {
            Tab t = tabFromConfig(tabConfig);
            if (t != null && Tabs.openWithMouse(t)) {
                Sleep.sleepUntil(() -> Tabs.isOpen(t), Calculations.random(800, 1400));
            }
            return Calculations.random(180, 320);
        }

        switch (prog) {
            case 0: case 7:                                   talkTo(NpcID.RUNESCAPE_GUIDE, "RuneScape Guide"); break;
            case 3:                                           /* opened settings */            break;
            case 10:                                          openDoor(); break;
            case 20: case 70:                                 talkTo(NpcID.SURVIVAL_EXPERT, "Survival Expert"); break;
            case 30:                                          Dialogues.clickContinue(); break;
            case 40:                                          chopTree(); break;
            case 50:                                          lightFire(); break;
            case 60:                                          /* opened skills */ break;
            case 80:                                          netFish(); break;
            case 90: case 100: case 110:                      cookShrimp(); break;
            case 120:                                         openGateAt(new Tile(3090, 3092, 0)); break;
            case 130:                                         openDoorTo(new Tile(3080, 3084, 0)); break;
            case 140:                                         talkTo(NpcID.MASTER_CHEF, "Master Chef"); break;
            case 150:                                         makeDough(); break;
            case 160:                                         bakeBread(); break;
            case 170:                                         /* opened music */ break;
            case 180:                                         openDoorTo(new Tile(3073, 3090, 0)); break;
            case 183:                                         /* opened emotes */ break;
            case 187:                                         doEmote(); break;
            case 190:                                         /* opened settings */ break;
            case 200:                                         enableRunSetting(); break;
            case 210:                                         openQuestDoor(); break;
            case 220: case 240:                               talkTo(NpcID.QUEST_GUIDE, "Quest Guide"); break;
            case 230:                                         /* open quest tab */ break;
            case 250:                                         climbDownLadder(); break;
            case 260: case 290: case 330:                     talkTo(NpcID.MINING_INSTRUCTOR, "Mining Instructor"); break;
            case 270:                                         prospect(new Tile(3077, 9504, 0)); break;
            case 280:                                         prospect(new Tile(3083, 9501, 0)); break;
            case 300:                                         mine(new Tile(3077, 9504, 0)); break;
            case 310:                                         mine(new Tile(3083, 9501, 0)); break;
            case 320:                                         smeltBronze(); break;
            case 340:                                         useBarOnAnvil(); break;
            case 350:                                         smithKnife(); break;
            case 360:                                         openGateGeneric(); break;
            case 370: case 410:                               talkTo(NpcID.COMBAT_INSTRUCTOR, "Combat Instructor"); break;
            case 390:                                         /* open equipment tab */ break;
            case 400:                                         openEquipmentStats(); break;
            case 405:                                         equipDagger(); break;
            case 420:                                         swapToSword(); break;
            case 430:                                         /* open combat tab */ break;
            case 440:                                         openRatGate(); break;
            case 450:                                         attackRatMelee(); break;
            case 460:                                         Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 460, 2400); break;
            case 470:                                         talkOrReopenGate(); break;
            case 480: case 490:                               rangeRat(); break;
            case 500:                                         climbCombatLadder(); break;
            case 510:                                         doBankBooth(); break;
            case 520:                                         doPollBooth(); break;
            case 525:                                         doSecondPollBooth(); break;
            case 530:                                         talkTo(NpcID.FINANCIAL_ADVISOR, "Financial Advisor"); break;
            case 540:                                         openDoorAt(new Tile(3130, 3124, 0)); break;
            case 550:                                         walkToPrayer(); break;
            case 560:                                         /* open prayer tab */ break;
            case 570: case 600:                               talkTo(NpcID.BROTHER_BRACE, "Brother Brace"); break;
            case 580:                                         /* open friends */ break;
            case 590:                                         /* open ignore */ break;
            case 610:                                         openDoorAtClosest("Door"); break;
            case 620:                                         walkToMage(); break;
            case 630:                                         /* open magic tab */ break;
            case 640:                                         talkTo(NpcID.MAGIC_INSTRUCTOR, "Magic Instructor"); break;
            case 650:                                         castWindStrike(); break;
            case 670:                                         finalChoice(); break;
            case 1000:
                long elapsed = System.currentTimeMillis() - started;
                log.info("Tutorial Island complete in " + (elapsed / 1000) + "s");
                completed = true;
                return 600;
        }
        return Calculations.random(180, 320);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Section handlers — split out for readability vs. the 1k-LOC original.
    // ────────────────────────────────────────────────────────────────────────

    private boolean handleAppearance() {
        Widget par = Widgets.getWidget(269);
        if (par == null || !par.isVisible()) return false;
        // Randomize roughly half the appearance categories, then accept.
        int[][] children = {{106,113},{107,114},{108,115},{109,116},{110,117},
                            {111,118},{112,119},{105,121},{123,127},{122,129},
                            {124,130},{125,131}};
        for (int[] pair : children) {
            if (Math.random() < 0.55) {
                int childId = pair[Math.random() < 0.5 ? 0 : 1];
                for (int i = 0; i < Calculations.random(2, 5); i++) {
                    par.getChild(childId).interact();
                    sleep(80, 140);
                }
            }
        }
        par.getChild(100).interact(); // ACCEPT
        Sleep.sleepUntil(() -> {
            WidgetChild wc = par.getChild(100);
            return wc == null || !wc.isVisible();
        }, 1200);
        return true;
    }

    private void talkTo(int npcId, String name) {
        if (!Dialogues.canContinue()) {
            NPC g = NPCs.closest(n -> n != null && (n.getId() == npcId
                    || name.equalsIgnoreCase(n.getName())));
            if (g != null) {
                if (g.isOnScreen()) {
                    if (g.interact("Talk-to")) walkSleep();
                } else {
                    Walking.walk(g);
                    walkSleep();
                }
            }
        } else {
            Dialogues.clickContinue();
            sleep(500, 800);
        }
    }

    private void chopTree() {
        GameObject tree = GameObjects.closest("Tree");
        if (tree == null) return;
        if (Players.getLocal().getAnimation() != -1) {
            Sleep.sleepUntil(() -> Inventory.contains("Logs"), Calculations.random(1000, 2000));
            return;
        }
        if (tree.interact("Chop down")) {
            walkSleep();
            Sleep.sleepUntil(() -> Inventory.contains("Logs"), Calculations.random(1000, 2500));
        }
    }

    private void lightFire() {
        if (!Inventory.contains("Logs")) { chopTree(); return; }
        if (!Inventory.isItemSelected()) {
            Inventory.interact("Tinderbox", "Use");
            Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        } else {
            Inventory.interact("Logs", "Use");
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() != -1, 1500);
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 8000);
        }
    }

    private void netFish() {
        if (Players.getLocal().getAnimation() != -1) {
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 80, 5000);
            return;
        }
        NPC spot = NPCs.closest("Fishing spot");
        if (spot != null && spot.interact("Net")) {
            walkSleep();
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 80,
                    Calculations.random(4000, 5000));
        }
    }

    private void cookShrimp() {
        GameObject fire = GameObjects.closest("Fire");
        if (fire == null) { lightFire(); return; }
        if (!Inventory.isItemSelected()) {
            if (Inventory.interact("Raw shrimps", "Use"))
                Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        }
        if (Inventory.isItemSelected() && fire.interact("Use")) {
            walkSleep();
            Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 4000);
        }
    }

    private void openGateAt(Tile dest) {
        if (Players.getLocal().distance(dest) > 5) {
            Walking.walk(dest);
            walkSleep();
            return;
        }
        GameObject gate = GameObjects.closest("Gate");
        if (gate != null && gate.interact("Open")) walkSleep();
    }

    private void openDoorTo(Tile dest) {
        if (Players.getLocal().distance(dest) > 5) {
            Walking.walk(dest);
            walkSleep();
            return;
        }
        GameObject d = GameObjects.closest("Door");
        if (d != null && d.interact("Open")) walkSleep();
    }

    private void openDoor() {
        if (!Walking.isRunEnabled()) Walking.toggleRun();
        GameObject d = GameObjects.closest("Door");
        if (d != null && d.interact("Open")) walkSleep();
    }

    private void makeDough() {
        if (!Inventory.isItemSelected()) {
            if (Inventory.interact("Bucket of water", "Use"))
                Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        } else {
            if (Inventory.interact("Pot of flour", "Use"))
                Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 1500);
        }
    }

    private void bakeBread() {
        if (!Inventory.isItemSelected()) {
            if (Inventory.interact("Bread dough", "Use"))
                Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        } else {
            GameObject range = GameObjects.closest("Range");
            if (range != null && range.interact("Use")) {
                walkSleep();
                Sleep.sleepUntil(() -> Inventory.contains("Bread"), 3000);
            }
        }
    }

    private void doEmote() {
        Rectangle r = new Rectangle(560, 213, 20, 40);
        Mouse.click(r);
        Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 187, 5000);
    }

    private void enableRunSetting() {
        WidgetChild wc = Widgets.get(261, 63);
        if (wc != null && wc.isVisible()) {
            wc.interact();
            Sleep.sleepUntil(Walking::isRunEnabled, 1200);
        }
    }

    private void openQuestDoor() {
        Tile target = new Tile(3086, 3126, 0);
        if (Players.getLocal().distance(target) > 5) { Walking.walk(target); walkSleep(); return; }
        GameObject d = GameObjects.closest("Door");
        if (d != null && d.interact("Open")) walkSleep();
    }

    private void climbDownLadder() {
        GameObject l = GameObjects.closest("Ladder");
        if (l != null && l.interact("Climb-down")) walkSleep();
    }

    private void prospect(Tile t) {
        GameObject rock = GameObjects.closest(g -> g != null && "Rocks".equals(g.getName()) && g.getTile().equals(t));
        if (rock != null && rock.interact("Prospect")) {
            walkSleep();
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS), 1500);
        }
    }

    private void mine(Tile t) {
        GameObject rock = GameObjects.closest(g -> g != null && "Rocks".equals(g.getName()) && g.getTile().equals(t));
        if (rock != null && rock.interact("Mine")) {
            walkSleep();
            Sleep.sleepUntil(() -> Inventory.contains("Tin ore") || Inventory.contains("Copper ore"), 3000);
        }
    }

    private void smeltBronze() {
        if (Dialogues.canContinue()) { Dialogues.clickContinue(); return; }
        if (!Inventory.isItemSelected()) {
            if (Inventory.interact("Tin ore", "Use"))
                Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        } else {
            GameObject f = GameObjects.closest(ObjectID.TUTORIAL_FURNACE);
            if (f != null && f.interact("Use")) {
                walkSleep();
                Sleep.sleepUntil(() -> Inventory.contains("Bronze bar"), 3000);
            }
        }
    }

    private void useBarOnAnvil() {
        if (!Inventory.isItemSelected()) {
            Inventory.interact("Bronze bar", "Use");
            Sleep.sleepUntil(Inventory::isItemSelected, 1200);
        } else {
            GameObject a = GameObjects.closest("Anvil");
            if (a != null && a.interact("Use")) walkSleep();
        }
    }

    private void smithKnife() {
        WidgetChild w = Widgets.get(312, 2);
        if (w != null) w.interact();
        Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 350, 3000);
    }

    private void openGateGeneric() {
        Tile t = new Tile(3094, 9502, 0);
        if (Players.getLocal().distance(t) > 5) { Walking.walk(t); walkSleep(); return; }
        GameObject g = GameObjects.closest("Gate");
        if (g != null && g.interact("Open")) walkSleep();
    }

    private void openEquipmentStats() {
        WidgetChild w = Widgets.get(387, 17);
        if (w != null) w.interact();
        Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 400, 1600);
    }

    private void equipDagger() {
        if (Inventory.interact("Bronze dagger", "Equip") || Inventory.interact("Bronze dagger", "Wield")) {
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 410, 1600);
        }
        WidgetChild w = Widgets.get(84, 4);
        if (w != null) w.interact();
    }

    private void swapToSword() {
        Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON.getSlot());
        if (weapon != null && weapon.getName().contains("dagger")) {
            Equipment.unequip(EquipmentSlot.WEAPON);
            return;
        }
        if (weapon != null) {
            Inventory.interact("Wooden shield", "Wield");
            Sleep.sleepUntil(() -> Equipment.getItemInSlot(EquipmentSlot.SHIELD.getSlot()) != null, 1600);
        } else {
            Inventory.interact("Bronze sword", "Wield");
            Sleep.sleepUntil(() -> Equipment.getItemInSlot(EquipmentSlot.WEAPON.getSlot()) != null, 1600);
        }
    }

    private void openRatGate() {
        Tile t = new Tile(3111, 9518, 0);
        if (Players.getLocal().distance(t) > 5) { Walking.walk(t); walkSleep(); return; }
        GameObject g = GameObjects.closest("Gate");
        if (g != null && g.interact("Open")) walkSleep();
    }

    private void attackRatMelee() {
        NPC rat = NPCs.closest(n -> n != null && "Giant rat".equals(n.getName()) && !n.isInCombat());
        if (rat != null) {
            if (rat.interact("Attack")) {
                walkSleep();
                Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 450, 2000);
            } else if (Camera.getPitch() < Calculations.random(150, 200)) {
                Camera.rotateToPitch(Calculations.random(200, 360));
            }
        }
    }

    private void talkOrReopenGate() {
        if (!Map.canReach(new Tile(3112, 9518, 0))) {
            GameObject g = GameObjects.closest("Gate");
            if (g != null && g.interact("Open")) walkSleep();
        } else {
            talkTo(NpcID.COMBAT_INSTRUCTOR, "Combat Instructor");
        }
    }

    private void rangeRat() {
        if (Equipment.isSlotEmpty(EquipmentSlot.ARROWS.getSlot())) {
            Inventory.interact("Bronze arrow", "Wield");
            Sleep.sleepUntil(() -> Equipment.isSlotFull(EquipmentSlot.ARROWS.getSlot()), 1500);
            return;
        }
        if (Inventory.contains("Shortbow")) {
            Inventory.interact("Shortbow", "Wield");
            Sleep.sleepUntil(() -> !Inventory.contains("Shortbow"), 1500);
            return;
        }
        if (Players.getLocal().getInteractingCharacter() != null) {
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 490, 3000);
            return;
        }
        NPC rat = NPCs.closest(n -> n != null && "Giant rat".equals(n.getName()) && !n.isInCombat());
        if (rat != null && rat.interact("Attack")) {
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 480, 3000);
        }
    }

    private void climbCombatLadder() {
        Tile dest = new Tile(3112, 9525, 0);
        if (Players.getLocal().distance(dest) > 5) { Walking.walk(dest); walkSleep(); return; }
        GameObject l = GameObjects.closest("Ladder");
        if (l != null && l.interact("Climb-up")) walkSleep();
    }

    private void doBankBooth() {
        Tile t = new Tile(3122, 3123, 0);
        if (Players.getLocal().distance(t) > 5) { Walking.walk(t); walkSleep(); return; }
        if (Dialogues.getOptionIndex("Yes.") > 0) {
            Dialogues.clickOption("Yes.");
            Sleep.sleepUntil(Bank::isOpen, 1600);
            Bank.depositAllItems(); sleep(800, 1200);
            Bank.depositAllEquipment(); sleep(800, 1200);
            Bank.close(); sleep(800, 1200);
            return;
        }
        if (!Dialogues.canContinue()) {
            GameObject b = GameObjects.closest("Bank booth");
            if (b != null && b.interact("Use")) Sleep.sleepUntil(Dialogues::canContinue, 3000);
        } else {
            Dialogues.clickContinue();
        }
    }

    private void doPollBooth() {
        if (Bank.isOpen()) { Bank.close(); return; }
        if (PlayerSettings.getConfig(Varbits.POLL_BOOTH_ACTIVE) == 0) {
            GameObject p = GameObjects.closest("Poll booth");
            if (p != null && p.interact("Use")) Sleep.sleepUntil(Dialogues::canContinue, 2400);
            while (Dialogues.canContinue() || PlayerSettings.getConfig(Varbits.POLL_BOOTH_ACTIVE) == 0) {
                Dialogues.clickContinue();
                sleep(300, 500);
            }
        }
        WidgetChild bar = Widgets.get(310, 1);
        if (bar != null) bar = bar.getChild(11);
        if (bar != null && bar.isVisible()) {
            bar.interact();
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.POLL_BOOTH_ACTIVE) == 0, 1500);
        }
    }

    private void doSecondPollBooth() {
        if (PlayerSettings.getConfig(Varbits.POLL_BOOTH_ACTIVE) > 0) {
            WidgetChild bar = Widgets.get(345, 1);
            if (bar != null) bar = bar.getChild(11);
            if (bar != null && bar.isVisible()) bar.interact();
        } else {
            GameObject d = GameObjects.closest(g -> g != null && "Door".equals(g.getName())
                    && g.getTile().equals(new Tile(3125, 3124, 0)));
            if (d != null && d.interact("Open")) walkSleep();
        }
    }

    private void openDoorAt(Tile t) {
        GameObject d = GameObjects.closest(g -> g != null && "Door".equals(g.getName()) && g.getTile().equals(t));
        if (d != null && d.interact("Open")) walkSleep();
    }

    private void openDoorAtClosest(String name) {
        GameObject d = GameObjects.closest(name);
        if (d != null && d.interact("Open")) walkSleep();
    }

    private void walkToPrayer() {
        Tile dest = new Tile(3126, 3106, 0);
        if (Players.getLocal().distance(dest) > 5) {
            Walking.walk(dest);
            walkSleep();
            return;
        }
        NPC brace = NPCs.closest("Brother Brace");
        if (brace != null && !Map.canReach(brace.getTile())) {
            GameObject g = GameObjects.closest("Large door");
            if (g != null) g.interact("Open");
        }
        talkTo(NpcID.BROTHER_BRACE, "Brother Brace");
    }

    private void walkToMage() {
        Tile dest = new Tile(3141, 3088, 0);
        if (Players.getLocal().distance(dest) > 5) { Walking.walk(dest); walkSleep(); return; }
        talkTo(NpcID.MAGIC_INSTRUCTOR, "Magic Instructor");
    }

    private void castWindStrike() {
        Tile dest = new Tile(3139, 3091, 0);
        if (Players.getLocal().distance(dest) > 2) { Walking.walk(dest); walkSleep(); return; }
        NPC chick = NPCs.closest("Chicken");
        if (chick != null && Magic.castSpellOn(Normal.WIND_STRIKE, chick)) {
            Sleep.sleepUntil(() -> PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS) != 650, 2400);
        }
    }

    private void finalChoice() {
        if (Dialogues.getOptions() == null) {
            talkTo(NpcID.MAGIC_INSTRUCTOR, "Magic Instructor");
        } else {
            Dialogues.clickOption(1);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void eatClickToContinue() {
        List<WidgetChild> ctc = Widgets.getAllContainingText("Click to continue");
        if (!ctc.isEmpty()) {
            WidgetChild wc = ctc.get(0);
            if (wc != null && wc.isVisible()) {
                wc.interact();
                sleep(700, 1100);
            }
        }
    }

    private static final Tab[] TABS = {
            Tab.COMBAT, Tab.SKILLS, Tab.QUEST, Tab.INVENTORY, Tab.EQUIPMENT,
            Tab.PRAYER, Tab.MAGIC, Tab.CLAN, Tab.ACCOUNT_MANAGEMENT,
            Tab.FRIENDS, Tab.LOGOUT, Tab.OPTIONS, Tab.EMOTES, Tab.MUSIC };

    private Tab tabFromConfig(int conf) {
        int idx = (conf & 15) - 1;
        int prog = PlayerSettings.getConfig(Varbits.TUTORIAL_PROGRESS);
        if (prog == 580) idx = 9;
        if (prog == 590) idx = 8;
        return (idx >= 0 && idx < TABS.length) ? TABS[idx] : null;
    }

    private static void walkSleep() {
        Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(1200, 1600));
        Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(2400, 3600));
    }

    private static void sleep(int min, int max) {
        try { Thread.sleep(Calculations.random(min, max)); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}



