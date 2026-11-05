package witchinggadgets.client.platform;

import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.GemCut;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.gems.InfusedGemItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgItemProperties {
    private WgItemProperties() {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
            GemsFeature.INFUSED_GEM.get(),
            WitchingGadgets.id("gem_cut"),
            (stack, level, entity, seed) -> InfusedGemItem.cut(stack) == GemCut.OVAL ? 1.0F : 0.0F
        ));
    }
}
