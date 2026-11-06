package witchinggadgets.client.cloaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.baubles.BaubleLayer;
import witchinggadgets.cloaks.CloakEvents;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class CloakClientEvents {
    private CloakClientEvents() {
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new CloakLayer(renderer));
                renderer.addLayer(new BaubleLayer(renderer));
                renderer.addLayer(new witchinggadgets.client.travellersgear.ToolDisplayLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    static void onRenderPlayer(RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(event.getRenderState().id);
        if (entity instanceof Player player && player != minecraft.player && CloakEvents.isSpectral(player) && !CloakEvents.seesSpectral(minecraft.player)) {
            event.setCanceled(true);
        }
    }
}
