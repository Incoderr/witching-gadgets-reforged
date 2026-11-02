package witchinggadgets.baubles;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.registry.CreativeVariants;

public class TitleItem extends WearableItem implements CreativeVariants {
    public static final String CULTIST = "wg.title.crimsonCultist";
    public static final String KNIGHT = "wg.title.crimsonKnight";
    public static final String PRAETOR = "wg.title.crimsonPraetor";

    public TitleItem(Properties properties) {
        super(GearSlot.TITLE, properties);
    }

    public static ItemStack withTitle(ItemStack stack, String title) {
        BaublesFeature.TITLE.set(stack, title);
        return stack;
    }

    @Override
    public void creativeStacks(Consumer<ItemStack> output) {
        for (String title : new String[] {CULTIST, KNIGHT, PRAETOR}) {
            output.accept(withTitle(new ItemStack(this), title));
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
