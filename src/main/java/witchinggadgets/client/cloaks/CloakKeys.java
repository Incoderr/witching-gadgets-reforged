package witchinggadgets.client.cloaks;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.platform.gear.ActivateGearPayload;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class CloakKeys {
    public static final KeyMapping ACTIVATE_KEY = new KeyMapping("key.witchinggadgets.cloak", InputConstants.KEY_R, KeyMapping.CATEGORY_GAMEPLAY);

    private CloakKeys() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE_KEY);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (ACTIVATE_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                WgClientNetwork.sendToServer(new ActivateGearPayload());
            }
        }
    }
}
