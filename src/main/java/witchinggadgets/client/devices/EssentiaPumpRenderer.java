package witchinggadgets.client.devices;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.EntityQuadSink;
import thaumcraft.client.render.AlchemyRendering;
import witchinggadgets.devices.EssentiaPumpBlock;
import witchinggadgets.devices.EssentiaPumpBlockEntity;

public class EssentiaPumpRenderer implements BlockEntityRenderer<EssentiaPumpBlockEntity, EssentiaPumpRenderer.State> {
    private static final Identifier ALEMBIC = Identifier.fromNamespaceAndPath("thaumcraft", "textures/model/alembic.png");
    private static final Identifier NOZZLE = Identifier.fromNamespaceAndPath("thaumcraft", "textures/block/alchemyblock.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float inflation = 0.65F;
    }

    public EssentiaPumpRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaPumpBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(EssentiaPumpBlock.FACING);
        state.inflation = entity.getLevel() == null ? 0.65F : inflation(entity.getLevel().getGameTime() + partialTicks);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        render(poseStack, collector, state.lightCoords, state.facing, state.inflation);
    }

    public static float inflation(float ticks) {
        float t = ticks % 16.0F;
        return t < 4.0F ? 0.65F - 0.075F * t : 0.35F + 0.025F * (t - 4.0F);
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, int light, Direction facing, float inflation) {
        poseStack.pushPose();
        switch (facing) {
            case SOUTH -> {
                poseStack.rotateDegrees(Axis.YP, 180.0F);
                poseStack.translate(-1.0F, 0.0F, -1.0F);
            }
            case EAST -> {
                poseStack.rotateDegrees(Axis.YP, 270.0F);
                poseStack.translate(0.0F, 0.0F, -1.0F);
            }
            case WEST -> {
                poseStack.rotateDegrees(Axis.YP, 90.0F);
                poseStack.translate(-1.0F, 0.0F, 0.0F);
            }
            default -> {
            }
        }
        collector.submitCustomGeometry(poseStack, TcRenderTypes.entityCutoutNoCull(ALEMBIC), (pose, buffer) -> PumpGeometry.funnel(new EntityQuadSink(pose, buffer), light));
        collector.submitCustomGeometry(poseStack, TcRenderTypes.entityCutoutNoCull(NOZZLE), (pose, buffer) -> PumpGeometry.nozzle(new EntityQuadSink(pose, buffer), light));

        poseStack.scale(0.375F, 0.375F, 0.375F);
        poseStack.translate(0.625F, -0.0625F, -0.125F);
        poseStack.rotateDegrees(Axis.ZP, 45.0F);
        poseStack.rotateDegrees(Axis.YP, -90.0F);

        poseStack.translate(0.0F, 0.0F, -1.0625F);
        bellows(poseStack, collector, light, inflation);
        poseStack.translate(0.0F, 0.0F, 1.0625F);

        poseStack.rotateDegrees(Axis.XP, -90.0F);
        poseStack.translate(0.0F, 0.9375F, 0.875F);
        bellows(poseStack, collector, light, inflation);
        poseStack.translate(0.0F, -0.9375F, -0.875F);

        poseStack.rotateDegrees(Axis.XP, -90.0F);
        poseStack.translate(0.0F, -1.0F, 1.8125F);
        bellows(poseStack, collector, light, inflation);
        poseStack.translate(0.0F, 1.0F, -1.8125F);

        poseStack.rotateDegrees(Axis.XP, -90.0F);
        poseStack.translate(0.0F, -1.9375F, -0.125F);
        bellows(poseStack, collector, light, inflation);
        poseStack.popPose();
    }

    private static void bellows(PoseStack poseStack, SubmitNodeCollector collector, int light, float inflation) {
        AlchemyRendering.bellows(collector, poseStack, light, Direction.DOWN, inflation);
    }
}
