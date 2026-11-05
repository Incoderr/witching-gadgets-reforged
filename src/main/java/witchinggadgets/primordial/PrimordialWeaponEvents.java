package witchinggadgets.primordial;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.effects.EffectsFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class PrimordialWeaponEvents {
    private static final ThreadLocal<Boolean> BREAKING = ThreadLocal.withInitial(() -> false);

    private PrimordialWeaponEvents() {
    }

    private static ItemStack weapon(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof PrimordialWeaponItem ? stack : ItemStack.EMPTY;
    }

    @SubscribeEvent
    static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack stack = weapon(player);
        if (stack.isEmpty() || !(player.level() instanceof ServerLevel level) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        strike(level, player, stack, target);
    }

    public static void strike(ServerLevel level, Player player, ItemStack stack, LivingEntity target) {
        int ability = PrimordialGear.ability(stack);
        if (PrimordialWeaponItem.is(stack, PrimordialWeaponItem.Kind.HAMMER)) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20));
        }
        if (PrimordialWeaponItem.is(stack, PrimordialWeaponItem.Kind.GREATAXE)) {
            for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST}) {
                ItemStack armor = target.getItemBySlot(slot);
                if (!armor.isEmpty() && armor.isDamageableItem()) {
                    armor.hurtAndBreak(5, target, slot);
                }
            }
        }
        if (ability == PrimordialGear.AIR) {
            sweep(level, player, stack, target);
        } else if (ability == PrimordialGear.FIRE) {
            target.addEffect(new MobEffectInstance(EffectsFeature.CINDER_COAT, 80, 1));
            target.igniteForSeconds(4.0F);
        } else if (ability == PrimordialGear.WATER) {
            target.addEffect(new MobEffectInstance(EffectsFeature.DISSOLVE, 80, 2));
        } else if (ability == PrimordialGear.ENTROPY) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
        }
    }

    private static void sweep(ServerLevel level, Player player, ItemStack stack, LivingEntity target) {
        AABB area = new AABB(target.getX() - 2, target.getY() - 2, target.getZ() - 2, target.getX() + 2, target.getY() + 2, target.getZ() + 2);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player && entity != target && entity.isAttackable());
        for (LivingEntity victim : victims) {
            DamageSource source = level.damageSources().playerAttack(player);
            float damage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            boolean critical = player.fallDistance > 0.0 && !player.onGround() && !player.onClimbable() && !player.isInWater()
                && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger();
            if (critical) {
                damage *= 1.5F;
            }
            damage = EnchantmentHelper.modifyDamage(level, stack, victim, source, damage);
            if (damage <= 0.0F || !victim.hurt(source, damage)) {
                continue;
            }
            float knockback = player.isSprinting() ? 1.0F : 0.0F;
            if (knockback > 0.0F) {
                victim.push(-Mth.sin(player.getYRot() * Mth.DEG_TO_RAD) * knockback * 0.5F, 0.1, Mth.cos(player.getYRot() * Mth.DEG_TO_RAD) * knockback * 0.5F);
            }
            player.setLastHurtMob(victim);
            player.causeFoodExhaustion(0.3F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player player && player.isBlocking() && PrimordialGear.has(player.getUseItem(), PrimordialGear.EARTH)) {
            MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            int time = Math.min((resistance == null ? 0 : resistance.getDuration()) + 30, 80);
            int amplifier = Math.min((resistance == null ? -1 : resistance.getAmplifier()) + 1, 2);
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, time, amplifier));
        }
        if (!(event.getSource().getDirectEntity() instanceof Player player)) {
            return;
        }
        event.setAmount(event.getAmount() * multiplier(weapon(player), victim, event.getSource()));
    }

    public static float multiplier(ItemStack stack, LivingEntity victim, DamageSource source) {
        if (PrimordialWeaponItem.is(stack, PrimordialWeaponItem.Kind.HAMMER) && (victim instanceof Slime || victim.getType().is(EntityTypeTags.ARTHROPOD))) {
            return 2.0F;
        }
        if (PrimordialWeaponItem.is(stack, PrimordialWeaponItem.Kind.GREATAXE) && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            float mod = 1.0F;
            for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                if (!victim.getItemBySlot(slot).isEmpty()) {
                    mod += 0.5F;
                }
            }
            return mod;
        }
        return 1.0F;
    }

    public static ItemStack head(LivingEntity victim, Player killer) {
        if (victim instanceof WitherSkeleton) {
            return new ItemStack(Items.WITHER_SKELETON_SKULL);
        }
        if (victim instanceof AbstractSkeleton) {
            return new ItemStack(Items.SKELETON_SKULL);
        }
        if (victim instanceof Zombie) {
            return new ItemStack(Items.ZOMBIE_HEAD);
        }
        if (victim instanceof Creeper) {
            return new ItemStack(Items.CREEPER_HEAD);
        }
        if (victim instanceof Player) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, new ResolvableProfile(killer.getGameProfile()));
            return head;
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!event.isRecentlyHit() || !(event.getSource().getDirectEntity() instanceof Player player) || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (!PrimordialWeaponItem.is(player.getMainHandItem(), PrimordialWeaponItem.Kind.CLAYMORE)) {
            return;
        }
        int looting = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), player.getMainHandItem());
        if (player.getRandom().nextInt(6) >= looting) {
            return;
        }
        ItemStack head = head(victim, player);
        if (head.isEmpty()) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            if (drop.getItem().is(head.getItem())) {
                return;
            }
        }
        event.getDrops().add(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), head));
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level) || !(event.getSource().getDirectEntity() instanceof Player player)) {
            return;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) || !PrimordialGear.has(weapon(player), PrimordialGear.ORDER)) {
            return;
        }
        int xp = 4 * mob.getExperienceReward(level, player);
        if (xp > 0) {
            ExperienceOrb.award(level, mob.position(), xp);
        }
    }

    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        BlockState state = event.getState();
        if (PrimordialWeaponItem.is(event.getEntity().getMainHandItem(), PrimordialWeaponItem.Kind.GREATAXE) && (state.is(BlockTags.LEAVES) || state.is(BlockTags.CAVE_VINES) || state.is(net.minecraft.world.level.block.Blocks.VINE))) {
            event.setNewSpeed(Math.max(event.getNewSpeed(), 6.0F));
        }
    }

    @SubscribeEvent
    static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (BREAKING.get() || !(stack.getItem() instanceof PrimordialWeaponItem weapon) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (weapon.kind() == PrimordialWeaponItem.Kind.CLAYMORE || weapon.kind() == PrimordialWeaponItem.Kind.HAMMER && player.isShiftKeyDown()) {
            return;
        }
        HitResult hit = player.pick(player.blockInteractionRange(), 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BREAKING.set(true);
        try {
            breakArea(level, player, stack, event.getPos(), blockHit.getDirection(), weapon.kind() == PrimordialWeaponItem.Kind.HAMMER ? PrimordialWeaponItem.HAMMER_MINEABLE : PrimordialWeaponItem.GREATAXE_MINEABLE);
        } finally {
            BREAKING.set(false);
        }
    }

    public static void breakArea(ServerLevel level, Player player, ItemStack tool, BlockPos center, Direction face, net.minecraft.tags.TagKey<Block> mineable) {
        int rangeX = face.getAxis() == Direction.Axis.X ? 0 : 1;
        int rangeY = face.getAxis() == Direction.Axis.Y ? 0 : 1;
        int rangeZ = face.getAxis() == Direction.Axis.Z ? 0 : 1;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-rangeX, -rangeY, -rangeZ), center.offset(rangeX, rangeY, rangeZ))) {
            if (pos.equals(center) || !level.isLoaded(pos)) {
                continue;
            }
            BlockPos target = pos.immutable();
            BlockState state = level.getBlockState(target);
            if (state.isAir() || state.getDestroyProgress(player, level, target) <= 0.0F || !state.is(mineable) || !player.hasCorrectToolForDrops(state)) {
                continue;
            }
            if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, target, state, player)).isCanceled()) {
                continue;
            }
            if (player.isCreative()) {
                level.removeBlock(target, false);
            } else {
                BlockEntity blockEntity = level.getBlockEntity(target);
                Block block = state.getBlock();
                block.playerWillDestroy(level, target, state, player);
                if (state.onDestroyedByPlayer(level, target, player, true, level.getFluidState(target))) {
                    block.destroy(level, target, state);
                    if (player instanceof ServerPlayer serverPlayer) {
                        block.playerDestroy(level, serverPlayer, target, state, blockEntity, tool.copy());
                    } else {
                        Block.dropResources(state, level, target, blockEntity, player, tool.copy());
                    }
                }
            }
            level.levelEvent(2001, target, Block.getId(state));
        }
    }
}
