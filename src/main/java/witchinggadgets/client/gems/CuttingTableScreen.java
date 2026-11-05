package witchinggadgets.client.gems;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.gems.CuttingTableMenu;

public class CuttingTableScreen extends AbstractContainerScreen<CuttingTableMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/cuttingtable.png");
    private static final Identifier CUTS = WitchingGadgets.id("textures/gui/research/gemcuts_transparent.png");

    public CuttingTableScreen(CuttingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int offset = menu.targetCut() == 0 ? 0 : 80;
        WgGuiBlit.region(graphics, CUTS, leftPos + 73, topPos + 17, 30, 30, offset, offset, 80, 80, 256, 256);
        Aspect aspect = menu.infusingAspect();
        if (aspect != null) {
            AspectRenderer.drawTag(graphics, leftPos + 118, topPos + 41, aspect, 0.0F);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        double x = event.x() - leftPos;
        double y = event.y() - topPos;
        if (y > 12 && y < 24) {
            if (x > 106 && x < 117) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CuttingTableMenu.BUTTON_NEXT);
                return true;
            }
            if (x > 59 && x < 70) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CuttingTableMenu.BUTTON_PREVIOUS);
                return true;
            }
        }
        return handled;
    }
}
