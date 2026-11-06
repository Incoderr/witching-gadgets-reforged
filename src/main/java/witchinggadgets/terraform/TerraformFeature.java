package witchinggadgets.terraform;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
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

public final class TerraformFeature {
    public static final DeferredBlock<TerraformerBlock> TERRAFORMER = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "terraformer", TerraformerBlock::new, properties -> properties.mapColor(MapColor.METAL).strength(4.0F, 15.0F).sound(SoundType.METAL)
    ));
    public static final Map<TerraformFocus, DeferredBlock<TerraformFocusBlock>> FOCI = new EnumMap<>(TerraformFocus.class);

    static {
        for (TerraformFocus focus : TerraformFocus.values()) {
            FOCI.put(focus, ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
                focus.id(), properties -> new TerraformFocusBlock(focus, properties), properties -> properties.mapColor(MapColor.METAL).strength(4.0F, 15.0F).sound(SoundType.METAL)
            )));
        }
    }

    public static final Supplier<BlockEntityType<TerraformerBlockEntity>> TERRAFORMER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "terraformer", () -> new BlockEntityType<>(TerraformerBlockEntity::new, TERRAFORMER.get())
    );
    public static final Supplier<BlockEntityType<TerraformFocusBlockEntity>> FOCUS_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "terraform_focus", () -> new BlockEntityType<>(TerraformFocusBlockEntity::new, FOCI.values().stream().map(DeferredBlock::get).toArray(Block[]::new))
    );

    private TerraformFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        WgRecipes.infusion(
            "TERRAFORMER", "TERRAFORMER", TcStack.of("witchinggadgets:terraformer"), 3,
            new AspectList().add(Aspect.ORDER, 16).add(Aspect.EXCHANGE, 8).add(Aspect.EARTH, 16),
            TcIngredient.item("thaumcraft:alchemical_construct"),
            TcIngredient.item("thaumcraft:balanced_shard"), TcIngredient.item("minecraft:iron_ingot"), TcIngredient.item("thaumcraft:essentia_tube"),
            TcIngredient.item("thaumcraft:silverwood_sapling"), TcIngredient.item("thaumcraft:essentia_tube"), TcIngredient.item("minecraft:iron_ingot")
        );
        focus("TERRAFORMER_PLAINS", "TERRAFORMER", TerraformFocus.PLAINS, new AspectList().add(Aspect.EARTH, 10), "minecraft:grass_block");
        focus("TERRAFORMFOCUS_COLDTAIGA", "TERRAFORMFOCUS_COLDTAIGA", TerraformFocus.COLD_TAIGA, new AspectList().add(Aspect.WATER, 10).add(Aspect.ORDER, 10), "minecraft:ice");
        focus("TERRAFORMFOCUS_DESERT", "TERRAFORMFOCUS_DESERT", TerraformFocus.DESERT, new AspectList().add(Aspect.FIRE, 10).add(Aspect.EARTH, 10), "minecraft:sand");
        focus("TERRAFORMFOCUS_JUNGLE", "TERRAFORMFOCUS_JUNGLE", TerraformFocus.JUNGLE, new AspectList().add(Aspect.WATER, 10).add(Aspect.EARTH, 10), "minecraft:jungle_log");
        focus("TERRAFORMFOCUS_HELL", "TERRAFORMFOCUS_HELL", TerraformFocus.HELL, new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 10), "minecraft:nether_bricks");
        focus("TERRAFORMFOCUS_MUSHROOM", "TERRAFORMFOCUS_MUSHROOM", TerraformFocus.MUSHROOM, new AspectList().add(Aspect.EARTH, 10).add(Aspect.WATER, 10), "minecraft:mycelium");
        TcIngredient iron = TcIngredient.item("minecraft:iron_ingot");
        WgRecipes.infusion(
            "TERRAFORMFOCUS_TAINT", "TERRAFORMFOCUS_TAINT", TcStack.of("witchinggadgets:" + TerraformFocus.TAINT.id()), 3,
            new AspectList().add(Aspect.TAINT, 32).add(Aspect.EXCHANGE, 8),
            TcIngredient.item("thaumcraft:crusted_taint"),
            TcIngredient.item("thaumcraft:balanced_shard"), iron, iron, TcIngredient.item("thaumcraft:essentia_tube"), iron, iron
        );
    }

    private static void focus(String key, String research, TerraformFocus focus, AspectList aspects, String block) {
        WgRecipes.arcane(
            key, research, TcStack.of("witchinggadgets:" + focus.id()), aspects,
            new String[] {" S ", "IBI", "ITI"},
            'B', TcIngredient.item(block),
            'I', TcIngredient.tag("c:ingots/iron"),
            'S', TcIngredient.item("thaumcraft:balanced_shard"),
            'T', TcIngredient.item("thaumcraft:essentia_tube")
        );
    }

    public static void research() {
        WgResearch.item("TERRAFORMER", new AspectList().add(Aspect.EARTH, 2).add(Aspect.EXCHANGE, 1).add(Aspect.ENERGY, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1), 6, -7, 2, TcStack.of("witchinggadgets:terraformer"))
            .setPages(WgResearch.text("1"), WgResearch.infusion("TERRAFORMER"), WgResearch.text("2"), WgResearch.arcane("TERRAFORMER_PLAINS"))
            .setParents("WGFAKECENTRIFUGE")
            .registerResearchItem();
        focusResearch("TERRAFORMFOCUS_COLDTAIGA", TerraformFocus.COLD_TAIGA, Aspect.COLD, Aspect.ORDER, 6, -9, 0, false);
        focusResearch("TERRAFORMFOCUS_DESERT", TerraformFocus.DESERT, Aspect.ENTROPY, Aspect.FIRE, 5, -8, 0, false);
        focusResearch("TERRAFORMFOCUS_JUNGLE", TerraformFocus.JUNGLE, Aspect.TREE, Aspect.PLANT, 7, -9, 0, false);
        focusResearch("TERRAFORMFOCUS_HELL", TerraformFocus.HELL, Aspect.DARKNESS, Aspect.FIRE, 8, -9, 2, false);
        focusResearch("TERRAFORMFOCUS_MUSHROOM", TerraformFocus.MUSHROOM, Aspect.PLANT, Aspect.EARTH, 9, -8, 0, false);
        focusResearch("TERRAFORMFOCUS_TAINT", TerraformFocus.TAINT, Aspect.TAINT, Aspect.MAGIC, 9, -7, 3, true);
    }

    private static void focusResearch(String key, TerraformFocus focus, Aspect main, Aspect second, int column, int row, int warp, boolean infusion) {
        var item = WgResearch.item(key, new AspectList().add(main, 8).add(second, 4).add(Aspect.EXCHANGE, 2).add(Aspect.ENERGY, 1), column, row, 2, TcStack.of("witchinggadgets:" + focus.id()));
        if (warp > 0) {
            item.setWarp(warp);
        }
        item.setSecondary()
            .setPages(WgResearch.text("1"), infusion ? WgResearch.infusion(key) : WgResearch.arcane(key))
            .setParents(infusion ? new String[] {"TERRAFORMER", "BOTTLETAINT"} : new String[] {"TERRAFORMER"})
            .registerResearchItem();
    }
}
