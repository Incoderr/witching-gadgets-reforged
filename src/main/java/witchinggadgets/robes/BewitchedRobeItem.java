package witchinggadgets.robes;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.gear.Repairable;
import thaumcraft.wand.VisDiscountGear;

// WG ItemAdvancedRobes: chest 5% and legs 4% vis discount.
public class BewitchedRobeItem extends Item implements Repairable, VisDiscountGear {
    public BewitchedRobeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return ArmorSupport.slotOf(stack) == EquipmentSlot.LEGS ? 4 : 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        ArmorSupport.visDiscountLine(builder, getVisDiscount(stack, null, null));
    }
}
