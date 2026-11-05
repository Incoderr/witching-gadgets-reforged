package witchinggadgets.gems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.device.LinkedMirrorBlockEntity;
import thaumcraft.node.Node;
import thaumcraft.platform.Teleport;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import witchinggadgets.fx.WgFx;
import witchinggadgets.platform.BaseConfig;

public final class GemEffects {
    private static final int NODE_SEARCH_CHUNKS = 8;

    private GemEffects() {
    }

    public static int charge(GemCut cut, Aspect aspect) {
        if (cut != GemCut.OVAL) {
            return InfusedGemItem.MAX_CHARGE;
        }
        if (aspect == Aspect.FIRE || aspect == Aspect.AIR || aspect == Aspect.EARTH) {
            return 1;
        }
        if (aspect == Aspect.WATER || aspect == Aspect.ORDER) {
            return 2;
        }
        if (aspect == Aspect.ENTROPY) {
            return BaseConfig.allowMirrors() ? 16 : 2;
        }
        return 0;
    }

    public static boolean perform(GemCut cut, @Nullable Aspect aspect, int potency, int brittle, Player player) {
        if (aspect == null || !aspect.isPrimal()) {
            return false;
        }
        return cut == GemCut.POINT ? point(aspect, potency, player) : oval(aspect, potency, player);
    }

    private static boolean point(Aspect aspect, int potency, Player player) {
        Level level = player.level();
        BlockPos origin = BlockPos.containing(player.getX(), (int) player.getY() + 1, player.getZ());
        if (aspect == Aspect.AIR) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300 + 20 * potency, 4 + (potency > 1 ? 1 : 0)));
        } else if (aspect == Aspect.FIRE) {
            int[] dist = potency > 2 ? new int[] {-8, -6, -4, 4, 6, 8} : potency > 1 ? new int[] {-6, -4, 4, 6} : potency > 0 ? new int[] {-4, 4} : new int[] {-2, 2};
            int ticks = 3600 + potency * 800;
            for (int dx : dist) {
                for (int dz : dist) {
                    glimmer(level, origin.offset(dx, 0, dz), ticks);
                }
            }
            glimmer(level, origin.above(2), ticks);
        } else if (aspect == Aspect.WATER) {
            if (!level.isClientSide()) {
                int dist = 2 + potency;
                for (int dy = -3; dy <= 0; dy++) {
                    for (int dx = -dist; dx <= dist; dx++) {
                        for (int dz = -dist; dz <= dist; dz++) {
                            freeze(level, origin.offset(dx, dy, dz));
                        }
                    }
                }
            }
        } else if (aspect == Aspect.EARTH) {
            if (!level.isClientSide()) {
                player.addEffect(new MobEffectInstance(GemsFeature.KNOCKBACK_RESISTANCE, 100 + potency * 100, 1 + (potency > 0 ? 1 : 0)));
            }
        } else if (aspect == Aspect.ORDER) {
            player.heal(6 + potency * 6);
        } else if (aspect == Aspect.ENTROPY) {
            if (player instanceof ServerPlayer serverPlayer) {
                Warp.add(serverPlayer, 3 + potency * 3, WarpType.TEMPORARY);
            }
        }
        return true;
    }

    private static void glimmer(Level level, BlockPos pos, int ticks) {
        if (!level.isEmptyBlock(pos) || level.getMaxLocalRawBrightness(pos) >= 10) {
            return;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, GemsFeature.GLIMMER_OF_LIGHT.get().defaultBlockState(), 3);
            if (level.getBlockEntity(pos) instanceof GlimmerBlockEntity glimmer) {
                glimmer.setLifetime(ticks);
            }
        }
    }

    private static void freeze(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        } else if (state.is(Blocks.LAVA)) {
            level.setBlock(pos, state.getFluidState().isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), 3);
        }
    }

    private static boolean oval(Aspect aspect, int potency, Player player) {
        Level level = player.level();
        BlockHitResult hit = pick(level, player);
        boolean onBlock = hit.getType() == HitResult.Type.BLOCK;
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        if (aspect == Aspect.FIRE) {
            if (onBlock && level.isEmptyBlock(target)) {
                level.playSound(player, target, SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                if (!level.isClientSide()) {
                    level.setBlock(target, Blocks.FIRE.defaultBlockState(), 3);
                }
                return true;
            }
            return false;
        }
        if (aspect == Aspect.WATER) {
            if (onBlock && (level.isEmptyBlock(target) || level.getBlockState(target).canBeReplaced())) {
                if (level.dimensionType().ultraWarm()) {
                    level.playSound(player, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                    WgFx.get().gemFizz(level, target);
                } else if (!level.isClientSide()) {
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir() && state.getFluidState().isEmpty()) {
                        level.destroyBlock(target, true);
                    }
                    level.setBlock(target, Fluids.WATER.defaultFluidState().createLegacyBlock(), 3);
                }
                return true;
            }
            return false;
        }
        if (aspect == Aspect.EARTH) {
            if (!onBlock) {
                return false;
            }
            if (level.isClientSide()) {
                WgFx.get().gemOres(level, hit.getBlockPos());
            }
            return true;
        }
        if (aspect == Aspect.AIR) {
            Vec3 look = player.getLookAngle();
            float mod = 1 + potency;
            player.push(look.x * mod, look.y * mod, look.z * mod);
            player.resetFallDistance();
            return true;
        }
        if (aspect == Aspect.ORDER) {
            BlockPos node = closestNode(level, player.blockPosition().above());
            if (node == null) {
                return false;
            }
            Vec3 center = Vec3.atCenterOf(node);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, center);
            } else {
                WgFx.get().gemNodeWisp(level, player, node);
            }
            return true;
        }
        if (aspect == Aspect.ENTROPY) {
            if (BaseConfig.allowMirrors()) {
                if (!onBlock) {
                    return false;
                }
                LinkedMirrorBlockEntity mirror = mirror(level, hit.getBlockPos(), target);
                if (mirror == null || !mirror.isLinked()) {
                    return false;
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    return teleport(serverPlayer, mirror);
                }
                return true;
            }
            AABB area = new AABB(player.getX() - 4, player.getY() - 2, player.getZ() - 4, player.getX() + 4, player.getY() + 2, player.getZ() + 4);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != player)) {
                living.push((living.getX() - player.getX()) * 0.4, 0.4, (living.getZ() - player.getZ()) * 0.4);
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15, 0));
            }
            return true;
        }
        return false;
    }

    private static BlockHitResult pick(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange()));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    @Nullable
    private static LinkedMirrorBlockEntity mirror(Level level, BlockPos clicked, BlockPos front) {
        if (level.getBlockEntity(clicked) instanceof LinkedMirrorBlockEntity mirror) {
            return mirror;
        }
        return level.getBlockEntity(front) instanceof LinkedMirrorBlockEntity mirror ? mirror : null;
    }

    private static boolean teleport(ServerPlayer player, LinkedMirrorBlockEntity mirror) {
        GlobalPos link = mirror.getLink();
        ServerLevel targetLevel = mirror.targetLevel();
        if (link == null || targetLevel == null) {
            return false;
        }
        BlockPos pos = link.pos();
        net.minecraft.core.Direction facing = targetLevel.getBlockEntity(pos) instanceof LinkedMirrorBlockEntity target ? target.getFacing() : net.minecraft.core.Direction.UP;
        float yaw = switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        Vec3 destination = new Vec3(pos.getX() + 0.5 + facing.getStepX() * 0.5, pos.getY() + facing.getStepY(), pos.getZ() + 0.5 + facing.getStepZ() * 0.5);
        return Teleport.to(player, targetLevel, destination, yaw, player.getXRot()) != null;
    }

    @Nullable
    public static BlockPos closestNode(Level level, BlockPos from) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        int centerX = SectionPos.blockToSectionCoord(from.getX());
        int centerZ = SectionPos.blockToSectionCoord(from.getZ());
        for (int cx = centerX - NODE_SEARCH_CHUNKS; cx <= centerX + NODE_SEARCH_CHUNKS; cx++) {
            for (int cz = centerZ - NODE_SEARCH_CHUNKS; cz <= centerZ + NODE_SEARCH_CHUNKS; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity instanceof Node) {
                        double distance = entity.getBlockPos().distSqr(from);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = entity.getBlockPos();
                        }
                    }
                }
            }
        }
        return best;
    }
}
