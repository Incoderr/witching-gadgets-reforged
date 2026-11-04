package witchinggadgets.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import thaumcraft.registry.ModItems;
import thaumcraft.research.ScanManager;
import witchinggadgets.gadgets.ArcaneAbacusItem;
import witchinggadgets.gadgets.GadgetsFeature;
import witchinggadgets.gadgets.GuidingStringItem;
import witchinggadgets.gadgets.PhotoItem;
import witchinggadgets.gadgets.ScanCameraItem;
import witchinggadgets.materials.MaterialsFeature;

public final class GadgetsGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("gadgets")
        .add("recipes_registered", GadgetsGameTests::recipes)
        .add("abacus_reduces_node_to_primals", GadgetsGameTests::node)
        .add("abacus_reports_matrix_symmetry", GadgetsGameTests::matrix)
        .add("guiding_string_records_turns", GadgetsGameTests::guidingString)
        .add("camera_photo_develops_and_teaches", GadgetsGameTests::camera);

    private GadgetsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"CALCULATOR", "SCANCAMERA", "SCANCAMERA_DEVELOP", "SCANCAMERA_CLEARPLATE", "LABYRINTHSTRING"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
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

    private static void guidingString(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
        player.setYRot(0.0F);
        ItemStack string = new ItemStack(MaterialsFeature.GUIDING_STRING.get());
        GuidingStringItem.toggle(string, player);
        helper.assertTrue(GuidingStringItem.active(string), "string is not active");
        helper.assertValueEqual(GuidingStringItem.path(string).size(), 1, "first point");
        GuidingStringItem.track(string, player);
        helper.assertValueEqual(GuidingStringItem.path(string).size(), 1, "point without a turn");
        player.setPos(helper.absoluteVec(new Vec3(4.5, 2.0, 4.5)));
        player.setYRot(90.0F);
        GuidingStringItem.track(string, player);
        helper.assertValueEqual(GuidingStringItem.path(string).size(), 2, "point after a turn");
        GuidingStringItem.toggle(string, player);
        helper.assertFalse(GuidingStringItem.active(string), "string still active");
        helper.assertValueEqual(GuidingStringItem.path(string).size(), 3, "toggle adds a point");
        player.setShiftKeyDown(true);
        GuidingStringItem.toggle(string, player);
        helper.assertTrue(GuidingStringItem.path(string).isEmpty(), "sneak did not clear the path");
        helper.succeed();
    }

    private static void camera(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        ServerPlayer player = serverPlayer(helper);
        ScanManager.Target target = ScanManager.resolveBlock(helper.getLevel(), helper.absolutePos(pos));
        helper.assertTrue(target != null, "stone is not scannable");
        ScanCameraItem.Shot shot = new ScanCameraItem.Shot(target, Component.literal("Stone"), Vec3.ZERO, 15);
        helper.assertFalse(ScanCameraItem.takePhoto(player, shot), "photo without mirrored glass");
        player.getInventory().add(new ItemStack(ModItems.MIRRORED_GLASS.get(), 2));
        helper.assertTrue(ScanCameraItem.takePhoto(player, shot), "photo was not taken");
        helper.assertValueEqual(player.getInventory().countItem(ModItems.MIRRORED_GLASS.get()), 1, "glass left");
        int plateSlot = -1;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(MaterialsFeature.PHOTO_PLATE.get())) {
                plateSlot = slot;
            }
        }
        ItemStack plate = plateSlot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(plateSlot);
        helper.assertTrue(GadgetsFeature.SCAN.has(plate), "plate has no scan");

        GadgetsFeature.PhotoDeveloping developing = new GadgetsFeature.PhotoDeveloping();
        List<ItemStack> grid = new ArrayList<>(List.of(plate, new ItemStack(Items.PAPER), new ItemStack(Items.PAPER), new ItemStack(Items.INK_SAC), new ItemStack(Items.INK_SAC)));
        while (grid.size() < 9) {
            grid.add(ItemStack.EMPTY);
        }
        helper.assertTrue(developing.matches(grid, player), "developing did not match");
        ItemStack developed = developing.result(grid);
        helper.assertValueEqual(developed.getCount(), 2, "copies");
        helper.assertValueEqual(developing.aspects(grid).getAmount(Aspect.WATER), 10, "aqua cost");
        grid.set(4, ItemStack.EMPTY);
        helper.assertFalse(developing.matches(grid, player), "uneven ink and paper matched");

        helper.assertFalse(ScanManager.hasBeenScanned(player, target), "stone already scanned");
        helper.assertTrue(PhotoItem.study(player, developed), "studying failed");
        helper.assertTrue(ScanManager.hasBeenScanned(player, target), "stone not scanned");
        helper.assertValueEqual(developed.getCount(), 1, "photo not consumed");
        helper.assertFalse(PhotoItem.study(player, developed), "studied twice");
        helper.succeed();
    }

    private static ServerPlayer serverPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        }.markClientLoaded();
        return player;
    }

    private static boolean hasWarning(ArcaneAbacusItem.Report report, String key) {
        return report.lines().stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents && contents.getKey().equals("wg.chat.infusionWarning." + key));
    }

    private static List<Object> args(Component component) {
        return Arrays.asList(((TranslatableContents) component.getContents()).getArgs());
    }
}
