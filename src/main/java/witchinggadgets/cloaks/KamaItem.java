package witchinggadgets.cloaks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public class KamaItem extends MagicCloakItem implements Bauble {
    public KamaItem(CloakVariant variant, Properties properties) {
        super(variant, properties);
    }

    @Override
    protected String slotDescription() {
        return "wg.desc.gearSlot.bauble.BELT";
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            cloakTick(player, stack);
        }
    }

    @Override
    public void onUnequipped(ItemStack stack, LivingEntity wearer) {
        if (wearer instanceof Player player) {
            cloakUnequipped(player, stack);
        }
    }
}
