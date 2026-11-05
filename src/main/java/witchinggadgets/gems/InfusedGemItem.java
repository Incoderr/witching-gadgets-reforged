package witchinggadgets.gems;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.vis.VisRelayBlockEntity;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.registry.CreativeVariants;

public class InfusedGemItem extends Item implements CreativeVariants {
    public static final int MAX_CHARGE = 32;

    public InfusedGemItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(@Nullable Aspect aspect, @Nullable GemCut cut) {
        ItemStack stack = new ItemStack(GemsFeature.INFUSED_GEM.get());
        if (cut != null) {
            GemsFeature.GEM_CUT.set(stack, cut.id());
        }
        if (aspect != null) {
            GemsFeature.GEM_ASPECT.set(stack, aspect.getTag());
        }
        return stack;
    }

    public static @Nullable Aspect aspect(ItemStack stack) {
        String tag = GemsFeature.GEM_ASPECT.get(stack);
        return tag == null ? null : Aspect.getAspect(tag);
    }

    public static @Nullable GemCut cut(ItemStack stack) {
        String id = GemsFeature.GEM_CUT.get(stack);
        return id == null ? null : GemCut.byId(id);
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (holder.is(key)) {
                return enchantments.getLevel(holder);
            }
        }
        return 0;
    }

    public static void enchant(Level level, ItemStack stack, ResourceKey<Enchantment> key, int value) {
        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
            .ifPresent(holder -> EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.set(holder, value)));
    }

    public static int potency(ItemStack stack) {
        return level(stack, GemsFeature.GEMSTONE_POTENCY);
    }

    public static int brittle(ItemStack stack) {
        return level(stack, GemsFeature.GEMSTONE_BRITTLE);
    }

    public static void shatter(Player player, ItemStack stack) {
        if (!player.level().isClientSide()) {
            ItemStack dust = new ItemStack(MaterialsFeature.GEMSTONE_DUST.get());
            if (!player.getInventory().add(dust)) {
                player.drop(dust, false, Prediction.SERVER_ONLY);
            }
            player.sendSystemMessage(Component.translatable("wg.chat.gem.shatter"));
        }
        stack.shrink(1);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int brittle = brittle(stack);
        if (brittle >= 3 || level.getRandom().nextInt(20) < brittle) {
            shatter(player, stack);
            return InteractionResult.SUCCESS;
        }
        int potency = potency(stack);
        Aspect aspect = aspect(stack);
        GemCut cut = cut(stack);
        if (cut == GemCut.POINT) {
            if (!GemEffects.perform(GemCut.POINT, aspect, potency, brittle, player)) {
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                ItemStack dust = new ItemStack(MaterialsFeature.GEMSTONE_DUST.get());
                if (!player.getInventory().add(dust)) {
                    player.drop(dust, false, Prediction.SERVER_ONLY);
                }
            }
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (cut == GemCut.OVAL && aspect != null && stack.getDamageValue() + GemEffects.charge(GemCut.OVAL, aspect) <= stack.getMaxDamage()) {
            boolean used = GemEffects.perform(GemCut.OVAL, aspect, potency, brittle, player);
            if (used && !player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue() + GemEffects.charge(GemCut.OVAL, aspect));
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void inventoryTick(Player player, ItemStack stack, boolean selected) {
        int brittle = brittle(stack);
        if (player.getRandom().nextInt(1000) < brittle || cut(stack) == GemCut.POINT && stack.getDamageValue() != 0) {
            shatter(player, stack);
            return;
        }
        if (player.getRandom().nextInt(20) < brittle) {
            return;
        }
        Aspect aspect = aspect(stack);
        if (cut(stack) != GemCut.OVAL || aspect == null || !selected || player.tickCount % 4 != 0 || stack.getDamageValue() <= 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel)) {
            return;
        }
        VisRelayBlockEntity relay = VisRelayBlockEntity.getNearbyRelay(player);
        if (relay == null) {
            return;
        }
        if (relay.isRemoved() || relay.getBlockPos().distToCenterSqr(player.position()) >= 26.0) {
            VisRelayBlockEntity.NEARBY_PLAYERS.remove(player.getId());
            return;
        }
        int amount = relay.consumeVis(aspect, 1);
        if (amount > 0) {
            stack.setDamageValue(stack.getDamageValue() - amount);
            relay.triggerConsumeEffect(aspect);
        }
    }

    @Override
    public void creativeStacks(Consumer<ItemStack> output) {
        output.accept(create(null, GemCut.POINT));
        Aspect[] order = {Aspect.ORDER, Aspect.ENTROPY, Aspect.FIRE, Aspect.WATER, Aspect.AIR, Aspect.EARTH};
        for (GemCut cut : GemCut.values()) {
            for (Aspect aspect : order) {
                output.accept(create(aspect, cut));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Aspect aspect = aspect(stack);
        if (aspect == null) {
            return Component.translatable(getDescriptionId() + ".inert");
        }
        return Component.translatable(getDescriptionId()).append(" (" + aspect.getName() + ")");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        GemCut cut = cut(stack);
        if (cut != null) {
            builder.accept(Component.translatable("wg.desc.gemcut." + cut.name()));
        }
    }
}
