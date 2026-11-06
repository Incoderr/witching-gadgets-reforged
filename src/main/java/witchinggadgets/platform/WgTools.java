package witchinggadgets.platform;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShearsItem;

public final class WgTools {
    private WgTools() {
    }

    public static Item.Properties shears(Item.Properties properties, int durability, TagKey<Item> repairs) {
        return properties.durability(durability).component(DataComponents.TOOL, ShearsItem.createToolProperties()).repairable(repairs);
    }
}
