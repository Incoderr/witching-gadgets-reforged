package witchinggadgets.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModEffects;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.primordial.PrimordialArmorItem;
import witchinggadgets.primordial.PrimordialEvents;
import witchinggadgets.primordial.PrimordialFeature;

public final class PrimordialGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("primordial")
        .add("recipes_registered", PrimordialGameTests::recipes)
        .add("abilities_cycle", PrimordialGameTests::cycle)
        .add("crafted_armor_returns_pearls", PrimordialGameTests::pearls)
        .add("armor_repairs_and_cures", PrimordialGameTests::repairAndCure)
        .add("ignis_and_terra_reduce_damage", PrimordialGameTests::reduction)
        .add("ordo_softens_warp_effects", PrimordialGameTests::ordo)
        .add("aer_deflects_projectiles", PrimordialGameTests::deflect);

    private PrimordialGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"EMPOWERPEARL", "PRIMORDIALARMOR_HELMET", "PRIMORDIALARMOR_CUIRASS", "PRIMORDIALARMOR_GREAVES", "PRIMORDIALARMOR_BOOTS"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        ItemStack result = ((InfusionRecipe) TcRecipes.get("PRIMORDIALARMOR_HELMET")).result().create();
        helper.assertTrue(PrimordialFeature.PEARLS_PENDING.getOrDefault(result, false), "crafted armor does not return pearls");
        helper.succeed();
    }

    private static Player armored(GameTestHelper helper, int ability) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        equip(player, EquipmentSlot.HEAD, new ItemStack(PrimordialFeature.PRIMORDIAL_HELMET.get()), ability);
        equip(player, EquipmentSlot.CHEST, new ItemStack(PrimordialFeature.PRIMORDIAL_CHESTPLATE.get()), ability);
        equip(player, EquipmentSlot.LEGS, new ItemStack(PrimordialFeature.PRIMORDIAL_LEGGINGS.get()), ability);
        equip(player, EquipmentSlot.FEET, new ItemStack(PrimordialFeature.PRIMORDIAL_BOOTS.get()), ability);
        return player;
    }

    private static void equip(Player player, EquipmentSlot slot, ItemStack stack, int ability) {
        PrimordialArmorItem.setAbility(stack, ability);
        player.setItemSlot(slot, stack);
    }

    private static void cycle(GameTestHelper helper) {
        ItemStack helmet = new ItemStack(PrimordialFeature.PRIMORDIAL_HELMET.get());
        helper.assertValueEqual(PrimordialArmorItem.ability(helmet), PrimordialArmorItem.AIR, "default ability");
        PrimordialArmorItem.cycle(helmet);
        helper.assertValueEqual(PrimordialArmorItem.ability(helmet), PrimordialArmorItem.EARTH, "cycled ability");
        Player player = armored(helper, PrimordialArmorItem.ENTROPY);
        helper.assertTrue(PrimordialArmorItem.cycleWorn(player), "worn armor did not cycle");
        for (ItemStack piece : PrimordialArmorItem.worn(player)) {
            helper.assertValueEqual(PrimordialArmorItem.ability(piece), PrimordialArmorItem.AIR, "worn ability wraps to Aer");
        }
        helper.succeed();
    }

    private static void pearls(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack crafted = new ItemStack(PrimordialFeature.PRIMORDIAL_CHESTPLATE.get());
        PrimordialFeature.PEARLS_PENDING.set(crafted, true);
        player.getInventory().add(crafted);
        PrimordialEvents.serverTick(player);
        helper.assertValueEqual(player.getInventory().countItem(MaterialsFeature.POWERLESS_PEARL.get()), 3, "returned pearls");
        PrimordialEvents.serverTick(player);
        helper.assertValueEqual(player.getInventory().countItem(MaterialsFeature.POWERLESS_PEARL.get()), 3, "pearls returned twice");
        helper.succeed();
    }

    private static void repairAndCure(GameTestHelper helper) {
        Player player = armored(helper, PrimordialArmorItem.WATER);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        chest.setDamageValue(10);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        player.tickCount = 40;
        PrimordialEvents.serverTick(player);
        helper.assertValueEqual(chest.getDamageValue(), 9, "worn piece repair");
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "aqua did not cure poison");
        helper.succeed();
    }

    private static void near(GameTestHelper helper, float actual, float expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1.0E-4F, what + ": expected " + expected + ", got " + actual);
    }

    private static void reduction(GameTestHelper helper) {
        Player ignis = armored(helper, PrimordialArmorItem.FIRE);
        near(helper, PrimordialEvents.reduction(ignis, helper.getLevel().damageSources().inFire()), 1.0F, "ignis fire reduction");
        near(helper, PrimordialEvents.reduction(ignis, helper.getLevel().damageSources().magic()), 0.0F, "ignis magic reduction");
        Player terra = armored(helper, PrimordialArmorItem.EARTH);
        near(helper, PrimordialEvents.reduction(terra, helper.getLevel().damageSources().starve()), 19.0F / 50.0F * 1.375F, "terra bypass reduction");
        near(helper, PrimordialEvents.reduction(terra, helper.getLevel().damageSources().inFire()), 0.0F, "terra fire reduction");
        helper.succeed();
    }

    private static void ordo(GameTestHelper helper) {
        Player player = armored(helper, PrimordialArmorItem.ORDER);
        player.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 500, 2));
        MobEffectInstance effect = player.getEffect(ModEffects.VIS_EXHAUST);
        helper.assertTrue(effect != null, "warp effect was not applied");
        helper.assertValueEqual(effect.getAmplifier(), 0, "softened amplifier");
        helper.assertValueEqual(effect.getDuration(), 100, "softened duration");
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, 2));
        helper.assertValueEqual(player.getEffect(MobEffects.SPEED).getAmplifier(), 2, "plain effect softened");
        helper.succeed();
    }

    private static void deflect(GameTestHelper helper) {
        Player player = armored(helper, PrimordialArmorItem.AIR);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 1.0, 3.5)));
        Zombie shooter = helper.spawn(EntityTypes.ZOMBIE, new Vec3(3.5, 1.0, 0.5));
        shooter.setNoAi(true);
        Arrow arrow = helper.spawn(EntityTypes.ARROW, new Vec3(3.5, 1.5, 2.0));
        arrow.setOwner(shooter);
        arrow.setDeltaMovement(0.0, 0.0, 1.5);
        PrimordialEvents.deflect(player);
        helper.assertTrue(arrow.getDeltaMovement().z < 0.0, "arrow was not turned back: " + arrow.getDeltaMovement());
        helper.succeed();
    }
}
