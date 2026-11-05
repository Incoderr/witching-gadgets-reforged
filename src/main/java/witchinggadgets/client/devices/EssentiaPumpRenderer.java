package witchinggadgets.client.devices;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.AlchemyRendering;
import witchinggadgets.devices.EssentiaPumpBlock;
import witchinggadgets.devices.EssentiaPumpBlockEntity;

public class EssentiaPumpRenderer implements BlockEntityRenderer<EssentiaPumpBlockEntity> {
    private static final ResourceLocation ALEMBIC = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/model/alembic.png");
    private static final ResourceLocation NOZZLE = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/block/alchemyblock.png");

    public EssentiaPumpRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaPumpBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        render(poseStack, buffers, packedLight, entity.getBlockState().getValue(EssentiaPumpBlock.FACING));
    }

    private static QuadSink sink(PoseStack.Pose pose, VertexConsumer buffer) {
        return (x, y, z, u, v, argb, light) -> buffer.addVertex(pose, x, y, z).setColor(argb).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers, int light, Direction facing) {
        poseStack.pushPose();
        switch (facing) {
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(-1.0F, 0.0F, -1.0F);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                poseStack.translate(0.0F, 0.0F, -1.0F);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(-1.0F, 0.0F, 0.0F);
            }
            default -> {
            }
        }
        PumpGeometry.funnel(sink(poseStack.last(), buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(ALEMBIC))), light);
        PumpGeometry.nozzle(sink(poseStack.last(), buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(NOZZLE))), light);

        poseStack.scale(0.375F, 0.375F, 0.375F);
        poseStack.translate(0.625F, -0.0625F, -0.125F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        poseStack.translate(0.0F, 0.0F, -1.0625F);
        bellows(poseStack, buffers, light);
        poseStack.translate(0.0F, 0.0F, 1.0625F);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, 0.9375F, 0.875F);
        bellows(poseStack, buffers, light);
        poseStack.translate(0.0F, -0.9375F, -0.875F);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, -1.0F, 1.8125F);
        bellows(poseStack, buffers, light);
        poseStack.translate(0.0F, 1.0F, -1.8125F);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0F, -1.9375F, -0.125F);
        bellows(poseStack, buffers, light);
        poseStack.popPose();
    }

    private static void bellows(PoseStack poseStack, MultiBufferSource buffers, int light) {
        AlchemyRendering.bellows(buffers, poseStack, light, Direction.DOWN, 1.0F);
    }
}
