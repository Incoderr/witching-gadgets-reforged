package witchinggadgets.client.platform;

import java.util.List;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.arcanestone.ArcaneStoneFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgBlockTints {
    private static final int WATER = 0x3F76E4;

    private WgBlockTints() {
    }

    @SubscribeEvent
    static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.constant(ARGB.opaque(WATER))), ArcaneStoneFeature.STONE_EXTRUDER.get());
    }
}
