package witchinggadgets.platform.gear;

import net.neoforged.bus.api.IEventBus;

public final class GearSetup {
    private GearSetup() {
    }

    public static void register(IEventBus bus) {
        GearAttachments.register(bus);
        GearMenus.register(bus);
        OpenGearPayload.register();
        ActivateGearPayload.register();
    }
}
