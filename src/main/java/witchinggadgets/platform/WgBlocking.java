package witchinggadgets.platform;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlocksAttacks;

public final class WgBlocking {
    private WgBlocking() {
    }

    public static Item.Properties halfBlock(Item.Properties properties) {
        return properties.delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
            0.0F,
            0.0F,
            List.of(new BlocksAttacks.DamageReduction(180.0F, Optional.empty(), 0.0F, 0.5F)),
            new BlocksAttacks.ItemDamageFunction(Float.MAX_VALUE, 0.0F, 0.0F),
            Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
            Optional.empty(),
            Optional.empty()
        ));
    }
}
