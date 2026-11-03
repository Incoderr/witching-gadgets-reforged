package witchinggadgets.client.platform;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.arcanestone.ArcaneStoneFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgBlockTints {
    private static final int WATER = 0xFF3F76E4;

    private WgBlockTints() {
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> WATER, ArcaneStoneFeature.STONE_EXTRUDER.get());
    }
}
