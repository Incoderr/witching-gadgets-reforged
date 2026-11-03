package witchinggadgets.client.devices;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.research.Knowledge;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.devices.LabelLibraryMenu;

public class LabelLibraryScreen extends AbstractContainerScreen<LabelLibraryMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/labellibrary.png");
    private static final float SCALE = 0.65F;
    private static final int ROW = (int) (8 / SCALE);
    private static final int CELL = (int) Math.floor(17 * SCALE);

    public LabelLibraryScreen(LabelLibraryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    private List<Aspect> discovered() {
        List<Aspect> aspects = new ArrayList<>();
        if (minecraft == null || minecraft.player == null) {
            return aspects;
        }
        for (Aspect aspect : Aspect.values()) {
            if (Knowledge.get(minecraft.player).hasDiscoveredAspect(aspect)) {
                aspects.add(aspect);
            }
        }
        return aspects;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos - 5, 0, 0, imageWidth + 18, imageHeight + 5);
        Aspect selected = menu.selected();
        int x = (int) Math.floor((leftPos + 36) / SCALE);
        int y = (int) Math.floor((topPos + 8) / SCALE);
        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE, SCALE);
        List<Aspect> aspects = discovered();
        for (int index = 0; index < aspects.size(); index++) {
            Aspect aspect = aspects.get(index);
            AspectRenderer.drawTag(graphics, x + 17 * (index % ROW), y + 17 * (index / ROW), aspect, 0.0F, aspect == selected ? 1 : 0, 1.0F, false);
        }
        graphics.pose().popMatrix();
        if (selected != null) {
            AspectRenderer.drawTag(graphics, leftPos + 8, topPos + 30, selected, 0.0F);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        double mouseX = event.x() - (leftPos + 36);
        double mouseY = event.y() - (topPos + 8);
        List<Aspect> aspects = discovered();
        for (int index = 0; index < aspects.size(); index++) {
            int column = index % ROW;
            int row = index / ROW;
            if (mouseX >= CELL * column && mouseX < CELL * (column + 1) && mouseY >= CELL * row && mouseY < CELL * (row + 1)) {
                if (aspects.get(index) != menu.selected()) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, LabelLibraryMenu.indexOf(aspects.get(index)));
                }
                return true;
            }
        }
        return handled;
    }
}
