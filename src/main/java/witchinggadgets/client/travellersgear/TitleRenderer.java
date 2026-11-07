package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.joml.Matrix4f;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class TitleRenderer {
    private TitleRenderer() {
    }

    public static String title(Player player) {
        ItemStack scroll = Gear.get(player, GearSlot.TITLE);
        return scroll.isEmpty() ? "" : BaublesFeature.TITLE.getOrDefault(scroll, "");
    }

    @SubscribeEvent
    static void onNameTag(RenderNameTagEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(event.getEntity() instanceof Player player) || player == minecraft.player || player.isDiscrete() || Mannequins.is(player)) {
            return;
        }
        if (event.canRender().isFalse() || player.isInvisibleTo(minecraft.player)) {
            return;
        }
        String key = title(player);
        if (key.isEmpty()) {
            return;
        }
        Vec3 attachment = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(event.getPartialTick()));
        if (attachment == null) {
            return;
        }
        Component title = Component.translatable(key);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + 0.5 - 9.0 * 1.15 * 0.025, attachment.z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        Font font = minecraft.font;
        float x = -font.width(title) / 2.0F;
        int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        font.drawInBatch(title, x, 0, 553648127, false, matrix, event.getMultiBufferSource(), Font.DisplayMode.SEE_THROUGH, background, event.getPackedLight());
        font.drawInBatch(title, x, 0, -1, false, matrix, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, event.getPackedLight());
        poseStack.popPose();
    }
}
