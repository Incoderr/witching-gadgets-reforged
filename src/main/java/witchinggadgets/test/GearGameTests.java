package witchinggadgets.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import top.theillusivec4.curios.api.CuriosApi;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

public final class GearGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("gear")
        .add("inventory_has_four_travellers_slots", GearGameTests::inventoryHasFourSlots)
        .add("gear_slots_keep_items", GearGameTests::slotsKeepItems);

    private GearGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void inventoryHasFourSlots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (GearSlot slot : GearSlot.values()) {
            helper.assertTrue(CuriosApi.getPlayerSlots(player).containsKey(Gear.curioId(slot)), "no curios slot " + Gear.curioId(slot));
        }
        helper.assertValueEqual(GearSlot.CLOAK.descriptionKey(), "wg.desc.gearSlot.tg.0", "cloak slot lang key");
        helper.assertTrue(Gear.stacks(player).stream().allMatch(ItemStack::isEmpty), "fresh player has gear");
        helper.succeed();
    }

    private static void slotsKeepItems(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Gear.set(player, GearSlot.TITLE, new ItemStack(Items.PAPER));
        helper.assertTrue(Gear.get(player, GearSlot.TITLE).is(Items.PAPER), "title slot lost its item");
        helper.assertTrue(Gear.get(player, GearSlot.CLOAK).isEmpty(), "cloak slot got the title");
        helper.succeed();
    }
}
