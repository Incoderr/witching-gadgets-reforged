package witchinggadgets.baubles;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;

public class WearableItem extends Item implements GearItem {
    private final GearSlot slot;

    public WearableItem(GearSlot slot, Properties properties) {
        super(properties.stacksTo(1));
        this.slot = slot;
    }

    @Override
    public GearSlot gearSlot(ItemStack stack) {
        return slot;
    }

    @Override
    public void onGearTick(ItemStack stack, Player player) {
        BaubleEffects.wornTick(stack, player);
    }

    @Override
    public void onGearUnequipped(ItemStack stack, Player player) {
        BaubleEffects.unequipped(stack, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(slot.descriptionKey()));
    }
}
