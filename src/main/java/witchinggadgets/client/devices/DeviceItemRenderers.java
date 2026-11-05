package witchinggadgets.client.devices;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import witchinggadgets.WitchingGadgets;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class DeviceItemRenderers {
    private DeviceItemRenderers() {
    }

    @SubscribeEvent
    static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(WitchingGadgets.id("essentia_pump"), EssentiaPumpItemRenderer.Unbaked.MAP_CODEC);
    }
}
