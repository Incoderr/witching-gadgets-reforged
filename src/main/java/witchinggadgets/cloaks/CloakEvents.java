package witchinggadgets.cloaks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import thaumcraft.entity.EldritchMob;
import thaumcraft.item.gear.Goggles;
import thaumcraft.node.Revealer;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class CloakEvents {
    private CloakEvents() {
    }

    public static ItemStack activeCloak(Player player) {
        ItemStack cloak = Gear.get(player, GearSlot.CLOAK);
        if (cloak.getItem() instanceof MagicCloakItem) {
            return cloak;
        }
        for (int slot = 0; slot < Baubles.slotCount(); slot++) {
            ItemStack stack = Baubles.get(player, slot);
            if (Baubles.slotType(slot) == BaubleType.BELT && stack.getItem() instanceof MagicCloakItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean isSpectral(Player player) {
        return MagicCloakItem.isSpectral(activeCloak(player));
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.typeHolder().is(Tags.EntityTypes.BOSSES);
    }

    public static boolean seesSpectral(LivingEntity entity) {
        Item helmet = entity.getItemBySlot(EquipmentSlot.HEAD).getItem();
        return helmet instanceof Revealer || helmet instanceof Goggles;
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            ItemStack cloak = activeCloak(player);
            if (cloak.getItem() instanceof MagicCloakItem item) {
                item.userDamaged(cloak, event.getAmount());
            }
        }
    }

    @SubscribeEvent
    static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player
            && event.getEntity() instanceof PathfinderMob mob
            && isSpectral(player)
            && !seesSpectral(mob)
            && !(mob instanceof EldritchMob)
            && !isBoss(mob)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isSpectral(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isSpectral(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isSpectral(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isSpectral(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onAttack(AttackEntityEvent event) {
        if (isSpectral(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
