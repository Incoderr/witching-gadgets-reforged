package witchinggadgets.client.baubles;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.client.platform.TcRenderTypes;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.travellersgear.TravellersGearFeature;
import thaumcraft.platform.Baubles;
import net.minecraft.world.entity.EquipmentSlot;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

public class BaubleLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/models/magicalbaubles.png");

    private static final Identifier TG_TEXTURE = WitchingGadgets.id("textures/models/travellersgear/tgtextures.png");

    private final ModelPart root;
    private final ModelPart travellersGear;

    public BaubleLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
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
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible && !witchinggadgets.client.travellersgear.Mannequins.is(state.id) || Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = witchinggadgets.client.travellersgear.Mannequins.entity(state.id);
        if (!(entity instanceof Player player)) {
            return;
        }
        for (GearSlot slot : new GearSlot[] {GearSlot.PAULDRON, GearSlot.VAMBRACE}) {
            ItemStack stack = Gear.get(player, slot);
            String name = stack.isEmpty() ? null : pieceName(stack.getItem());
            if (name == null) {
                continue;
            }
            arm(poseStack, collector, lightCoords, getParentModel().rightArm, root.getChild(name + "_right"), TEXTURE);
            arm(poseStack, collector, lightCoords, getParentModel().leftArm, root.getChild(name + "_left"), TEXTURE);
        }
        String travellersGearName = null;
        if (Gear.get(player, GearSlot.PAULDRON).is(TravellersGearFeature.PAULDRON.get())) {
            travellersGearName = player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? "pauldron" : "pauldron_armored";
        }
        if (travellersGearName != null) {
            arm(poseStack, collector, lightCoords, getParentModel().rightArm, travellersGear.getChild(travellersGearName + "_right"), TG_TEXTURE);
            arm(poseStack, collector, lightCoords, getParentModel().leftArm, travellersGear.getChild(travellersGearName + "_left"), TG_TEXTURE);
        }
        if (Gear.get(player, GearSlot.VAMBRACE).is(TravellersGearFeature.VAMBRACE.get())) {
            arm(poseStack, collector, lightCoords, getParentModel().rightArm, travellersGear.getChild("vambrace_right"), TG_TEXTURE);
            arm(poseStack, collector, lightCoords, getParentModel().leftArm, travellersGear.getChild("vambrace_left"), TG_TEXTURE);
        }
        if (Baubles.isEquipped(player, TravellersGearFeature.BELT.get())) {
            arm(poseStack, collector, lightCoords, getParentModel().body, travellersGear.getChild("belt"), TG_TEXTURE);
        }
    }

    private static void arm(PoseStack poseStack, SubmitNodeCollector collector, int light, ModelPart arm, ModelPart piece, Identifier texture) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        collector.submitModelPart(piece, poseStack, TcRenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }
}
