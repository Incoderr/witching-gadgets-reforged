package witchinggadgets.gems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import witchinggadgets.WitchingGadgets;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class GemEvents {
    private GemEvents() {
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DispenserBlock.registerBehavior(GemsFeature.CRYSTAL_CAPSULE.get(), new CapsuleDispenseBehavior()));
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof InfusedGemItem) {
                InfusedGemItem.inventoryTick(player, stack, slot == inventory.selected);
            }
        }
    }

    public static final class CapsuleDispenseBehavior extends DefaultDispenseItemBehavior {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.state().getValue(DispenserBlock.FACING);
            BlockPos target = source.pos().relative(facing);
            ItemStack result = CrystalCapsuleItem.use(source.level(), target, stack);
            if (!result.isEmpty()) {
                ItemStack left = source.blockEntity().insertItem(result);
                if (!left.isEmpty()) {
                    spawnItem(source.level(), left, 6, facing, DispenserBlock.getDispensePosition(source));
                }
            }
            return stack;
        }
    }
}
