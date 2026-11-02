package witchinggadgets.materials;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import witchinggadgets.WitchingGadgets;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class MaterialEvents {
    private MaterialEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) {
            return;
        }
        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            looting = wolf.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.LOOTING)
                .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, attacker))
                .orElse(0);
        }
        for (int roll = 0; roll < 2 + Math.min(4, looting); roll++) {
            if (wolf.getRandom().nextInt(Math.max(1, 3 - looting)) == 0) {
                ItemEntity item = new ItemEntity(wolf.level(), wolf.getX(), wolf.getY(), wolf.getZ(), new ItemStack(MaterialsFeature.WOLF_PELT.get()));
                item.setPickUpDelay(10);
                event.getDrops().add(item);
            }
        }
    }
}
