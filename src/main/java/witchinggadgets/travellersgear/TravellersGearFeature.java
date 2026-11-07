package witchinggadgets.travellersgear;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Menus;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.baubles.WearableItem;
import witchinggadgets.gear.AbilitySlots;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.registry.ModItems;

public final class TravellersGearFeature {
    public static final String[] TITLES = {"treepuncher", "titan", "librarian", "bursar", "archchancellor", "justicar", "explorer", "defender", "seeker", "boxFox", "freshPrince"};

    public static final DeferredItem<WearableItem> TRAVELLER_CLOAK = register("traveller_cloak", properties -> new WearableItem(GearSlot.CLOAK, properties));
    public static final DeferredItem<SimpleBaubleItem> BELT = register("belt", properties -> new SimpleBaubleItem(BaubleType.BELT, properties));
    public static final DeferredItem<SimpleBaubleItem> GOLD_RING = register("gold_ring", properties -> new SimpleBaubleItem(BaubleType.RING, properties));
    public static final DeferredItem<SimpleBaubleItem> SILVER_RING = register("silver_ring", properties -> new SimpleBaubleItem(BaubleType.RING, properties));
    public static final DeferredItem<WearableItem> PAULDRON = register("pauldron", properties -> new WearableItem(GearSlot.PAULDRON, properties));
    public static final DeferredItem<WearableItem> VAMBRACE = register("vambrace", properties -> new WearableItem(GearSlot.VAMBRACE, properties));
    public static final DeferredItem<TitleScrollItem> TITLE_SCROLL = register("title_scroll", TitleScrollItem::new);

    public static final DeferredBlock<ArmorStandBlock> ARMOR_STAND = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "armor_stand",
        ArmorStandBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).strength(2.5F, 10.0F).sound(SoundType.WOOD).dynamicShape()
    ));
    public static final Supplier<BlockEntityType<ArmorStandBlockEntity>> ARMOR_STAND_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "armor_stand", () -> BlockEntityType.Builder.of(ArmorStandBlockEntity::new, ARMOR_STAND.get()).build(null)
    );
    public static final Supplier<MenuType<ArmorStandMenu>> ARMOR_STAND_MENU = ModMenus.MENUS.register(
        "armor_stand", () -> Menus.withPos(ArmorStandMenu::new)
    );

    private TravellersGearFeature() {
    }

    private static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> factory) {
        return ModItems.ITEMS.registerItem(name, factory);
    }

    public static void load() {
        AbilitySlots.register(thaumcraft.registry.ModItems.FOCUS_POUCH, new FocusPouchAbility());
    }
}
