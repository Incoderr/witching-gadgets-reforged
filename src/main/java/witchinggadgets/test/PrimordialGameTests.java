package witchinggadgets.test;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import witchinggadgets.effects.EffectsFeature;
import witchinggadgets.primordial.PrimordialWeaponEvents;
import witchinggadgets.primordial.PrimordialWeaponItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModEffects;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.primordial.PrimordialArmorItem;
import witchinggadgets.primordial.PrimordialGear;
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
        .add("aer_deflects_projectiles", PrimordialGameTests::deflect)
        .add("weapon_recipes_registered", PrimordialGameTests::weaponRecipes)
        .add("weapon_affinities_afflict_targets", PrimordialGameTests::strikes)
        .add("hammer_and_greataxe_multipliers", PrimordialGameTests::multipliers)
        .add("claymore_takes_heads", PrimordialGameTests::heads)
        .add("hammer_breaks_three_by_three", PrimordialGameTests::area)
        .add("dissolve_and_cinder_coat", PrimordialGameTests::effects)
        .add("weapons_block_attacks", PrimordialGameTests::blocking);

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
        PrimordialGear.setAbility(stack, ability);
        player.setItemSlot(slot, stack);
    }

    private static void cycle(GameTestHelper helper) {
        ItemStack helmet = new ItemStack(PrimordialFeature.PRIMORDIAL_HELMET.get());
        helper.assertValueEqual(PrimordialGear.ability(helmet), PrimordialGear.AIR, "default ability");
        PrimordialGear.cycle(helmet);
        helper.assertValueEqual(PrimordialGear.ability(helmet), PrimordialGear.EARTH, "cycled ability");
        Player player = armored(helper, PrimordialGear.ENTROPY);
        helper.assertTrue(PrimordialArmorItem.cycleWorn(player), "worn armor did not cycle");
        for (ItemStack piece : PrimordialArmorItem.worn(player)) {
            helper.assertValueEqual(PrimordialGear.ability(piece), PrimordialGear.AIR, "worn ability wraps to Aer");
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
        Player player = armored(helper, PrimordialGear.WATER);
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
        Player ignis = armored(helper, PrimordialGear.FIRE);
        near(helper, PrimordialEvents.reduction(ignis, helper.getLevel().damageSources().inFire()), 1.0F, "ignis fire reduction");
        near(helper, PrimordialEvents.reduction(ignis, helper.getLevel().damageSources().magic()), 0.0F, "ignis magic reduction");
        Player terra = armored(helper, PrimordialGear.EARTH);
        near(helper, PrimordialEvents.reduction(terra, helper.getLevel().damageSources().starve()), 19.0F / 50.0F * 1.375F, "terra bypass reduction");
        near(helper, PrimordialEvents.reduction(terra, helper.getLevel().damageSources().inFire()), 0.0F, "terra fire reduction");
        helper.succeed();
    }

    private static void ordo(GameTestHelper helper) {
        Player player = armored(helper, PrimordialGear.ORDER);
        player.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 500, 2));
        MobEffectInstance effect = player.getEffect(ModEffects.VIS_EXHAUST);
        helper.assertTrue(effect != null, "warp effect was not applied");
        helper.assertValueEqual(effect.getAmplifier(), 0, "softened amplifier");
        helper.assertValueEqual(effect.getDuration(), 100, "softened duration");
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 2));
        helper.assertValueEqual(player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier(), 2, "plain effect softened");
        helper.succeed();
    }

    private static void deflect(GameTestHelper helper) {
        Player player = armored(helper, PrimordialGear.AIR);
        player.setPos(helper.absoluteVec(new Vec3(3.5, 1.0, 3.5)));
        Zombie shooter = helper.spawn(EntityType.ZOMBIE, new Vec3(3.5, 1.0, 0.5));
        shooter.setNoAi(true);
        Arrow arrow = helper.spawn(EntityType.ARROW, new Vec3(3.5, 1.5, 2.0));
        arrow.setOwner(shooter);
        arrow.setDeltaMovement(0.0, 0.0, 1.5);
        PrimordialEvents.deflect(player);
        helper.assertTrue(arrow.getDeltaMovement().z < 0.0, "arrow was not turned back: " + arrow.getDeltaMovement());
        helper.succeed();
    }

    private static void weaponRecipes(GameTestHelper helper) {
        for (String key : new String[] {"PRIMORDIALWEAPONRY_CLAYMORE", "PRIMORDIALWEAPONRY_HAMMER", "PRIMORDIALWEAPONRY_GREATAXE"}) {
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionRecipe, "recipe " + key + " is missing");
        }
        ItemStack sword = ((InfusionRecipe) TcRecipes.get("PRIMORDIALWEAPONRY_CLAYMORE")).result().create();
        helper.assertTrue(PrimordialFeature.PEARLS_PENDING.getOrDefault(sword, false), "crafted weapon does not return pearls");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().add(sword);
        PrimordialEvents.serverTick(player);
        helper.assertValueEqual(player.getInventory().countItem(MaterialsFeature.POWERLESS_PEARL.get()), 2, "weapon pearls");
        helper.succeed();
    }

    private static ItemStack weapon(Item item, int ability) {
        ItemStack stack = new ItemStack(item);
        PrimordialGear.setAbility(stack, ability);
        return stack;
    }

    private static Zombie zombie(GameTestHelper helper, double x, double z) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new Vec3(x, 1.0, z));
        zombie.setNoAi(true);
        return zombie;
    }

    private static void strikes(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Zombie target = zombie(helper, 2.5, 2.5);
        PrimordialWeaponEvents.strike(helper.getLevel(), player, weapon(PrimordialFeature.PRIMORDIAL_SWORD.get(), PrimordialGear.FIRE), target);
        helper.assertTrue(target.hasEffect(EffectsFeature.CINDER_COAT) && target.isOnFire(), "ignis did not burn");
        PrimordialWeaponEvents.strike(helper.getLevel(), player, weapon(PrimordialFeature.PRIMORDIAL_SWORD.get(), PrimordialGear.WATER), target);
        helper.assertTrue(target.hasEffect(EffectsFeature.DISSOLVE), "aqua did not dissolve");
        PrimordialWeaponEvents.strike(helper.getLevel(), player, weapon(PrimordialFeature.PRIMORDIAL_HAMMER.get(), PrimordialGear.ENTROPY), target);
        helper.assertTrue(target.hasEffect(MobEffects.WEAKNESS) && target.hasEffect(MobEffects.HUNGER), "perditio did not weaken");
        helper.assertTrue(target.hasEffect(MobEffects.CONFUSION), "hammer did not daze");
        target.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        PrimordialWeaponEvents.strike(helper.getLevel(), player, weapon(PrimordialFeature.PRIMORDIAL_AXE.get(), PrimordialGear.ORDER), target);
        helper.assertValueEqual(target.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 5, "greataxe armor wear");
        Zombie neighbour = zombie(helper, 3.5, 2.5);
        float health = neighbour.getHealth();
        player.setItemSlot(EquipmentSlot.MAINHAND, weapon(PrimordialFeature.PRIMORDIAL_SWORD.get(), PrimordialGear.AIR));
        PrimordialWeaponEvents.strike(helper.getLevel(), player, player.getMainHandItem(), target);
        helper.assertTrue(neighbour.getHealth() < health, "aer did not sweep");
        helper.succeed();
    }

    private static void multipliers(GameTestHelper helper) {
        Zombie zombie = zombie(helper, 2.5, 2.5);
        Spider spider = helper.spawn(EntityType.SPIDER, new Vec3(4.5, 1.0, 4.5));
        DamageSource source = helper.getLevel().damageSources().mobAttack(zombie);
        near(helper, PrimordialWeaponEvents.multiplier(new ItemStack(PrimordialFeature.PRIMORDIAL_HAMMER.get()), spider, source), 2.0F, "hammer vs spider");
        near(helper, PrimordialWeaponEvents.multiplier(new ItemStack(PrimordialFeature.PRIMORDIAL_HAMMER.get()), zombie, source), 1.0F, "hammer vs zombie");
        zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        near(helper, PrimordialWeaponEvents.multiplier(new ItemStack(PrimordialFeature.PRIMORDIAL_AXE.get()), zombie, source), 2.0F, "greataxe vs armor");
        helper.succeed();
    }

    private static void heads(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(PrimordialWeaponEvents.head(zombie(helper, 2.5, 2.5), player).is(Items.ZOMBIE_HEAD), "zombie head");
        Creeper creeper = helper.spawn(EntityType.CREEPER, new Vec3(4.5, 1.0, 4.5));
        helper.assertTrue(PrimordialWeaponEvents.head(creeper, player).is(Items.CREEPER_HEAD), "creeper head");
        helper.succeed();
    }

    private static void area(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hammer = new ItemStack(PrimordialFeature.PRIMORDIAL_HAMMER.get());
        player.setItemSlot(EquipmentSlot.MAINHAND, hammer);
        BlockPos center = new BlockPos(3, 3, 3);
        for (int x = 2; x <= 4; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(x, y, 3), Blocks.STONE);
            }
        }
        helper.setBlock(new BlockPos(4, 4, 3), Blocks.OAK_PLANKS);
        PrimordialWeaponEvents.breakArea(helper.getLevel(), player, hammer, helper.absolutePos(center), Direction.NORTH, PrimordialWeaponItem.HAMMER_MINEABLE);
        helper.assertBlockPresent(Blocks.STONE, center);
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 2, 3));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 3, 3));
        helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(4, 4, 3));
        helper.succeed();
    }

    private static void effects(GameTestHelper helper) {
        Zombie zombie = zombie(helper, 2.5, 2.5);
        zombie.addEffect(new MobEffectInstance(EffectsFeature.CINDER_COAT, 100, 1));
        near(helper, EffectsFeature.cinderMultiplier(zombie), 3.0F, "cinder multiplier");
        helper.assertTrue(EffectsFeature.DISSOLVE.value().shouldApplyEffectTickThisTick(20, 2), "dissolve tick at amplifier 2");
        helper.assertFalse(EffectsFeature.DISSOLVE.value().shouldApplyEffectTickThisTick(25, 2), "dissolve tick rate");
        helper.succeed();
    }

    private static void blocking(GameTestHelper helper) {
        for (Item item : new Item[] {PrimordialFeature.PRIMORDIAL_SWORD.get(), PrimordialFeature.PRIMORDIAL_HAMMER.get(), PrimordialFeature.PRIMORDIAL_AXE.get()}) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(stack.getUseAnimation() == UseAnim.BLOCK, "use animation of " + item);
        }
        helper.succeed();
    }
}
