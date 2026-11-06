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
import witchinggadgets.client.WgClientConfig;
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

        poseStack.pushPose();
        poseStack.translate(side * -Mth.sin(swingSqrt * Mth.PI) * 0.4F, Mth.sin(swingSqrt * Mth.PI * 2.0F) * 0.2F, -Mth.sin(swing * Mth.PI) * 0.2F);
        poseStack.translate(side * WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_X), WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_Y) - event.getEquipProgress() * 0.6F, WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_Z));
        poseStack.rotateDegrees(Axis.XP, WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_ROT_X) - Mth.sin(swingSqrt * Mth.PI) * 30.0F);
        poseStack.rotateDegrees(Axis.YP, side * WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_ROT_Y));
        poseStack.rotateDegrees(Axis.ZP, side * WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_ROT_Z));
        float scale = WgClientConfig.gauntlet(WgClientConfig.GAUNTLET_SCALE);
        poseStack.scale(side * scale, scale, scale);
        GauntletItemRenderer.submitGlove(stack, poseStack, collector, light);
        poseStack.popPose();
    }
}
