package witchinggadgets;

import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectRegistry;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.test.MaterialsGameTests;
import witchinggadgets.test.SpinningGameTests;

public final class Features {
    private Features() {
    }

    public static void load() {
        MaterialsFeature.load();
        SpinningFeature.load();
    }

    public static void research() {
        SpinningFeature.research();
    }

    public static void recipes() {
        SpinningFeature.recipes();
    }

    public static void aspects(AspectRegistry registry) {
        MaterialsFeature.aspects(registry);
    }

    public static void tests(IEventBus modEventBus) {
        MaterialsGameTests.register(modEventBus);
        SpinningGameTests.register(modEventBus);
    }
}
