package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import witchinggadgets.terraform.TerraformFocus;
import witchinggadgets.terraform.TerraformFocusBlockEntity;

public class TerraformFocusRenderer implements BlockEntityRenderer<TerraformFocusBlockEntity> {
    public TerraformFocusRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TerraformFocusBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        TerraformFocus focus = entity.focus();
        TerraformRendering.focus(poseStack, buffers, packedLight, 0xFF000000 | focus.aspect().getColor());
        if (entity.getLevel() == null) {
            return;
        }
        float rotation = (entity.getLevel().getGameTime() % 40 + partialTick) / 40.0F;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(focus.displayed())));
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.3125F, 0.5F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation * 360.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation * 360.0F));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffers, entity.getLevel(), 0);
        poseStack.popPose();
    }
}
