package witchinggadgets;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import witchinggadgets.alchemy.AlchemyFeature;
import witchinggadgets.arcanestone.ArcaneStoneFeature;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.effects.EffectsFeature;
import witchinggadgets.enchantments.EnchantmentsFeature;
import witchinggadgets.food.FoodFeature;
import witchinggadgets.furnace.FurnaceFeature;
import witchinggadgets.gadgets.GadgetsFeature;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.robes.RobesFeature;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.terraform.TerraformFeature;
import witchinggadgets.test.ArcaneStoneGameTests;
import witchinggadgets.test.ClothingGameTests;
import witchinggadgets.test.CloaksGameTests;
import witchinggadgets.test.DevicesGameTests;
import witchinggadgets.test.EnchantmentsGameTests;
import witchinggadgets.test.FoodGameTests;
import witchinggadgets.test.FurnaceGameTests;
import witchinggadgets.test.GadgetsGameTests;
import witchinggadgets.test.GemsGameTests;
import witchinggadgets.test.AlchemyGameTests;
import witchinggadgets.test.MaterialsGameTests;
import witchinggadgets.test.PrimordialGameTests;
import witchinggadgets.test.SpinningGameTests;
import witchinggadgets.test.TerraformGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        MaterialsFeature.load();
        SpinningFeature.load();
        CloaksFeature.load();
        BagsFeature.load();
        BaublesFeature.load();
        RobesFeature.load();
        ArcaneStoneFeature.load();
        DevicesFeature.load();
        GadgetsFeature.load();
        GemsFeature.load();
        EnchantmentsFeature.load();
        PrimordialFeature.load();
        EffectsFeature.load();
        AlchemyFeature.load();
        FurnaceFeature.load();
        TerraformFeature.load();
        FoodFeature.load();
    }

    public static void research() {
        SpinningFeature.research();
        CloaksFeature.research();
        BagsFeature.research();
        BaublesFeature.research();
        RobesFeature.research();
        ArcaneStoneFeature.research();
        DevicesFeature.research();
        GadgetsFeature.research();
        GemsFeature.research();
        EnchantmentsFeature.research();
        PrimordialFeature.research();
        EffectsFeature.research();
        AlchemyFeature.research();
        FurnaceFeature.research();
        TerraformFeature.research();
    }

    public static void recipes() {
        SpinningFeature.recipes();
        CloaksFeature.recipes();
        BagsFeature.recipes();
        BaublesFeature.recipes();
        RobesFeature.recipes();
        ArcaneStoneFeature.recipes();
        DevicesFeature.recipes();
        GadgetsFeature.recipes();
        GemsFeature.recipes();
        EnchantmentsFeature.recipes();
        PrimordialFeature.recipes();
        AlchemyFeature.recipes();
        FurnaceFeature.recipes();
        TerraformFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
        MaterialsFeature.aspects(registry);
        AlchemyFeature.aspects(registry);
        FoodFeature.aspects(registry);
    }

    public static void tests(IEventBus modEventBus) {
        MaterialsGameTests.register(modEventBus);
        SpinningGameTests.register(modEventBus);
        CloaksGameTests.register(modEventBus);
        ClothingGameTests.register(modEventBus);
        ArcaneStoneGameTests.register(modEventBus);
        DevicesGameTests.register(modEventBus);
        GadgetsGameTests.register(modEventBus);
        GemsGameTests.register(modEventBus);
        EnchantmentsGameTests.register(modEventBus);
        PrimordialGameTests.register(modEventBus);
        AlchemyGameTests.register(modEventBus);
        FurnaceGameTests.register(modEventBus);
        TerraformGameTests.register(modEventBus);
        FoodGameTests.register(modEventBus);
    }
}
