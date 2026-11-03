package witchinggadgets.client.devices;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import witchinggadgets.devices.LabelLibraryBlock;
import witchinggadgets.devices.LabelLibraryBlockEntity;

public class LabelLibraryRenderer implements BlockEntityRenderer<LabelLibraryBlockEntity, LabelLibraryRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState output = new ItemStackRenderState();
        final ItemStackRenderState input = new ItemStackRenderState();
        int stacked;
        float rotation;
    }

    public LabelLibraryRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LabelLibraryBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.output.clear();
        state.input.clear();
        state.stacked = 0;
        Direction facing = entity.getBlockState().getValue(LabelLibraryBlock.FACING);
        state.rotation = switch (facing) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        if (entity.getLevel() == null) {
            return;
        }
        int seed = (int) entity.getBlockPos().asLong();
        ItemStack output = entity.output();
        if (!output.isEmpty()) {
            itemModelResolver.updateForTopItem(state.output, output, ItemDisplayContext.FIXED, entity.getLevel(), null, seed);
        }
        ItemStack input = entity.getItem(LabelLibraryBlockEntity.INPUT);
        if (!input.isEmpty()) {
            itemModelResolver.updateForTopItem(state.input, input.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, seed);
            state.stacked = Math.max(1, input.getCount() / 6);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, state.rotation);
        if (!state.output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(-0.1875F, 1.02F, -0.25F);
            poseStack.rotateDegrees(Axis.XP, 90.0F);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            state.output.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (!state.input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.14F, 1.02F, 0.275F);
            for (int index = 0; index < state.stacked; index++) {
                poseStack.rotateDegrees(Axis.YP, -(10 + index * index));
                poseStack.translate(0.0F, 0.019F, 0.0F);
                poseStack.pushPose();
                poseStack.rotateDegrees(Axis.XP, 90.0F);
                poseStack.scale(0.75F, 0.75F, 0.75F);
                state.input.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
