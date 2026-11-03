package witchinggadgets.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.client.bags.BagScreen;
import witchinggadgets.client.bags.VoidBagScreen;
import witchinggadgets.client.cloaks.CloakStorageScreen;
import witchinggadgets.client.devices.LabelLibraryRenderer;
import witchinggadgets.client.devices.LabelLibraryScreen;
import witchinggadgets.client.spinning.SpinningWheelScreen;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.spinning.SpinningFeature;

public final class ClientFeatures {
    private ClientFeatures() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SpinningFeature.SPINNING_WHEEL_MENU.get(), SpinningWheelScreen::new);
        event.register(CloaksFeature.STORAGE_MENU.get(), CloakStorageScreen::new);
        event.register(BagsFeature.BAG_MENU.get(), BagScreen::new);
        event.register(BagsFeature.VOID_BAG_MENU.get(), VoidBagScreen::new);
        event.register(DevicesFeature.LABEL_LIBRARY_MENU.get(), LabelLibraryScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DevicesFeature.LABEL_LIBRARY_ENTITY.get(), LabelLibraryRenderer::new);
    }
}
