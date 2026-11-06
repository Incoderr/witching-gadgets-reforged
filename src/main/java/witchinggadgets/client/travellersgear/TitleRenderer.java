package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class TitleRenderer {
    private static final float LINE = 9.0F * 1.15F * 0.025F;

    private TitleRenderer() {
    }

    public static String title(Player player) {
        ItemStack scroll = Gear.get(player, GearSlot.TITLE);
        return scroll.isEmpty() ? "" : BaublesFeature.TITLE.getOrDefault(scroll, "");
    }

    @SubscribeEvent
    static void onNameTag(RenderNameTagEvent.DoRender event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(event.getEntityRenderState() instanceof AvatarRenderState state) || event.getContent() != state.nameTag || state.isDiscrete || minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(state.id);
        if (!(entity instanceof Player player) || player == minecraft.player) {
            return;
        }
        String title = title(player);
        if (title.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0F, -LINE, 0.0F);
        event.getSubmitNodeCollector().submitNameTag(poseStack, state.nameTagAttachment, state.showExtraEars ? -10 : 0, Component.translatable(title), true, state.lightCoords, event.getCameraRenderState());
        poseStack.popPose();
    }
}
