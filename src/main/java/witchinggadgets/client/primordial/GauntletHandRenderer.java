package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
import witchinggadgets.primordial.PrimordialGauntletItem;
import witchinggadgets.primordial.StoredNode;
import witchinggadgets.primordial.PrimordialFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GauntletHandRenderer {
    private static final ResourceLocation NODES = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
    private static final int FULL_BRIGHT = 0xF000F0;

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
        poseStack.pushPose();
        poseStack.translate(side * 0.56F, -0.52F, -0.72F);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0F));
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
        poseStack.translate(0.1F, 0.675F, 0.0F);
        submitSelectedGem(stack, poseStack, buffers);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-40.0F));
        poseStack.translate(0.0F, -0.01F, 0.05F);
        submitNode(stack, poseStack, buffers, minecraft.player.tickCount);
        poseStack.popPose();
    }

    private static void submitSelectedGem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers) {
        int selected = PrimordialGauntletItem.selected(stack);
        ItemStack gem = PrimordialGauntletItem.gems(stack).get(selected);
        Aspect aspect = InfusedGemItem.aspect(gem);
        if (gem.isEmpty() || aspect == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.1875F, 0.147F, 0.0F);
        poseStack.translate(selected < 2 ? 0.09375F : selected == 3 ? 0.0125F : 0.035F, 0.0F,
            selected == 0 || selected == 2 ? 0.05625F : selected == 1 || selected == 4 ? 0.14F : 0.1F);
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        int frame = (int) ((System.currentTimeMillis() / 64L) % 32L);
        float u0 = frame * 0.03125F;
        float u1 = (frame + 1) * 0.03125F;
        int color = 0xFF000000 | aspect.getColor();
        boolean damaged = gem.isDamaged();
        quad(buffers, poseStack, 1, 0.025F, 0.0F, u0, u1, damaged ? 0.09375F : 0.0F, damaged ? 0.125F : 0.03125F, color);
        quad(buffers, poseStack, 1, 0.025F, 0.0F, u0, u1, damaged ? 0.09375F : 0.125F, damaged ? 0.125F : 0.15625F, color);
        poseStack.popPose();
    }

    private static void submitNode(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int ticks) {
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
            quad(buffers, poseStack, aspect.getBlend(), radius, -count * 0.001F, frame * 0.03125F, (frame + 1) * 0.03125F, 0.0F, 0.03125F, color);
            count++;
            if (aspect.getBlend() == 771) {
                alpha /= 1.5F;
            }
        }
        int type = node.type();
        int row = type == 2 ? 6 : type == 3 ? 2 : type == 4 ? 5 : type == 5 ? 4 : type == 6 ? 3 : 1;
        int frame = (int) ((time / 64L) % 32L);
        quad(buffers, poseStack, type == 3 || type == 4 ? 771 : 1, 0.0825F, -count * 0.001F, frame * 0.03125F, (frame + 1) * 0.03125F, row * 0.03125F, (row + 1) * 0.03125F, 0xFFFFFFFF);
    }

    private static void quad(MultiBufferSource buffers, PoseStack poseStack, int blend, float half, float z, float u0, float u1, float v0, float v1, int color) {
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.byBlend(NODES, blend));
        {
            buffer.addVertex(pose, -half, half, z).setColor(color).setUv(u0, v1).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, half, z).setColor(color).setUv(u1, v1).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, half, -half, z).setColor(color).setUv(u1, v0).setLight(FULL_BRIGHT);
            buffer.addVertex(pose, -half, -half, z).setColor(color).setUv(u0, v0).setLight(FULL_BRIGHT);
        }
    }
}
