package witchinggadgets.alchemy;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.research.WgResearch;

public final class AlchemyFeature {
    public static final DeferredBlock<RoseVineBlock> ROSE_VINE = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "rose_vine",
        RoseVineBlock::new,
        properties -> properties.mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion()
    ));

    private AlchemyFeature() {
    }

    public static void load() {
    }

    public static void aspects(AspectRegistry registry) {
        registry.register(ROSE_VINE.get().asItem(), new AspectList().add(Aspect.PLANT, 6).add(Aspect.ENTROPY, 4).add(Aspect.MAGIC, 4).add(Aspect.LIFE, 2));
    }

    public static void recipes() {
        WgRecipes.crucible("ROSEVINE", "ROSEVINE", TcStack.of("witchinggadgets:rose_vine"), TcIngredient.item("minecraft:rose_bush"),
            new AspectList().add(Aspect.PLANT, 4).add(Aspect.ENTROPY, 4).add(Aspect.MAGIC, 4));
        WgRecipes.crucible("ALCHEMICALTRANSMOGRIFY_GRASS", "ALCHEMICALTRANSMOGRIFY", TcStack.of("minecraft:grass_block"), TcIngredient.item("minecraft:dirt"),
            new AspectList().add(Aspect.PLANT, 2).add(Aspect.LIFE, 1));
        WgRecipes.crucible("ALCHEMICALTRANSMOGRIFY_MYCEL", "ALCHEMICALTRANSMOGRIFY", TcStack.of("minecraft:mycelium"), TcIngredient.item("minecraft:dirt"),
            new AspectList().add(Aspect.PLANT, 2).add(Aspect.DARKNESS, 1));
        WgRecipes.crucible("ALCHEMICALTRANSMOGRIFY_SAND", "ALCHEMICALTRANSMOGRIFY", TcStack.of("minecraft:sand"), TcIngredient.item("minecraft:cobblestone"),
            new AspectList().add(Aspect.ENTROPY, 3));
        WgRecipes.crucible("ALCHEMICALTRANSMOGRIFY_FLINT", "ALCHEMICALTRANSMOGRIFY", TcStack.of("minecraft:flint"), TcIngredient.item("minecraft:gravel"),
            new AspectList().add(Aspect.CRYSTAL, 3));
        WgRecipes.crucible("PURECINNABAR", "PURECINNABAR", TcStack.of("thaumcraft:native_cinnabar_cluster"), TcIngredient.tag("c:ores/cinnabar"),
            new AspectList().add(Aspect.METAL, 1).add(Aspect.ORDER, 1));
    }

    public static void research() {
        WgResearch.item("ALCHEMICALTRANSMOGRIFY", new AspectList().add(Aspect.PLANT, 4).add(Aspect.LIFE, 2).add(Aspect.WATER, 2), -6, -4, 1, TcStack.of("minecraft:grass_block"))
            .setSecondary()
            .setParents("WGFAKEALCHEMICALMANUFACTURE")
            .setConcealed()
            .setPages(
                WgResearch.text("1"),
                WgResearch.crucible("ALCHEMICALTRANSMOGRIFY_GRASS"),
                WgResearch.crucible("ALCHEMICALTRANSMOGRIFY_MYCEL"),
                WgResearch.crucible("ALCHEMICALTRANSMOGRIFY_SAND"),
                WgResearch.crucible("ALCHEMICALTRANSMOGRIFY_FLINT")
            )
            .registerResearchItem();
        WgResearch.item("ROSEVINE", new AspectList().add(Aspect.PLANT, 2).add(Aspect.AIR, 3).add(Aspect.ENTROPY, 2), -6, -5, 1, TcStack.of("witchinggadgets:rose_vine"))
            .setSecondary()
            .setParents("WGFAKEALCHEMICALMANUFACTURE")
            .setConcealed()
            .setPages(WgResearch.text("1"), WgResearch.crucible("ROSEVINE"))
            .registerResearchItem();
        WgResearch.item("PURECINNABAR", new AspectList().add(Aspect.METAL, 5).add(Aspect.ORDER, 1).add(Aspect.POISON, 1), -6, -3, 1, TcStack.of("thaumcraft:native_cinnabar_cluster"))
            .setConcealed()
            .setSecondary()
            .setParents("WGFAKEPUREIRON")
            .setPages(WgResearch.text("1"), WgResearch.crucible("PURECINNABAR"))
            .registerResearchItem();
    }
}
