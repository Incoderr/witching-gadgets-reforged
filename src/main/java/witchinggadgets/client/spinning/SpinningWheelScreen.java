package witchinggadgets.client.spinning;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.spinning.SpinningWheelMenu;

public class SpinningWheelScreen extends AbstractContainerScreen<SpinningWheelMenu> {
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/gui/spinningwheel.png");

    public SpinningWheelScreen(SpinningWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 184;
        imageHeight = 233;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos + 144, 0, 144, 184, 90);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, 38, 144);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos + 126, topPos, 126, 0, 48, 144);
        int progress = menu.progressScaled(88);
        if (progress > 0) {
            WgGuiBlit.blit(graphics, TEXTURE, leftPos + 38, topPos, 38, 0, progress, 144);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
