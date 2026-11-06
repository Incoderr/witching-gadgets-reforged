package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.platform.Gear;
import witchinggadgets.travellersgear.ToolDisplay;

public class ToolDisplayLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public ToolDisplayLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (state.isInvisible || minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof Player player)) {
            return;
        }
        int seed = 0;
        for (ToolDisplay display : Gear.toolDisplay(player)) {
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
                poseStack.rotateDegrees(Axis.YP, yRot);
                poseStack.rotateDegrees(Axis.XP, xRot);
            }
            poseStack.translate(0.5F, 0.5F, 0.0F);
            poseStack.scale(sx, sy, sz);
            poseStack.translate(-0.5F, -0.5F, 0.0F);
            poseStack.translate(-0.5F / sx, -0.25F / sy, 0.0F);
            poseStack.translate(display.translation().get(0) / sx, display.translation().get(1) / sy, display.translation().get(2) / sz);
            poseStack.translate(0.5F, 0.5F, 0.0F);
            poseStack.rotateDegrees(Axis.YP, display.rotation().get(1));
            poseStack.rotateDegrees(Axis.ZP, display.rotation().get(2));
            poseStack.rotateDegrees(Axis.XP, display.rotation().get(0));
            ItemStackRenderState item = new ItemStackRenderState();
            minecraft.getItemModelResolver().updateForTopItem(item, stack, ItemDisplayContext.NONE, minecraft.level, player, player.getId() * 31 + seed++);
            item.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
