package witchinggadgets.client.platform;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class WgGuiBlit {
    private WgGuiBlit() {
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }
}
