package witchinggadgets.client.primordial;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation TEXTURE = WitchingGadgets.id("textures/gui/primordialglove.png");
    private static final ResourceLocation NODES = ResourceLocation.fromNamespaceAndPath("thaumcraft", "textures/misc/nodes.png");
    private static final String TYPE_COLOURS = "728545";
    private static final String MODIFIER_COLOURS = "f78";

    public GauntletScreen(GauntletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        WgGuiBlit.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawNode(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 75 && mouseX < leftPos + 101 && mouseY >= topPos + 26 && mouseY < topPos + 52) {
            List<Component> lines = nodeInfo();
            if (!lines.isEmpty()) {
                graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void drawNode(GuiGraphics graphics) {
        StoredNode node = minecraft == null || minecraft.player == null ? null : PrimordialFeature.GAUNTLET_NODE.get(minecraft.player.getMainHandItem());
        if (node == null) {
            return;
        }
        long time = System.currentTimeMillis();
        int cx = leftPos + 88;
        int cy = topPos + 39;
        float average = node.aspects().visSize() / (float) Math.max(1, node.aspects().size());
        int count = 0;
        for (Aspect aspect : node.aspects().getAspects()) {
            float mod = 2.0F * ((time + count * 512L) % 4096) / 4096.0F;
            if (mod > 1.0F) {
                mod = 2.0F - mod;
            }
            int radius = (int) (10 + 8 * mod * (node.aspects().getAmount(aspect) / average));
            int frame = (int) ((time / 64 + count * 4L) % 32);
            WgGuiBlit.tinted(graphics, NODES, cx - radius, cy - radius, radius * 2, radius * 2, frame * 64, 0, 64, 64, 2048, 2048, 0x40000000 | aspect.getColor());
            count++;
        }
        int type = node.type();
        int row = type == 2 ? 6 : type == 3 ? 2 : type == 4 ? 5 : type == 5 ? 4 : type == 6 ? 3 : 1;
        int frame = (int) ((time / 64) % 32);
        WgGuiBlit.tinted(graphics, NODES, cx - 10, cy - 10, 20, 20, frame * 64, row * 64, 64, 64, 2048, 2048, 0xFFFFFFFF);
    }

    private List<Component> nodeInfo() {
        List<Component> lines = new ArrayList<>();
        StoredNode node = minecraft == null || minecraft.player == null ? null : PrimordialFeature.GAUNTLET_NODE.get(minecraft.player.getMainHandItem());
        if (node == null) {
            return lines;
        }
        NodeType[] types = NodeType.values();
        NodeType type = types[Math.max(0, Math.min(node.type(), types.length - 1))];
        MutableComponent title = Component.translatable("nodetype." + type.name() + ".name").withStyle(ChatFormatting.getByCode(TYPE_COLOURS.charAt(type.ordinal())));
        if (node.modifier() >= 0 && node.modifier() < NodeModifier.values().length) {
            title = title.append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("nodemod." + NodeModifier.values()[node.modifier()].name() + ".name").withStyle(ChatFormatting.getByCode(MODIFIER_COLOURS.charAt(node.modifier()))));
        }
        lines.add(title);
        lines.add(Component.literal("  ").append(Component.translatable("wg.gui.visSize")).append(": " + node.aspects().visSize()).withStyle(ChatFormatting.DARK_PURPLE));
        if (Screen.hasShiftDown()) {
            for (Aspect aspect : node.aspects().getAspects()) {
                lines.add(Component.literal("   " + aspect.getName() + " " + node.aspects().getAmount(aspect)));
            }
        } else {
            lines.add(Component.literal("  ").append(Component.translatable("wg.gui.shiftForAspectList")));
        }
        return lines;
    }
}
