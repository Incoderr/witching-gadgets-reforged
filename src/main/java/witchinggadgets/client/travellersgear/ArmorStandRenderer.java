package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.platform.Baubles;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.Gear;
import witchinggadgets.travellersgear.ArmorStandBlock;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;
import witchinggadgets.travellersgear.ArmorStandMenu;

public class ArmorStandRenderer implements BlockEntityRenderer<ArmorStandBlockEntity, ArmorStandRenderer.State> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/models/travellersgear/armorstand.png");
    private static final float PLAYER_SCALE = 1.03125F;
    private static final float[] HSCALE = {0.625F, 0.5F, 0.5F, 1.0F, 0.5F, 1.0F, 1.0F};
    private static final float[] ROTATION = {0, 0, 0, 0, 0, 0, -45};
    private static final float[] SCALE_FOR_TYPE = {1.0F, 0.5F, 1.25F};
    private static final float[][] POS_NORMAL = {{1.5F, -0.3F}, {-1.5F, -0.3F}, {-0.15F, -1.2F}, {0.8F, -0.6F}};
    private static final float[][] POS_TINY = {{-1.5F, -1.5F}, {-1.0F, -1.5F}, {-0.5F, -1.5F}, {-1.45F, -1.0F}, {-0.9F, -0.8F}};
    private static final float[][] POS_BIG = {{1.2F, -1.6F}, {-0.3F, -0.8F}};
    private static @Nullable Model model;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.SOUTH;
        int flags = ArmorStandBlockEntity.ALL_FLAGS;
        final ItemStack[] items = new ItemStack[ArmorStandBlockEntity.SIZE];
        final ItemStackRenderState[] display = new ItemStackRenderState[7];
        @Nullable EntityRenderState mannequin;

        boolean flag(int bit) {
            return (flags & 1 << bit) != 0;
        }
    }

    public record Model(ModelPart floor, ModelPart head, ModelPart spine, ModelPart corpus, ModelPart table) {
    }

    public ArmorStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelPart box(int u, int v, float x, float y, float z, int w, int h, int d, float px, float py, float pz, float rx, float rz) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("box", CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d), PartPose.offsetAndRotation(px, py, pz, rx, 0.0F, rz));
        return LayerDefinition.create(mesh, 96, 32).bakeRoot();
    }

    private static ModelPart group(ModelPart... parts) {
        java.util.Map<String, ModelPart> children = new java.util.LinkedHashMap<>();
        for (int index = 0; index < parts.length; index++) {
            children.put("part" + index, parts[index]);
        }
        return new ModelPart(java.util.List.of(), children);
    }

    public static Model model() {
        if (model == null) {
            model = new Model(
                box(0, 12, 0, 0, 0, 16, 2, 16, -8.0F, 0.0F, -8.0F, 0, 0),
                box(68, 0, 0, 0, 0, 7, 7, 7, -3.5F, 26.0F, -7.5F, 0, 0),
                box(56, 0, 0, 0, 0, 3, 29, 3, -1.5F, 0.0F, -5.5F, 0, 0),
                group(
                    box(0, 17, 0, 0, 0, 4, 8, 4, -2.0F, 16.0F, -6.0F, 0, 0),
                    box(68, 14, 0, 0, 0, 6, 2, 4, -8.0F, 22.0F, -6.0F, 0, 0),
                    box(68, 14, 0, 0, 0, 6, 2, 4, 2.0F, 22.0F, -6.0F, 0, 0),
                    box(56, 0, -1, 0, -5, 2, 7, 2, 1.0F, 18.0F, 0.0F, 0, -0.7853982F),
                    box(56, 0, -1, 0, -5, 2, 7, 2, -1.0F, 18.0F, 0.0F, 0, 0.7853982F)
                ),
                group(
                    box(0, 0, 0, 0, 0, 16, 2, 10, -8.0F, 12.0F, -1.0F, 0.3F, 0),
                    box(9, 9, 0, 0, 0, 16, 1, 1, -8.0F, 11.2F, 8.175F, 0.3F, 0),
                    box(0, 14, 0, 0, 0, 4, 10, 4, 3.0F, 1.875F, 2.0F, 0, 0),
                    box(0, 14, 0, 0, 0, 4, 10, 4, -6.0F, 1.875F, 2.0F, 0, 0)
                )
            );
        }
        return model;
    }

    public static float rotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ArmorStandBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(ArmorStandBlock.FACING);
        state.flags = entity.flags();
        for (int slot = 0; slot < ArmorStandBlockEntity.SIZE; slot++) {
            state.items[slot] = entity.getItem(slot).copy();
        }
        Minecraft minecraft = Minecraft.getInstance();
        state.mannequin = null;
        if (minecraft.level instanceof ClientLevel level) {
            RemotePlayer fake = Mannequins.of(level);
            boolean any = dress(fake, state);
            if (any) {
                fake.setPos(Vec3.atBottomCenterOf(entity.getBlockPos()));
                fake.setYRot(0.0F);
                fake.setYHeadRot(0.0F);
                fake.yBodyRot = 0.0F;
                fake.yBodyRotO = 0.0F;
                fake.yHeadRotO = 0.0F;
                EntityRenderState mannequin = minecraft.getEntityRenderDispatcher().extractEntity(fake, partialTicks);
                mannequin.isInvisible = true;
                mannequin.nameTag = null;
                mannequin.shadowPieces.clear();
                mannequin.lightCoords = state.lightCoords;
                mannequin.outlineColor = 0;
                if (mannequin instanceof LivingEntityRenderState living) {
                    living.bodyRot = 0.0F;
                    living.yRot = 0.0F;
                    living.xRot = 0.0F;
                }
                state.mannequin = mannequin;
            }
            for (int dis = 0; dis < 7; dis++) {
                ItemStack stack = state.items[ArmorStandBlockEntity.BAUBLES + dis];
                if (stack.isEmpty()) {
                    state.display[dis] = null;
                    continue;
                }
                ItemStackRenderState item = new ItemStackRenderState();
                minecraft.getItemModelResolver().updateForTopItem(item, stack, ItemDisplayContext.NONE, level, null, (int) entity.getBlockPos().asLong() + dis);
                state.display[dis] = item;
            }
        }
    }

    private static boolean dress(RemotePlayer fake, State state) {
        boolean any = false;
        for (int index = 0; index < 4; index++) {
            ItemStack armor = state.flag(ArmorStandBlockEntity.RENDER_ARMOR + index) ? state.items[ArmorStandBlockEntity.ARMOR + index] : ItemStack.EMPTY;
            fake.setItemSlot(ArmorStandMenu.ARMOR_SLOTS[index], armor);
            ItemStack bauble = state.flag(ArmorStandBlockEntity.RENDER_BAUBLES + index) ? state.items[ArmorStandBlockEntity.BAUBLES + index] : ItemStack.EMPTY;
            Baubles.set(fake, index, bauble);
            any |= !armor.isEmpty() || !bauble.isEmpty();
        }
        for (int index = 0; index < ArmorStandMenu.GEAR_SLOTS.length; index++) {
            ItemStack gear = state.flag(ArmorStandBlockEntity.RENDER_GEAR + index) ? state.items[ArmorStandBlockEntity.GEAR + index] : ItemStack.EMPTY;
            Gear.set(fake, ArmorStandMenu.GEAR_SLOTS[index], gear);
            any |= !gear.isEmpty();
        }
        return any;
    }

    public static void renderStand(PoseStack poseStack, SubmitNodeCollector collector, int light, int flags) {
        Model parts = model();
        var type = TcRenderTypes.entityCutoutNoCull(TEXTURE);
        boolean floor = (flags & 1 << ArmorStandBlockEntity.RENDER_FLOOR) != 0;
        boolean table = (flags & 1 << ArmorStandBlockEntity.RENDER_TABLE) != 0;
        boolean head = (flags & 1 << ArmorStandBlockEntity.RENDER_ARMOR) != 0;
        boolean chest = (flags & 1 << ArmorStandBlockEntity.RENDER_ARMOR + 1) != 0;
        boolean legs = (flags & 1 << ArmorStandBlockEntity.RENDER_ARMOR + 2) != 0;
        if (floor) {
            collector.submitModelPart(parts.floor(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        } else {
            poseStack.translate(0.0F, -0.125F, 0.0F);
        }
        if (!table) {
            poseStack.translate(0.0F, 0.0F, 0.25F);
        }
        if (head) {
            collector.submitModelPart(parts.head(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        }
        if (head || chest || legs) {
            collector.submitModelPart(parts.spine(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        }
        if (chest) {
            collector.submitModelPart(parts.corpus(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        }
        if (table) {
            collector.submitModelPart(parts.table(), poseStack, type, light, OverlayTexture.NO_OVERLAY, null);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, rotation(state.facing));
        renderStand(poseStack, collector, state.lightCoords, state.flags);
        poseStack.translate(0.0F, 1.61F, -0.25F);
        if (state.mannequin != null && Minecraft.getInstance().level instanceof ClientLevel level) {
            dress(Mannequins.of(level), state);
            poseStack.pushPose();
            poseStack.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
            poseStack.translate(0.0F, 0.2F - 1.62F, 0.0F);
            Minecraft.getInstance().getEntityRenderDispatcher().submit(state.mannequin, camera, 0.0, 0.0, 0.0, poseStack, collector);
            poseStack.popPose();
        }
        if (state.flag(ArmorStandBlockEntity.RENDER_TABLE)) {
            renderTable(state, poseStack, collector);
        }
        poseStack.popPose();
    }

    private static void renderTable(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.translate(0.0F, 0.2F * PLAYER_SCALE, 0.0F);
        poseStack.rotateDegrees(Axis.XP, 180.0F);
        poseStack.rotateDegrees(Axis.ZP, 180.0F);
        poseStack.rotateDegrees(Axis.XP, (float) Math.toDegrees(-0.3));
        poseStack.scale(0.25F, 1.0F, 0.25F);
        poseStack.translate(-0.5F, -0.6625F, -2.6F);
        poseStack.rotateDegrees(Axis.XP, 90.0F);
        int occupiedTiny = 0;
        for (int dis = 0; dis < 7; dis++) {
            if (state.display[dis] != null && type(dis) == 1) {
                occupiedTiny++;
            }
        }
        int usedNormal = 0;
        int usedTiny = 0;
        int usedBig = 0;
        for (int dis = 0; dis < 7; dis++) {
            if (dis < 4 && !state.flag(ArmorStandBlockEntity.DISPLAY_BAUBLES + dis) || dis >= 4 && !state.flag(ArmorStandBlockEntity.DISPLAY_GEAR + dis - 4)) {
                continue;
            }
            ItemStackRenderState item = state.display[dis];
            if (item == null) {
                continue;
            }
            int type = type(dis);
            float[] position = type == 2 ? POS_BIG[Math.min(usedBig, POS_BIG.length - 1)] : type == 1 ? POS_TINY[Math.min(usedTiny, POS_TINY.length - 1)] : POS_NORMAL[Math.min(usedNormal, POS_NORMAL.length - 1)];
            float scale = SCALE_FOR_TYPE[type];
            float oY = position[1];
            if (dis == 5 && occupiedTiny < 4) {
                scale = 1.25F;
                oY -= 0.375F;
            }
            poseStack.pushPose();
            poseStack.translate(position[0], oY, 0.15F);
            poseStack.scale(scale, scale, HSCALE[dis]);
            poseStack.rotateDegrees(Axis.ZP, ROTATION[dis]);
            poseStack.translate(0.5F, 0.5F, -0.03125F);
            item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            if (type == 2 && usedBig < POS_BIG.length) {
                usedBig++;
            }
            if (type == 1 && usedTiny < POS_TINY.length) {
                usedTiny++;
            }
            if (type == 0 && usedNormal < POS_NORMAL.length) {
                usedNormal++;
            }
        }
    }

    private static int type(int dis) {
        return dis == 1 || dis == 2 ? 1 : dis == 3 || dis == 4 ? 2 : 0;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
