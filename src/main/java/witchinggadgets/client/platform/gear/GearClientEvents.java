package witchinggadgets.client.platform.gear;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import thaumcraft.client.platform.baubles.BaublesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.platform.gear.GearMenus;
import witchinggadgets.platform.gear.OpenGearPayload;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GearClientEvents {
    public static final KeyMapping OPEN_KEY = new KeyMapping("key.witchinggadgets.gear", InputConstants.KEY_G, KeyMapping.Category.INVENTORY);

    private GearClientEvents() {
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GearMenus.EXPANDED.get(), GearScreen::new);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_KEY);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                WgClientNetwork.sendToServer(new OpenGearPayload());
            }
        }
    }

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen || event.getScreen() instanceof BaublesScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event.getScreen();
            event.addListener(new GearButton(screen, 20, Component.translatable("button.witchinggadgets.gear"), () -> WgClientNetwork.sendToServer(new OpenGearPayload())));
        } else if (event.getScreen() instanceof GearScreen screen) {
            event.addListener(new GearButton(screen, 9, Component.translatable("button.witchinggadgets.normal"), () -> {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.player.connection.send(new ServerboundContainerClosePacket(screen.getMenu().containerId));
                minecraft.player.containerMenu = minecraft.player.inventoryMenu;
                minecraft.gui.setScreen(new InventoryScreen(minecraft.player));
            }));
        }
    }
}
