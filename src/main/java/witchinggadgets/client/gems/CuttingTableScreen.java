package witchinggadgets.client.gems;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.gems.CuttingTableMenu;

public class CuttingTableScreen extends AbstractContainerScreen<CuttingTableMenu> {
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/gui/cuttingtable.png");
    private static final ResourceLocation CUTS = WitchingGadgets.id("textures/gui/research/gemcuts_transparent.png");

    public CuttingTableScreen(CuttingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int offset = menu.targetCut() == 0 ? 0 : 80;
        WgGuiBlit.region(graphics, CUTS, leftPos + 73, topPos + 17, 30, 30, offset, offset, 80, 80, 256, 256);
        Aspect aspect = menu.infusingAspect();
        if (aspect != null) {
            AspectRenderer.drawTag(graphics, leftPos + 118, topPos + 41, aspect, 0.0F);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double clickX, double clickY, int button) {
        boolean handled = super.mouseClicked(clickX, clickY, button);
        double x = clickX - leftPos;
        double y = clickY - topPos;
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
