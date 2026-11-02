package witchinggadgets.baubles;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import thaumcraft.entity.boss.CultistLeader;
import thaumcraft.entity.monster.CultistCleric;
import thaumcraft.entity.monster.CultistKnight;
import thaumcraft.platform.Baubles;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class BaubleEffects {
    public static final AttributeModifier KNOCKBACK = new AttributeModifier(WitchingGadgets.id("knockback_shoulders"), 0.6, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier STRENGTH = new AttributeModifier(WitchingGadgets.id("strength_vambraces"), 2.0, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier SNIPER = new AttributeModifier(WitchingGadgets.id("sniper_ring"), 2.75, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    public static final int LUCK_BONUS = 2;

    private static final Set<Player> LUCKY = Collections.newSetFromMap(new WeakHashMap<>());

    private BaubleEffects() {
    }

    static void wornTick(ItemStack stack, Player player) {
        Item item = stack.getItem();
        if (item == BaublesFeature.KNOCKBACK_SHOULDERS.get()) {
            ensure(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK);
        } else if (item == BaublesFeature.STRENGTH_VAMBRACES.get()) {
            ensure(player, Attributes.ATTACK_DAMAGE, STRENGTH);
        } else if (item == BaublesFeature.HASTE_VAMBRACES.get()) {
            climb(player);
        } else if (item == BaublesFeature.SNIPER_RING.get()) {
            if (isDrawingBow(player)) {
                ensure(player, Attributes.MOVEMENT_SPEED, SNIPER);
            } else {
                remove(player, Attributes.MOVEMENT_SPEED, SNIPER);
            }
        } else if (item == BaublesFeature.LUCK_RING.get()) {
            LUCKY.add(player);
        }
    }

    static void unequipped(ItemStack stack, Player player) {
        Item item = stack.getItem();
        if (item == BaublesFeature.KNOCKBACK_SHOULDERS.get()) {
            remove(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK);
        } else if (item == BaublesFeature.STRENGTH_VAMBRACES.get()) {
            remove(player, Attributes.ATTACK_DAMAGE, STRENGTH);
        } else if (item == BaublesFeature.SNIPER_RING.get()) {
            remove(player, Attributes.MOVEMENT_SPEED, SNIPER);
        } else if (item == BaublesFeature.LUCK_RING.get()) {
            LUCKY.remove(player);
        }
    }

    public static boolean isDrawingBow(Player player) {
        return player.isUsingItem() && player.getUseItem().getItem() instanceof BowItem;
    }

    public static boolean wearsRing(Player player, Item ring) {
        return Baubles.isEquipped(player, ring);
    }

    public static boolean wearsGear(Player player, GearSlot slot, Item item) {
        return Gear.get(player, slot).is(item);
    }

    private static void ensure(Player player, Holder<Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && !instance.hasModifier(modifier.id())) {
            instance.addTransientModifier(modifier);
        }
    }

    private static void remove(Player player, Holder<Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifier.id());
        }
    }

    // WG moves the wearer by 0.25 up a ladder it pushes against and 0.1875 down otherwise.
    private static void climb(Player player) {
        if (!player.level().isClientSide() || !player.onClimbable()) {
            return;
        }
        if (player.horizontalCollision) {
            player.move(MoverType.SELF, new Vec3(0.0, 0.25, 0.0));
        } else if (!player.isShiftKeyDown()) {
            player.move(MoverType.SELF, new Vec3(0.0, -0.1875, 0.0));
        }
    }

    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!wearsGear(player, GearSlot.VAMBRACE, BaublesFeature.HASTE_VAMBRACES.get())) {
            return;
        }
        float speed = event.getNewSpeed();
        if (!player.onGround()) {
            speed *= 5.0F;
        }
        if (player.isEyeInFluid(FluidTags.WATER) && player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED) < 1.0) {
            speed *= 5.0F;
        }
        BlockPos pos = event.getPosition().orElse(null);
        if (pos != null) {
            float hardness = event.getState().getDestroySpeed(player.level(), pos);
            if (hardness > 20) {
                speed = 5 + hardness;
            }
        }
        event.setNewSpeed(speed);
    }

    // The WG coremod adds two levels of fortune and looting to the held item of a luck ring wearer.
    @SubscribeEvent
    static void onEnchantmentLevel(GetEnchantmentLevelEvent event) {
        if (LUCKY.isEmpty()) {
            return;
        }
        for (Player player : LUCKY) {
            if (player.getMainHandItem() == event.getStack() && wearsRing(player, BaublesFeature.LUCK_RING.get())) {
                boost(event, Enchantments.FORTUNE);
                boost(event, Enchantments.LOOTING);
                return;
            }
        }
    }

    private static void boost(GetEnchantmentLevelEvent event, ResourceKey<Enchantment> key) {
        if (event.isTargetting(key)) {
            event.getHolder(key).ifPresent(holder -> event.getEnchantments().set(holder, event.getEnchantments().getLevel(holder) + LUCK_BONUS));
        }
    }

    @SubscribeEvent
    static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        String title = null;
        if (entity instanceof CultistCleric && entity.getRandom().nextInt(10) < 1) {
            title = TitleItem.CULTIST;
        } else if (entity instanceof CultistKnight && entity.getRandom().nextInt(10) < 1) {
            title = TitleItem.KNIGHT;
        } else if (entity instanceof CultistLeader && entity.getRandom().nextInt(2) == 0) {
            title = TitleItem.PRAETOR;
        }
        if (title != null) {
            ItemStack stack = TitleItem.withTitle(new ItemStack(BaublesFeature.CRIMSON_CULT_TITLE.get()), title);
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack));
        }
    }
}
