package witchinggadgets.client.platform;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import witchinggadgets.WitchingGadgets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.cloaks.MagicCloakItem;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.robes.RobesFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgItemTints {
    private static final int BAG_DEFAULT = 0x8A74BD;
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
        event.register((stack, layer) -> layer == 0 ? DyedItemColor.getOrDefault(stack, BAG_DEFAULT) | 0xFF000000 : -1, BagsFeature.BAG_OF_TRICKS.get());
        event.register((stack, layer) -> layer == 0 ? DyedItemColor.getOrDefault(stack, ROBE_DEFAULT) | 0xFF000000 : -1, RobesFeature.BEWITCHED_ROBE.get(), RobesFeature.BEWITCHED_LEGGINGS.get());
    }

    @SubscribeEvent
    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return DyedItemColor.getOrDefault(stack, ROBE_DEFAULT) | 0xFF000000;
            }
        }, RobesFeature.BEWITCHED_ROBE.get(), RobesFeature.BEWITCHED_LEGGINGS.get());
    }
}
