package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.platform.Gear;
import witchinggadgets.travellersgear.ToolDisplay;

public class ToolDisplayLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public ToolDisplayLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float ageInTicks, float yRot, float xRot) {
        if (player.isInvisible() || Mannequins.is(player)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        List<ToolDisplay> preview = ToolDisplayScreen.preview(player);
        int seed = 0;
        for (ToolDisplay display : preview != null ? preview : Gear.toolDisplay(player)) {
            ItemStack stack = player.getInventory().getItem(display.slot());
            if (stack.isEmpty() || display.hideWhenEquipped() && ItemStack.matches(stack, player.getMainHandItem())) {
                continue;
            }
            float sx = display.scale().get(0);
            float sy = display.scale().get(1);
            float sz = display.scale().get(2);
            if (sx == 0.0F || sy == 0.0F || sz == 0.0F) {
                continue;
            }
            poseStack.pushPose();
            if (display.rotateWithHead()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            }
            poseStack.translate(0.5F, 0.5F, 0.0F);
            poseStack.scale(sx, sy, sz);
            poseStack.translate(-0.5F, -0.5F, 0.0F);
            poseStack.translate(-0.5F / sx, -0.25F / sy, 0.0F);
            poseStack.translate(display.translation().get(0) / sx, display.translation().get(1) / sy, display.translation().get(2) / sz);
            poseStack.translate(0.5F, 0.5F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(display.rotation().get(1)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(display.rotation().get(2)));
            poseStack.mulPose(Axis.XP.rotationDegrees(display.rotation().get(0)));
            minecraft.getItemRenderer().renderStatic(player, stack, ItemDisplayContext.NONE, false, poseStack, buffers, player.level(), light, OverlayTexture.NO_OVERLAY, player.getId() * 31 + seed++);
            poseStack.popPose();
        }
    }
}
