package witchinggadgets.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import witchinggadgets.client.cloaks.CloakStorageScreen;
import witchinggadgets.client.spinning.SpinningWheelScreen;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.spinning.SpinningFeature;

public final class ClientFeatures {
    private ClientFeatures() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SpinningFeature.SPINNING_WHEEL_MENU.get(), SpinningWheelScreen::new);
        event.register(CloaksFeature.STORAGE_MENU.get(), CloakStorageScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    }
}
