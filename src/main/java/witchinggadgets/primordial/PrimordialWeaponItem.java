package witchinggadgets.primordial;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.gear.GearAbility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import witchinggadgets.WitchingGadgets;

public class PrimordialWeaponItem extends Item implements PrimordialGear, GearAbility {
    public static final TagKey<Block> HAMMER_MINEABLE = BlockTags.create(WitchingGadgets.id("primordial_hammer_mineable"));
    public static final TagKey<Block> GREATAXE_MINEABLE = BlockTags.create(WitchingGadgets.id("primordial_greataxe_mineable"));

    public enum Kind {
        CLAYMORE,
        HAMMER,
        GREATAXE
    }

    private final Kind kind;

    public PrimordialWeaponItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public static boolean is(ItemStack stack, Kind kind) {
        return stack.getItem() instanceof PrimordialWeaponItem weapon && weapon.kind == kind;
    }

    @Override
    public int returnedPearls() {
        return 2;
    }

    @Override
    public Component getName(ItemStack stack) {
        return PrimordialGear.name(super.getName(stack), stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ability == ItemAbilities.SHIELD_BLOCK || super.canPerformAction(stack, ability);
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
