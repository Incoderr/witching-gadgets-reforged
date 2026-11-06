package witchinggadgets.client.terraform;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import witchinggadgets.terraform.TerraformFocus;
import witchinggadgets.terraform.TerraformFocusBlockEntity;

public class TerraformFocusRenderer implements BlockEntityRenderer<TerraformFocusBlockEntity, TerraformFocusRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState displayed = new ItemStackRenderState();
        int color = 0xFFFFFFFF;
        float rotation;
    }

    public TerraformFocusRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TerraformFocusBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        TerraformFocus focus = entity.focus();
        state.color = 0xFF000000 | focus.aspect().getColor();
        state.displayed.clear();
        if (entity.getLevel() != null) {
            state.rotation = (entity.getLevel().getGameTime() % 40 + partialTicks) / 40.0F;
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(focus.displayed())));
            itemModelResolver.updateForTopItem(state.displayed, stack, ItemDisplayContext.NONE, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        TerraformRendering.focus(poseStack, collector, state.lightCoords, state.color);
        if (!state.displayed.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.3125F, 0.5F);
            poseStack.scale(0.25F, 0.25F, 0.25F);
            poseStack.rotateDegrees(Axis.YP, state.rotation * 360.0F);
            poseStack.rotateDegrees(Axis.ZP, state.rotation * 360.0F);
            state.displayed.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
