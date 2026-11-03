package witchinggadgets.bags;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.Wands;
import witchinggadgets.WitchingGadgets;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class BagEvents {
    private static final int HOTBAR = 9;

    private BagEvents() {
    }

    @SubscribeEvent
    static void onPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemEntity item = event.getItemEntity();
        if (player.level().isClientSide() || item.hasPickUpDelay() || item.getItem().isEmpty()) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack bag = inventory.getItem(slot);
            if (!(bag.getItem() instanceof BagItem bagItem)) {
                continue;
            }
            if ((bagItem.kind() == BagKind.VOID && voidItem(player, bag, item)) || (bagItem.kind() == BagKind.HUNGRY && feed(bag, item))) {
                event.setCanPickup(TriState.FALSE);
                return;
            }
        }
    }

    public static boolean voidItem(Player player, ItemStack bag, ItemEntity item) {
        ItemStack picked = item.getItem();
        boolean filtered = BagsFeature.CONTENTS.getOrDefault(bag, ItemContainerContents.EMPTY).nonEmptyItemCopyStream()
            .anyMatch(filter -> ItemStack.isSameItemSameComponents(filter, picked));
        if (!filtered) {
            return false;
        }
        AspectList primals = AspectHelper.reduceToPrimals(AspectHelper.getBonusTags(picked));
        List<Aspect> aspects = primals.getAspects();
        if (!aspects.isEmpty()) {
            Aspect aspect = aspects.get(player.getRandom().nextInt(aspects.size()));
            ItemStack wand = wandWithRoom(player, aspect);
            if (!wand.isEmpty()) {
                Wands.addVis(wand, aspect, primals.getAmount(aspect), true);
            }
        }
        item.discard();
        return true;
    }

    private static ItemStack wandWithRoom(Player player, Aspect aspect) {
        for (int slot = 0; slot < HOTBAR; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (Wands.isWand(stack) && Wands.getVis(stack, aspect) + 100 <= Wands.getMaxVis(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean feed(ItemStack bag, ItemEntity item) {
        ItemStack picked = item.getItem().copy();
        int before = picked.getCount();
        NonNullList<ItemStack> stored = NonNullList.withSize(BagItem.SIZE, ItemStack.EMPTY);
        BagsFeature.CONTENTS.getOrDefault(bag, ItemContainerContents.EMPTY).copyInto(stored);
        for (int index = 0; index < stored.size() && !picked.isEmpty(); index++) {
            ItemStack slot = stored.get(index);
            if (slot.isEmpty()) {
                stored.set(index, picked.copy());
                picked.setCount(0);
            } else if (ItemStack.isSameItemSameComponents(slot, picked)) {
                int fit = Math.min(Math.min(64, slot.getMaxStackSize()) - slot.getCount(), picked.getCount());
                if (fit > 0) {
                    slot.grow(fit);
                    picked.shrink(fit);
                }
            }
        }
        if (picked.getCount() == before) {
            return false;
        }
        BagsFeature.CONTENTS.set(bag, ItemContainerContents.fromItems(stored));
        if (picked.isEmpty()) {
            item.discard();
            return true;
        }
        item.setItem(picked);
        return false;
    }
}
