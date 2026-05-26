package nezz.dreambot.master.id;

import java.util.Arrays;
import java.util.List;

/**
 * Port of {@code com.questhelper.ItemCollections} from Zoinkwiz/quest-helper —
 * named groups of items used by quest steps ("any pickaxe", "any axe",
 * "any teleport to Varrock", "F2P food", ...).
 *
 * <p>These let quest steps check for *any* member of a category in the
 * inventory / bank without enumerating IDs at each call site.</p>
 *
 * <p>Reference: <a href="https://github.com/Zoinkwiz/quest-helper/blob/master/src/main/java/com/questhelper/ItemCollections.java">
 *   quest-helper/ItemCollections.java</a></p>
 */
public final class ItemCollections {

    private ItemCollections() { }

    public static final List<Integer> PICKAXES = Arrays.asList(
            ItemID.BRONZE_PICKAXE, ItemID.IRON_PICKAXE, ItemID.STEEL_PICKAXE,
            ItemID.BLACK_PICKAXE, ItemID.MITHRIL_PICKAXE, ItemID.ADAMANT_PICKAXE,
            ItemID.RUNE_PICKAXE, ItemID.DRAGON_PICKAXE, ItemID.CRYSTAL_PICKAXE);

    public static final List<Integer> AXES = Arrays.asList(
            ItemID.BRONZE_AXE, ItemID.IRON_AXE, ItemID.STEEL_AXE,
            ItemID.BLACK_AXE, ItemID.MITHRIL_AXE, ItemID.ADAMANT_AXE,
            ItemID.RUNE_AXE, ItemID.DRAGON_AXE, ItemID.CRYSTAL_AXE);

    public static final List<Integer> F2P_FOOD = Arrays.asList(
            ItemID.SHRIMPS, ItemID.ANCHOVIES, ItemID.BREAD, ItemID.TROUT,
            ItemID.SALMON, ItemID.LOBSTER, ItemID.TUNA, ItemID.SWORDFISH,
            ItemID.COOKED_MEAT, ItemID.COOKED_CHICKEN);

    public static final List<Integer> P2P_FOOD = Arrays.asList(
            ItemID.SHARK, ItemID.COOKED_KARAMBWAN);

    public static final List<Integer> F2P_RUNES_AIR = Arrays.asList(ItemID.AIR_RUNE);
    public static final List<Integer> F2P_RUNES_FIRE = Arrays.asList(ItemID.FIRE_RUNE);
    public static final List<Integer> F2P_RUNES_WATER = Arrays.asList(ItemID.WATER_RUNE);
    public static final List<Integer> F2P_RUNES_EARTH = Arrays.asList(ItemID.EARTH_RUNE);

    public static final List<Integer> COINS = Arrays.asList(
            ItemID.COINS_995, ItemID.PLATINUM_TOKEN);

    public static final List<Integer> F2P_LOGS = Arrays.asList(
            ItemID.LOGS, ItemID.OAK_LOGS, ItemID.WILLOW_LOGS,
            ItemID.MAPLE_LOGS, ItemID.YEW_LOGS);

    public static final List<Integer> P2P_LOGS = Arrays.asList(
            ItemID.MAGIC_LOGS, ItemID.REDWOOD_LOGS, ItemID.TEAK_LOGS, ItemID.MAHOGANY_LOGS);

    public static final List<Integer> RAW_FISH_NET = Arrays.asList(
            ItemID.RAW_SHRIMPS, ItemID.RAW_ANCHOVIES);

    public static final List<Integer> BONES_ALL = Arrays.asList(
            ItemID.BONES, ItemID.BIG_BONES, ItemID.BABY_DRAGON_BONES,
            ItemID.DRAGON_BONES, ItemID.SUPERIOR_DRAGON_BONES,
            ItemID.DAGANNOTH_BONES, ItemID.WYVERN_BONES);

    /** Tutorial Island starter inventory. */
    public static final List<Integer> TUTORIAL_INVENTORY = Arrays.asList(
            ItemID.BRONZE_DAGGER, ItemID.WOODEN_SHIELD, ItemID.BRONZE_SWORD,
            ItemID.SHORTBOW, ItemID.BRONZE_ARROW, ItemID.TINDERBOX);
}
