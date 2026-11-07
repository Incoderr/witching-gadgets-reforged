package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;
import witchinggadgets.travellersgear.ArmorStandMenu;
import witchinggadgets.travellersgear.TravellersGearFeature;

public final class TravellersGearGameTests {
    private static final BlockPos STAND = new BlockPos(3, 1, 3);
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("travellersgear")
        .add("armor_stand_swaps", TravellersGearGameTests::swaps);

    private TravellersGearGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void swaps(GameTestHelper helper) {
        helper.setBlock(STAND, TravellersGearFeature.ARMOR_STAND.get().defaultBlockState());
        ArmorStandBlockEntity stand = (ArmorStandBlockEntity) helper.getBlockEntity(STAND);
        Player player = TestSupport.mockServerPlayer(helper);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        stand.setItem(ArmorStandBlockEntity.ARMOR, new ItemStack(Items.GOLDEN_HELMET));
        stand.setItem(ArmorStandBlockEntity.GEAR, new ItemStack(TravellersGearFeature.TRAVELLER_CLOAK.get()));
        ArmorStandMenu menu = new ArmorStandMenu(0, player.getInventory(), stand);
        menu.quickMoveStack(player, 0);
        helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).is(Items.GOLDEN_HELMET), "player did not take the stand helmet");
        helper.assertTrue(stand.getItem(ArmorStandBlockEntity.ARMOR).is(Items.IRON_HELMET), "stand did not take the player helmet");
        menu.quickMoveStack(player, ArmorStandMenu.STAND_START + ArmorStandBlockEntity.GEAR);
        helper.assertTrue(Gear.get(player, GearSlot.CLOAK).is(TravellersGearFeature.TRAVELLER_CLOAK.get()), "player did not take the stand cloak");
        helper.assertTrue(stand.getItem(ArmorStandBlockEntity.GEAR).isEmpty(), "stand kept the cloak");
        helper.assertFalse(menu.slots.get(ArmorStandMenu.STAND_START).mayPlace(new ItemStack(Items.IRON_BOOTS)), "boots fit the helmet slot");
        helper.succeed();
    }
}
