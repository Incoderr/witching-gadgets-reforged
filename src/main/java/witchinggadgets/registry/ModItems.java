package witchinggadgets.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import witchinggadgets.WitchingGadgets;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WitchingGadgets.MODID);

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
