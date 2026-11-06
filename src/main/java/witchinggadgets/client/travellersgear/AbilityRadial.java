package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.client.platform.GuiLines;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.WgClientConfig;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.client.platform.WgGuiQuads;
import witchinggadgets.gear.AbilitySlots;
import witchinggadgets.platform.gear.ActivateAbilityPayload;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class AbilityRadial {
    public static final KeyMapping KEY = new KeyMapping("TG.keybind.activeaAbilities", InputConstants.KEY_R, KeyMapping.Category.INVENTORY);

    private static float radial;
    private static boolean lock;
    private static boolean keyDown;
    private static boolean released;

    private AbilityRadial() {
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WitchingGadgets.id("ability_radial"), AbilityRadial::render);
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
        float speed = WgClientConfig.abilityRadialSpeed();
        boolean down = KEY.isDown();
        if (down && !keyDown && !AbilitySlots.list(player).isEmpty()) {
            if (lock) {
                close(minecraft);
                keyDown = true;
            } else {
                radial = Math.min(1.0F, radial + speed);
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
                radial = Math.max(0.0F, radial - speed);
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
        Player player = minecraft.player;
        if (player != null) {
            List<AbilitySlots.Entry> gear = AbilitySlots.list(player);
            int selected = segment(minecraft, gear.size()) - 1;
            if (selected >= 0 && selected < gear.size()) {
                WgClientNetwork.sendToServer(new ActivateAbilityPayload(gear.get(selected).slot()));
            }
        }
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

    private static float mouseX(Minecraft minecraft) {
        return (float) (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth()) - minecraft.getWindow().getGuiScaledWidth() / 2.0F;
    }

    private static float mouseY(Minecraft minecraft) {
        return (float) (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight()) - minecraft.getWindow().getGuiScaledHeight() / 2.0F - 1.0F;
    }

    private static int segment(Minecraft minecraft, int count) {
        double rad = 62.5 * radial;
        double inner = rad * 0.6875;
        double mx = mouseX(minecraft);
        double my = mouseY(minecraft);
        double radius = Math.sqrt(mx * mx + my * my);
        if (radius < inner || radius > rad) {
            return -1;
        }
        float segmentAngle = 360.0F / (count + 1);
        double angle = Math.toDegrees(Math.acos(my / radius));
        if (mx < 0) {
            angle = 360.0 - angle;
        }
        angle = (angle + segmentAngle / 2.0) % 360.0;
        return (int) (angle / segmentAngle);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || radial <= 0.0F) {
            return;
        }
        List<AbilitySlots.Entry> gear = AbilitySlots.list(player);
        if (gear.isEmpty()) {
            return;
        }
        int count = gear.size() + 1;
        float segmentAngle = 360.0F / count;
        float rad = 62.5F * radial;
        float inner = rad * 0.6875F;
        float itemRadius = rad * 0.95F * 0.875F;
        float spin = -(180.0F + 180.0F * radial);
        int hovered = segment(minecraft, gear.size());
        int limit = Math.max(1, 16 / gear.size());
        float subSegment = segmentAngle / limit;
        graphics.pose().pushMatrix();
        graphics.pose().translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F);
        for (int i = 0; i < count; i++) {
            boolean hover = i == hovered;
            float start = i * segmentAngle - segmentAngle / 2.0F + spin;
            float[] points = new float[limit * 8];
            int[] colors = new int[limit * 4];
            int outerColor = 0xFF000000 | (hover ? 0xF9EED2 : 0x4D2412);
            int innerColor = 0xFF000000 | (hover ? 0xFCF7E9 : 0x803B26);
            for (int j = 0; j < limit; j++) {
                float a0 = start + subSegment * j;
                float a1 = start + subSegment * (j + 1);
                put(points, j * 8, a0, rad);
                put(points, j * 8 + 2, a1, rad);
                put(points, j * 8 + 4, a1, inner);
                put(points, j * 8 + 6, a0, inner);
                colors[j * 4] = outerColor;
                colors[j * 4 + 1] = outerColor;
                colors[j * 4 + 2] = innerColor;
                colors[j * 4 + 3] = innerColor;
            }
            WgGuiQuads.quads(graphics, points, colors);
            arc(graphics, start, subSegment, limit, rad, 0xFF000000 | (hover ? 0xFFFFFF : 0x777777));
            arc(graphics, start, subSegment, limit, inner, 0xFF000000 | (hover ? 0xFCF7E9 : 0x999999));
        }
        for (int i = 1; i < count; i++) {
            double angle = Math.toRadians(segmentAngle * i + spin);
            float x = (float) (Math.sin(angle) * itemRadius);
            float y = (float) (Math.cos(angle) * itemRadius);
            graphics.item(gear.get(i - 1).stack(), (int) (x - 8.0F), (int) (y - 8.0F));
        }
        graphics.pose().popMatrix();
    }

    private static void arc(GuiGraphicsExtractor graphics, float start, float subSegment, int limit, float radius, int color) {
        float[] xs = new float[limit + 1];
        float[] ys = new float[limit + 1];
        int[] colors = new int[limit + 1];
        for (int j = 0; j <= limit; j++) {
            double angle = Math.toRadians(start + subSegment * j);
            xs[j] = (float) (Math.sin(angle) * radius);
            ys[j] = (float) (Math.cos(angle) * radius);
            colors[j] = color;
        }
        GuiLines.strip(graphics, xs, ys, colors, false);
    }

    private static void put(float[] points, int offset, float degrees, float radius) {
        double angle = Math.toRadians(degrees);
        points[offset] = (float) (Math.sin(angle) * radius);
        points[offset + 1] = (float) (Math.cos(angle) * radius);
    }
}
