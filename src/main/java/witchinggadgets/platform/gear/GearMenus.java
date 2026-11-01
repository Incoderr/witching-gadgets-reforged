package witchinggadgets.platform.gear;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import witchinggadgets.WitchingGadgets;

public final class GearMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, WitchingGadgets.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GearMenu>> EXPANDED = MENUS.register("gear", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> new GearMenu(containerId, inventory)));

    private GearMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
