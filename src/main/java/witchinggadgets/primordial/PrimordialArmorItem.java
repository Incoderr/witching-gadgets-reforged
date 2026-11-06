package witchinggadgets.primordial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.gear.GearAbility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.item.armor.FortressArmorItem;

public class PrimordialArmorItem extends FortressArmorItem implements PrimordialGear, GearAbility {
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

    @Override
    public int returnedPearls() {
        return 3;
    }

    @Override
    public Component getName(ItemStack stack) {
        return PrimordialGear.name(super.getName(stack), stack);
    }

    @Override
    public boolean canActivate(Player player, ItemStack stack, boolean inHand) {
        return true;
    }

    @Override
    public void activate(Player player, ItemStack stack) {
        if (!player.level().isClientSide()) {
            PrimordialGear.cycleAndAnnounce(player, stack);
        }
    }
}
