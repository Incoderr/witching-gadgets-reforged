package witchinggadgets.client.platform.gear;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class GearButton extends AbstractButton {
    private final AbstractContainerScreen<?> screen;
    private final Runnable action;
    private final int offsetY;

    public GearButton(AbstractContainerScreen<?> screen, int offsetY, Component label, Runnable action) {
        super(screen.getLeftPos() + 66, screen.getTopPos() + offsetY, 10, 10, label);
        this.offsetY = offsetY;
        this.screen = screen;
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        setPosition(screen.getLeftPos() + 66, screen.getTopPos() + offsetY);
        if (isHovered()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GearScreen.TEXTURE, getX(), getY(), 210.0F, 48.0F, 10, 10, 256, 256);
            graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + 5, getY() + getHeight(), -1);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, GearScreen.TEXTURE, getX(), getY(), 200.0F, 48.0F, 10, 10, 256, 256);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
