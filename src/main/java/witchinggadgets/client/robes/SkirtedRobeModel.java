package witchinggadgets.client.robes;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import thaumcraft.item.gear.Goggles;
import thaumcraft.node.Revealer;
import witchinggadgets.WitchingGadgets;

public class SkirtedRobeModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(WitchingGadgets.id("skirted_robe"), "main");

    public SkirtedRobeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.5F), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
            CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 11, 4, new CubeDeformation(0.5F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("sleeve", box(40, 32, -2.5F, 4.2F, 2.0F, 3, 5, 1, 0.25F), PartPose.ZERO);
        rightArm.addOrReplaceChild("sleeve_tip", box(48, 32, -2.0F, 6.4F, 2.0F, 2, 3, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4, 11, 4, new CubeDeformation(0.5F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("sleeve", box(40, 32, -0.5F, 4.2F, 2.0F, 3, 5, 1, 0.25F), PartPose.ZERO);
        leftArm.addOrReplaceChild("sleeve_tip", box(48, 32, 0.0F, 6.4F, 2.0F, 2, 3, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("hood", box(32, 0, -4.0F, -8.0F, -4.0F, 8, 8, 7, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("hood_back", box(64, 0, -4.0F, -7.8F, 3.0F, 8, 6, 2, 0.5F), PartPose.rotation(-0.0875F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_tip", box(64, 8, -3.0F, -7.5F, 5.0F, 6, 6, 1, 0.25F), PartPose.rotation(-0.125F, 0.0F, 0.0F));
        PartDefinition rightLeg = root.getChild("right_leg");
        rightLeg.addOrReplaceChild("tail", box(4, 32, -0.5F, 0.0F, 1.4F, 2, 8, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("tail_side", box(0, 32, -2.0F, 0.0F, 1.4F, 1, 7, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        PartDefinition leftLeg = root.getChild("left_leg");
        leftLeg.addOrReplaceChild("tail", box(4, 32, -1.5F, 0.0F, 1.4F, 2, 8, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("tail_side", box(0, 32, 1.0F, 0.0F, 1.4F, 1, 7, 1, 0.25F), PartPose.rotation(0.125F, 0.0F, 0.0F));
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("skirt_r", box(10, 32, -2.0F, 12.5F, -2.0F, 1, 6, 4, 0.25F), PartPose.rotation(0.0F, 0.0F, 0.1875F));
        body.addOrReplaceChild("hip_r", box(10, 32, -4.3F, 11.0F, -2.0F, 1, 1, 4, 0.25F), PartPose.ZERO);
        body.addOrReplaceChild("skirt_l", box(10, 32, 1.0F, 12.5F, -2.0F, 1, 6, 4, 0.25F), PartPose.rotation(0.0F, 0.0F, -0.1875F));
        body.addOrReplaceChild("hip_l", box(10, 32, 3.3F, 11.0F, -2.0F, 1, 1, 4, 0.25F), PartPose.ZERO);
        body.addOrReplaceChild("back", box(64, 8, -3.0F, 4.0F, 1.0F, 6, 8, 1, 0.25F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static CubeListBuilder box(int u, int v, float x, float y, float z, int width, int height, int depth, float grow) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth, new CubeDeformation(grow));
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        Item helmet = state.headEquipment.getItem();
        head.visible = state.headEquipment.isEmpty() || helmet instanceof Goggles || helmet instanceof Revealer;
        hat.visible = false;
        body.visible = true;
        rightArm.visible = true;
        leftArm.visible = true;
        rightLeg.visible = true;
        leftLeg.visible = true;
        if (state instanceof ZombieRenderState) {
            float f6 = Mth.sin(state.swingAnimation * (float) Math.PI);
            float f7 = Mth.sin((1.0F - (1.0F - state.swingAnimation) * (1.0F - state.swingAnimation)) * (float) Math.PI);
            rightArm.zRot = Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F;
            leftArm.zRot = -(Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F);
            rightArm.yRot = -(0.1F - f6 * 0.6F);
            leftArm.yRot = 0.1F - f6 * 0.6F;
            rightArm.xRot = (float) (-Math.PI / 2) - (f6 * 1.2F - f7 * 0.4F) + Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
            leftArm.xRot = (float) (-Math.PI / 2) - (f6 * 1.2F - f7 * 0.4F) - Mth.sin(state.ageInTicks * 0.067F) * 0.05F;
        }
    }
}
