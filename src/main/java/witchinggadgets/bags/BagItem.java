package witchinggadgets.bags;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BagItem extends Item {
    public static final int SIZE = 18;

    public BagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        String name = player.getGameProfile().getName();
        if (!BagsFeature.OWNER.has(stack)) {
            BagsFeature.OWNER.set(stack, name);
        }
        String owner = BagsFeature.OWNER.getOrDefault(stack, name);
        boolean unlocked = BagsFeature.UNLOCKED.getOrDefault(stack, false);
        if (!player.isCreative() && !unlocked && !owner.equalsIgnoreCase(name)) {
            player.hurt(player.damageSources().magic(), 4.0F);
            player.drop(stack.copy(), true);
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.sendSystemMessage(Component.translatable("wg.chat.notyourbag", owner));
            return InteractionResultHolder.fail(stack);
        }
        if (owner.equalsIgnoreCase(name) && player.isShiftKeyDown()) {
            BagsFeature.UNLOCKED.set(stack, !unlocked);
            player.sendSystemMessage(Component.translatable(unlocked ? "wg.chat.baglocked" : "wg.chat.bagunlocked"));
            return InteractionResultHolder.success(stack);
        }
        BagMenu.open(serverPlayer, hand);
        return InteractionResultHolder.success(stack);
    }
}
