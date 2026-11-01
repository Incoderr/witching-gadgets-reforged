package witchinggadgets.platform.gear;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gear.GearItem;
import witchinggadgets.platform.Gear;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class GearCurios {
    private GearCurios() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosApi.registerCurioPredicate(WitchingGadgets.id("gear"), result -> fits(result.slotContext(), result.stack()));
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof GearItem gear) {
                    CuriosApi.registerCurio(item, new Adapter(gear));
                }
            }
        });
    }

    private static boolean fits(SlotContext context, ItemStack stack) {
        return stack.getItem() instanceof GearItem gear && Gear.curioId(gear.gearSlot(stack)).equals(context.identifier());
    }

    private record Adapter(GearItem gear) implements ICurioItem {
        @Override
        public void curioTick(SlotContext context, ItemStack stack) {
            if (context.entity() instanceof Player player) {
                gear.onGearTick(stack, player);
            }
        }

        @Override
        public void onEquip(SlotContext context, ItemStack previous, ItemStack stack) {
            if (context.entity() instanceof Player player && !player.level().isClientSide()) {
                gear.onGearEquipped(stack, player);
            }
        }

        @Override
        public void onUnequip(SlotContext context, ItemStack next, ItemStack stack) {
            if (context.entity() instanceof Player player && !player.level().isClientSide()) {
                gear.onGearUnequipped(stack, player);
            }
        }

        @Override
        public boolean canEquip(SlotContext context, ItemStack stack) {
            return fits(context, stack) && (!(context.entity() instanceof Player player) || gear.canEquipGear(stack, player));
        }
    }
}
