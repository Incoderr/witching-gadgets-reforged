package witchinggadgets.client.primordial;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.aspect.Aspect;
import thaumcraft.node.NodeModifier;
import thaumcraft.node.NodeType;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.primordial.GauntletMenu;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.primordial.StoredNode;

public class GauntletScreen extends AbstractContainerScreen<GauntletMenu> {
    private static final Identifier TEXTURE = WitchingGadgets.id("textures/gui/primordialglove.png");

    public GauntletScreen(GauntletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (mouseX >= leftPos + 75 && mouseX < leftPos + 101 && mouseY >= topPos + 26 && mouseY < topPos + 52) {
            List<Component> lines = nodeInfo();
            if (!lines.isEmpty()) {
                graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    private List<Component> nodeInfo() {
        List<Component> lines = new ArrayList<>();
        StoredNode node = minecraft == null || minecraft.player == null ? null : PrimordialFeature.GAUNTLET_NODE.get(minecraft.player.getMainHandItem());
        if (node == null) {
            return lines;
        }
        NodeType[] types = NodeType.values();
        NodeType type = types[Math.max(0, Math.min(node.type(), types.length - 1))];
        Component title = Component.translatable("nodetype." + type.name() + ".name");
        if (node.modifier() >= 0 && node.modifier() < NodeModifier.values().length) {
            title = title.copy().append(", ").append(Component.translatable("nodemod." + NodeModifier.values()[node.modifier()].name() + ".name"));
        }
        lines.add(title);
        lines.add(Component.literal("  ").append(Component.translatable("wg.gui.visSize")).append(": " + node.aspects().visSize()).withStyle(ChatFormatting.DARK_PURPLE));
        if (minecraft.hasShiftDown()) {
            for (Aspect aspect : node.aspects().getAspects()) {
                lines.add(Component.literal("   " + aspect.getName() + " " + node.aspects().getAmount(aspect)));
            }
        }
        return lines;
    }
}
