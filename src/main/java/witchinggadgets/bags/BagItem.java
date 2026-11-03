package witchinggadgets.bags;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class BagItem extends Item {
    public static final int SIZE = 18;

    private final BagKind kind;

    public BagItem(BagKind kind, Properties properties) {
        super(properties.stacksTo(1));
        this.kind = kind;
    }

    public BagKind kind() {
        return kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (kind == BagKind.VOID) {
            builder.accept(Component.translatable("wg.desc.filteredItems").withStyle(ChatFormatting.DARK_PURPLE));
            BagsFeature.CONTENTS.getOrDefault(stack, ItemContainerContents.EMPTY).nonEmptyItemCopyStream()
                .forEach(filter -> builder.accept(Component.literal(" ").append(filter.getHoverName()).withStyle(ChatFormatting.DARK_GRAY)));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (kind == BagKind.ENDER) {
            serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, user) -> ChestMenu.threeRows(containerId, inventory, user.getEnderChestInventory()), Component.translatable("container.enderchest")));
            return InteractionResult.SUCCESS;
        }
        if (kind == BagKind.VOID) {
            VoidBagMenu.open(serverPlayer, hand);
            return InteractionResult.SUCCESS;
        }
        String name = player.getGameProfile().name();
        if (!BagsFeature.OWNER.has(stack)) {
            BagsFeature.OWNER.set(stack, name);
        }
        String owner = BagsFeature.OWNER.getOrDefault(stack, name);
        boolean unlocked = BagsFeature.UNLOCKED.getOrDefault(stack, false);
        if (!player.isCreative() && !unlocked && !owner.equalsIgnoreCase(name)) {
            player.hurtServer(serverPlayer.level(), player.damageSources().magic(), 4.0F);
            player.drop(stack.copy(), true, Prediction.SERVER_ONLY);
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.sendSystemMessage(Component.translatable("wg.chat.notyourbag", owner));
            return InteractionResult.FAIL;
        }
        if (owner.equalsIgnoreCase(name) && player.isShiftKeyDown()) {
            BagsFeature.UNLOCKED.set(stack, !unlocked);
            player.sendSystemMessage(Component.translatable(unlocked ? "wg.chat.baglocked" : "wg.chat.bagunlocked"));
            return InteractionResult.SUCCESS;
        }
        BagMenu.open(serverPlayer, hand);
        return InteractionResult.SUCCESS;
    }
}
