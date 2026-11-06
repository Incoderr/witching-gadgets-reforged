package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import witchinggadgets.terraform.TerraformerBlockEntity;

public class TerraformerRenderer implements BlockEntityRenderer<TerraformerBlockEntity, TerraformerRenderer.State> {
    public static class State extends BlockEntityRenderState {
        int color = 0xFFFFFFFF;
    }

    public TerraformerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TerraformerBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        Aspect suction = entity.suction();
        state.color = suction == null ? 0xFFFFFFFF : 0xFF000000 | suction.getColor();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        TerraformRendering.terraformer(poseStack, collector, state.lightCoords, state.color);
    }
}
