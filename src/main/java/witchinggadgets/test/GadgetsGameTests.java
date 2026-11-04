package witchinggadgets.test;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.registry.ModBlocks;
import witchinggadgets.gadgets.ArcaneAbacusItem;
import witchinggadgets.materials.MaterialsFeature;

public final class GadgetsGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("gadgets")
        .add("recipes_registered", GadgetsGameTests::recipes)
        .add("abacus_reduces_node_to_primals", GadgetsGameTests::node)
        .add("abacus_reports_matrix_symmetry", GadgetsGameTests::matrix);

    private GadgetsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get("CALCULATOR") != null, "recipe CALCULATOR is missing");
        helper.succeed();
    }

    private static void node(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, ModBlocks.AURA_NODE.get());
        NodeBlockEntity node = (NodeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        node.setAspects(new AspectList().add(Aspect.MAGIC, 10).add(Aspect.AIR, 5));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack abacus = new ItemStack(MaterialsFeature.ARCANE_ABACUS.get());
        BlockPos absolute = helper.absolutePos(pos);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertTrue(abacus.getItem().onItemUseFirst(abacus, context).consumesAction(), "abacus ignored the node");
        List<Object> args = args(ArcaneAbacusItem.nodeContents(node));
        helper.assertValueEqual(args, List.<Object>of(15, 0, 10, 0, 10, 10), "primal amounts");
        helper.succeed();
    }

    private static void matrix(GameTestHelper helper) {
        BlockPos matrix = new BlockPos(2, 3, 2);
        helper.setBlock(matrix, ModBlocks.RUNIC_MATRIX.get());
        helper.setBlock(new BlockPos(2, 1, 2), ModBlocks.ARCANE_PEDESTAL.get());
        helper.setBlock(new BlockPos(1, 1, 2), ModBlocks.ARCANE_PEDESTAL.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(matrix);

        ArcaneAbacusItem.Report lonely = ArcaneAbacusItem.inspectMatrix(helper.getLevel(), absolute, player);
        helper.assertValueEqual(lonely.stability(), -2, "stability of an unpaired pedestal");
        helper.assertTrue(hasWarning(lonely, "noPartnerPedestal"), "missing pedestal partner warning");

        helper.setBlock(new BlockPos(3, 1, 2), ModBlocks.ARCANE_PEDESTAL.get());
        ArcaneAbacusItem.Report paired = ArcaneAbacusItem.inspectMatrix(helper.getLevel(), absolute, player);
        helper.assertValueEqual(paired.stability(), 0, "stability of paired pedestals");
        helper.assertFalse(hasWarning(paired, "noPartnerPedestal"), "paired pedestals warned");

        helper.setBlock(new BlockPos(1, 2, 1), Blocks.SKELETON_SKULL);
        helper.assertTrue(hasWarning(ArcaneAbacusItem.inspectMatrix(helper.getLevel(), absolute, player), "noPartnerStabilizer"), "missing stabilizer partner warning");
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.SKELETON_SKULL);
        helper.assertFalse(hasWarning(ArcaneAbacusItem.inspectMatrix(helper.getLevel(), absolute, player), "noPartnerStabilizer"), "paired stabilizers warned");
        helper.succeed();
    }

    private static boolean hasWarning(ArcaneAbacusItem.Report report, String key) {
        return report.lines().stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents && contents.getKey().equals("wg.chat.infusionWarning." + key));
    }

    private static List<Object> args(Component component) {
        return Arrays.asList(((TranslatableContents) component.getContents()).getArgs());
    }
}
