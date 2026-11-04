package witchinggadgets.gadgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;
import thaumcraft.block.InfusionStabiliser;
import thaumcraft.crafting.InfusionCrafting;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.infusion.InfusionHooks;
import thaumcraft.infusion.InfusionMatrixBlockEntity;
import thaumcraft.infusion.PedestalBlockEntity;
import thaumcraft.node.Node;

public class ArcaneAbacusItem extends Item {
    private static final String CHAT = "wg.chat.";

    public record Warning(String key, BlockPos pos) {
    }

    public record Report(int stability, List<Component> lines) {
    }

    public ArcaneAbacusItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockEntity entity = level.getBlockEntity(pos);
        if (player == null || !(entity instanceof Node || entity instanceof InfusionMatrixBlockEntity)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            List<Component> lines = entity instanceof Node node ? List.of(nodeContents(node)) : inspectMatrix(level, pos, player).lines();
            for (Component line : lines) {
                player.sendSystemMessage(line);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static Component nodeContents(Node node) {
        AspectList primals = AspectHelper.reduceToPrimals(node.getAspects());
        return Component.translatable(
            CHAT + "nodeContents.primal",
            primals.getAmount(Aspect.AIR),
            primals.getAmount(Aspect.EARTH),
            primals.getAmount(Aspect.FIRE),
            primals.getAmount(Aspect.WATER),
            primals.getAmount(Aspect.ORDER),
            primals.getAmount(Aspect.ENTROPY)
        );
    }

    public static Report inspectMatrix(Level level, BlockPos matrix, Player player) {
        List<BlockPos> stabilizers = new ArrayList<>();
        List<BlockPos> pedestals = new ArrayList<>();
        List<Warning> warnings = new ArrayList<>();
        List<ItemStack> components = new ArrayList<>();
        for (int xx = -12; xx <= 12; xx++) {
            for (int zz = -12; zz <= 12; zz++) {
                boolean skip = false;
                for (int yy = -10; yy <= 5; yy++) {
                    if (xx == 0 && zz == 0) {
                        continue;
                    }
                    BlockPos check = matrix.offset(xx, yy, zz);
                    if (!level.hasChunkAt(check)) {
                        continue;
                    }
                    if (!skip && level.getBlockEntity(check) instanceof PedestalBlockEntity) {
                        if (yy >= 0) {
                            warnings.add(new Warning("pedestalHeight", check));
                        } else if (Math.abs(xx) > 8 || Math.abs(zz) > 8) {
                            warnings.add(new Warning("pedestalPos", check));
                        } else {
                            pedestals.add(check);
                            skip = true;
                        }
                    } else if (isStabilizer(level, check)) {
                        stabilizers.add(check);
                    }
                }
            }
        }
        int symmetry = 0;
        for (BlockPos pedestalPos : pedestals) {
            boolean items = false;
            if (level.getBlockEntity(pedestalPos) instanceof PedestalBlockEntity pedestal) {
                symmetry += 2;
                if (!pedestal.getStack().isEmpty()) {
                    symmetry += 1;
                    items = true;
                    components.add(pedestal.getStack());
                }
            }
            BlockPos mirror = mirror(matrix, pedestalPos);
            if (level.hasChunkAt(mirror) && level.getBlockEntity(mirror) instanceof PedestalBlockEntity partner) {
                symmetry -= 2;
                if (!partner.getStack().isEmpty()) {
                    if (items) {
                        symmetry -= 1;
                    } else {
                        warnings.add(new Warning("noPartnerItem", mirror));
                    }
                }
            } else {
                warnings.add(new Warning("noPartnerPedestal", pedestalPos));
            }
        }
        float stabilizerSymmetry = 0.0F;
        for (BlockPos stabilizerPos : stabilizers) {
            if (isStabilizer(level, stabilizerPos)) {
                stabilizerSymmetry += 0.1F;
            }
            BlockPos mirror = mirror(matrix, stabilizerPos);
            if (level.hasChunkAt(mirror) && isStabilizer(level, mirror)) {
                stabilizerSymmetry -= 0.2F;
            } else {
                warnings.add(new Warning("noPartnerStabilizer", stabilizerPos));
            }
        }
        symmetry = (int) (symmetry + stabilizerSymmetry);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(CHAT + "infusionInfo.stabilityTotal", symmetry * -1).withStyle(ChatFormatting.DARK_PURPLE));

        ItemStack central = level.getBlockEntity(matrix.below(2)) instanceof PedestalBlockEntity pedestal ? pedestal.getStack().copy() : ItemStack.EMPTY;
        if (!central.isEmpty()) {
            InfusionCrafting.Plan infusion = InfusionCrafting.findRecipe(components, central, player);
            InfusionCrafting.Plan enchantment = InfusionCrafting.findEnchantment(level, components, central, player);
            if (enchantment != null) {
                lines.add(Component.translatable(CHAT + "infusionInfo.instability", enchantment.instability()).withStyle(ChatFormatting.DARK_PURPLE));
                if (TcRecipes.get(enchantment.key()) instanceof InfusionEnchantmentRecipe recipe) {
                    float essentiaMod = 1 + essentiaMod(level, recipe, central);
                    if (essentiaMod > 1) {
                        StringBuilder plain = new StringBuilder();
                        Iterator<Aspect> iterator = recipe.aspects().getAspects().iterator();
                        while (iterator.hasNext()) {
                            Aspect aspect = iterator.next();
                            plain.append((int) (recipe.aspects().getAmount(aspect) * essentiaMod)).append(' ').append(aspect.getName());
                            if (iterator.hasNext()) {
                                plain.append(", ");
                            }
                        }
                        lines.add(Component.translatable(CHAT + "infusionInfo.essentiaMod", essentiaMod).withStyle(ChatFormatting.GREEN));
                        lines.add(Component.literal(plain.toString()).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
            if (infusion != null) {
                lines.add(Component.translatable(CHAT + "infusionInfo.instability", infusion.instability()).withStyle(ChatFormatting.DARK_PURPLE));
                if (infusion.type() == InfusionCrafting.TYPE_RUNIC) {
                    int charge = InfusionHooks.getFinalCharge(central);
                    int vis = (int) (32.0 * Math.pow(2.0, charge));
                    String plain = vis > 0
                        ? vis + " " + Aspect.ENERGY.getName() + ", " + vis / 2 + " " + Aspect.MAGIC.getName() + ", " + vis / 2 + " " + Aspect.ARMOR.getName()
                        : "";
                    lines.add(Component.translatable(CHAT + "infusionInfo.essentiaRunicMod", charge).withStyle(ChatFormatting.GREEN));
                    lines.add(Component.literal(plain).withStyle(ChatFormatting.GRAY));
                }
            }
        }
        for (Warning warning : warnings) {
            lines.add(Component.translatable(
                CHAT + "infusionWarning." + warning.key(),
                warning.pos().getX(),
                warning.pos().getY(),
                warning.pos().getZ()
            ).withStyle(ChatFormatting.DARK_GRAY));
        }
        return new Report(symmetry * -1, lines);
    }

    private static float essentiaMod(Level level, InfusionEnchantmentRecipe recipe, ItemStack central) {
        Holder<Enchantment> holder = InfusionCrafting.enchantment(level, recipe.enchantment());
        if (holder == null) {
            return 0.0F;
        }
        ItemEnchantments existing = EnchantmentHelper.getEnchantmentsForCrafting(central);
        float modifier = existing.getLevel(holder);
        for (Holder<Enchantment> other : existing.keySet()) {
            if (!other.equals(holder)) {
                modifier += existing.getLevel(other) * 0.1F;
            }
        }
        return modifier;
    }

    private static BlockPos mirror(BlockPos matrix, BlockPos pos) {
        return new BlockPos(2 * matrix.getX() - pos.getX(), pos.getY(), 2 * matrix.getZ() - pos.getZ());
    }

    private static boolean isStabilizer(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof AbstractSkullBlock || block instanceof InfusionStabiliser stabiliser && stabiliser.canStabiliseInfusion(level, pos);
    }
}
