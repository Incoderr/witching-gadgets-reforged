package witchinggadgets.registry;

import net.neoforged.bus.api.IEventBus;

public final class ModItems {
    public static final witchinggadgets.platform.WgRegistration.Items ITEMS = new witchinggadgets.platform.WgRegistration.Items();

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
