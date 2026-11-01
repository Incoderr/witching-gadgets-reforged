package witchinggadgets;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import thaumcraft.aspect.AspectSync;
import witchinggadgets.network.ModPayloads;
import witchinggadgets.platform.gear.GearSetup;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModCreativeTabs;
import witchinggadgets.registry.ModEffects;
import witchinggadgets.registry.ModEntities;
import witchinggadgets.registry.ModItems;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.registry.ModSounds;
import witchinggadgets.research.BaseResearchLinks;
import witchinggadgets.research.WgResearch;
import witchinggadgets.test.CoreGameTests;
import witchinggadgets.test.GearGameTests;
import witchinggadgets.test.ResearchTableGameTests;
import witchinggadgets.test.SmokeGameTests;

@Mod(WitchingGadgets.MODID)
public class WitchingGadgets {
    public static final String MODID = "witchinggadgets";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WitchingGadgets(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        Features.load();
        ModEffects.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModMenus.register(modEventBus);
        ModPayloads.register();
        GearSetup.register(modEventBus);
        WgResearch.init();
        Features.recipes();
        BaseResearchLinks.register();
        Features.research();
        AspectSync.addRegistrar(Features::aspects);
        SmokeGameTests.register(modEventBus);
        CoreGameTests.register(modEventBus);
        GearGameTests.register(modEventBus);
        ResearchTableGameTests.register(modEventBus);
        Features.tests(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
