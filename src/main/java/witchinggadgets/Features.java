package witchinggadgets;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import witchinggadgets.arcanestone.ArcaneStoneFeature;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.gadgets.GadgetsFeature;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.robes.RobesFeature;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.test.ArcaneStoneGameTests;
import witchinggadgets.test.ClothingGameTests;
import witchinggadgets.test.CloaksGameTests;
import witchinggadgets.test.DevicesGameTests;
import witchinggadgets.test.GadgetsGameTests;
import witchinggadgets.test.GemsGameTests;
import witchinggadgets.test.MaterialsGameTests;
import witchinggadgets.test.SpinningGameTests;

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
    }

    public static void aspects(AspectRegistry registry) {
        MaterialsFeature.aspects(registry);
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
    }
}
