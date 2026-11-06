package witchinggadgets.food;

import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;
import witchinggadgets.platform.WgTools;
import witchinggadgets.registry.ModItems;

public final class FoodFeature {
    public static final DeferredItem<ThaumiumShearsItem> THAUMIUM_SHEARS = ModItems.ITEMS.registerItem(
        "thaumium_shears", ThaumiumShearsItem::new, properties -> WgTools.shears(properties, 400, TcMaterials.REPAIRS_THAUMIUM)
    );
    public static final DeferredItem<net.minecraft.world.item.Item> SWEETWART = ModItems.ITEMS.registerItem(
        "sweetwart", net.minecraft.world.item.Item::new, properties -> Registration.food(properties, 2, 0.4F)
    );
    public static final DeferredItem<net.minecraft.world.item.Item> NETHERCAKE = ModItems.ITEMS.registerItem(
        "nethercake", net.minecraft.world.item.Item::new, properties -> Registration.food(properties, 6, 1.5F)
    );
    public static final DeferredItem<BrainJerkyItem> BRAIN_JERKY = ModItems.ITEMS.registerItem(
        "brain_jerky", BrainJerkyItem::new, properties -> Registration.food(properties, 6, 1.0F)
    );

    private FoodFeature() {
    }

    public static void load() {
    }

    public static void aspects(AspectRegistry registry) {
        registry.register(SWEETWART.get(), new AspectList().add(Aspect.FIRE, 1).add(Aspect.HUNGER, 1));
        registry.register(BRAIN_JERKY.get(), new AspectList().add(Aspect.MIND, 3).add(Aspect.HUNGER, 2).add(Aspect.FLESH, 2));
    }
}
