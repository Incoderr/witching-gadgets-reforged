package witchinggadgets.enchantments;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.cloaks.CloakEvents;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class EnchantmentEvents {
    private EnchantmentEvents() {
    }

    public static boolean sameDirection(Entity first, Entity second) {
        Vec3 a = first.getLookAngle();
        Vec3 b = second.getLookAngle();
        return Math.signum(a.x) == Math.signum(b.x) && Math.signum(a.z) == Math.signum(b.z);
    }

    public static float backstab(Player player, LivingEntity target) {
        ItemStack weapon = player.getMainHandItem();
        int level = EnchantmentsFeature.level(weapon, EnchantmentsFeature.BACKSTAB);
        if (level <= 0 || !sameDirection(target, player)) {
            return 1.0F;
        }
        float mod = 1.0F + 0.2F * level;
        if (target instanceof PathfinderMob mob && mob.getTarget() != player) {
            mod += 0.4F;
        }
        return mod;
    }

    public static float stealthChance(Player player, LivingEntity watcher) {
        float chance = EnchantmentsFeature.level(player, EquipmentSlot.FEET, EnchantmentsFeature.STEALTH) * 0.1F
            + EnchantmentsFeature.level(player, EquipmentSlot.LEGS, EnchantmentsFeature.STEALTH) * 0.1F;
        if (!sameDirection(watcher, player)) {
            chance -= 0.1F;
        }
        return chance;
    }

    public static boolean hasStealth(Player player) {
        return EnchantmentsFeature.level(player, EquipmentSlot.FEET, EnchantmentsFeature.STEALTH) > 0
            || EnchantmentsFeature.level(player, EquipmentSlot.LEGS, EnchantmentsFeature.STEALTH) > 0;
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getDirectEntity() instanceof Player player && !player.getMainHandItem().isEmpty()) {
            event.setAmount(event.getAmount() * backstab(player, event.getEntity()));
        }
    }

    @SubscribeEvent
    static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player) || !(event.getEntity() instanceof PathfinderMob mob)) {
            return;
        }
        if (!player.isShiftKeyDown() || !hasStealth(player)) {
            return;
        }
        if (player.getRandom().nextFloat() < stealthChance(player, mob)) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }
        List<PathfinderMob> nearby = player.level().getEntitiesOfClass(PathfinderMob.class, new AABB(player.position(), player.position()).inflate(5.0));
        for (PathfinderMob other : nearby) {
            if (!CloakEvents.isBoss(other) && other.getTarget() == player) {
                event.setNewAboutToBeSetTarget(null);
                return;
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel level && player.isVehicle() && EnchantmentsFeature.level(player, EquipmentSlot.HEAD, EnchantmentsFeature.RIDE_PROTECT) > 0) {
            throwOff(level, player);
        }
    }

    public static void throwOff(ServerLevel level, LivingEntity vehicle) {
        DamageSource source = vehicle instanceof Player player ? level.damageSources().playerAttack(player) : level.damageSources().mobAttack(vehicle);
        for (Entity passenger : List.copyOf(vehicle.getPassengers())) {
            if (passenger instanceof LivingEntity living) {
                living.hurtServer(level, source, 1.0F);
                living.push(vehicle.getRandom().nextFloat() * 0.4F, 0.1F, vehicle.getRandom().nextFloat() * 0.4F);
                living.stopRiding();
            }
        }
    }
}
