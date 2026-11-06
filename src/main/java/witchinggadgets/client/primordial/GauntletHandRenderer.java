package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
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
        poseStack.translate(side * -Mth.sin(swingSqrt * Mth.PI) * 0.4F, Mth.sin(swingSqrt * Mth.PI * 2.0F) * 0.2F, -Mth.sin(swing * Mth.PI) * 0.2F);
        poseStack.translate(side * 0.58F, -0.476F - event.getEquipProgress() * 0.6F, -0.658F);
        poseStack.mulPose(Axis.XP.rotationDegrees(8.53F - Mth.sin(swingSqrt * Mth.PI) * 30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * -23.04F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * 82.74F));
        poseStack.scale(side, 1.0F, 1.0F);
        GauntletItemRenderer.renderGlove(stack, poseStack, buffers, light);
        poseStack.popPose();
    }
}
