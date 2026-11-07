package witchinggadgets.client.primordial;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.WgClientConfig;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.primordial.GauntletPayload;
import witchinggadgets.primordial.PrimordialGauntletItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GauntletKeys {
    public static final KeyMapping SELECT_KEY = new KeyMapping("key.witchinggadgets.gauntlet", InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY);
    private static final Identifier RADIAL = WitchingGadgets.id("textures/gui/gauntletradial0.png");
    private static final Identifier RADIAL_SPIN = WitchingGadgets.id("textures/gui/gauntletradial1.png");
    private static final int[] GEM_X = {-54, 13, -76, -22, 35};
    private static final int[] GEM_Y = {-64, -64, -6, 36, -6};

    private static float radial;
    private static boolean lock;
    private static boolean keyDown;
    private static boolean released;

    private GauntletKeys() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SELECT_KEY);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WitchingGadgets.id("gem_radial"), GauntletKeys::render);
    }

    private static boolean holdsGauntlet(Player player) {
        return player.getMainHandItem().getItem() instanceof PrimordialGauntletItem;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) {
            if (lock || radial > 0.0F) {
                close(minecraft);
                radial = 0.0F;
            }
            return;
        }
        if (lock && !holdsGauntlet(player)) {
            close(minecraft);
        }
        float step = WgClientConfig.gemRadialSpeed();
        boolean down = SELECT_KEY.isDown();
        if (down && !keyDown) {
            if (player.isShiftKeyDown() && holdsGauntlet(player)) {
                WgClientNetwork.sendToServer(new GauntletPayload(GauntletPayload.OPEN_MENU));
                keyDown = true;
            } else if (lock) {
                close(minecraft);
                keyDown = true;
            } else if (holdsGauntlet(player)) {
                radial = Math.min(1.0F, radial + step);
                if (radial >= 1.0F) {
                    lock = true;
                    keyDown = true;
                }
            }
        } else {
            if (keyDown && !down) {
                keyDown = false;
            }
            if (!lock && radial > 0.0F) {
                radial = Math.max(0.0F, radial - step);
            }
        }
        if (lock && !released) {
            minecraft.mouseHandler.releaseMouse();
            released = true;
        }
    }

    @SubscribeEvent
    static void onMouse(InputEvent.MouseButton.Pre event) {
        if (!lock) {
            return;
        }
        event.setCanceled(true);
        if (event.getButton() != InputConstants.MOUSE_BUTTON_LEFT || event.getAction() != InputConstants.PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        WgClientNetwork.sendToServer(new GauntletPayload(selection(minecraft)));
        close(minecraft);
    }

    private static void close(Minecraft minecraft) {
        lock = false;
        if (released) {
            released = false;
            if (minecraft.gui.screen() == null) {
                minecraft.mouseHandler.grabMouse();
            }
        }
    }

    private static int selection(Minecraft minecraft) {
        double mx = minecraft.mouseHandler.xpos() - minecraft.getWindow().getScreenWidth() / 2.0;
        double my = minecraft.getWindow().getScreenHeight() / 2.0 - minecraft.mouseHandler.ypos();
        double radius = Math.sqrt(mx * mx + my * my);
        if (radius == 0.0) {
            return 0;
        }
        double angle = (mx < 0 ? 180 : 0) + Math.abs((mx < 0 ? -180 : 0) + (my < 0 ? 90 : 0) + Math.abs((my < 0 ? -90 : 0) + Math.abs(Math.toDegrees(Math.acos(mx / radius)) - 90)));
        return angle > 288 ? 0 : angle < 72 ? 1 : 2 + (int) ((288 - angle) / 72);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || radial <= 0.0F) {
            return;
        }
        int rad = (int) (50 * radial);
        graphics.pose().pushMatrix();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);
        graphics.pose().rotate((float) Math.toRadians(180.0F + 180.0F * radial));
        WgGuiBlit.region(graphics, RADIAL, -rad, -rad, rad * 2, rad * 2, 0, 0, 256, 256, 256, 256);
        float spin = (System.currentTimeMillis() % 10000L) / 10000.0F;
        graphics.pose().pushMatrix();
        graphics.pose().rotate((float) Math.toRadians(360.0F * spin));
        WgGuiBlit.region(graphics, RADIAL_SPIN, -rad, -rad, rad * 2, rad * 2, 0, 0, 256, 256, 256, 256);
        graphics.pose().popMatrix();
        if (holdsGauntlet(player)) {
            List<ItemStack> gems = PrimordialGauntletItem.gems(player.getMainHandItem());
            int selected = selection(minecraft);
            for (int gem = 0; gem < gems.size() && gem < GEM_X.length; gem++) {
                ItemStack stack = gems.get(gem);
                if (stack.isEmpty()) {
                    continue;
                }
                int x = (int) (GEM_X[gem] / 256.0F * rad * 2);
                int y = (int) (GEM_Y[gem] / 256.0F * rad * 2);
                graphics.item(stack, x, y);
                if (selected != gem) {
                    graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x40000000);
                    graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x40000000);
                }
            }
        }
        graphics.pose().popMatrix();
    }
}
