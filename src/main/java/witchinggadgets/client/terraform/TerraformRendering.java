package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.model.ObjModel;
import witchinggadgets.WitchingGadgets;

public final class TerraformRendering {
    public static final Identifier MODEL = WitchingGadgets.id("obj/terraformer.obj");
    public static final Identifier TEXTURE = WitchingGadgets.id("textures/models/terraformer.png");
    private static final int WHITE = 0xFFFFFFFF;

    private TerraformRendering() {
    }

    private static void part(SubmitNodeCollector collector, PoseStack poseStack, String name, int color, int light) {
        ObjModel obj = ObjModel.get(MODEL);
        collector.submitCustomGeometry(poseStack, TcRenderTypes.entityCutoutNoCull(TEXTURE), (pose, buffer) -> obj.renderPart(name, pose, buffer, color, light));
    }

    public static void terraformer(PoseStack poseStack, SubmitNodeCollector collector, int light, int crystalColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        part(collector, poseStack, "main_01", WHITE, light);
        part(collector, poseStack, "crystals_02", crystalColor, light);
        poseStack.popPose();
    }

    public static void focus(PoseStack poseStack, SubmitNodeCollector collector, int light, int crystalColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, -1.0F, 0.5F);
        part(collector, poseStack, "focus_03", WHITE, light);
        part(collector, poseStack, "focus_crystal_04", crystalColor, light);
        poseStack.popPose();
    }
}
