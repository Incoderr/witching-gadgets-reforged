package witchinggadgets.client.platform;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import witchinggadgets.WitchingGadgets;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import thaumcraft.aspect.Aspect;
import witchinggadgets.gems.CrystalCapsuleItem;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.alchemy.AlchemyFeature;
import witchinggadgets.bags.BagItem;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.cloaks.MagicCloakItem;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.primordial.PrimordialArmorItem;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.primordial.PrimordialGear;
import witchinggadgets.robes.RobesFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgItemTints {
    private static final int ROBE_DEFAULT = 6961280;

    private WgItemTints() {
    }

    @SubscribeEvent
    static void registerItemTints(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? 0xFF888888 : -1, MaterialsFeature.SPACIOUS_CLOTH.get());
        event.register((stack, layer) -> layer == 0 ? 0xFFE6BE44 : -1, MaterialsFeature.GOLDEN_CLOTH.get());
        event.register((stack, layer) -> layer == 0 ? 0xFF9700C0 : -1, MaterialsFeature.BEWITCHED_CLOTH.get());
        event.register((stack, layer) -> layer == 0 && stack.getItem() instanceof MagicCloakItem cloak ? 0xFF000000 | cloak.color(stack) : -1,
            CloaksFeature.CLOAK.get(), CloaksFeature.SPECTRAL_CLOAK.get(), CloaksFeature.STORAGE_CLOAK.get(),
            CloaksFeature.KAMA.get(), CloaksFeature.SPECTRAL_KAMA.get(), CloaksFeature.STORAGE_KAMA.get());
        event.register((stack, layer) -> layer == 0 && stack.getItem() instanceof BagItem bag ? DyedItemColor.getOrDefault(stack, bag.kind().color()) | 0xFF000000 : -1,
            BagsFeature.BAG_OF_TRICKS.get(), BagsFeature.VOID_BAG.get(), BagsFeature.ENDER_BAG.get(), BagsFeature.HUNGRY_BAG.get());
        event.register((stack, layer) -> layer == 0 ? DyedItemColor.getOrDefault(stack, ROBE_DEFAULT) | 0xFF000000 : -1, RobesFeature.BEWITCHED_ROBE.get(), RobesFeature.BEWITCHED_LEGGINGS.get());
        event.register(
            (stack, layer) -> layer == 0 ? 0xFF000000 | PrimordialGear.abilityAspect(stack).getColor() : -1,
            PrimordialFeature.PRIMORDIAL_HELMET.get(), PrimordialFeature.PRIMORDIAL_CHESTPLATE.get(), PrimordialFeature.PRIMORDIAL_LEGGINGS.get(), PrimordialFeature.PRIMORDIAL_BOOTS.get()
        );
        event.register((stack, layer) -> layer == 0 ? 0xFF48B518 : -1, AlchemyFeature.ROSE_VINE.get());
        event.register(
            (stack, layer) -> layer == 1 ? 0xFF000000 | PrimordialGear.abilityAspect(stack).getColor() : -1,
            PrimordialFeature.PRIMORDIAL_SWORD.get(), PrimordialFeature.PRIMORDIAL_HAMMER.get(), PrimordialFeature.PRIMORDIAL_AXE.get()
        );
        event.register((stack, layer) -> {
            Aspect aspect = InfusedGemItem.aspect(stack);
            return layer == 0 && aspect != null ? 0xFF000000 | aspect.getColor() : -1;
        }, GemsFeature.INFUSED_GEM.get());
        event.register((stack, layer) -> {
            if (layer != 0) {
                return -1;
            }
            Fluid fluid = CrystalCapsuleItem.fluid(stack);
            return fluid == null ? 0x00FFFFFF : fluidColor(fluid);
        }, GemsFeature.CRYSTAL_CAPSULE.get());
    }

    public static int fluidColor(Fluid fluid) {
        int tint = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if ((tint & 0xFFFFFF) != 0xFFFFFF) {
            return 0xFF000000 | tint;
        }
        if (fluid.isSame(Fluids.LAVA)) {
            return 0xFFD96415;
        }
        return 0xFF000000 | fluid.defaultFluidState().createLegacyBlock().getBlock().defaultMapColor().col;
    }

}
