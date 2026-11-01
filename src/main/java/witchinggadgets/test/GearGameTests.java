package witchinggadgets.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;
import witchinggadgets.platform.gear.GearAttachments;
import witchinggadgets.platform.gear.GearInventory;
import witchinggadgets.platform.gear.GearMenu;

public final class GearGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("gear")
        .add("inventory_has_four_travellers_slots", GearGameTests::inventoryHasFourSlots)
        .add("plain_items_are_not_gear", GearGameTests::plainItemsAreNotGear)
        .add("menu_places_gear_slots_after_armor", GearGameTests::menuPlacesGearSlots);

    private GearGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void inventoryHasFourSlots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        GearInventory inventory = GearAttachments.inventory(player);
        helper.assertValueEqual(inventory.getContainerSize(), 4, "gear slot count");
        helper.assertValueEqual(GearSlot.CLOAK.descriptionKey(), "wg.desc.gearSlot.tg.0", "cloak slot lang key");
        helper.assertValueEqual(GearSlot.TITLE.descriptionKey(), "wg.desc.gearSlot.tg.3", "title slot lang key");
        helper.assertTrue(Gear.stacks(player).stream().allMatch(ItemStack::isEmpty), "fresh player has gear");
        helper.succeed();
    }

    private static void plainItemsAreNotGear(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        GearInventory inventory = GearAttachments.inventory(player);
        for (GearSlot slot : GearSlot.values()) {
            helper.assertFalse(inventory.canPlaceItem(slot.ordinal(), new ItemStack(Items.DIAMOND_CHESTPLATE)), "a chestplate fits the " + slot + " slot");
        }
        helper.assertValueEqual(inventory.getMaxStackSize(), 1, "gear stack size");
        helper.succeed();
    }

    private static void menuPlacesGearSlots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        GearMenu menu = new GearMenu(0, player.getInventory());
        for (GearSlot slot : GearSlot.values()) {
            var view = menu.slots.get(GearMenu.GEAR_START + slot.ordinal());
            helper.assertTrue(view.container == GearAttachments.inventory(player), slot + " slot is not backed by the gear inventory");
            helper.assertValueEqual(view.getContainerSlot(), slot.ordinal(), slot + " container index");
        }
        helper.assertValueEqual(menu.slots.size(), GearMenu.HOTBAR_END, "menu slot count");
        helper.succeed();
    }
}
