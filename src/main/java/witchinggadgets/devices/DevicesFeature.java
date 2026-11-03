package witchinggadgets.devices;

import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumcraft.platform.Transfer;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.research.WgResearch;

public final class DevicesFeature {
    public static final DeferredBlock<SaunaStoveBlock> SAUNA_STOVE = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "sauna_stove",
        SaunaStoveBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).strength(2.5F, 10.0F).sound(SoundType.WOOD)
    ));
    public static final DeferredBlock<LabelLibraryBlock> LABEL_LIBRARY = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "label_library",
        LabelLibraryBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).strength(2.5F, 10.0F).sound(SoundType.WOOD)
    ));

    public static final Supplier<BlockEntityType<SaunaStoveBlockEntity>> SAUNA_STOVE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "sauna_stove", () -> new BlockEntityType<>(SaunaStoveBlockEntity::new, SAUNA_STOVE.get())
    );
    public static final Supplier<BlockEntityType<LabelLibraryBlockEntity>> LABEL_LIBRARY_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "label_library", () -> new BlockEntityType<>(LabelLibraryBlockEntity::new, LABEL_LIBRARY.get())
    );

    public static final Supplier<MenuType<LabelLibraryMenu>> LABEL_LIBRARY_MENU = ModMenus.MENUS.register(
        "label_library", () -> Menus.simple(LabelLibraryMenu::new)
    );

    private DevicesFeature() {
    }

    public static void load() {
        Transfer.exposeFluids(SAUNA_STOVE_ENTITY, SaunaStoveBlockEntity::exposedFluids);
    }

    public static void recipes() {
        TcIngredient greatwood = TcIngredient.item("thaumcraft:greatwood_planks");
        WgRecipes.arcane(
            "SAUNASTOVE", "SAUNASTOVE", TcStack.of("witchinggadgets:sauna_stove"),
            new AspectList().add(Aspect.FIRE, 20).add(Aspect.WATER, 10).add(Aspect.ORDER, 10),
            new String[] {"SCS", "WBW", "WWW"},
            'S', TcIngredient.item("minecraft:smooth_stone_slab"),
            'C', TcIngredient.tag("c:storage_blocks/coal"),
            'W', greatwood,
            'B', TcIngredient.item("minecraft:bucket")
        );
        WgRecipes.arcane(
            "LABELLIB", "LABELLIB", TcStack.of("witchinggadgets:label_library"),
            new AspectList().add(Aspect.ORDER, 30),
            new String[] {"BLW", " T "},
            'B', TcIngredient.item("minecraft:book"),
            'L', TcIngredient.item("thaumcraft:jar_label"),
            'W', TcIngredient.item("thaumcraft:scribing_tools"),
            'T', TcIngredient.item("thaumcraft:table")
        );
    }

    public static void research() {
        WgResearch.item("SAUNASTOVE", new AspectList().add(Aspect.WATER, 3).add(Aspect.FIRE, 3).add(Aspect.MECHANISM, 1), -1, -1, 1, TcStack.of("witchinggadgets:sauna_stove"))
            .setParents("WGFAKEBATHSALTS")
            .setPages(WgResearch.text("1"), WgResearch.text("2"), WgResearch.arcane("SAUNASTOVE"))
            .setSecondary()
            .setConcealed()
            .registerResearchItem();
        WgResearch.item("LABELLIB", new AspectList().add(Aspect.SENSES, 4).add(Aspect.MIND, 4).add(Aspect.TOOL, 2), 4, -3, 2, TcStack.of("witchinggadgets:label_library"))
            .setParents("WGFAKEJARLABEL")
            .setPages(WgResearch.text("1"), WgResearch.arcane("LABELLIB"))
            .setSecondary()
            .registerResearchItem();
    }
}
