package witchinggadgets.client.baubles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.platform.TcRenderTypes;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.client.travellersgear.Mannequins;
import witchinggadgets.travellersgear.TravellersGearFeature;
import thaumcraft.platform.Baubles;
import net.minecraft.world.entity.EquipmentSlot;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

public class BaubleLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/models/magicalbaubles.png");

    private static final ResourceLocation TG_TEXTURE = WitchingGadgets.id("textures/models/travellersgear/tgtextures.png");

    private final ModelPart root;
    private final ModelPart travellersGear;

    public BaubleLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
        this.root = createLayer().bakeRoot();
        this.travellersGear = createTravellersGearLayer().bakeRoot();
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        piece(mesh, "double_jump", 24, 16, 0, 1.125F);
        piece(mesh, "knockback", 40, 16, 0, 1.125F);
        piece(mesh, "strength", 40, 24, 7, 0.125F);
        piece(mesh, "haste", 24, 24, 7, 0.125F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition createTravellersGearLayer() {
        MeshDefinition mesh = new MeshDefinition();
        piece(mesh, "pauldron", 40, 16, 0, 0.5F);
        piece(mesh, "pauldron_armored", 40, 16, 0, 1.125F);
        piece(mesh, "vambrace", 40, 24, 7, 0.125F);
        mesh.getRoot().addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, 8.5F, -2.0F, 8, 4, 4, new CubeDeformation(0.125F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void piece(MeshDefinition mesh, String name, int u, int v, int yOffset, float grow) {
        mesh.getRoot().addOrReplaceChild(name + "_right", CubeListBuilder.create().texOffs(u, v).addBox(-3.0F, -2.0F + yOffset, -2.0F, 4, 4, 4, new CubeDeformation(grow)), PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild(name + "_left", CubeListBuilder.create().texOffs(u, v).mirror().addBox(-1.0F, -2.0F + yOffset, -2.0F, 4, 4, 4, new CubeDeformation(grow)), PartPose.ZERO);
    }

    private static String pieceName(Item item) {
        Map<Item, String> names = Map.of(
            BaublesFeature.DOUBLE_JUMP_SHOULDERS.get(), "double_jump",
            BaublesFeature.KNOCKBACK_SHOULDERS.get(), "knockback",
            BaublesFeature.STRENGTH_VAMBRACES.get(), "strength",
            BaublesFeature.HASTE_VAMBRACES.get(), "haste"
        );
        return names.get(item);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float ageInTicks, float yRot, float xRot) {
        if (player.isInvisible() && !Mannequins.is(player)) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(TEXTURE));
        for (GearSlot slot : new GearSlot[] {GearSlot.PAULDRON, GearSlot.VAMBRACE}) {
            ItemStack stack = Gear.get(player, slot);
            String name = stack.isEmpty() ? null : pieceName(stack.getItem());
            if (name == null) {
                continue;
            }
            arm(poseStack, buffer, light, getParentModel().rightArm, root.getChild(name + "_right"));
            arm(poseStack, buffer, light, getParentModel().leftArm, root.getChild(name + "_left"));
        }
        VertexConsumer tg = buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(TG_TEXTURE));
        if (Gear.get(player, GearSlot.PAULDRON).is(TravellersGearFeature.PAULDRON.get())) {
            String name = player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? "pauldron" : "pauldron_armored";
            arm(poseStack, tg, light, getParentModel().rightArm, travellersGear.getChild(name + "_right"));
            arm(poseStack, tg, light, getParentModel().leftArm, travellersGear.getChild(name + "_left"));
        }
        if (Gear.get(player, GearSlot.VAMBRACE).is(TravellersGearFeature.VAMBRACE.get())) {
            arm(poseStack, tg, light, getParentModel().rightArm, travellersGear.getChild("vambrace_right"));
            arm(poseStack, tg, light, getParentModel().leftArm, travellersGear.getChild("vambrace_left"));
        }
        if (Baubles.isEquipped(player, TravellersGearFeature.BELT.get())) {
            arm(poseStack, tg, light, getParentModel().body, travellersGear.getChild("belt"));
        }
    }

    private static void arm(PoseStack poseStack, VertexConsumer buffer, int light, ModelPart arm, ModelPart piece) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        piece.render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
