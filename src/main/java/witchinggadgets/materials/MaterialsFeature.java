package witchinggadgets.materials;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import witchinggadgets.gadgets.ArcaneAbacusItem;
import witchinggadgets.gadgets.GuidingStringItem;
import witchinggadgets.gadgets.PhotoItem;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModItems;

public final class MaterialsFeature {
    public static final DeferredItem<Item> SIMPLE_THREAD = ModItems.ITEMS.registerSimpleItem("simple_thread");
    public static final DeferredItem<Item> GOLDEN_THREAD = ModItems.ITEMS.registerSimpleItem("golden_thread");
    public static final DeferredItem<Item> THAUMIUM_THREAD = ModItems.ITEMS.registerSimpleItem("thaumium_thread");
    public static final DeferredItem<Item> SPACIOUS_CLOTH = ModItems.ITEMS.registerSimpleItem("spacious_cloth");
    public static final DeferredItem<Item> GOLDEN_CLOTH = ModItems.ITEMS.registerSimpleItem("golden_cloth");
    public static final DeferredItem<Item> BEWITCHED_CLOTH = ModItems.ITEMS.registerSimpleItem("bewitched_cloth");
    public static final DeferredItem<Item> WOLF_PELT = ModItems.ITEMS.registerSimpleItem("wolf_pelt");
    public static final DeferredItem<ArcaneAbacusItem> ARCANE_ABACUS = ModItems.ITEMS.registerItem("arcane_abacus", ArcaneAbacusItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> GEMCUTTING_TOOLS = single("gemcutting_tools");
    public static final DeferredItem<PhotoItem> PHOTO_PLATE = ModItems.ITEMS.registerItem("photo_plate", properties -> new PhotoItem(false, properties));
    public static final DeferredItem<PhotoItem> DEVELOPED_PHOTO = ModItems.ITEMS.registerItem("developed_photo", properties -> new PhotoItem(true, properties));
    public static final DeferredItem<GuidingStringItem> GUIDING_STRING = ModItems.ITEMS.registerItem("guiding_string", GuidingStringItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> POWERLESS_PEARL = ModItems.ITEMS.registerSimpleItem("powerless_pearl");
    public static final DeferredItem<Item> GEMSTONE_DUST = ModItems.ITEMS.registerSimpleItem("gemstone_dust");

    public static final DeferredBlock<Block> VOID_METAL_BLOCK = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "void_metal_block",
        Block::new,
        properties -> properties
            .mapColor(MapColor.COLOR_BLACK)
            .strength(4.0F, 15.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
    ));

    private MaterialsFeature() {
    }

    public static void load() {
    }

    public static void aspects(AspectRegistry registry) {
        registry.register(SIMPLE_THREAD.get(), new AspectList().add(Aspect.CLOTH, 1));
        registry.register(GOLDEN_THREAD.get(), new AspectList().add(Aspect.CLOTH, 1).add(Aspect.GREED, 1));
        registry.register(THAUMIUM_THREAD.get(), new AspectList().add(Aspect.CLOTH, 1).add(Aspect.METAL, 1).add(Aspect.MAGIC, 1));
        registry.register(SPACIOUS_CLOTH.get(), new AspectList().add(Aspect.CLOTH, 3).add(Aspect.VOID, 3));
        registry.register(GOLDEN_CLOTH.get(), new AspectList().add(Aspect.CLOTH, 3).add(Aspect.GREED, 3));
        registry.register(BEWITCHED_CLOTH.get(), new AspectList().add(Aspect.CLOTH, 3).add(Aspect.MAGIC, 2).add(Aspect.TAINT, 1));
        registry.register(WOLF_PELT.get(), new AspectList().add(Aspect.CLOTH, 2).add(Aspect.BEAST, 3));
        registry.register(PHOTO_PLATE.get(), new AspectList().add(Aspect.CRYSTAL, 5).add(Aspect.SENSES, 3).add(Aspect.EXCHANGE, 2).add(Aspect.POISON, 1));
        registry.register(DEVELOPED_PHOTO.get(), new AspectList().add(Aspect.AIR, 2).add(Aspect.WATER, 2).add(Aspect.ORDER, 2).add(Aspect.SENSES, 2).add(Aspect.MIND, 2));
        registry.register(POWERLESS_PEARL.get(), new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.VOID, 4));
    }

    private static DeferredItem<Item> single(String name) {
        return ModItems.ITEMS.registerItem(name, Item::new, properties -> properties.stacksTo(1));
    }
}
