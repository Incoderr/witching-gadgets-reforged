package witchinggadgets.baubles;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class RingItem extends Item implements Bauble {
    public RingItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            BaubleEffects.wornTick(stack, player);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            BaubleEffects.unequipped(stack, player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        builder.add(Component.translatable("wg.desc.gearSlot.bauble.RING"));
    }
}
