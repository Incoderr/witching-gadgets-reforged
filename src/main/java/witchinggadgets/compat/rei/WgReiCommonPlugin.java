package witchinggadgets.compat.rei;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;
import witchinggadgets.WitchingGadgets;

@REIPluginCommon
public final class WgReiCommonPlugin implements REICommonPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(WitchingGadgets.id("spinning_wheel"), SpinningDisplay.SERIALIZER);
        registry.register(WitchingGadgets.id("infernal_blast_furnace"), BlastFurnaceDisplay.SERIALIZER);
    }
}
