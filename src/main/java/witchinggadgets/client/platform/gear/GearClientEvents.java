package witchinggadgets.client.platform.gear;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.platform.gear.GearMenus;
import witchinggadgets.client.travellersgear.TgLayout;
import witchinggadgets.client.travellersgear.TravellersInventoryScreen;
import witchinggadgets.platform.gear.OpenGearPayload;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GearClientEvents {
    public static final KeyMapping OPEN_KEY = new KeyMapping("key.witchinggadgets.gear", InputConstants.KEY_G, KeyMapping.CATEGORY_INVENTORY);

    private GearClientEvents() {
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        TgLayout.install();
        event.register(GearMenus.EXPANDED.get(), TravellersInventoryScreen::new);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                WgClientNetwork.sendToServer(new OpenGearPayload(TgLayout.hiddenMask()));
            }
        }
    }
}
