package witchinggadgets.platform.gear;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gear.GearItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class GearEvents {
    private GearEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        GearInventory inventory = GearAttachments.inventory(player);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof GearItem gear) {
                gear.onGearTick(stack, player);
            }
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level && !level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
            GearAttachments.inventory(player).dropAll(event.getDrops());
        }
    }
}
