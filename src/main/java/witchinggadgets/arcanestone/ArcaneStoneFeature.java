package witchinggadgets.arcanestone;

import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.research.WgResearch;

public final class ArcaneStoneFeature {
    public static final DeferredBlock<EtherealWallBlock> ETHEREAL_WALL = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "ethereal_wall",
        EtherealWallBlock::new,
        properties -> properties.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).isValidSpawn((state, level, pos, type) -> false)
    ));
    public static final DeferredBlock<AgeingStoneBlock> AGEING_STONE = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "ageing_stone",
        AgeingStoneBlock::new,
        properties -> properties.mapColor(MapColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE).isValidSpawn((state, level, pos, type) -> false)
    ));
    public static final DeferredBlock<StoneExtruderBlock> STONE_EXTRUDER = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "stone_extruder",
        StoneExtruderBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD)
    ));

    public static final Supplier<BlockEntityType<EtherealWallBlockEntity>> ETHEREAL_WALL_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "ethereal_wall", () -> new BlockEntityType<>(EtherealWallBlockEntity::new, ETHEREAL_WALL.get())
    );
    public static final Supplier<BlockEntityType<AgeingStoneBlockEntity>> AGEING_STONE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "ageing_stone", () -> new BlockEntityType<>(AgeingStoneBlockEntity::new, AGEING_STONE.get())
    );
    public static final Supplier<BlockEntityType<StoneExtruderBlockEntity>> STONE_EXTRUDER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "stone_extruder", () -> new BlockEntityType<>(StoneExtruderBlockEntity::new, STONE_EXTRUDER.get())
    );

    private ArcaneStoneFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient stone = TcIngredient.item("thaumcraft:arcane_stone");
        TcIngredient shard = TcIngredient.tag("thaumcraft:shards");
        WgRecipes.arcane(
            "ETHEREALWALL", "ETHEREALWALL", TcStack.of("witchinggadgets:ethereal_wall", 6),
            new AspectList().add(Aspect.ORDER, 5).add(Aspect.EARTH, 3),
            new String[] {"SsS", "STS", "S S"},
            'S', stone,
            's', shard,
            'T', TcIngredient.item("minecraft:redstone_torch")
        );
        WgRecipes.arcane(
            "AGEINGSTONE", "AGEINGSTONE", TcStack.of("witchinggadgets:ageing_stone"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10),
            new String[] {" s ", "SCS", " s "},
            'S', stone,
            's', shard,
            'C', TcIngredient.item("minecraft:clock")
        );
        WgRecipes.arcane(
            "STONEEXTRUDER", "STONEEXTRUDER", TcStack.of("witchinggadgets:stone_extruder"),
            new AspectList().add(Aspect.ENTROPY, 4).add(Aspect.EARTH, 8),
            new String[] {" P ", "WSL", "wSw"},
            'S', stone,
            'w', TcIngredient.item("thaumcraft:greatwood_planks"),
            'W', TcIngredient.item("minecraft:water_bucket"),
            'L', TcIngredient.item("minecraft:lava_bucket"),
            'P', TcIngredient.item("thaumcraft:thaumium_pickaxe")
        );
    }

    public static void research() {
        WgResearch.item("STONEEXTRUDER", new AspectList().add(Aspect.EARTH, 1).add(Aspect.MECHANISM, 1).add(Aspect.TOOL, 1), -1, -5, 1, TcStack.of("witchinggadgets:stone_extruder"))
            .setParents("WGFAKEARCANESTONE", "THAUMIUM")
            .setPages(WgResearch.text("1"), WgResearch.arcane("STONEEXTRUDER"))
            .registerResearchItem();
        WgResearch.item("AGEINGSTONE", new AspectList().add(Aspect.LIFE, 3).add(Aspect.MECHANISM, 3), -1, -7, 2, TcStack.of("witchinggadgets:ageing_stone"))
            .setWarp(1)
            .setParents("WGFAKEARCANESTONE")
            .setPages(WgResearch.text("1"), WgResearch.arcane("AGEINGSTONE"))
            .setSecondary()
            .registerResearchItem();
        WgResearch.item("ETHEREALWALL", new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.MECHANISM, 2).add(Aspect.EARTH, 4), -1, -6, 2, TcStack.of("witchinggadgets:ethereal_wall"))
            .setParents("WGFAKEARCANESTONE")
            .setPages(WgResearch.text("1"), WgResearch.text("2"), WgResearch.arcane("ETHEREALWALL"))
            .setSecondary()
            .registerResearchItem();
    }
}
