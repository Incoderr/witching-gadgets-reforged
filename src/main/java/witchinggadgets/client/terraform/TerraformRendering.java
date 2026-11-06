package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.model.ObjModel;
import witchinggadgets.WitchingGadgets;

public final class TerraformRendering {
    public static final ResourceLocation MODEL = WitchingGadgets.id("obj/terraformer.obj");
    public static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/models/terraformer.png");
    private static final int WHITE = 0xFFFFFFFF;

    private TerraformRendering() {
    }

    private static void part(MultiBufferSource buffers, PoseStack poseStack, String name, int color, int light) {
        ObjModel.get(MODEL).renderPart(name, poseStack.last(), buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(TEXTURE)), color, light);
    }

    public static void terraformer(PoseStack poseStack, MultiBufferSource buffers, int light, int crystalColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        part(buffers, poseStack, "main_01", WHITE, light);
        part(buffers, poseStack, "crystals_02", crystalColor, light);
        poseStack.popPose();
    }

    public static void focus(PoseStack poseStack, MultiBufferSource buffers, int light, int crystalColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, -1.0F, 0.5F);
        part(buffers, poseStack, "focus_03", WHITE, light);
        part(buffers, poseStack, "focus_crystal_04", crystalColor, light);
        poseStack.popPose();
    }
}
