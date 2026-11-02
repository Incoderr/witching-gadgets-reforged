package witchinggadgets.robes;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.armor.ArmorSupport;
import thaumcraft.item.gear.Repairable;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import thaumcraft.wand.VisDiscountGear;
import witchinggadgets.platform.WgArmor;

public class BewitchedRobeItem extends ArmorItem implements Repairable, VisDiscountGear {
    public BewitchedRobeItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(WgArmor.dyeable(material), Registration.armorType(slot), properties);
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player, Aspect aspect) {
        return ArmorSupport.slotOf(stack) == EquipmentSlot.LEGS ? 4 : 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        ArmorSupport.visDiscountLine(builder, getVisDiscount(stack, null, null));
    }
}
