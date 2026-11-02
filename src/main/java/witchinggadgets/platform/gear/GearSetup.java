package witchinggadgets.platform.gear;

import net.neoforged.bus.api.IEventBus;
import witchinggadgets.platform.DoubleJumpPayload;

public final class GearSetup {
    private GearSetup() {
    }

    public static void register(IEventBus bus) {
        ActivateGearPayload.register();
        DoubleJumpPayload.register();
    }
}
