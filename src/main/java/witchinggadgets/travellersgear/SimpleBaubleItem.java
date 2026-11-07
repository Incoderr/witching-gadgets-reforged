package witchinggadgets.travellersgear;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class SimpleBaubleItem extends Item implements Bauble {
    private final BaubleType type;

    public SimpleBaubleItem(BaubleType type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        builder.add(Component.translatable("TG.desc.gearSlot.bauble." + type.name()));
    }
}
