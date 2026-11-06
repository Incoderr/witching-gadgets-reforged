package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.TcRenderTypes;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.primordial.PrimordialGauntletItem;
import witchinggadgets.primordial.StoredNode;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GauntletHandRenderer {
    private static final Identifier NODES = Identifier.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
    private static final int FULL_BRIGHT = 0xF000F0;

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
        poseStack.pushPose();
        poseStack.translate(side * 0.56F, -0.52F, -0.72F);
        poseStack.rotateDegrees(Axis.YP, side * 45.0F);
        poseStack.scale(0.4F, 0.4F, 0.4F);
        poseStack.translate(side * -0.5F, -0.5F, -0.5F);
        poseStack.rotateDegrees(Axis.ZP, side * 280.0F);
        poseStack.rotateDegrees(Axis.XP, -60.0F);
        poseStack.rotateDegrees(Axis.YP, side * -75.0F);
        poseStack.scale(2.0F, 2.0F, -2.0F);
        poseStack.translate(0.0F, -0.85F, -0.51F);
        poseStack.scale(-side, 1.0F, 1.0F);
        AvatarRenderState avatar = event.getPlayerState().avatarRenderState;
        if (avatar != null && !minecraft.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, -90.0F);
            poseStack.translate(1.0F / 16.0F, 2.0F / 16.0F, 0.0F);
            poseStack.rotate(Axis.ZP, -0.1F);
            poseStack.translate(5.0F / 16.0F, -2.0F / 16.0F, 0.0F);
            AvatarRenderer<?> renderer = (AvatarRenderer<?>) minecraft.getEntityRenderDispatcher().getRenderer(avatar);
            Identifier skin = avatar.skin.body().texturePath();
            if (side > 0.0F) {
                renderer.renderRightHand(poseStack, collector, light, skin, avatar.showRightSleeve);
            } else {
                poseStack.scale(-1.0F, 1.0F, 1.0F);
                renderer.renderLeftHand(poseStack, collector, light, skin, avatar.showLeftSleeve);
            }
            poseStack.popPose();
        }
        GauntletItemRenderer.submitGlove(stack, poseStack, collector, light);
        poseStack.translate(0.1F, 0.675F, 0.0F);
        submitSelectedGem(stack, poseStack, collector);
        poseStack.rotateDegrees(Axis.YP, -90.0F);
        poseStack.rotateDegrees(Axis.XP, -40.0F);
        poseStack.translate(0.0F, -0.01F, 0.05F);
        submitNode(stack, poseStack, collector, minecraft.player.tickCount);
        poseStack.popPose();
    }

    private static void submitSelectedGem(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector) {
        int selected = PrimordialGauntletItem.selected(stack);
        ItemStack gem = PrimordialGauntletItem.gems(stack).get(selected);
        Aspect aspect = InfusedGemItem.aspect(gem);
        if (gem.isEmpty() || aspect == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.XP, -90.0F);
        poseStack.rotateDegrees(Axis.YP, -90.0F);
        poseStack.translate(-0.1875F, 0.147F, 0.0F);
        poseStack.translate(selected < 2 ? 0.09375F : selected == 3 ? 0.0125F : 0.035F, 0.0F,
            selected == 0 || selected == 2 ? 0.05625F : selected == 1 || selected == 4 ? 0.14F : 0.1F);
        poseStack.rotateDegrees(Axis.XP, 45.0F);
        int frame = (int) ((System.currentTimeMillis() / 64L) % 32L);
        float u0 = frame * 0.03125F;
        float u1 = (frame + 1) * 0.03125F;
        int color = 0xFF000000 | aspect.getColor();
        boolean damaged = gem.isDamaged();
        quad(collector, poseStack, 1, 0.025F, 0.0F, u0, u1, damaged ? 0.09375F : 0.0F, damaged ? 0.125F : 0.03125F, color);
        quad(collector, poseStack, 1, 0.025F, 0.0F, u0, u1, damaged ? 0.09375F : 0.125F, damaged ? 0.125F : 0.15625F, color);
        poseStack.popPose();
    }

    private static void submitNode(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int ticks) {
        StoredNode node = PrimordialFeature.GAUNTLET_NODE.get(stack);
        if (node == null) {
            return;
        }
        float alpha = 1.0F;
        switch (node.modifier()) {
            case 1 -> alpha *= 1.5F;
            case 2 -> alpha *= 0.66F;
            case 3 -> alpha *= Mth.sin(ticks / 3.0F) * 0.25F + 0.33F;
            default -> {
            }
        }
        long time = System.currentTimeMillis();
        int count = 0;
        float average = 0.0F;
        for (Aspect aspect : node.aspects().getAspects()) {
            if (aspect.getBlend() == 771) {
                alpha *= 1.5F;
            }
            average += node.aspects().getAmount(aspect);
            float mod = 2.0F * ((time + count * 512L) % 4096L) / 4096.0F;
            if (mod > 1.0F) {
                mod = 2.0F - mod;
            }
            float radius = 0.075F + 0.05F * mod * (node.aspects().getAmount(aspect) / average);
            int frame = (int) ((time / 64L) % 32L) + count * 4;
            int color = Math.min(255, (int) (alpha * 255.0F)) << 24 | aspect.getColor() & 0xFFFFFF;
            quad(collector, poseStack, aspect.getBlend(), radius, -count * 0.001F, frame * 0.03125F, (frame + 1) * 0.03125F, 0.0F, 0.03125F, color);
            count++;
            if (aspect.getBlend() == 771) {
                alpha /= 1.5F;
            }
        }
        int type = node.type();
        int row = type == 2 ? 6 : type == 3 ? 2 : type == 4 ? 5 : type == 5 ? 4 : type == 6 ? 3 : 1;
        int frame = (int) ((time / 64L) % 32L);
        quad(collector, poseStack, type == 3 || type == 4 ? 771 : 1, 0.0825F, -count * 0.001F, frame * 0.03125F, (frame + 1) * 0.03125F, row * 0.03125F, (row + 1) * 0.03125F, 0xFFFFFFFF);
    }

    private static void quad(SubmitNodeCollector collector, PoseStack poseStack, int blend, float half, float z, float u0, float u1, float v0, float v1, int color) {
        collector.submitCustomGeometry(poseStack, TcRenderTypes.byBlend(NODES, blend), (pose, buffer) -> {
            buffer.addVertex(pose, -half, half, z).setColor(color).setUv(u0, v1).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, half, z).setColor(color).setUv(u1, v1).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, -half, z).setColor(color).setUv(u1, v0).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, -half, -half, z).setColor(color).setUv(u0, v0).setLight(FULL_BRIGHT);
        });
    }
}
