package witchinggadgets.client.spinning;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.spinning.SpinningWheelMenu;

public class SpinningWheelScreen extends AbstractContainerScreen<SpinningWheelMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/spinningwheel.png");

    public SpinningWheelScreen(SpinningWheelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 184, 233);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos + 144, 0, 144, 184, 90);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, 38, 144);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos + 126, topPos, 126, 0, 48, 144);
        int progress = menu.progressScaled(88);
        if (progress > 0) {
            WgGuiBlit.blit(graphics, TEXTURE, leftPos + 38, topPos, 38, 0, progress, 144);
        }
    }
}
