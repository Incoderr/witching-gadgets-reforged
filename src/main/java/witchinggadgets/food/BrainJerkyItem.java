package witchinggadgets.food;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchManager;

public class BrainJerkyItem extends Item {
    public BrainJerkyItem(Properties properties) {
        super(properties);
    }

    public static void feedMind(ServerPlayer player) {
        List<Aspect> primals = Aspect.getPrimalAspects();
        int luck = player.getRandom().nextInt(3);
        for (int pass = 0; pass <= luck; pass++) {
            Aspect aspect = primals.get(player.getRandom().nextInt(primals.size()));
            ResearchManager.addAspectPool(player, aspect, player.getRandom().nextInt(2) + 1);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            feedMind(player);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
