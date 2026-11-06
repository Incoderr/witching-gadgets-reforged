package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.primordial.PrimordialFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GauntletHandRenderer {
    private GauntletHandRenderer() {
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack stack = event.getItemStack();
        LocalPlayer player = minecraft.player;
        if (player == null || !stack.is(PrimordialFeature.PRIMORDIAL_GAUNTLET.get())) {
            return;
        }
        event.setCanceled(true);
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float swing = event.getSwingProgress();
        float swingSqrt = Mth.sqrt(swing);

        poseStack.pushPose();
        poseStack.translate(side * (-0.3F * Mth.sin(swingSqrt * Mth.PI) + 0.64F), 0.4F * Mth.sin(swingSqrt * Mth.PI * 2.0F) - 0.6F - event.getEquipProgress() * 0.6F, -0.4F * Mth.sin(swing * Mth.PI) - 0.72F);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * Mth.sin(swingSqrt * Mth.PI) * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * Mth.sin(swing * swing * Mth.PI) * -20.0F));
        poseStack.translate(side * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -135.0F));
        poseStack.translate(side * 5.6F, 0.0F, 0.0F);
        PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(poseStack, buffers, light, player);
        } else {
            renderer.renderLeftHand(poseStack, buffers, light, player);
        }
        poseStack.translate(side * -5.0F / 16.0F, 2.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotation(side * 0.1F));
        poseStack.translate(side * -1.0F / 16.0F, -2.0F / 16.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 90.0F));
        poseStack.scale(side, 1.0F, 1.0F);
        GauntletItemRenderer.renderGlove(stack, poseStack, buffers, light);
        poseStack.popPose();
    }
}
