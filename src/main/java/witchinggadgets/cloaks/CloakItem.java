package witchinggadgets.cloaks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;

public class CloakItem extends MagicCloakItem implements GearItem {
    public CloakItem(CloakVariant variant, Properties properties) {
        super(variant, properties);
    }

    @Override
    protected String slotDescription() {
        return GearSlot.CLOAK.descriptionKey();
    }

    @Override
    public GearSlot gearSlot(ItemStack stack) {
        return GearSlot.CLOAK;
    }

    @Override
    public void onGearTick(ItemStack stack, Player player) {
        cloakTick(player, stack);
    }

    @Override
    public void onGearUnequipped(ItemStack stack, Player player) {
        cloakUnequipped(player, stack);
    }
}
