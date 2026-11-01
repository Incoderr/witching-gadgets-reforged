package witchinggadgets.client.platform.gear;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.gear.GearMenu;
import witchinggadgets.platform.gear.GearSlotView;

public class GearScreen extends AbstractContainerScreen<GearMenu> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/baubles/expanded_inventory.png");

    private final EffectsInInventory effects;

    public GearScreen(GearMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.effects = new EffectsInInventory(this);
    }

    @Override
    public boolean showsActiveEffects() {
        return effects.canSeeEffects();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        effects.extractRenderState(graphics, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        for (int index = GearMenu.ARMOR_START; index < GearMenu.GEAR_END; index++) {
            Slot slot = menu.slots.get(index);
            if (index < GearMenu.ARMOR_END || index >= GearMenu.GEAR_START) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + slot.x, topPos + slot.y, 200.0F, 0.0F, 16, 16, 256, 256);
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 26, topPos + 8, leftPos + 75, topPos + 78, 30, 0.0625F, mouseX, mouseY, minecraft.player);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot instanceof GearSlotView view && !view.hasItem() && menu.getCarried().isEmpty()) {
            graphics.setTooltipForNextFrame(Component.translatable(view.type().descriptionKey()), mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, Component.translatable("container.crafting"), 106, 16, -12566464, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (GearClientEvents.OPEN_KEY.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}
