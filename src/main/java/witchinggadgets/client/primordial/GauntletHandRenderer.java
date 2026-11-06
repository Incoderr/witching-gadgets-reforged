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

        float swingSqrtSin = Mth.sin(swingSqrt * Mth.PI);

        poseStack.pushPose();
        poseStack.translate(side * -swingSqrtSin * 0.4F, Mth.sin(swingSqrt * Mth.PI * 2.0F) * 0.2F, -Mth.sin(swing * Mth.PI) * 0.2F);
        poseStack.translate(side * 0.56F, -0.52F - event.getEquipProgress() * 0.6F, -0.72F);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -Mth.sin(swing * swing * Mth.PI) * 20.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * -swingSqrtSin * 20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-swingSqrtSin * 80.0F));
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(side * -0.5F, -0.5F, -0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 280.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -75.0F));
        poseStack.scale(2.0F, 2.0F, -2.0F);
        poseStack.translate(0.0F, -0.85F, -0.51F);
        poseStack.scale(-side, 1.0F, 1.0F);
        if (!player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.translate(1.0F / 16.0F, 2.0F / 16.0F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotation(-0.1F));
            poseStack.translate(5.0F / 16.0F, -2.0F / 16.0F, 0.0F);
            PlayerRenderer renderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(player);
            if (side > 0.0F) {
                renderer.renderRightHand(poseStack, buffers, light, player);
            } else {
                poseStack.scale(-1.0F, 1.0F, 1.0F);
                renderer.renderLeftHand(poseStack, buffers, light, player);
            }
            poseStack.popPose();
        }
        GauntletItemRenderer.renderGlove(stack, poseStack, buffers, light);
        poseStack.popPose();
    }
}
