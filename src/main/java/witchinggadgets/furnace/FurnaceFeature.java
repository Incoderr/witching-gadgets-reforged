package witchinggadgets.furnace;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchGate;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.research.WgResearch;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class FurnaceFeature {
    public static final String RESEARCH = "INFERNALBLASTFURNACE";

    public static final DeferredBlock<InfernalBlastFurnaceBlock> INFERNAL_BLAST_FURNACE = ModBlocks.BLOCKS.registerBlock(
        "infernal_blast_furnace",
        InfernalBlastFurnaceBlock::new,
        properties -> properties.mapColor(MapColor.NETHER).strength(10.0F, 500.0F).sound(SoundType.STONE)
    );

    public static final Supplier<BlockEntityType<InfernalBlastFurnaceBlockEntity>> BLAST_FURNACE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "infernal_blast_furnace", () -> BlockEntityType.Builder.of(InfernalBlastFurnaceBlockEntity::new, INFERNAL_BLAST_FURNACE.get()).build(null)
    );

    private static final AspectList FORM_COST = new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50).add(Aspect.ENTROPY, 50);

    private FurnaceFeature() {
    }

    public static void load() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> WandTriggers.register(
            state -> state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.OBSIDIAN) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.NETHER_BRICK_STAIRS),
            FurnaceFeature::form
        ));
    }

    private static boolean form(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (level.isClientSide() || !ResearchGate.isComplete(player, RESEARCH) || !(wand.getItem() instanceof WandItem item)) {
            return false;
        }
        if (!item.consumeAllVisCrafting(wand, player, FORM_COST, false)) {
            return false;
        }
        BlockPos master = BlastFurnaceStructure.findMaster(level, pos);
        if (master == null) {
            return false;
        }
        BlastFurnaceStructure.form(level, master, player.getDirection().getOpposite());
        item.consumeAllVisCrafting(wand, player, FORM_COST, true);
        if (player instanceof ServerPlayer serverPlayer) {
            Warp.add(serverPlayer, 3, WarpType.STICKY);
        }
        level.playSound(null, master, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static void recipes() {
        tripling("thaumcraft:native_iron_cluster", "minecraft:iron_ingot", "minecraft:iron_nugget");
        tripling("thaumcraft:native_gold_cluster", "minecraft:gold_ingot", "minecraft:gold_nugget");
        tripling("thaumcraft:native_copper_cluster", "#c:ingots/copper", "#c:nuggets/copper");
        tripling("thaumcraft:native_tin_cluster", "#c:ingots/tin", "#c:nuggets/tin");
        tripling("thaumcraft:native_silver_cluster", "#c:ingots/silver", "#c:nuggets/silver");
        tripling("thaumcraft:native_lead_cluster", "#c:ingots/lead", "#c:nuggets/lead");
        BlastFurnaceRecipe.add("thaumcraft:native_cinnabar_cluster", 1, "thaumcraft:quicksilver", 3, 440, false, "thaumcraft:quicksilver_drop");
        BlastFurnaceRecipe.add("#c:ingots/iron", 1, "#c:ingots/steel", 1, 1200, false, null);
        BlastFurnaceRecipe.add("#c:storage_blocks/iron", 1, "#c:storage_blocks/steel", 1, 1200, false, null);
        BlastFurnaceRecipe.add("#c:ores/tungsten", 1, "#c:ingots/tungsten", 1, 1200, true, null);
        BlastFurnaceRecipe.add("#c:ores/rutile", 1, "#c:ingots/titanium", 1, 1200, true, null);

        TcStack stair = TcStack.of("minecraft:nether_brick_stairs");
        TcStack obsidian = TcStack.of("minecraft:obsidian");
        TcStack soul = TcStack.of("minecraft:soul_sand");
        TcStack brick = TcStack.of("minecraft:nether_bricks");
        WgRecipes.compound(
            RESEARCH, RESEARCH, new AspectList().add(Aspect.FIRE, 50).add(Aspect.EARTH, 50).add(Aspect.ENTROPY, 50), 3, 3, 3,
            stair, stair, stair,
            stair, TcStack.of("minecraft:lava_bucket"), stair,
            stair, stair, stair,
            obsidian, soul, obsidian,
            soul, soul, soul,
            obsidian, soul, obsidian,
            brick, brick, brick,
            brick, obsidian, brick,
            brick, brick, brick
        );
    }

    private static void tripling(String cluster, String ingot, String nugget) {
        BlastFurnaceRecipe.add(cluster, 1, ingot, 3, 440, false, nugget);
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList().add(Aspect.FIRE, 2).add(Aspect.METAL, 1).add(Aspect.CRAFT, 1).add(Aspect.DARKNESS, 1).add(Aspect.TAINT, 1), 4, -7, 2, WitchingGadgets.id("textures/gui/research/icon_blastfurnace.png"))
            .setWarp(3)
            .setConcealed()
            .setPages(WgResearch.text("1"), WgResearch.text("2"), WgResearch.compound(RESEARCH), WgResearch.text("3"))
            .setParents("WGFAKEINFERNALFURNACE")
            .registerResearchItem();
    }

    public static BlockState state(int position) {
        return INFERNAL_BLAST_FURNACE.get().defaultBlockState().setValue(InfernalBlastFurnaceBlock.POSITION, position);
    }
}
