package witchinggadgets.primordial;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import witchinggadgets.WitchingGadgets;

public class PrimordialWeaponItem extends Item implements PrimordialGear {
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                PrimordialGear.cycleAndAnnounce(player, player.getItemInHand(hand));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}
