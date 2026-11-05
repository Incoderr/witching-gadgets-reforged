package witchinggadgets.client.primordial;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.armor.FortressArmorItem;
import witchinggadgets.WitchingGadgets;

public class PrimordialArmorModel extends HumanoidModel<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(WitchingGadgets.id("primordial_armor"), "main");
    public static final ModelLayerLocation BOOTS_LAYER = new ModelLayerLocation(WitchingGadgets.id("primordial_armor"), "boots");

    private final EquipmentSlot slot;
    private final ModelPart chainmail;
    private final ModelPart belt;
    private final ModelPart leftChainmail;
    private final ModelPart rightChainmail;
    private final ModelPart leftBoot;
    private final ModelPart rightBoot;
    private final ModelPart goggles;
    private final ModelPart[] masks = new ModelPart[3];

    public PrimordialArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;
        chainmail = body.getChild("bd_chainmail");
        belt = body.getChild("bd_belt");
        leftChainmail = leftLeg.getChild("ll_chainmail");
        rightChainmail = rightLeg.getChild("rl_chainmail");
        leftBoot = leftLeg.getChild("lb_base");
        rightBoot = rightLeg.getChild("rb_base");
        goggles = head.getChild("face_goggles");
        for (int index = 0; index < masks.length; index++) {
            masks[index] = head.getChild("face_mask" + index);
        }
    }

    private static CubeListBuilder box(int u, int v, boolean mirror, float x, float y, float z, int width, int height, int depth, float grow) {
        CubeListBuilder builder = CubeListBuilder.create().texOffs(u, v);
        if (mirror) {
            builder = builder.mirror();
        }
        return builder.addBox(x, y, z, width, height, depth, new CubeDeformation(grow));
    }

    private static PartPose pose(float x, float y, float z, float xRot, float yRot, float zRot) {
        return PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
    }

    private static PartPose at(float x, float y, float z) {
        return PartPose.offset(x, y, z);
    }

    public static LayerDefinition createLayer() {
        return create(0.0625F);
    }

    public static LayerDefinition createBootsLayer() {
        return create(0.3125F);
    }

    private static LayerDefinition create(float s) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(s), 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition chain = body.addOrReplaceChild("bd_chainmail", box(16, 16, false, -4.0F, 0.0F, -2.0F, 8, 12, 4, s), at(0.0F, s, 0.0F));
        chain.addOrReplaceChild("bd_chest", box(66, 31, false, -3.5F, 0.0F, -0.5F, 7, 9, 2, s), at(0.0F, 1.0F, -2.5F));
        chain.addOrReplaceChild("bd_add0", box(66, 46, false, -2.0F, 0.0F, -1.0F, 4, 4, 2, s), pose(0.0F, 0.0F, -3.3F, 0.31415927F, 0.0F, 0.0F));
        chain.addOrReplaceChild("bd_add1", box(66, 52, true, -1.5F, 0.0F, -0.5F, 3, 2, 1, s), pose(2.5F, 8.7F, -2.5F, -0.2617994F, -0.34906584F, 0.0F));
        chain.addOrReplaceChild("bd_add2", box(66, 52, false, -1.5F, 0.0F, -0.5F, 3, 2, 1, s), pose(-2.5F, 8.7F, -2.5F, -0.2617994F, 0.34906584F, 0.0F));
        chain.addOrReplaceChild("bd_add3", box(66, 52, false, -1.5F, 0.0F, -0.5F, 3, 2, 1, s), pose(0.0F, 8.7F, -2.9F, -0.2617994F, 0.0F, 0.0F));
        chain.addOrReplaceChild("bd_strap0", box(66, 42, false, -4.0F, 0.0F, -0.5F, 8, 2, 1, s), at(0.0F, 6.7F, -2.0F));
        chain.addOrReplaceChild("bd_strap1", box(66, 42, true, -1.0F, 0.0F, -0.5F, 2, 2, 1, s), pose(2.8F, -0.4F, -2.0F, 0.0F, 0.0F, 0.3926991F));
        chain.addOrReplaceChild("bd_strap2", box(66, 42, false, -1.0F, 0.0F, -0.5F, 2, 2, 1, s), pose(-2.8F, -0.4F, -2.0F, 0.0F, 0.0F, -0.3926991F));
        chain.addOrReplaceChild("bd_back", box(80, 46, false, -3.5F, 0.0F, -0.5F, 7, 9, 2, s), at(0.0F, 1.0F, 1.5F));
        chain.addOrReplaceChild("bd_strap3", box(66, 42, false, -4.0F, 0.0F, -0.5F, 8, 2, 1, s), at(0.0F, 5.7F, 2.0F));
        chain.addOrReplaceChild("bd_strap4", box(66, 42, true, -1.0F, 0.0F, -0.5F, 2, 2, 1, s), pose(2.8F, -0.4F, 2.0F, 0.0F, 0.0F, 0.3926991F));
        chain.addOrReplaceChild("bd_strap5", box(66, 42, false, -1.0F, 0.0F, -0.5F, 2, 2, 1, s), pose(-2.8F, -0.4F, 2.0F, 0.0F, 0.0F, -0.3926991F));
        chain.addOrReplaceChild("bd_add4", box(84, 40, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(-2.0F, 7.9F, 2.3F, 0.30019665F, -0.13962634F, 0.0F));
        chain.addOrReplaceChild("bd_add5", box(84, 40, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(-2.0F, 9.8F, 2.1F, 0.30019665F, -0.13962634F, 0.0F));
        chain.addOrReplaceChild("bd_add6", box(84, 40, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(2.0F, 7.9F, 2.3F, 0.30019665F, 0.13962634F, 0.0F));
        chain.addOrReplaceChild("bd_add7", box(84, 40, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(2.0F, 9.8F, 2.1F, 0.30019665F, 0.13962634F, 0.0F));
        body.addOrReplaceChild("bd_belt", box(66, 56, false, -4.0F, 0.0F, -2.0F, 8, 2, 4, s), at(0.0F, 10.2F, 0.0F));

        PartDefinition leftArm = root.getChild("left_arm");
        PartDefinition leftShoulder = leftArm.addOrReplaceChild("la_shoulder0", box(42, 41, true, -2.5F, -2.5F, -2.5F, 5, 5, 5, s), at(0.7F, -0.5F, 0.0F));
        leftShoulder.addOrReplaceChild("la_shoulder1", box(42, 51, false, -0.5F, 0.0F, -2.5F, 1, 5, 5, s), pose(2.5F, -3.3F, 0.0F, 0.0F, 0.0F, -0.32332313F));
        leftShoulder.addOrReplaceChild("la_shoulder2", box(42, 51, true, -0.5F, 0.0F, -2.5F, 1, 5, 5, s), pose(2.5F, -0.5F, 0.0F, 0.0F, 0.0F, -0.20943952F));
        leftShoulder.addOrReplaceChild("la_shoulder3", box(42, 62, true, -0.5F, 0.0F, -0.5F, 4, 1, 1, s), pose(-1.0F, -3.2F, 0.0F, 0.0F, 0.0F, -0.07504916F));
        leftShoulder.addOrReplaceChild("la_shoulder4", box(42, 62, true, -0.1F, -0.5F, -0.5F, 3, 1, 1, s), pose(-1.1F, -2.8F, 0.0F, 0.0F, 0.0F, -0.50265485F));
        leftArm.addOrReplaceChild("la_plate0", box(42, 32, true, -1.5F, 0.0F, -2.5F, 2, 4, 5, s), at(3.0F, 5.5F, 0.0F));
        leftArm.addOrReplaceChild("la_plate1", box(56, 32, true, -0.5F, 0.0F, -2.0F, 1, 2, 4, s), pose(2.5F, 3.7F, 0.0F, 0.0F, 0.0F, -0.17453292F));

        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition rightShoulder = rightArm.addOrReplaceChild("ra_shoulder0", box(42, 41, false, -2.5F, -2.5F, -2.5F, 5, 5, 5, s), at(-0.7F, -0.5F, 0.0F));
        rightShoulder.addOrReplaceChild("ra_shoulder1", box(42, 51, false, -0.5F, 0.0F, -2.5F, 1, 5, 5, s), pose(-2.5F, -3.3F, 0.0F, 0.0F, 0.0F, 0.32332313F));
        rightShoulder.addOrReplaceChild("ra_shoulder2", box(42, 51, false, -0.5F, 0.0F, -2.5F, 1, 5, 5, s), pose(-2.5F, -0.5F, 0.0F, 0.0F, 0.0F, 0.20943952F));
        rightShoulder.addOrReplaceChild("ra_shoulder3", box(42, 62, false, -3.5F, 0.0F, -0.5F, 4, 1, 1, s), pose(1.0F, -3.2F, 0.0F, 0.0F, 0.0F, 0.07504916F));
        rightShoulder.addOrReplaceChild("ra_shoulder4", box(42, 62, false, -2.9F, -0.5F, -0.5F, 3, 1, 1, s), pose(1.1F, -2.8F, 0.0F, 0.0F, 0.0F, 0.50265485F));
        rightArm.addOrReplaceChild("ra_plate0", box(42, 32, false, -1.5F, 0.0F, -2.5F, 2, 4, 5, s), at(-2.0F, 5.5F, 0.0F));
        rightArm.addOrReplaceChild("ra_plate1", box(56, 32, false, -0.5F, 0.0F, -2.0F, 1, 2, 4, s), pose(-2.5F, 3.7F, 0.0F, 0.0F, 0.0F, 0.17453292F));

        PartDefinition leftLeg = root.getChild("left_leg");
        PartDefinition leftChain = leftLeg.addOrReplaceChild("ll_chainmail", box(26, 32, true, -2.0F, 0.0F, -2.0F, 4, 8, 4, s), PartPose.ZERO);
        leftChain.addOrReplaceChild("ll_plate_f0", box(0, 32, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.4F, 0.2F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        leftChain.addOrReplaceChild("ll_plate_f1", box(0, 32, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 3.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        leftChain.addOrReplaceChild("ll_plate_f2", box(10, 32, true, -2.0F, 0.0F, -0.7F, 2, 5, 1, s), pose(0.0F, 0.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        leftChain.addOrReplaceChild("ll_plate_f3", box(10, 32, true, -2.0F, 0.0F, -0.7F, 2, 5, 1, s), pose(0.0F, 3.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        leftChain.addOrReplaceChild("ll_plate_o0", box(16, 32, true, -0.5F, 0.0F, -2.0F, 1, 4, 4, s), pose(1.8F, 0.2F, 0.0F, 0.0F, 0.0F, -0.31415927F));
        leftChain.addOrReplaceChild("ll_plate_o1", box(16, 32, true, -0.5F, 0.0F, -2.0F, 1, 4, 4, s), pose(1.9F, 3.0F, 0.0F, 0.0F, 0.0F, -0.2443461F));
        leftChain.addOrReplaceChild("ll_plate_b0", box(0, 32, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 0.2F, 2.0F, 0.17453292F, 0.0F, 0.0F));
        leftChain.addOrReplaceChild("ll_plate_b1", box(0, 32, true, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 3.0F, 2.0F, 0.17453292F, 0.0F, 0.0F));
        PartDefinition leftBoot = leftLeg.addOrReplaceChild("lb_base", box(0, 40, true, -2.0F, -0.5F, -2.0F, 4, 5, 4, s), at(0.0F, 8.0F, 0.0F));
        leftBoot.addOrReplaceChild("lb_front", box(0, 50, true, -2.0F, 0.5F, 0.0F, 4, 1, 1, s), at(0.0F, 3.0F, -2.8F));
        leftBoot.addOrReplaceChild("lb_side0", box(10, 50, true, -0.5F, 0.5F, 0.0F, 1, 1, 4, s), at(2.0F, 3.0F, -2.1F));
        leftBoot.addOrReplaceChild("lb_side1", box(20, 50, true, -0.5F, 0.5F, 0.0F, 1, 1, 3, s), at(2.0F, 2.2F, -1.1F));

        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition rightChain = rightLeg.addOrReplaceChild("rl_chainmail", box(26, 32, false, -2.0F, 0.0F, -2.0F, 4, 8, 4, s), PartPose.ZERO);
        rightChain.addOrReplaceChild("rl_plate_f0", box(0, 32, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(-0.4F, 0.2F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        rightChain.addOrReplaceChild("rl_plate_f1", box(0, 32, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 3.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        rightChain.addOrReplaceChild("rl_plate_f2", box(10, 32, false, 0.0F, 0.0F, -0.7F, 2, 5, 1, s), pose(0.0F, 0.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        rightChain.addOrReplaceChild("rl_plate_f3", box(10, 32, false, 0.0F, 0.0F, -0.7F, 2, 5, 1, s), pose(0.0F, 3.0F, -1.7F, -0.17453292F, 0.0F, 0.0F));
        rightChain.addOrReplaceChild("rl_plate_o0", box(16, 32, false, -0.5F, 0.0F, -2.0F, 1, 4, 4, s), pose(-1.8F, 0.2F, 0.0F, 0.0F, 0.0F, 0.31415927F));
        rightChain.addOrReplaceChild("rl_plate_o1", box(16, 32, false, -0.5F, 0.0F, -2.0F, 1, 4, 4, s), pose(-1.9F, 3.0F, 0.0F, 0.0F, 0.0F, 0.2443461F));
        rightChain.addOrReplaceChild("rl_plate_b0", box(0, 32, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 0.2F, 2.0F, 0.17453292F, 0.0F, 0.0F));
        rightChain.addOrReplaceChild("rl_plate_b1", box(0, 32, false, -2.0F, 0.0F, -0.5F, 4, 4, 1, s), pose(0.0F, 3.0F, 2.0F, 0.17453292F, 0.0F, 0.0F));
        PartDefinition rightBoot = rightLeg.addOrReplaceChild("rb_base", box(0, 40, false, -2.0F, -0.5F, -2.0F, 4, 5, 4, s), at(0.0F, 8.0F, 0.0F));
        rightBoot.addOrReplaceChild("rb_front", box(0, 50, false, -2.0F, 0.5F, 0.0F, 4, 1, 1, s), at(0.0F, 3.0F, -2.8F));
        rightBoot.addOrReplaceChild("rb_side0", box(10, 50, false, -0.5F, 0.5F, 0.0F, 1, 1, 4, s), at(-2.0F, 3.0F, -2.1F));
        rightBoot.addOrReplaceChild("rb_side1", box(20, 50, false, -0.5F, 0.5F, 0.0F, 1, 1, 3, s), at(-2.0F, 2.2F, -1.1F));

        PartDefinition head = root.getChild("head");
        head.addOrReplaceChild("hm_top", box(32, 0, false, -4.0F, -8.0F, -4.0F, 9, 4, 9, 0.0F), at(-0.5F, -1.0F, -0.5F));
        head.addOrReplaceChild("hm_l0", box(68, 0, true, -0.5F, 0.0F, -4.0F, 1, 4, 10, 0.0F), pose(4.1F, -6.3F, -1.2F, 0.0F, 0.0F, -0.3926991F));
        head.addOrReplaceChild("hm_r0", box(68, 0, false, -0.5F, 0.0F, -4.0F, 1, 4, 10, 0.0F), pose(-4.1F, -6.3F, -1.2F, 0.0F, 0.0F, 0.3926991F));
        head.addOrReplaceChild("hm_b0", box(68, 14, false, -4.5F, 0.0F, -0.5F, 9, 4, 1, 0.0F), pose(0.0F, -4.0F, 3.7F, 0.3926991F, 0.0F, 0.0F));
        head.addOrReplaceChild("hm_b1", box(68, 14, false, -4.5F, 0.0F, -0.5F, 9, 4, 1, 0.0F), pose(0.0F, -6.3F, 4.1F, 0.3926991F, 0.0F, 0.0F));
        head.addOrReplaceChild("hm_f0", box(68, 19, false, -4.0F, 0.0F, -0.5F, 9, 1, 1, 0.0F), at(-0.5F, -6.5F, -4.7F));
        PartDefinition face = head.addOrReplaceChild("face_0", box(102, 0, false, -1.0F, 0.0F, -0.5F, 2, 3, 1, 0.0F), pose(0.0F, -2.8F, -4.1F, 0.08726646F, 0.0F, 0.0F));
        face.addOrReplaceChild("face_l1", box(90, 0, true, -1.0F, 0.0F, -0.5F, 2, 3, 4, 0.0F), pose(3.4F, 0.0F, 0.2F, -0.08726646F, 0.0F, 0.0F));
        face.addOrReplaceChild("face_l2", box(102, 4, true, -1.0F, 0.0F, -0.5F, 2, 2, 1, 0.0F), pose(1.7F, 0.9F, 0.2F, -0.08726646F, 0.0F, 0.0F));
        face.addOrReplaceChild("face_r1", box(90, 0, false, -1.0F, 0.0F, -0.5F, 2, 3, 4, 0.0F), pose(-3.4F, 0.0F, 0.2F, -0.08726646F, 0.0F, 0.0F));
        face.addOrReplaceChild("face_r2", box(102, 4, false, -1.0F, 0.0F, -0.5F, 2, 2, 1, 0.0F), at(-1.7F, 0.9F, 0.2F));
        for (int mask = 0; mask < 3; mask++) {
            head.addOrReplaceChild("face_mask" + mask, box(90, 8 + mask * 7, false, -4.0F, 0.0F, -0.5F, 8, 4, 3, 0.0F), at(0.0F, -5.0F, -3.6F));
        }
        head.addOrReplaceChild("face_goggles", box(112, 8, false, -4.0F, 0.0F, -0.5F, 8, 4, 3, 0.0F), at(0.0F, -5.0F, -3.5F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        boolean headSlot = slot == EquipmentSlot.HEAD;
        boolean chestSlot = slot == EquipmentSlot.CHEST;
        boolean legsSlot = slot == EquipmentSlot.LEGS;
        boolean feetSlot = slot == EquipmentSlot.FEET;
        head.visible = headSlot;
        hat.visible = false;
        body.visible = chestSlot || legsSlot;
        rightArm.visible = chestSlot;
        leftArm.visible = chestSlot;
        rightLeg.visible = legsSlot || feetSlot;
        leftLeg.visible = legsSlot || feetSlot;
        chainmail.visible = chestSlot;
        belt.visible = legsSlot;
        leftChainmail.visible = legsSlot;
        rightChainmail.visible = legsSlot;
        leftBoot.visible = feetSlot;
        rightBoot.visible = feetSlot;
        ItemStack helmet = state.headEquipment;
        boolean fortress = helmet.getItem() instanceof FortressArmorItem;
        int mask = fortress ? FortressArmorItem.getMask(helmet) : -1;
        goggles.visible = fortress && FortressArmorItem.hasGoggles(helmet);
        for (int index = 0; index < masks.length; index++) {
            masks[index].visible = mask == index;
        }
    }
}
