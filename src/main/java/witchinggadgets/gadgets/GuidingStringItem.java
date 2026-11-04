package witchinggadgets.gadgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class GuidingStringItem extends Item {
    public GuidingStringItem(Properties properties) {
        super(properties);
    }

    public static boolean active(ItemStack stack) {
        return GadgetsFeature.GUIDE_ACTIVE.getOrDefault(stack, false);
    }

    public static List<GuidePoint> path(ItemStack stack) {
        return GadgetsFeature.GUIDE_PATH.getOrDefault(stack, List.of());
    }

    public static void toggle(ItemStack stack, Player player) {
        if (player.isShiftKeyDown()) {
            GadgetsFeature.GUIDE_PATH.remove(stack);
            GadgetsFeature.GUIDE_ACTIVE.set(stack, false);
            return;
        }
        GadgetsFeature.GUIDE_ACTIVE.set(stack, !active(stack));
        append(stack, GuidePoint.of(player));
    }

    public static void track(ItemStack stack, Entity entity) {
        List<GuidePoint> path = path(stack);
        if (path.isEmpty() || !active(stack)) {
            return;
        }
        GuidePoint last = path.getLast();
        double dist = Math.abs(last.x() - entity.getX()) + Math.abs(last.z() - entity.getZ()) + Math.abs(last.z() - entity.getZ());
        if (last.facing() != GuidePoint.facing(entity) && dist > 1) {
            append(stack, GuidePoint.of(entity));
        }
    }

    private static void append(ItemStack stack, GuidePoint point) {
        List<GuidePoint> path = new ArrayList<>(path(stack));
        path.add(point);
        GadgetsFeature.GUIDE_PATH.set(stack, List.copyOf(path));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            toggle(player.getItemInHand(hand), player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return active(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        if (active(stack)) {
            builder.accept(Component.translatable("wg.desc.active"));
        }
    }
}
