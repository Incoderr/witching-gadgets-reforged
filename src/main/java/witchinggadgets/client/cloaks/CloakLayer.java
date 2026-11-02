package witchinggadgets.client.cloaks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.platform.ItemGeometry;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.WgClientConfig;
import witchinggadgets.client.geom.CloakGeometry;
import witchinggadgets.cloaks.CloakEvents;
import witchinggadgets.cloaks.KamaItem;
import witchinggadgets.cloaks.MagicCloakItem;

public class CloakLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation BELT = WitchingGadgets.id("textures/models/magicalbaubles.png");
    private static final int INVISIBLE = 0x1AFFFFFF;
    private static final int WHITE = 0xFFFFFFFF;

    public CloakLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource collector, int lightCoords, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float ageInTicks, float yRot, float xRot) {
        ItemStack stack = CloakEvents.activeCloak(player);
        if (!(stack.getItem() instanceof MagicCloakItem cloak)) {
            return;
        }
        boolean invisible = player.hasEffect(MobEffects.INVISIBILITY);
        if (player.isInvisible() && !invisible) {
            return;
        }
        int argb = invisible ? INVISIBLE : 0xFF000000 | cloak.color(stack);
        float leftLeg = (float) Math.toDegrees(getParentModel().leftLeg.xRot);
        float rightLeg = (float) Math.toDegrees(getParentModel().rightLeg.xRot);
        ResourceLocation texture = WitchingGadgets.id("textures/models/" + cloak.variant().texture() + ".png");
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        if (stack.getItem() instanceof KamaItem) {
            poseStack.translate(0.0F, 1.45F, 0.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            boolean sprinting = player.isSprinting();
            ItemGeometry.solid(collector, poseStack, texture, true, sink -> CloakGeometry.kama(sink::vertex, sprinting, leftLeg, rightLeg, argb, lightCoords));
            ItemGeometry.solid(collector, poseStack, BELT, true, sink -> CloakGeometry.belt(sink::vertex, invisible ? INVISIBLE : WHITE, lightCoords));
        } else {
            int mode = WgClientConfig.cloakAnimation();
            poseStack.mulPose(Axis.XP.rotationDegrees(CloakGeometry.rotationDegrees(mode, leftLeg, rightLeg)));
            poseStack.translate(0.0F, 1.45F, 0.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            double stretch = CloakGeometry.stretch(mode, leftLeg, rightLeg);
            ItemGeometry.solid(collector, poseStack, texture, true, sink -> CloakGeometry.cloak(sink::vertex, stretch, argb, lightCoords));
        }
        poseStack.popPose();
    }
}
