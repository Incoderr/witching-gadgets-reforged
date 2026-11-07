package witchinggadgets.client.cloaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
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
        for (PlayerSkin.Model skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new CloakLayer(renderer));
                renderer.addLayer(new BaubleLayer(renderer));
                renderer.addLayer(new witchinggadgets.client.travellersgear.ToolDisplayLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = event.getEntity();
        if (minecraft.player != null && player != minecraft.player && CloakEvents.isSpectral(player) && !CloakEvents.seesSpectral(minecraft.player)) {
            event.setCanceled(true);
        }
    }
}
