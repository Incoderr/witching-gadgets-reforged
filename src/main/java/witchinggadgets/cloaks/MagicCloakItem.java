package witchinggadgets.cloaks;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import witchinggadgets.gear.GearAbility;
import witchinggadgets.platform.Gear;

public abstract class MagicCloakItem extends Item implements GearAbility {
    public static final int STORAGE_SIZE = 27;

    private final CloakVariant variant;

    protected MagicCloakItem(CloakVariant variant, Properties properties) {
        super(properties.stacksTo(1));
        this.variant = variant;
    }

    public CloakVariant variant() {
        return variant;
    }

    public int color(ItemStack stack) {
        if (variant == CloakVariant.STANDARD) {
            DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
            return dyed == null ? variant.color() : dyed.rgb();
        }
        return variant.color();
    }

    public static boolean isSpectral(ItemStack stack) {
        return CloaksFeature.SPECTRAL.getOrDefault(stack, false);
    }

    protected abstract String slotDescription();

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(slotDescription()));
    }

    public void cloakTick(Player player, ItemStack stack) {
        if (variant == CloakVariant.SPECTRAL && !player.level().isClientSide() && isSpectral(stack) && player.tickCount % 100 == 0) {
            if (!consumeAer(player)) {
                CloaksFeature.SPECTRAL.set(stack, false);
                Gear.sync(player);
            }
        }
        if (variant == CloakVariant.WOLF && CloaksFeature.WOLF_POTION.has(stack)) {
            int amplifier = CloaksFeature.WOLF_POTION.getOrDefault(stack, 0);
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, amplifier));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, amplifier));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, amplifier));
            CloaksFeature.WOLF_POTION.remove(stack);
        }
    }

    public void cloakUnequipped(Player player, ItemStack stack) {
        if (isSpectral(stack)) {
            CloaksFeature.SPECTRAL.set(stack, false);
            if (!player.level().isClientSide()) {
                Gear.sync(player);
            }
        }
    }

    public void userDamaged(ItemStack stack, float amount) {
        if (variant != CloakVariant.WOLF) {
            return;
        }
        int amplifier = 1;
        if (amount >= 8) {
            amplifier++;
        }
        if (amount >= 12) {
            amplifier++;
        }
        CloaksFeature.WOLF_POTION.set(stack, amplifier);
    }

    @Override
    public boolean canActivate(Player player, ItemStack stack, boolean inHand) {
        return !inHand && (variant == CloakVariant.SPECTRAL || variant == CloakVariant.STORAGE);
    }

    @Override
    public void activate(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (variant == CloakVariant.STORAGE) {
            CloakStorageMenu.open(serverPlayer, stack);
        } else if (variant == CloakVariant.SPECTRAL && consumeAer(player)) {
            boolean spectral = !isSpectral(stack);
            CloaksFeature.SPECTRAL.set(stack, spectral);
            Gear.sync(player);
            if (spectral) {
                for (Mob mob : player.level().getEntitiesOfClass(Mob.class, new AABB(player.blockPosition()).inflate(16))) {
                    if (!CloakEvents.isBoss(mob) && player.equals(mob.getTarget())) {
                        mob.setTarget(null);
                    }
                }
            }
        }
    }

    private static boolean consumeAer(Player player) {
        AspectList cost = new AspectList().add(Aspect.AIR, 1);
        Inventory inventory = player.getInventory();
        for (int slot = Inventory.INVENTORY_SIZE - 1; slot >= 0; slot--) {
            ItemStack wand = inventory.getItem(slot);
            if (wand.getItem() instanceof WandItem item && item.consumeAllVisCrafting(wand, player, cost, true)) {
                return true;
            }
        }
        return false;
    }
}
