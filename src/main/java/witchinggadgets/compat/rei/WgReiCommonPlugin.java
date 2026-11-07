package witchinggadgets.compat.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;

@REIPluginCommon
public final class WgReiCommonPlugin implements REIServerPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(WgRei.SPINNING, SpinningDisplay.SERIALIZER);
        registry.register(WgRei.BLAST_FURNACE, BlastFurnaceDisplay.SERIALIZER);
    }
}
