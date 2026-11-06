package witchinggadgets.client.primordial;

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
import witchinggadgets.primordial.GauntletPayload;
import witchinggadgets.primordial.PrimordialGauntletItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GauntletKeys {
    public static final KeyMapping SELECT_KEY = new KeyMapping("key.witchinggadgets.gauntlet", InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY);

    private GauntletKeys() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SELECT_KEY);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (SELECT_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null && minecraft.player.getMainHandItem().getItem() instanceof PrimordialGauntletItem) {
                WgClientNetwork.sendToServer(new GauntletPayload(minecraft.player.isShiftKeyDown()));
            }
        }
    }
}
