package witchinggadgets.client.travellersgear;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;
import witchinggadgets.travellersgear.ArmorStandFlagsPayload;
import witchinggadgets.travellersgear.ArmorStandMenu;

public class ArmorStandScreen extends AbstractContainerScreen<ArmorStandMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/travellersgear/armorstand.png");
    private static final int FOLD_OUT = 73;
    private static final int[][] OVERLAYS = {
        {203, 116}, {203, 134}, {221, 116}, {221, 134},
        {203, 170}, {203, 152}, {203, 152}, {203, 188},
        {221, 170}, {221, 152}, {221, 188},
    };

    private boolean styleMenu;
    private int foldOut;
    private int flags;

    public ArmorStandScreen(ArmorStandMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 199);
        ArmorStandBlockEntity stand = menu.blockEntity();
        flags = stand == null ? ArmorStandBlockEntity.ALL_FLAGS : stand.flags();
    }

    private boolean flag(int bit) {
        return (flags & 1 << bit) != 0;
    }

    private void toggle(int bit) {
        flags ^= 1 << bit;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (styleMenu && foldOut < FOLD_OUT) {
            foldOut = Math.min(FOLD_OUT, foldOut + 4);
        }
        if (!styleMenu && foldOut > 0) {
            foldOut = Math.max(0, foldOut - 4);
        }
        if (foldOut > 0) {
            WgGuiBlit.blit(graphics, TEXTURE, leftPos + 174, topPos, 176, 0, foldOut, 114);
        }
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, 176, imageHeight);
        for (int slot = 0; slot < ArmorStandMenu.STAND_END; slot++) {
            int[] position = slot < ArmorStandMenu.EQUIPMENT ? ArmorStandMenu.PLAYER_POSITIONS[slot] : ArmorStandMenu.STAND_POSITIONS[slot - ArmorStandMenu.EQUIPMENT];
            int[] overlay = OVERLAYS[slot % ArmorStandMenu.EQUIPMENT];
            WgGuiBlit.blit(graphics, TEXTURE, leftPos + position[0] - 1, topPos + position[1] - 1, 184, 115, 18, 18);
            if (!menu.slots.get(slot).hasItem()) {
                WgGuiBlit.blit(graphics, TEXTURE, leftPos + position[0], topPos + position[1], overlay[0], overlay[1], 16, 16);
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 23, topPos + 20, leftPos + 73, topPos + 90, 30, 0.0625F, mouseX, mouseY, minecraft.player);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (styleMenu && foldOut >= FOLD_OUT) {
            box(graphics, tooltip, x, y, 178, 8, ArmorStandBlockEntity.RENDER_FLOOR, "TG.guitext.showPlate");
            box(graphics, tooltip, x, y, 191, 8, ArmorStandBlockEntity.RENDER_TABLE, "TG.guitext.showTable");
            for (int i = 0; i < 4; i++) {
                box(graphics, tooltip, x, y, 178, 26 + 18 * i, ArmorStandBlockEntity.RENDER_ARMOR + i, "TG.guitext.showArmor" + i);
                box(graphics, tooltip, x, y, 191, 26 + 18 * i, ArmorStandBlockEntity.DISPLAY_BAUBLES + i, "TG.guitext.showBaubles" + i);
                box(graphics, tooltip, x, y, 200, 26 + 18 * i, ArmorStandBlockEntity.RENDER_BAUBLES + i, "TG.guitext.renderBaubles" + i);
            }
            for (int i = 0; i < 3; i++) {
                box(graphics, tooltip, x, y, 211, 26 + 18 * i, ArmorStandBlockEntity.DISPLAY_GEAR + i, "TG.guitext.showTG" + i);
                box(graphics, tooltip, x, y, 220, 26 + 18 * i, ArmorStandBlockEntity.RENDER_GEAR + i, "TG.guitext.renderTG" + i);
            }
        }
        Component style = Component.translatable("TG.guitext.style");
        graphics.text(font, style, 147 - font.width(style) / 2, 98, 0xFFFFFFFF, true);
        if (!tooltip.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }

    private void box(GuiGraphicsExtractor graphics, List<Component> tooltip, int mouseX, int mouseY, int x, int y, int bit, String key) {
        if (mouseX > x && mouseX < x + 8 && mouseY > y && mouseY < y + 8) {
            tooltip.add(Component.translatable(key));
        }
        if (flag(bit)) {
            graphics.text(font, "✔", x + 1, y - 1, 0xFFFFFFFF, true);
        }
    }

    private static boolean inBox(double mouseX, double mouseY, int x, int y) {
        return mouseX > x && mouseX < x + 8 && mouseY > y && mouseY < y + 8;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x() - leftPos;
        double y = event.y() - topPos;
        if (x >= 130 && x < 164 && y >= 93 && y < 111) {
            styleMenu = !styleMenu;
            return true;
        }
        if (styleMenu && foldOut >= FOLD_OUT) {
            boolean hit = false;
            if (inBox(x, y, 178, 8)) {
                toggle(ArmorStandBlockEntity.RENDER_FLOOR);
                hit = true;
            }
            if (inBox(x, y, 194, 8)) {
                toggle(ArmorStandBlockEntity.RENDER_TABLE);
                hit = true;
            }
            for (int i = 0; i < 4; i++) {
                int row = 26 + 18 * i;
                if (inBox(x, y, 178, row)) {
                    toggle(ArmorStandBlockEntity.RENDER_ARMOR + i);
                    hit = true;
                }
                if (inBox(x, y, 191, row)) {
                    toggle(ArmorStandBlockEntity.DISPLAY_BAUBLES + i);
                    hit = true;
                }
                if (inBox(x, y, 200, row)) {
                    toggle(ArmorStandBlockEntity.RENDER_BAUBLES + i);
                    hit = true;
                }
                if (i < 3 && inBox(x, y, 211, row)) {
                    toggle(ArmorStandBlockEntity.DISPLAY_GEAR + i);
                    hit = true;
                }
                if (i < 3 && inBox(x, y, 220, row)) {
                    toggle(ArmorStandBlockEntity.RENDER_GEAR + i);
                    hit = true;
                }
            }
            if (hit) {
                WgClientNetwork.sendToServer(new ArmorStandFlagsPayload(menu.containerId, flags));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        return super.hasClickedOutside(mouseX, mouseY, left, top) && !(styleMenu && mouseX >= left + 174 && mouseX < left + 174 + FOLD_OUT && mouseY >= top && mouseY < top + 114);
    }
}
