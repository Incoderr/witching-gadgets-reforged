package witchinggadgets.client.devices;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.render.ItemSlot;
import witchinggadgets.devices.LabelLibraryBlock;
import witchinggadgets.devices.LabelLibraryBlockEntity;

public class LabelLibraryRenderer implements BlockEntityRenderer<LabelLibraryBlockEntity> {
    private final ItemSlot output = new ItemSlot();
    private final ItemSlot input = new ItemSlot();

    public LabelLibraryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LabelLibraryBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) {
            return;
        }
        output.clear();
        input.clear();
        int seed = (int) entity.getBlockPos().asLong();
        ItemStack label = entity.output();
        if (!label.isEmpty()) {
            output.set(label, ItemDisplayContext.FIXED, entity.getLevel(), seed);
        }
        ItemStack labels = entity.getItem(LabelLibraryBlockEntity.INPUT);
        int stacked = 0;
        if (!labels.isEmpty()) {
            input.set(labels.copyWithCount(1), ItemDisplayContext.FIXED, entity.getLevel(), seed);
            stacked = Math.max(1, labels.getCount() / 6);
        }
        float rotation = switch (entity.getBlockState().getValue(LabelLibraryBlock.FACING)) {
            case SOUTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        if (!output.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(-0.1875F, 1.02F, -0.25F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
            output.submit(poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.14F, 1.02F, 0.275F);
            for (int index = 0; index < stacked; index++) {
                poseStack.mulPose(Axis.YP.rotationDegrees(-(10 + index * index)));
                poseStack.translate(0.0F, 0.019F, 0.0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.scale(0.75F, 0.75F, 0.75F);
                input.submit(poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
