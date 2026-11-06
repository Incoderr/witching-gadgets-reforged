package witchinggadgets.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import witchinggadgets.WitchingGadgets;

public final class ModCreativeTabs {
    public static final DeferredItem<Item> TAB_ICON = ModItems.ITEMS.registerSimpleItem("tab_icon");

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WitchingGadgets.MODID);

    public static final Supplier<CreativeModeTab> WITCHING_GADGETS = TABS.register("witchinggadgets", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.witchinggadgets"))
        .icon(() -> new ItemStack(TAB_ICON.get()))
        .displayItems((parameters, output) -> {
            for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
                if (holder == TAB_ICON || holder.getId().getPath().equals("brain_jerky")) {
                    continue;
                }
                if (holder.get() instanceof CreativeVariants variants) {
                    variants.creativeStacks(output::accept);
                } else {
                    output.accept(holder.get());
                }
            }
        })
        .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
