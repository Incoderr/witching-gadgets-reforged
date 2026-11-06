package witchinggadgets.primordial;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.item.armor.FortressArmorItem;
import thaumcraft.item.armor.Hover;
import thaumcraft.registry.ModEffects;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.materials.MaterialsFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class PrimordialEvents {
    private static final AttributeModifier STEP = new AttributeModifier(WitchingGadgets.id("primordial_step"), 0.4, AttributeModifier.Operation.ADD_VALUE);
    private static final ThreadLocal<Boolean> REDUCING = ThreadLocal.withInitial(() -> false);

    private PrimordialEvents() {
    }

    private static boolean movingForward(Player player) {
        return player.level().isClientSide() && player.zza > 0.0F;
    }

    public static double set(LivingEntity entity) {
        double set = 0.875;
        for (ItemStack piece : PrimordialArmorItem.worn(entity)) {
            set += 0.125;
            if (FortressArmorItem.hasMask(piece)) {
                set += 0.05;
            }
        }
        return set;
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        tickBoots(player);
        if (!player.level().isClientSide()) {
            serverTick(player);
        }
    }

    public static void serverTick(Player player) {
        List<ItemStack> pieces = PrimordialArmorItem.worn(player);
        if (player.tickCount % 20 == 0) {
            for (ItemStack piece : pieces) {
                repair(piece);
            }
        }
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof PrimordialGauntletItem) {
                PrimordialGauntletItem.tick(player, stack);
            }
            if (stack.getItem() instanceof PrimordialGear gear) {
                if (player.tickCount % 40 == 0 && !pieces.contains(stack)) {
                    repair(stack);
                }
                if (PrimordialFeature.PEARLS_PENDING.getOrDefault(stack, false)) {
                    PrimordialFeature.PEARLS_PENDING.remove(stack);
                    returnPearls(player, gear.returnedPearls());
                }
            }
        }
        for (ItemStack piece : pieces) {
            int ability = PrimordialGear.ability(piece);
            if (ability == PrimordialGear.AIR) {
                deflect(player);
            } else if (ability == PrimordialGear.WATER) {
                cure(player);
            }
        }
    }

    private static void repair(ItemStack stack) {
        if (stack.isDamaged()) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    public static void returnPearls(Player player, int count) {
        ItemStack pearls = new ItemStack(MaterialsFeature.POWERLESS_PEARL.get(), count);
        if (!player.getInventory().add(pearls)) {
            player.drop(pearls, false);
        }
    }

    private static void tickBoots(Player player) {
        boolean boots = player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof PrimordialArmorItem;
        boolean flying = player.getAbilities().flying || player.isFallFlying();
        boolean forward = movingForward(player);
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) {
            boolean active = step.hasModifier(STEP.id());
            if (boots && !flying && forward && !player.isShiftKeyDown()) {
                if (!active) {
                    step.addTransientModifier(STEP);
                }
            } else if (active && (!boots || flying || player.isShiftKeyDown())) {
                step.removeModifier(STEP.id());
            }
        }
        if (!boots) {
            return;
        }
        if (!flying && forward) {
            if (player.onGround()) {
                float bonus = 0.055F;
                if (player.isInWater()) {
                    bonus /= 4.0F;
                }
                player.moveRelative(bonus, new Vec3(0.0, 0.0, 1.0));
            } else {
                player.moveRelative(Hover.isHovering(Hover.getHarness(player)) ? 0.01F : 0.03F, new Vec3(0.0, 0.0, 1.0));
            }
        }
        if (player.fallDistance > 0.0) {
            player.fallDistance -= 0.25;
        }
    }

    public static void deflect(Player player) {
        AABB area = new AABB(player.getX() - 0.5, player.getY() - 0.5, player.getZ() - 0.5, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5).inflate(4.0);
        for (Projectile projectile : player.level().getEntitiesOfClass(Projectile.class, area)) {
            Entity shooter = projectile.getOwner();
            if (shooter == player) {
                continue;
            }
            Vec3 motion = projectile.getDeltaMovement();
            Vec3 offset = projectile.position().subtract(player.position());
            double speed = motion.length();
            if (speed <= 0.0 || offset.length() <= 0.0) {
                continue;
            }
            double angle = Math.acos(offset.dot(motion) / (offset.length() * speed));
            if (angle < 3.0 * Math.PI / 4.0) {
                continue;
            }
            Vec3 direction = shooter != null
                ? new Vec3(shooter.getX(), shooter.getY() + shooter.getEyeHeight(), shooter.getZ()).subtract(projectile.position())
                : offset;
            projectile.setDeltaMovement(direction.normalize().scale(speed));
            projectile.hurtMarked = true;
        }
    }

    private static void cure(Player player) {
        List<Holder<MobEffect>> cured = List.of(MobEffects.BLINDNESS, MobEffects.POISON, MobEffects.WITHER, MobEffects.CONFUSION, ModEffects.TAINT_POISON);
        for (Holder<MobEffect> effect : cured) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
            }
        }
    }

    @SubscribeEvent
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        DamageSource source = event.getSource();
        for (ItemStack piece : PrimordialArmorItem.worn(player)) {
            int ability = PrimordialGear.ability(piece);
            if (ability == PrimordialGear.AIR && source.is(DamageTypeTags.IS_PROJECTILE)) {
                event.setCanceled(true);
                return;
            }
            if (ability == PrimordialGear.ENTROPY && source.getDirectEntity() instanceof LivingEntity attacker && player.getRandom().nextInt(4) == 0) {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10, 0));
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3));
            }
        }
    }

    public static float reduction(LivingEntity entity, DamageSource source) {
        boolean magic = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
        boolean fire = source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        boolean bypass = source.is(DamageTypeTags.BYPASSES_ARMOR);
        double ratio = 0.0;
        if (!magic && fire) {
            ratio = PrimordialArmorItem.count(entity, PrimordialGear.FIRE) * 0.75 * set(entity);
        } else if (!magic && bypass) {
            for (EquipmentSlot slot : PrimordialArmorItem.SLOTS) {
                ItemStack piece = entity.getItemBySlot(slot);
                if (piece.getItem() instanceof PrimordialArmorItem && PrimordialGear.ability(piece) == PrimordialGear.EARTH) {
                    ratio += defense(slot) / 50.0 * set(entity);
                }
            }
        }
        return (float) Math.min(1.0, ratio);
    }

    private static int defense(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> PrimordialFeature.PRIMORDIAL_ARMOR.helmetDefense();
            case CHEST -> PrimordialFeature.PRIMORDIAL_ARMOR.chestplateDefense();
            case LEGS -> PrimordialFeature.PRIMORDIAL_ARMOR.leggingsDefense();
            default -> PrimordialFeature.PRIMORDIAL_ARMOR.bootsDefense();
        };
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(DamageTypeTags.IS_FIRE) && PrimordialArmorItem.count(entity, PrimordialGear.FIRE) > 0) {
            entity.clearFire();
        }
        float reduction = reduction(entity, source);
        if (reduction > 0.0F) {
            event.setNewDamage(event.getNewDamage() * (1.0F - reduction));
        }
    }

    public static boolean isWarpEffect(Holder<MobEffect> effect) {
        return effect.is(ModEffects.VIS_EXHAUST.getKey())
            || effect.is(ModEffects.THAUMARHIA.getKey())
            || effect.is(ModEffects.UNNATURAL_HUNGER.getKey())
            || effect.is(ModEffects.BLURRED_VISION.getKey())
            || effect.is(ModEffects.SUN_SCORNED.getKey())
            || effect.is(ModEffects.INFECTIOUS_VIS_EXHAUST.getKey())
            || effect.is(ModEffects.DEATH_GAZE.getKey());
    }

    public static MobEffectInstance soften(MobEffectInstance effect, int ordo) {
        int amplifier = effect.getAmplifier() > 0 ? Math.max(0, effect.getAmplifier() - ordo) : effect.getAmplifier();
        int duration = effect.getDuration() > 0 ? effect.getDuration() / (ordo + 1) : effect.getDuration();
        return new MobEffectInstance(effect.getEffect(), duration, amplifier, effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    @SubscribeEvent
    static void onEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = event.getEffectInstance();
        if (REDUCING.get() || entity.level().isClientSide() || effect == null || !isWarpEffect(effect.getEffect())) {
            return;
        }
        int ordo = PrimordialArmorItem.count(entity, PrimordialGear.ORDER);
        if (ordo <= 0) {
            return;
        }
        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        REDUCING.set(true);
        try {
            entity.addEffect(soften(effect, ordo), event.getEffectSource());
        } finally {
            REDUCING.set(false);
        }
    }
}
