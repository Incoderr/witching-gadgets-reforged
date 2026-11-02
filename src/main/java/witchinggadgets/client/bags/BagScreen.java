package witchinggadgets.client.bags;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.bags.BagMenu;
import witchinggadgets.client.platform.WgGuiBlit;

public class BagScreen extends AbstractContainerScreen<BagMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/bag.png");

    public BagScreen(BagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, playerInventoryTitle, 4, imageHeight - 92, 0xFF404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
