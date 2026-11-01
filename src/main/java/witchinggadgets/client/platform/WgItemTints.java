package witchinggadgets.client.platform;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.materials.MaterialsFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgItemTints {
    private WgItemTints() {
    }

    @SubscribeEvent
    static void registerItemTints(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0 ? 0xFF888888 : -1, MaterialsFeature.SPACIOUS_CLOTH.get());
        event.register((stack, layer) -> layer == 0 ? 0xFFE6BE44 : -1, MaterialsFeature.GOLDEN_CLOTH.get());
        event.register((stack, layer) -> layer == 0 ? 0xFF9700C0 : -1, MaterialsFeature.BEWITCHED_CLOTH.get());
    }
}
