package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import witchinggadgets.client.platform.WgGuiBlit;

public class TgCustomizationScreen extends Screen {
    private static final int WIDTH = 218;
    private static final int HEIGHT = 200;

    private final Set<TgLayout.Element> multiSelect = new HashSet<>();
    private int guiLeft;
    private int guiTop;
    private TgLayout.@Nullable Element drag;
    private int dragX;
    private int dragY;
    private int[] selectStart;

    public TgCustomizationScreen() {
        super(Component.translatable("TG.guitext.equipment"));
    }

    @Override
    protected void init() {
        guiLeft = (width - WIDTH) / 2;
        guiTop = (height - HEIGHT) / 2;
        int y = guiTop + 20;
        for (Map.Entry<String, TgLayout.Preset> preset : TgLayout.presets().entrySet()) {
            addRenderableWidget(Button.builder(Component.literal(preset.getKey()), button -> {
                TgLayout.apply(preset.getValue());
                multiSelect.clear();
            }).bounds(guiLeft + WIDTH, y, 64, 12).build());
            y += 13;
        }
        y = guiTop + 120;
        for (String texture : TgLayout.TEXTURES) {
            addRenderableWidget(Button.builder(Component.literal(texture), button -> TgLayout.setTexture(texture)).bounds(guiLeft + WIDTH, y, 64, 12).build());
            y += 13;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TgLayout.textureId(), guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);
        TgLayout.Element hovered = null;
        for (TgLayout.Element element : TgLayout.elements()) {
            int x = guiLeft + element.x;
            int y = guiTop + element.y;
            boolean selected = multiSelect.contains(element);
            int primary = element.hidden ? 0xFF664400 : selected ? 0xFF00FFCC : 0xFFFFCC00;
            int secondary = element.hidden ? 0xFF664400 : selected ? 0xFF00CC88 : 0xFFCC8800;
            graphics.fill(x, y, x + element.width, y + 1, primary);
            graphics.fill(x + element.width - 1, y, x + element.width, y + element.height, primary);
            graphics.fill(x, y + element.height - 1, x + element.width, y + element.height, secondary);
            graphics.fill(x, y, x + 1, y + element.height, secondary);
            graphics.outline(x + element.width - 6, y, 6, 6, 0xFFCCCCCC);
            if (mouseX >= x && mouseY >= y && mouseX < x + element.width && mouseY < y + element.height) {
                hovered = element;
            }
        }
        if (hovered != null) {
            int x = guiLeft + hovered.x;
            int y = guiTop + hovered.y;
            boolean hide = hideBox(hovered, mouseX, mouseY);
            String label = hide ? "Hide" : hovered.name;
            int centerX = hide ? x + hovered.width - 3 : x + hovered.width / 2;
            int textY = hide ? y : y + (hovered.height - 8) / 2;
            graphics.text(font, label, centerX - font.width(label) / 2, textY, 0xFFFFFFFF);
        }
        center(graphics, Component.translatable("TG.guitext.preset").getString() + ":", guiLeft + WIDTH + 32, guiTop + 10);
        center(graphics, Component.translatable("TG.guitext.texture").getString() + ":", guiLeft + WIDTH + 32, guiTop + 110);
        if (selectStart != null) {
            graphics.outline(Math.min(selectStart[0], mouseX), Math.min(selectStart[1], mouseY), Math.abs(mouseX - selectStart[0]) + 1, Math.abs(mouseY - selectStart[1]) + 1, 0xFFFFFFFF);
        }
    }

    private void center(GuiGraphicsExtractor graphics, String text, int x, int y) {
        graphics.text(font, text, x - font.width(text) / 2, y, 0xFFFFFFFF);
    }

    private boolean hideBox(TgLayout.Element element, double mouseX, double mouseY) {
        int x = guiLeft + element.x;
        int y = guiTop + element.y;
        return mouseX >= x + element.width - 6 && mouseX <= x + element.width - 1 && mouseY >= y + 1 && mouseY <= y + 6;
    }

    private TgLayout.@Nullable Element at(double mouseX, double mouseY) {
        List<TgLayout.Element> elements = TgLayout.elements();
        for (int index = elements.size() - 1; index >= 0; index--) {
            TgLayout.Element element = elements.get(index);
            int x = guiLeft + element.x;
            int y = guiTop + element.y;
            if (mouseX >= x && mouseY >= y && mouseX < x + element.width && mouseY < y + element.height) {
                return element;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            selectStart = new int[] {(int) event.x(), (int) event.y()};
            return true;
        }
        TgLayout.Element element = at(event.x(), event.y());
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || element == null) {
            return false;
        }
        if (hideBox(element, event.x(), event.y())) {
            element.hidden = !element.hidden;
        } else {
            drag = element;
            dragX = (int) event.x();
            dragY = (int) event.y();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (drag != null) {
            int moveX = event.hasControlDown() ? 0 : (int) event.x() - dragX;
            int moveY = event.hasShiftDown() ? 0 : (int) event.y() - dragY;
            dragX += moveX;
            dragY += moveY;
            Set<TgLayout.Element> moved = new HashSet<>(multiSelect);
            moved.add(drag);
            for (TgLayout.Element element : moved) {
                element.x += moveX;
                element.y += moveY;
            }
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && selectStart != null) {
            multiSelect.clear();
            int x0 = Math.min(selectStart[0], (int) event.x());
            int y0 = Math.min(selectStart[1], (int) event.y());
            int x1 = Math.max(selectStart[0], (int) event.x());
            int y1 = Math.max(selectStart[1], (int) event.y());
            for (TgLayout.Element element : TgLayout.elements()) {
                int x = guiLeft + element.x;
                int y = guiTop + element.y;
                if (x >= x0 && x + element.width <= x1 && y >= y0 && y + element.height <= y1) {
                    multiSelect.add(element);
                }
            }
            selectStart = null;
            return true;
        }
        drag = null;
        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        TgLayout.save();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
