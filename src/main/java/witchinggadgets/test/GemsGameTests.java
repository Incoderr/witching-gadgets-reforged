package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import witchinggadgets.gems.CrystalCapsuleItem;
import witchinggadgets.gems.CuttingTableBlock;
import witchinggadgets.gems.CuttingTableBlockEntity;
import witchinggadgets.gems.GemCut;
import witchinggadgets.gems.GemEffects;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.materials.MaterialsFeature;

public final class GemsGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("gems")
        .add("recipes_registered", GemsGameTests::recipes)
        .add("tools_turn_a_table_into_a_cutting_table", GemsGameTests::tools)
        .add("cutting_table_infuses_gems", GemsGameTests::cutting)
        .add("point_gems_heal_and_freeze", GemsGameTests::pointGems)
        .add("brittle_gems_shatter", GemsGameTests::shatter)
        .add("capsule_carries_a_fluid", GemsGameTests::capsule)
        .add("knockback_effect_adds_resistance", GemsGameTests::knockback);

    private GemsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"GEMCUTTING_TOOLS", "GEMCUTTING", "CRYSTALCAPSULE"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void tools(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.TABLE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack tools = new ItemStack(MaterialsFeature.GEMCUTTING_TOOLS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tools);
        BlockPos absolute = helper.absolutePos(pos);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertTrue(tools.getItem().useOn(context).consumesAction(), "tools ignored the table");
        helper.assertTrue(helper.getBlockState(pos).is(GemsFeature.CUTTING_TABLE.get()), "table was not converted");
        helper.assertFalse(tools.isEmpty(), "tools were consumed");
        helper.succeed();
    }

    private static ItemStack phial(Aspect aspect) {
        ItemStack stack = new ItemStack(ModItems.ESSENTIA_PHIAL.get());
        AspectKeys.ASPECTS.set(stack, new AspectList().add(aspect, 8));
        return stack;
    }

    private static void cutting(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, GemsFeature.CUTTING_TABLE.get().defaultBlockState().setValue(CuttingTableBlock.FACING, Direction.NORTH));
        CuttingTableBlockEntity table = (CuttingTableBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(table.getItem(CuttingTableBlockEntity.OUTPUT).isEmpty(), "output without a gem");
        table.setItem(CuttingTableBlockEntity.GEM, new ItemStack(Items.DIAMOND, 2));
        helper.assertTrue(table.getItem(CuttingTableBlockEntity.OUTPUT).isEmpty(), "output without essence");
        table.setItem(1, phial(Aspect.ORDER));
        table.setItem(2, phial(Aspect.ORDER));
        helper.assertFalse(table.canPlaceItem(3, phial(Aspect.FIRE)), "mixed aspects accepted");
        helper.assertFalse(table.canPlaceItem(3, phial(Aspect.MAGIC)), "compound aspect accepted");
        ItemStack gem = table.getItem(CuttingTableBlockEntity.OUTPUT);
        helper.assertTrue(InfusedGemItem.aspect(gem) == Aspect.ORDER, "gem aspect");
        helper.assertTrue(InfusedGemItem.cut(gem) == GemCut.POINT, "gem cut");
        helper.assertValueEqual(InfusedGemItem.potency(gem), 2, "diamond affinity potency");
        helper.assertValueEqual(InfusedGemItem.brittle(gem), 0, "diamond affinity brittleness");
        table.setTargetCut(1);
        ItemStack oval = table.getItem(CuttingTableBlockEntity.OUTPUT);
        helper.assertValueEqual(InfusedGemItem.potency(oval), 1, "oval potency");
        helper.assertFalse(table.canTakeItemThroughFace(CuttingTableBlockEntity.OUTPUT, oval, Direction.DOWN), "hoppers can take the output");
        table.consumeInputs();
        helper.assertValueEqual(table.getItem(CuttingTableBlockEntity.GEM).getCount(), 1, "gems left");
        helper.assertTrue(table.getItem(1).isEmpty() && table.getItem(2).isEmpty(), "essence left");

        table.setItem(CuttingTableBlockEntity.GEM, new ItemStack(Items.EMERALD));
        table.setItem(1, phial(Aspect.WATER));
        table.setTargetCut(0);
        helper.assertValueEqual(InfusedGemItem.brittle(table.getItem(CuttingTableBlockEntity.OUTPUT)), 2, "emerald aversion brittleness");
        table.setItem(1, phial(Aspect.FIRE));
        helper.assertValueEqual(InfusedGemItem.brittle(table.getItem(CuttingTableBlockEntity.OUTPUT)), 1, "neutral brittleness");
        helper.succeed();
    }

    private static void pointGems(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 2.0, 3.5)));
        player.setHealth(4.0F);
        helper.assertTrue(GemEffects.perform(GemCut.POINT, Aspect.ORDER, 0, 0, player), "order gem failed");
        helper.assertValueEqual(player.getHealth(), 10.0F, "healed health");
        BlockPos water = new BlockPos(4, 1, 3);
        helper.setBlock(water, Blocks.WATER);
        helper.assertTrue(GemEffects.perform(GemCut.POINT, Aspect.WATER, 0, 0, player), "water gem failed");
        helper.assertTrue(helper.getBlockState(water).is(Blocks.ICE), "water did not freeze");
        helper.assertFalse(GemEffects.perform(GemCut.POINT, Aspect.MAGIC, 0, 0, player), "compound aspect worked");
        helper.succeed();
    }

    private static void shatter(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gem = InfusedGemItem.create(Aspect.ORDER, GemCut.OVAL);
        InfusedGemItem.enchant(helper.getLevel(), gem, GemsFeature.GEMSTONE_BRITTLE, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, gem);
        gem.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(gem.isEmpty(), "brittle gem did not shatter");
        helper.assertValueEqual(player.getInventory().countItem(MaterialsFeature.GEMSTONE_DUST.get()), 1, "gemstone dust");
        helper.succeed();
    }

    private static void capsule(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.WATER);
        ItemStack capsules = new ItemStack(GemsFeature.CRYSTAL_CAPSULE.get(), 2);
        ItemStack filled = CrystalCapsuleItem.use(helper.getLevel(), helper.absolutePos(pos), capsules);
        helper.assertTrue(CrystalCapsuleItem.fluid(filled) == Fluids.WATER, "capsule did not take water");
        helper.assertValueEqual(capsules.getCount(), 1, "empty capsules left");
        helper.assertTrue(helper.getBlockState(pos).isAir(), "water was not removed");
        BlockPos target = new BlockPos(3, 1, 1);
        ItemStack empty = CrystalCapsuleItem.use(helper.getLevel(), helper.absolutePos(target), filled);
        helper.assertTrue(helper.getBlockState(target).is(Blocks.WATER), "water was not placed");
        helper.assertTrue(CrystalCapsuleItem.fluid(empty) == null && filled.isEmpty(), "capsule was not emptied");
        helper.succeed();
    }

    private static void knockback(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        double before = player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(GemsFeature.KNOCKBACK_RESISTANCE, 100, 1));
        helper.assertValueEqual(Math.round((player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - before) * 100), 68L, "knockback resistance");
        helper.succeed();
    }
}
