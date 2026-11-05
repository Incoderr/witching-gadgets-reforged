package witchinggadgets.platform;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.primordial.PrimordialWeaponItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class WgBlocking {
    private WgBlocking() {
    }

    public static Item.Properties halfBlock(Item.Properties properties) {
        return properties;
    }

    @SubscribeEvent
    static void onShieldBlock(LivingShieldBlockEvent event) {
        ItemStack using = event.getEntity().getUseItem();
        if (!event.getEntity().isBlocking() || !(using.getItem() instanceof PrimordialWeaponItem) || event.getDamageSource().is(DamageTypeTags.BYPASSES_SHIELD)) {
            return;
        }
        event.setBlocked(true);
        event.setBlockedDamage(event.getOriginalBlockedDamage() * 0.5F);
        event.setShieldDamage(0.0F);
    }
}
