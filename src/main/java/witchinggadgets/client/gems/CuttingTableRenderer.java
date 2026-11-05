package witchinggadgets.client.gems;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.geom.ModelBoxGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.EntityQuadSink;
import thaumcraft.registry.ModItems;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.CuttingTableBlock;
import witchinggadgets.gems.CuttingTableBlockEntity;

public class CuttingTableRenderer implements BlockEntityRenderer<CuttingTableBlockEntity, CuttingTableRenderer.State> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/models/cuttingtable.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final float[] FLASK_X = {-7.5F, -3.0F, 1.5F};

    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState gem = new ItemStackRenderState();
        final int[] colors = new int[3];
        final boolean[] wisp = new boolean[3];
        float rotation;
    }

    public CuttingTableRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CuttingTableBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.gem.clear();
        state.rotation = switch (entity.getBlockState().getValue(CuttingTableBlock.FACING)) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        for (int index = 0; index < 3; index++) {
            ItemStack stack = entity.getItem(1 + index);
            Aspect aspect = CuttingTableBlockEntity.topAspect(stack);
            state.colors[index] = stack.isEmpty() || aspect == null ? 0 : 0xFF000000 | aspect.getColor();
            state.wisp[index] = stack.is(ModItems.WISP_ESSENCE.get());
        }
        ItemStack gem = entity.getItem(CuttingTableBlockEntity.GEM);
        if (!gem.isEmpty() && entity.getLevel() != null) {
            itemModelResolver.updateForTopItem(state.gem, gem.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), null, (int) entity.getBlockPos().asLong());
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, state.rotation);
        poseStack.pushPose();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, TcRenderTypes.entityTranslucent(TEXTURE), (pose, buffer) -> {
            QuadSink sink = new EntityQuadSink(pose, buffer);
            for (int index = 0; index < 3; index++) {
                if (state.colors[index] == 0) {
                    continue;
                }
                if (state.wisp[index]) {
                    bowl(sink, FLASK_X[index], state.colors[index], light);
                } else {
                    flask(sink, FLASK_X[index], state.colors[index], light);
                }
            }
        });
        poseStack.popPose();
        if (!state.gem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(-0.0125F, 0.875F, -0.0625F);
            poseStack.rotateDegrees(Axis.XP, 90.0F);
            poseStack.scale(0.375F, 0.375F, 0.375F);
            state.gem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void flask(QuadSink sink, float x, int color, int light) {
        ModelBoxGeometry.box(sink, x + 1.0F, 17.5F, 5.0F, 1, 1, 1, 24, 24, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x + 0.5F, 14.5F, 4.5F, 2, 3, 2, 28, 27, 64.0F, 32.0F, true, color, light);
        ModelBoxGeometry.box(sink, x, 14.0F, 4.0F, 3, 3, 3, 16, 26, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x + 0.5F, 17.0F, 4.5F, 2, 1, 2, 16, 23, 64.0F, 32.0F, true, WHITE, light);
    }

    private static void bowl(QuadSink sink, float x, int color, int light) {
        ModelBoxGeometry.box(sink, x, 14.0F, 4.0F, 3, 1, 3, 36, 24, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x - 0.5F, 15.0F, 4.0F, 1, 1, 3, 36, 20, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x + 2.5F, 15.0F, 4.0F, 1, 1, 3, 36, 20, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x, 15.0F, 6.5F, 3, 1, 1, 44, 20, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x, 15.0F, 3.5F, 3, 1, 1, 44, 20, 64.0F, 32.0F, true, WHITE, light);
        ModelBoxGeometry.box(sink, x + 0.5F, 14.75F, 4.5F, 2, 1, 2, 48, 22, 64.0F, 32.0F, true, color, light);
    }
}
