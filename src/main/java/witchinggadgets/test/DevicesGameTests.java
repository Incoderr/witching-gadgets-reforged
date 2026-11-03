package witchinggadgets.test;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModEffects;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Knowledge;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.devices.LabelLibraryBlockEntity;
import witchinggadgets.devices.LabelLibraryMenu;
import witchinggadgets.devices.SaunaEvents;
import witchinggadgets.devices.SaunaStoveBlockEntity;

public final class DevicesGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("devices")
        .add("recipes_registered", DevicesGameTests::recipes)
        .add("sauna_steam_boxes_merge", DevicesGameTests::merge)
        .add("sauna_stove_steams_players", DevicesGameTests::sauna)
        .add("label_library_attunes_labels", DevicesGameTests::library)
        .add("label_library_needs_discovered_aspect", DevicesGameTests::discovery);

    private DevicesGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"SAUNASTOVE", "LABELLIB"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void merge(GameTestHelper helper) {
        Set<BlockPos> cells = Set.of(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(0, 1, 0));
        List<AABB> boxes = SaunaStoveBlockEntity.merge(cells);
        helper.assertValueEqual(boxes.size(), 2, "box count");
        helper.assertValueEqual(boxes.get(0), new AABB(0, 0, 0, 2, 1, 2), "floor box");
        helper.succeed();
    }

    private static void sauna(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, DevicesFeature.SAUNA_STOVE.get());
        SaunaStoveBlockEntity stove = (SaunaStoveBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(!stove.fill(new FluidStack(Fluids.LAVA, 1000)), "stove took lava");
        helper.assertTrue(stove.fill(new FluidStack(Fluids.WATER, 1000)), "stove refused water");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos standing = helper.absolutePos(pos.east(2));
        player.setPos(standing.getX() + 0.5, standing.getY(), standing.getZ() + 0.5);
        helper.runAfterDelay(70, () -> {
            helper.assertValueEqual(stove.tank().getAmount(), 0, "water left in the tank");
            helper.assertTrue(stove.tick() > 0, "stove is not steaming");
            helper.assertFalse(stove.boxes().isEmpty(), "no steam area");
            helper.assertTrue(stove.steams(player), "player is not in the steam");
            SaunaEvents.steam(player, helper.getLevel().getRandom());
            helper.assertTrue(player.hasEffect(ModEffects.WARP_WARD), "no warp ward in the steam");
            helper.succeed();
        });
    }

    private static void library(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, DevicesFeature.LABEL_LIBRARY.get());
        LabelLibraryBlockEntity library = (LabelLibraryBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(library.output().isEmpty(), "output without labels");
        helper.assertFalse(library.canPlaceItem(LabelLibraryBlockEntity.INPUT, new ItemStack(Items.PAPER)), "accepted a non-label");
        library.setItem(LabelLibraryBlockEntity.INPUT, new ItemStack(ModItems.JAR_LABEL.get(), 3));
        helper.assertTrue(library.output().isEmpty(), "output without an aspect");
        library.setAspect(Aspect.FIRE);
        ItemStack output = library.output();
        AspectList aspects = AspectKeys.ASPECTS.get(output);
        helper.assertTrue(aspects != null && aspects.getAspects().contains(Aspect.FIRE), "label is not attuned");
        library.consumeLabel();
        helper.assertValueEqual(library.getItem(LabelLibraryBlockEntity.INPUT).getCount(), 2, "labels left");
        helper.assertFalse(library.canTakeItemThroughFace(LabelLibraryBlockEntity.OUTPUT, output, Direction.DOWN), "output extractable");
        helper.succeed();
    }

    private static void discovery(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, DevicesFeature.LABEL_LIBRARY.get());
        LabelLibraryBlockEntity library = (LabelLibraryBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        LabelLibraryMenu menu = new LabelLibraryMenu(0, player.getInventory(), library);
        Aspect aspect = Aspect.ELDRITCH;
        int id = LabelLibraryMenu.indexOf(aspect);
        if (!Knowledge.get(player).hasDiscoveredAspect(aspect)) {
            helper.assertFalse(menu.clickMenuButton(player, id), "undiscovered aspect selected");
            helper.assertTrue(library.aspect() == null, "aspect changed");
        }
        Knowledge.get(player).discoverAspect(aspect);
        helper.assertTrue(menu.clickMenuButton(player, id), "discovered aspect rejected");
        helper.assertTrue(library.aspect() == aspect, "aspect not set");
        helper.succeed();
    }
}
