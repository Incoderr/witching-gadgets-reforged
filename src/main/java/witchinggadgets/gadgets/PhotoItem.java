package witchinggadgets.gadgets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.research.ScanManager;

public class PhotoItem extends Item {
    private final boolean developed;

    public PhotoItem(boolean developed, Properties properties) {
        super(properties);
        this.developed = developed;
    }

    public static boolean study(ServerPlayer player, ItemStack stack) {
        ScanSnapshot scan = GadgetsFeature.SCAN.get(stack);
        if (scan == null) {
            return false;
        }
        ScanManager.Target target = scan.target();
        if (ScanManager.hasBeenScanned(player, target) || !ScanManager.completeScan(player, target)) {
            return false;
        }
        stack.shrink(1);
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!developed) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        ScanSnapshot scan = GadgetsFeature.SCAN.get(stack);
        if (scan == null || ScanManager.hasBeenScanned(player, scan.target())) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            study(serverPlayer, stack);
        } else {
            ScanManager.validScan(scan.aspects(), player);
        }
        return InteractionResult.SUCCESS;
    }
}
