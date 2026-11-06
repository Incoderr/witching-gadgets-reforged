package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
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
        if (minecraft.player == null || !stack.is(PrimordialFeature.PRIMORDIAL_GAUNTLET.get())) {
            return;
        }
        event.setCanceled(true);
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND ? minecraft.player.getMainArm() : minecraft.player.getMainArm().getOpposite();
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        int light = event.getPackedLight();
        float swing = event.getSwingProgress();
        float swingSqrt = Mth.sqrt(swing);

        float swingSqrtSin = Mth.sin(swingSqrt * Mth.PI);

        poseStack.pushPose();
        poseStack.translate(side * -swingSqrtSin * 0.4F, Mth.sin(swingSqrt * Mth.PI * 2.0F) * 0.2F, -Mth.sin(swing * Mth.PI) * 0.2F);
        poseStack.translate(side * 0.56F, -0.52F - event.getEquipProgress() * 0.6F, -0.72F);
        poseStack.rotateDegrees(Axis.YP, side * 45.0F);
        poseStack.rotateDegrees(Axis.YP, side * -Mth.sin(swing * swing * Mth.PI) * 20.0F);
        poseStack.rotateDegrees(Axis.ZP, side * -swingSqrtSin * 20.0F);
        poseStack.rotateDegrees(Axis.XP, -swingSqrtSin * 80.0F);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(side * -0.5F, -0.5F, -0.5F);
        poseStack.rotateDegrees(Axis.ZP, side * 280.0F);
        poseStack.rotateDegrees(Axis.XP, -60.0F);
        poseStack.rotateDegrees(Axis.YP, side * -75.0F);
        poseStack.scale(2.0F, 2.0F, -2.0F);
        poseStack.translate(0.0F, -0.85F, -0.51F);
        poseStack.scale(-side, 1.0F, 1.0F);
        GauntletItemRenderer.submitGlove(stack, poseStack, collector, light);
        poseStack.popPose();
    }
}
