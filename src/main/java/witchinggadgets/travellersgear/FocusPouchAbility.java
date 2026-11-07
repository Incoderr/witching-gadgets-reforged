package witchinggadgets.travellersgear;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.menu.FocusPouchMenu;
import thaumcraft.platform.Baubles;
import thaumcraft.platform.Menus;
import witchinggadgets.gear.GearAbility;

final class FocusPouchAbility implements GearAbility {
    @Override
    public boolean canActivate(Player player, ItemStack stack, boolean inHand) {
        return !inHand;
    }

    @Override
    public void activate(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int slot = -1;
        for (int index = 0; index < Baubles.slotCount(); index++) {
            if (Baubles.get(player, index) == stack) {
                slot = index;
            }
        }
        if (slot < 0) {
            return;
        }
        int baubleSlot = slot;
        Menus.open(serverPlayer, new SimpleMenuProvider((containerId, inventory, ignored) -> new FocusPouchMenu(containerId, inventory, stack) {
            @Override
            public void removed(Player closing) {
                super.removed(closing);
                if (!closing.level().isClientSide() && Baubles.get(closing, baubleSlot) == stack) {
                    Baubles.set(closing, baubleSlot, stack);
                }
            }
        }, Component.translatable("item.thaumcraft.focus_pouch")));
    }
}
