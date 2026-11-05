package witchinggadgets.primordial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.item.armor.FortressArmorItem;

public class PrimordialArmorItem extends FortressArmorItem implements PrimordialGear {
    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public PrimordialArmorItem(Properties properties) {
        super(properties);
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
            if (PrimordialGear.ability(piece) == ability) {
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
        int next = (PrimordialGear.ability(pieces.getFirst()) + 1) % ABILITIES.length;
        for (ItemStack piece : pieces) {
            PrimordialGear.setAbility(piece, next);
        }
        player.sendOverlayMessage(PrimordialGear.announcement(ABILITIES[next]));
        return true;
    }

    @Override
    public int returnedPearls() {
        return 3;
    }

    @Override
    public Component getName(ItemStack stack) {
        return PrimordialGear.name(super.getName(stack), stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide()) {
                PrimordialGear.cycleAndAnnounce(player, stack);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}
