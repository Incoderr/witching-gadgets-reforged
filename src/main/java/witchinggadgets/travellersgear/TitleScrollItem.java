package witchinggadgets.travellersgear;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.baubles.TitleItem;
import witchinggadgets.baubles.WearableItem;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.registry.CreativeVariants;

public class TitleScrollItem extends WearableItem implements CreativeVariants {
    public TitleScrollItem(Properties properties) {
        super(GearSlot.TITLE, properties);
    }

    @Override
    public void creativeStacks(Consumer<ItemStack> output) {
        output.accept(new ItemStack(this));
        for (String title : TravellersGearFeature.TITLES) {
            output.accept(TitleItem.withTitle(new ItemStack(this), "TG.personaltitle." + title));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        if (BaublesFeature.TITLE.has(stack)) {
            builder.accept(Component.translatable(BaublesFeature.TITLE.getOrDefault(stack, "")));
        }
    }
}
