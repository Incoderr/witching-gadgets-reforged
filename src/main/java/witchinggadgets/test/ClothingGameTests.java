package witchinggadgets.test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;
import thaumcraft.test.TestSupport;
import witchinggadgets.bags.BagMenu;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.baubles.TitleItem;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;
import witchinggadgets.robes.RobesFeature;

public final class ClothingGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("clothing")
        .add("clothing_recipes_registered", ClothingGameTests::recipesRegistered)
        .add("bag_keeps_items", ClothingGameTests::bagKeepsItems)
        .add("bag_rejects_strangers", ClothingGameTests::bagRejectsStrangers)
        .add("shoulders_and_vambraces_modify_attributes", ClothingGameTests::attributes)
        .add("luck_ring_adds_fortune_and_looting", ClothingGameTests::luckRing)
        .add("lucky_coin_is_enchanted", ClothingGameTests::luckyCoin)
        .add("titles_have_three_variants", ClothingGameTests::titles)
        .add("bewitched_robes_discount_vis", ClothingGameTests::robes);

    private ClothingGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return TestSupport.mockServerPlayer(helper);
    }

    private static void recipesRegistered(GameTestHelper helper) {
        for (String key : new String[] {
            "BAGOFTRICKS_CLOTH", "BAGOFTRICKS_BAG", "ADVANCEDROBES_CLOTH", "ADVANCEDROBES_CHEST", "ADVANCEDROBES_LEGS",
            "WGBAUBLES_WOLFVAMBRACES", "WGBAUBLES_KNOCKBACKSHOULDERS", "WGBAUBLES_COIN", "WGBAUBLES_HASTEVAMBRACES",
            "WGBAUBLES_DOUBLEJUMPSHOULDERS", "WGBAUBLES_SNIPERRING", "WGBAUBLES_LUCKRING"
        }) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void bagKeepsItems(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack bag = new ItemStack(BagsFeature.BAG_OF_TRICKS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        bag.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.containerMenu instanceof BagMenu, "bag menu not opened");
        helper.assertValueEqual(BagsFeature.OWNER.getOrDefault(bag, ""), player.getGameProfile().getName(), "bag owner");
        player.containerMenu.getSlot(3).set(new ItemStack(Items.EMERALD, 7));
        ItemContainerContents contents = BagsFeature.CONTENTS.getOrDefault(bag, ItemContainerContents.EMPTY);
        helper.assertTrue(contents.stream().anyMatch(stack -> stack.is(Items.EMERALD) && stack.getCount() == 7), "emeralds not stored");
        helper.assertFalse(player.containerMenu.getSlot(0).mayPlace(new ItemStack(BagsFeature.BAG_OF_TRICKS.get())), "a bag fits into a bag");
        player.closeContainer();
        helper.succeed();
    }

    private static void bagRejectsStrangers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack bag = new ItemStack(BagsFeature.BAG_OF_TRICKS.get());
        BagsFeature.OWNER.set(bag, "SomeoneElse");
        player.setItemInHand(InteractionHand.MAIN_HAND, bag);
        bag.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertFalse(player.containerMenu instanceof BagMenu, "a stranger opened the bag");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the bag stayed in hand");
        helper.succeed();
    }

    private static void attributes(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Gear.set(player, GearSlot.PAULDRON, new ItemStack(BaublesFeature.KNOCKBACK_SHOULDERS.get()));
        Gear.set(player, GearSlot.VAMBRACE, new ItemStack(BaublesFeature.STRENGTH_VAMBRACES.get()));
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (GearSlot slot : new GearSlot[] {GearSlot.PAULDRON, GearSlot.VAMBRACE}) {
            ItemStack stack = Gear.get(player, slot);
            ((GearItem) stack.getItem()).onGearTick(stack, player);
        }
        helper.assertValueEqual(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.6, "knockback resistance");
        helper.assertValueEqual(player.getAttributeValue(Attributes.ATTACK_DAMAGE), damage + 2.0, "attack damage");
        ItemStack shoulders = Gear.get(player, GearSlot.PAULDRON);
        ((GearItem) shoulders.getItem()).onGearUnequipped(shoulders, player);
        Gear.set(player, GearSlot.PAULDRON, ItemStack.EMPTY);
        helper.assertValueEqual(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, "knockback resistance after unequip");
        helper.succeed();
    }

    private static void luckRing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        int ring = -1;
        for (int slot = 0; slot < Baubles.slotCount(); slot++) {
            if (Baubles.slotType(slot) == BaubleType.RING) {
                ring = slot;
                break;
            }
        }
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        Holder<Enchantment> fortune = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Holder<Enchantment> looting = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
        helper.assertValueEqual(pickaxe.getEnchantmentLevel(fortune), 0, "fortune without the ring");
        Baubles.set(player, ring, new ItemStack(BaublesFeature.LUCK_RING.get()));
        ItemStack worn = Baubles.get(player, ring);
        ((Bauble) worn.getItem()).onWornTick(worn, player);
        helper.assertValueEqual(pickaxe.getEnchantmentLevel(fortune), 2, "fortune with the ring");
        helper.assertValueEqual(pickaxe.getEnchantmentLevel(looting), 2, "looting with the ring");
        helper.assertValueEqual(new ItemStack(Items.DIAMOND_PICKAXE).getEnchantmentLevel(fortune), 0, "fortune on an item not held");
        Baubles.set(player, ring, ItemStack.EMPTY);
        helper.assertValueEqual(pickaxe.getEnchantmentLevel(fortune), 0, "fortune after removing the ring");
        helper.succeed();
    }

    private static void luckyCoin(GameTestHelper helper) {
        ItemStack coin = BaublesFeature.luckyCoin().create();
        helper.assertTrue(coin.isEnchanted(), "lucky coin has no enchantments");
        helper.assertTrue(TcIngredient.of(BaublesFeature.luckyCoin()).test(coin), "lucky coin does not match itself");
        helper.assertFalse(TcIngredient.of(BaublesFeature.luckyCoin()).test(new ItemStack(coin.getItem())), "a plain coin matches the lucky coin");
        helper.succeed();
    }

    private static void titles(GameTestHelper helper) {
        List<ItemStack> stacks = new ArrayList<>();
        ((TitleItem) BaublesFeature.CRIMSON_CULT_TITLE.get()).creativeStacks(stacks::add);
        helper.assertValueEqual(stacks.size(), 3, "title variants");
        helper.assertValueEqual(BaublesFeature.TITLE.getOrDefault(stacks.get(2), ""), TitleItem.PRAETOR, "praetor title");
        helper.succeed();
    }

    private static void robes(GameTestHelper helper) {
        ItemStack chest = new ItemStack(RobesFeature.BEWITCHED_ROBE.get());
        ItemStack legs = new ItemStack(RobesFeature.BEWITCHED_LEGGINGS.get());
        helper.assertValueEqual(RobesFeature.BEWITCHED_ROBE.get().getVisDiscount(chest, null, null), 5, "robe discount");
        helper.assertValueEqual(RobesFeature.BEWITCHED_LEGGINGS.get().getVisDiscount(legs, null, null), 4, "leggings discount");
        helper.succeed();
    }
}
