package witchinggadgets.primordial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.armor.FortressArmorItem;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.TcArmorMaterial;

public class PrimordialArmorItem extends FortressArmorItem {
    public static final Aspect[] ABILITIES = {Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
    public static final int AIR = 0;
    public static final int EARTH = 1;
    public static final int FIRE = 2;
    public static final int WATER = 3;
    public static final int ORDER = 4;
    public static final int ENTROPY = 5;
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public PrimordialArmorItem(TcArmorMaterial material, ArmorSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    public static int ability(ItemStack stack) {
        int ability = PrimordialFeature.ABILITY.getOrDefault(stack, 0);
        return ability >= 0 && ability < ABILITIES.length ? ability : 0;
    }

    public static Aspect abilityAspect(ItemStack stack) {
        return ABILITIES[ability(stack)];
    }

    public static void setAbility(ItemStack stack, int ability) {
        PrimordialFeature.ABILITY.set(stack, ability == 0 ? null : ability);
    }

    public static void cycle(ItemStack stack) {
        setAbility(stack, (ability(stack) + 1) % ABILITIES.length);
    }

    public static List<ItemStack> worn(LivingEntity entity) {
        List<ItemStack> pieces = new ArrayList<>();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof PrimordialArmorItem) {
                pieces.add(stack);
            }
        }
        return pieces;
    }

    public static int count(LivingEntity entity, int ability) {
        int count = 0;
        for (ItemStack piece : worn(entity)) {
            if (ability(piece) == ability) {
                count++;
            }
        }
        return count;
    }

    public static boolean cycleWorn(Player player) {
        List<ItemStack> pieces = worn(player);
        if (pieces.isEmpty()) {
            return false;
        }
        int next = (ability(pieces.getFirst()) + 1) % ABILITIES.length;
        for (ItemStack piece : pieces) {
            setAbility(piece, next);
        }
        player.displayClientMessage(Component.translatable("wg.chat.primordialArmor.ability", aspectName(ABILITIES[next])), true);
        return true;
    }

    public static MutableComponent aspectName(Aspect aspect) {
        MutableComponent name = Component.literal(aspect.getName());
        String code = aspect.getChatcolor();
        ChatFormatting color = code == null || code.isEmpty() ? null : ChatFormatting.getByCode(code.charAt(0));
        return color == null ? name : name.withStyle(color);
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy()
            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
            .append(aspectName(abilityAspect(stack)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                cycle(stack);
                player.displayClientMessage(Component.translatable("wg.chat.primordialArmor.ability", aspectName(abilityAspect(stack))), true);
            }
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }
}
