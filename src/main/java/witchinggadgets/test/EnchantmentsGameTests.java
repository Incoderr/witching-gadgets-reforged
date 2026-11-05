package witchinggadgets.test;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModItems;
import witchinggadgets.enchantments.EnchantmentEvents;
import witchinggadgets.enchantments.EnchantmentsFeature;
import witchinggadgets.gems.InfusedGemItem;

public final class EnchantmentsGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("enchantments")
        .add("recipes_registered", EnchantmentsGameTests::recipes)
        .add("enchantments_fit_their_items", EnchantmentsGameTests::supported)
        .add("backstab_multiplies_damage_from_behind", EnchantmentsGameTests::backstab)
        .add("stealth_drops_targets_of_sneaking_players", EnchantmentsGameTests::stealth)
        .add("ride_protect_throws_off_riders", EnchantmentsGameTests::rideProtect);

    private EnchantmentsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"ENCH_INVISIBLEGEAR", "ENCH_UNVEILING", "ENCH_STEALTH", "ENCH_BACKSTAB", "ENCH_RIDEPROTECT"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static Holder<Enchantment> holder(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static void fits(GameTestHelper helper, ResourceKey<Enchantment> key, Item item, boolean expected) {
        helper.assertValueEqual(holder(helper, key).value().isSupportedItem(new ItemStack(item)), expected, key.toString() + " on " + item);
    }

    private static void supported(GameTestHelper helper) {
        fits(helper, EnchantmentsFeature.INVISIBLE_GEAR, Items.IRON_CHESTPLATE, true);
        fits(helper, EnchantmentsFeature.UNVEILING, ModItems.GOGGLES_OF_REVEALING.get(), true);
        fits(helper, EnchantmentsFeature.UNVEILING, Items.IRON_HELMET, false);
        fits(helper, EnchantmentsFeature.STEALTH, Items.IRON_BOOTS, true);
        fits(helper, EnchantmentsFeature.STEALTH, Items.IRON_LEGGINGS, true);
        fits(helper, EnchantmentsFeature.STEALTH, Items.IRON_CHESTPLATE, false);
        fits(helper, EnchantmentsFeature.BACKSTAB, Items.IRON_SWORD, true);
        fits(helper, EnchantmentsFeature.BACKSTAB, Items.IRON_AXE, true);
        fits(helper, EnchantmentsFeature.BACKSTAB, Items.IRON_PICKAXE, false);
        fits(helper, EnchantmentsFeature.RIDE_PROTECT, Items.IRON_HELMET, true);
        helper.assertFalse(
            Enchantment.areCompatible(holder(helper, EnchantmentsFeature.BACKSTAB), helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SMITE)),
            "backstab and smite are compatible"
        );
        helper.succeed();
    }

    private static void near(GameTestHelper helper, float actual, float expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-4F, what + ": expected " + expected + ", got " + actual);
    }

    private static Zombie zombie(GameTestHelper helper, float yaw) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(3.5, 1.0, 3.5));
        zombie.setNoAi(true);
        zombie.setYRot(yaw);
        zombie.setYHeadRot(yaw);
        zombie.setXRot(0.0F);
        return zombie;
    }

    private static void backstab(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setYRot(30.0F);
        player.setXRot(0.0F);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        InfusedGemItem.enchant(helper.getLevel(), sword, EnchantmentsFeature.BACKSTAB, 2);
        player.setItemSlot(EquipmentSlot.MAINHAND, sword);
        Zombie behind = zombie(helper, 30.0F);
        near(helper, EnchantmentEvents.backstab(player, behind), 1.8F, "unaware backstab");
        behind.setTarget(player);
        near(helper, EnchantmentEvents.backstab(player, behind), 1.4F, "aware backstab");
        Zombie facing = zombie(helper, 210.0F);
        near(helper, EnchantmentEvents.backstab(player, facing), 1.0F, "frontal attack");
        helper.succeed();
    }

    private static void stealth(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setYRot(30.0F);
        player.setXRot(0.0F);
        ItemStack boots = new ItemStack(Items.IRON_BOOTS);
        ItemStack legs = new ItemStack(Items.IRON_LEGGINGS);
        InfusedGemItem.enchant(helper.getLevel(), boots, EnchantmentsFeature.STEALTH, 5);
        InfusedGemItem.enchant(helper.getLevel(), legs, EnchantmentsFeature.STEALTH, 5);
        player.setItemSlot(EquipmentSlot.FEET, boots);
        player.setItemSlot(EquipmentSlot.LEGS, legs);
        Zombie zombie = zombie(helper, 30.0F);
        near(helper, EnchantmentEvents.stealthChance(player, zombie), 1.0F, "stealth chance from behind");
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "standing player was not targeted");
        zombie.setTarget(null);
        player.setShiftKeyDown(true);
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == null, "sneaking player was targeted");
        helper.succeed();
    }

    private static void rideProtect(GameTestHelper helper) {
        Zombie vehicle = zombie(helper, 0.0F);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        InfusedGemItem.enchant(helper.getLevel(), helmet, EnchantmentsFeature.RIDE_PROTECT, 1);
        vehicle.setItemSlot(EquipmentSlot.HEAD, helmet);
        Chicken rider = helper.spawn(EntityType.CHICKEN, new Vec3(3.5, 1.0, 3.5));
        helper.assertTrue(rider.startRiding(vehicle, true), "chicken could not ride");
        float health = rider.getHealth();
        EnchantmentEvents.throwOff(helper.getLevel(), vehicle);
        helper.assertFalse(vehicle.isVehicle(), "rider stayed on");
        helper.assertTrue(rider.getHealth() < health, "rider was not hurt");
        helper.succeed();
    }
}
