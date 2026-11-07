package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.platform.Baubles;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.Gear;
import witchinggadgets.travellersgear.ArmorStandBlock;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;
import witchinggadgets.travellersgear.ArmorStandMenu;

public class ArmorStandRenderer implements BlockEntityRenderer<ArmorStandBlockEntity> {
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/models/travellersgear/armorstand.png");
    private static final float PLAYER_SCALE = 1.03125F;
    private static final float[] HSCALE = {0.625F, 0.5F, 0.5F, 1.0F, 0.5F, 1.0F, 1.0F};
    private static final float[] ROTATION = {0, 0, 0, 0, 0, 0, -45};
    private static final float[] SCALE_FOR_TYPE = {1.0F, 0.5F, 1.25F};
    private static final float[][] POS_NORMAL = {{1.5F, -0.3F}, {-1.5F, -0.3F}, {-0.15F, -1.2F}, {0.8F, -0.6F}};
    private static final float[][] POS_TINY = {{-1.5F, -1.5F}, {-1.0F, -1.5F}, {-0.5F, -1.5F}, {-1.45F, -1.0F}, {-0.9F, -0.8F}};
    private static final float[][] POS_BIG = {{1.2F, -1.6F}, {-0.3F, -0.8F}};
    @Nullable
    private static Model model;

    public record Model(ModelPart floor, ModelPart head, ModelPart spine, ModelPart corpus, ModelPart table) {
    }

    public ArmorStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelPart box(int u, int v, float x, float y, float z, int w, int h, int d, float px, float py, float pz, float rx, float rz) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("box", CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, w, h, d), PartPose.offsetAndRotation(px, py, pz, rx, 0.0F, rz));
        return LayerDefinition.create(mesh, 96, 32).bakeRoot();
    }

    private static ModelPart group(ModelPart... parts) {
        Map<String, ModelPart> children = new LinkedHashMap<>();
        for (int index = 0; index < parts.length; index++) {
            children.put("part" + index, parts[index]);
        }
        return new ModelPart(List.of(), children);
    }

    public static Model model() {
        if (model == null) {
            model = new Model(
                box(0, 12, 0, 0, 0, 16, 2, 16, -8.0F, 0.0F, -8.0F, 0, 0),
                box(68, 0, 0, 0, 0, 7, 7, 7, -3.5F, 26.0F, -7.5F, 0, 0),
                box(56, 0, 0, 0, 0, 3, 29, 3, -1.5F, 0.0F, -5.5F, 0, 0),
                group(
                    box(0, 17, 0, 0, 0, 4, 8, 4, -2.0F, 16.0F, -6.0F, 0, 0),
                    box(68, 14, 0, 0, 0, 6, 2, 4, -8.0F, 22.0F, -6.0F, 0, 0),
                    box(68, 14, 0, 0, 0, 6, 2, 4, 2.0F, 22.0F, -6.0F, 0, 0),
                    box(56, 0, -1, 0, -5, 2, 7, 2, 1.0F, 18.0F, 0.0F, 0, -0.7853982F),
                    box(56, 0, -1, 0, -5, 2, 7, 2, -1.0F, 18.0F, 0.0F, 0, 0.7853982F)
                ),
                group(
                    box(0, 0, 0, 0, 0, 16, 2, 10, -8.0F, 12.0F, -1.0F, 0.3F, 0),
                    box(9, 9, 0, 0, 0, 16, 1, 1, -8.0F, 11.2F, 8.175F, 0.3F, 0),
                    box(0, 14, 0, 0, 0, 4, 10, 4, 3.0F, 1.875F, 2.0F, 0, 0),
                    box(0, 14, 0, 0, 0, 4, 10, 4, -6.0F, 1.875F, 2.0F, 0, 0)
                )
            );
        }
        return model;
    }

    public static float rotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static boolean flag(int flags, int bit) {
        return (flags & 1 << bit) != 0;
    }

    public static void renderStand(PoseStack poseStack, MultiBufferSource buffers, int light, int flags) {
        Model parts = model();
        VertexConsumer buffer = buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(TEXTURE));
        boolean head = flag(flags, ArmorStandBlockEntity.RENDER_ARMOR);
        boolean chest = flag(flags, ArmorStandBlockEntity.RENDER_ARMOR + 1);
        boolean legs = flag(flags, ArmorStandBlockEntity.RENDER_ARMOR + 2);
        if (flag(flags, ArmorStandBlockEntity.RENDER_FLOOR)) {
            parts.floor().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        } else {
            poseStack.translate(0.0F, -0.125F, 0.0F);
        }
        if (!flag(flags, ArmorStandBlockEntity.RENDER_TABLE)) {
            poseStack.translate(0.0F, 0.0F, 0.25F);
        }
        if (head) {
            parts.head().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
        if (head || chest || legs) {
            parts.spine().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
        if (chest) {
            parts.corpus().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
        if (flag(flags, ArmorStandBlockEntity.RENDER_TABLE)) {
            parts.table().render(poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    private static boolean dress(RemotePlayer fake, ArmorStandBlockEntity stand) {
        boolean any = false;
        for (int index = 0; index < 4; index++) {
            ItemStack armor = stand.flag(ArmorStandBlockEntity.RENDER_ARMOR + index) ? stand.getItem(ArmorStandBlockEntity.ARMOR + index) : ItemStack.EMPTY;
            fake.setItemSlot(ArmorStandMenu.ARMOR_SLOTS[index], armor.copy());
            ItemStack bauble = stand.flag(ArmorStandBlockEntity.RENDER_BAUBLES + index) ? stand.getItem(ArmorStandBlockEntity.BAUBLES + index) : ItemStack.EMPTY;
            Baubles.set(fake, index, bauble.copy());
            any |= !armor.isEmpty() || !bauble.isEmpty();
        }
        for (int index = 0; index < ArmorStandMenu.GEAR_SLOTS.length; index++) {
            ItemStack gear = stand.flag(ArmorStandBlockEntity.RENDER_GEAR + index) ? stand.getItem(ArmorStandBlockEntity.GEAR + index) : ItemStack.EMPTY;
            Gear.set(fake, ArmorStandMenu.GEAR_SLOTS[index], gear.copy());
            any |= !gear.isEmpty();
        }
        return any;
    }

    @Override
    public void render(ArmorStandBlockEntity stand, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        int flags = stand.flags();
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation(stand.getBlockState().getValue(ArmorStandBlock.FACING))));
        renderStand(poseStack, buffers, light, flags);
        poseStack.translate(0.0F, 1.61F, -0.25F);
        if (minecraft.level instanceof ClientLevel level) {
            RemotePlayer fake = Mannequins.of(level);
            if (dress(fake, stand)) {
                fake.setPos(Vec3.atBottomCenterOf(stand.getBlockPos()));
                fake.setYRot(0.0F);
                fake.yRotO = 0.0F;
                fake.setXRot(0.0F);
                fake.xRotO = 0.0F;
                fake.yBodyRot = 0.0F;
                fake.yBodyRotO = 0.0F;
                fake.yHeadRot = 0.0F;
                fake.yHeadRotO = 0.0F;
                poseStack.pushPose();
                poseStack.scale(PLAYER_SCALE, PLAYER_SCALE, PLAYER_SCALE);
                poseStack.translate(0.0F, 0.2F - 1.62F, 0.0F);
                EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
                dispatcher.setRenderShadow(false);
                dispatcher.render(fake, 0.0, 0.0, 0.0, 0.0F, partialTick, poseStack, buffers, light);
                dispatcher.setRenderShadow(true);
                poseStack.popPose();
            }
        }
        if (flag(flags, ArmorStandBlockEntity.RENDER_TABLE)) {
            renderTable(stand, poseStack, buffers, light);
        }
        poseStack.popPose();
    }

    private static void renderTable(ArmorStandBlockEntity stand, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.translate(0.0F, 0.2F * PLAYER_SCALE, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(-0.3)));
        poseStack.scale(0.25F, 1.0F, 0.25F);
        poseStack.translate(-0.5F, -0.6625F, -2.6F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        int occupiedTiny = 0;
        for (int dis = 0; dis < 7; dis++) {
            if (!stand.getItem(ArmorStandBlockEntity.BAUBLES + dis).isEmpty() && type(dis) == 1) {
                occupiedTiny++;
            }
        }
        int usedNormal = 0;
        int usedTiny = 0;
        int usedBig = 0;
        for (int dis = 0; dis < 7; dis++) {
            if (dis < 4 && !stand.flag(ArmorStandBlockEntity.DISPLAY_BAUBLES + dis) || dis >= 4 && !stand.flag(ArmorStandBlockEntity.DISPLAY_GEAR + dis - 4)) {
                continue;
            }
            ItemStack stack = stand.getItem(ArmorStandBlockEntity.BAUBLES + dis);
            if (stack.isEmpty()) {
                continue;
            }
            int type = type(dis);
            float[] position = type == 2 ? POS_BIG[Math.min(usedBig, POS_BIG.length - 1)] : type == 1 ? POS_TINY[Math.min(usedTiny, POS_TINY.length - 1)] : POS_NORMAL[Math.min(usedNormal, POS_NORMAL.length - 1)];
            float scale = SCALE_FOR_TYPE[type];
            float oY = position[1];
            if (dis == 5 && occupiedTiny < 4) {
                scale = 1.25F;
                oY -= 0.375F;
            }
            poseStack.pushPose();
            poseStack.translate(position[0], oY, 0.15F);
            poseStack.scale(scale, scale, HSCALE[dis]);
            poseStack.mulPose(Axis.ZP.rotationDegrees(ROTATION[dis]));
            poseStack.translate(0.5F, 0.5F, -0.03125F);
            minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, buffers, stand.getLevel(), (int) stand.getBlockPos().asLong() + dis);
            poseStack.popPose();
            if (type == 2 && usedBig < POS_BIG.length) {
                usedBig++;
            }
            if (type == 1 && usedTiny < POS_TINY.length) {
                usedTiny++;
            }
            if (type == 0 && usedNormal < POS_NORMAL.length) {
                usedNormal++;
            }
        }
    }

    private static int type(int dis) {
        return dis == 1 || dis == 2 ? 1 : dis == 3 || dis == 4 ? 2 : 0;
    }

    @Override
    public boolean shouldRenderOffScreen(ArmorStandBlockEntity stand) {
        return true;
    }
}
