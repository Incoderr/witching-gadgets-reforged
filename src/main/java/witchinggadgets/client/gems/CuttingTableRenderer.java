package witchinggadgets.client.gems;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.geom.ModelBoxGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.platform.EntityQuadSink;
import thaumcraft.client.render.ItemSlot;
import thaumcraft.registry.ModItems;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.CuttingTableBlock;
import witchinggadgets.gems.CuttingTableBlockEntity;

public class CuttingTableRenderer implements BlockEntityRenderer<CuttingTableBlockEntity> {
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/models/cuttingtable.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final float[] FLASK_X = {-7.5F, -3.0F, 1.5F};

    private final ItemSlot gem = new ItemSlot();

    public CuttingTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CuttingTableBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) {
            return;
        }
        float rotation = switch (entity.getBlockState().getValue(CuttingTableBlock.FACING)) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.pushPose();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        QuadSink sink = new EntityQuadSink(poseStack.last(), buffers.getBuffer(TcRenderTypes.of(TcRenderTypes.Kind.ENTITY_TRANSLUCENT, TEXTURE)));
        for (int index = 0; index < 3; index++) {
            ItemStack stack = entity.getItem(1 + index);
            Aspect aspect = CuttingTableBlockEntity.topAspect(stack);
            if (stack.isEmpty() || aspect == null) {
                continue;
            }
            int color = 0xFF000000 | aspect.getColor();
            if (stack.is(ModItems.WISP_ESSENCE.get())) {
                bowl(sink, FLASK_X[index], color, packedLight);
            } else {
                flask(sink, FLASK_X[index], color, packedLight);
            }
        }
        poseStack.popPose();
        gem.clear();
        ItemStack stone = entity.getItem(CuttingTableBlockEntity.GEM);
        if (!stone.isEmpty()) {
            gem.set(stone.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), (int) entity.getBlockPos().asLong());
            poseStack.pushPose();
            poseStack.translate(-0.0125F, 0.875F, -0.0625F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.375F, 0.375F, 0.375F);
            gem.submit(poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
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
