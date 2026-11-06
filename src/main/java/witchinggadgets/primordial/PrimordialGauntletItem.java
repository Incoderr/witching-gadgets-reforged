package witchinggadgets.primordial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.node.NodeBlockEntity;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import witchinggadgets.gems.GemCut;
import witchinggadgets.gems.GemEffects;
import witchinggadgets.gems.InfusedGemItem;

public class PrimordialGauntletItem extends Item implements PrimordialGear {
    public static final int SLOTS = 5;

    public PrimordialGauntletItem(Properties properties) {
        super(properties);
    }

    @Override
    public int returnedPearls() {
        return 1;
    }

    public static NonNullList<ItemStack> gems(ItemStack gauntlet) {
        NonNullList<ItemStack> gems = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        PrimordialFeature.GAUNTLET_GEMS.getOrDefault(gauntlet, ItemContainerContents.EMPTY).copyInto(gems);
        return gems;
    }

    public static void setGems(ItemStack gauntlet, NonNullList<ItemStack> gems) {
        PrimordialFeature.GAUNTLET_GEMS.set(gauntlet, ItemContainerContents.fromItems(gems));
    }

    public static int selected(ItemStack gauntlet) {
        int selected = PrimordialFeature.GAUNTLET_SELECTED.getOrDefault(gauntlet, 0);
        return selected >= 0 && selected < SLOTS ? selected : 0;
    }

    public static void select(ItemStack gauntlet, int slot) {
        PrimordialFeature.GAUNTLET_SELECTED.set(gauntlet, Mth.clamp(slot, 0, SLOTS - 1));
    }

    public static boolean useGem(ItemStack gauntlet, Player player) {
        NonNullList<ItemStack> gems = gems(gauntlet);
        int selected = selected(gauntlet);
        ItemStack gem = gems.get(selected);
        Aspect aspect = InfusedGemItem.aspect(gem);
        if (!(gem.getItem() instanceof InfusedGemItem) || aspect == null) {
            return false;
        }
        GemCut cut = InfusedGemItem.cut(gem);
        int charge = GemEffects.charge(cut, aspect);
        if (gem.getDamageValue() + charge > gem.getMaxDamage()) {
            return false;
        }
        if (!GemEffects.perform(cut, aspect, InfusedGemItem.potency(gem), InfusedGemItem.brittle(gem), player)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            gem.setDamageValue(gem.getDamageValue() + charge);
            gems.set(selected, gem);
            setGems(gauntlet, gems);
        }
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack gauntlet = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        boolean used = useGem(gauntlet, player);
        if (!used && player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            GauntletMenu.open(serverPlayer, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof NodeBlockEntity node) || node.getClass() != NodeBlockEntity.class) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            AspectList primals = new AspectList();
            AspectList reduced = AspectHelper.reduceToPrimals(node.getAspects(), true);
            NodeModifier modifier = node.getNodeModifier();
            for (Aspect aspect : reduced.getAspects()) {
                int amount = reduced.getAmount(aspect);
                if (modifier == NodeModifier.BRIGHT) {
                    amount = (int) (amount * 1.2F);
                } else if (modifier == NodeModifier.PALE) {
                    amount = (int) (amount * 0.8F);
                } else if (modifier == NodeModifier.FADING) {
                    amount = (int) (amount * 0.5F);
                }
                amount = Mth.floor(Math.sqrt(amount));
                if (node.getNodeType() == NodeType.UNSTABLE) {
                    amount += level.getRandom().nextInt(5) - 2;
                }
                if (amount >= 1) {
                    primals.merge(aspect, amount);
                }
            }
            PrimordialFeature.GAUNTLET_NODE.set(context.getItemInHand(), new StoredNode(node.getNodeType().ordinal(), modifier == null ? -1 : modifier.ordinal(), primals));
            level.removeBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }

    public static void restore(ItemStack gauntlet, NonNullList<ItemStack> gems) {
        StoredNode node = PrimordialFeature.GAUNTLET_NODE.get(gauntlet);
        if (node == null) {
            return;
        }
        for (ItemStack gem : gems) {
            Aspect aspect = InfusedGemItem.aspect(gem);
            if (!gem.isEmpty() && gem.isDamaged() && aspect != null) {
                gem.setDamageValue(Math.max(gem.getDamageValue() - node.aspects().getAmount(aspect), 0));
            }
        }
    }

    public static void tick(Player player, ItemStack gauntlet) {
        if (player.tickCount % 10 != 0 || !PrimordialFeature.GAUNTLET_NODE.has(gauntlet)) {
            return;
        }
        if (player.containerMenu instanceof GauntletMenu menu && menu.gauntlet() == gauntlet) {
            restore(gauntlet, menu.storage());
            menu.storageChanged();
            return;
        }
        NonNullList<ItemStack> gems = gems(gauntlet);
        restore(gauntlet, gems);
        setGems(gauntlet, gems);
    }
}
