package witchinggadgets.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.devices.DevicesClient;

@Mod(value = WitchingGadgets.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WitchingGadgetsClient {
    public WitchingGadgetsClient(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, WgClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        ClientPayloadHandlers.register();
        DevicesClient.init();
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        ClientFeatures.registerScreens(event);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClientFeatures.registerRenderers(event);
    }
}
