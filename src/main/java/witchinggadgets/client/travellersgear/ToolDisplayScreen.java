package witchinggadgets.client.travellersgear;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.platform.Gear;
import witchinggadgets.platform.gear.SetToolDisplayPayload;
import witchinggadgets.travellersgear.ToolDisplay;

public class ToolDisplayScreen extends Screen {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    private final List<ToolDisplay> tools = new ArrayList<>();
    private int selected = -1;
    private float rotationH;
    private float rotationV;
    private boolean sliding;
    private double dragX;
    private double dragY;

    public ToolDisplayScreen() {
        super(Component.literal("Tool Display"));
    }

    private Player player() {
        return minecraft.player;
    }

    @Override
    protected void init() {
        if (tools.isEmpty() && selected < 0) {
            tools.addAll(Gear.toolDisplay(player()));
        }
        clearWidgets();
        if (tools.size() < ToolDisplay.MAX) {
            addRenderableWidget(Button.builder(Component.translatable("TG.guitext.add"), button -> {
                tools.add(ToolDisplay.create());
                selected = tools.size() - 1;
                rebuildWidgets();
            }).bounds(20, 10, 40, 20).build());
        }
        if (selected >= 0 && selected < tools.size()) {
            ToolDisplay tool = tools.get(selected);
            addRenderableWidget(Button.builder(Component.translatable("TG.guitext.rem"), button -> {
                tools.remove(selected);
                selected = -1;
                rebuildWidgets();
            }).bounds(60, 10, 40, 20).build());
            for (int axis = 0; axis < 3; axis++) {
                int index = axis;
                slider(height - 170 + axis * 10, (tool.translation().get(axis) + 1.0F) / 2.0F, value -> update(current -> vector(current.translation(), index, (float) (-1.0 + value * 2.0)), null, null));
                slider(height - 120 + axis * 10, (tool.rotation().get(axis) + 180.0F) / 360.0F, value -> update(null, current -> vector(current.rotation(), index, (float) (-180.0 + value * 360.0)), null));
                slider(height - 70 + axis * 10, (tool.scale().get(axis) + 2.0F) / 4.0F, value -> update(null, null, current -> vector(current.scale(), index, (float) (Math.round((value * 4.0 - 2.0) / 0.0625) * 0.0625))));
            }
            addRenderableWidget(Button.builder(check(tool.hideWhenEquipped()), button -> {
                ToolDisplay current = tools.get(selected);
                tools.set(selected, new ToolDisplay(current.slot(), current.translation(), current.rotation(), current.scale(), current.rotateWithHead(), !current.hideWhenEquipped()));
                button.setMessage(check(!current.hideWhenEquipped()));
            }).bounds(width - 100, height - 30, 40, 20).tooltip(Tooltip.create(Component.translatable("TG.guitext.hideWhenEquipped"))).build());
            addRenderableWidget(Button.builder(check(tool.rotateWithHead()), button -> {
                ToolDisplay current = tools.get(selected);
                tools.set(selected, new ToolDisplay(current.slot(), current.translation(), current.rotation(), current.scale(), !current.rotateWithHead(), current.hideWhenEquipped()));
                button.setMessage(check(!current.rotateWithHead()));
            }).bounds(width - 60, height - 30, 40, 20).tooltip(Tooltip.create(Component.translatable("TG.guitext.rotateWithHead"))).build());
            for (int slot = 0; slot < ToolDisplay.SLOTS; slot++) {
                int index = slot;
                int x = width - 105 + slot % 9 * 10;
                int y = slot < 9 ? height - 200 : height - 245 + slot / 9 * 10;
                ItemStack stack = player().getInventory().getItem(slot);
                Button button = Button.builder(Component.empty(), pressed -> {
                    ToolDisplay current = tools.get(selected);
                    tools.set(selected, new ToolDisplay(index, current.translation(), current.rotation(), current.scale(), current.rotateWithHead(), current.hideWhenEquipped()));
                    rebuildWidgets();
                }).bounds(x, y, 10, 10).build();
                if (!stack.isEmpty()) {
                    button.setTooltip(Tooltip.create(stack.getHoverName()));
                }
                button.active = tool.slot() != slot;
                addRenderableWidget(button);
            }
        }
        for (int index = 0; index < tools.size(); index++) {
            int tool = index;
            ItemStack stack = player().getInventory().getItem(tools.get(index).slot());
            addRenderableWidget(Button.builder(stack.isEmpty() ? Component.literal("-") : stack.getHoverName(), button -> {
                selected = tool;
                rebuildWidgets();
            }).bounds(20, 40 + index * 30, 80, 20).build());
        }
    }

    private static Component check(boolean value) {
        return value ? Component.literal("✔").withStyle(ChatFormatting.DARK_GREEN) : Component.literal("✖").withStyle(ChatFormatting.DARK_RED);
    }

    private static List<Float> vector(List<Float> current, int axis, float value) {
        List<Float> values = new ArrayList<>(current);
        values.set(axis, value);
        return List.copyOf(values);
    }

    private interface VectorUpdate {
        List<Float> apply(ToolDisplay current);
    }

    private void update(VectorUpdate translation, VectorUpdate rotation, VectorUpdate scale) {
        ToolDisplay current = tools.get(selected);
        tools.set(selected, new ToolDisplay(current.slot(),
            translation != null ? translation.apply(current) : current.translation(),
            rotation != null ? rotation.apply(current) : current.rotation(),
            scale != null ? scale.apply(current) : current.scale(),
            current.rotateWithHead(), current.hideWhenEquipped()));
    }

    private void slider(int y, double value, DoubleConsumer apply) {
        addRenderableWidget(new AbstractSliderButton(width - 100, y, 80, 10, Component.empty(), Math.max(0.0, Math.min(1.0, value))) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                apply.accept(this.value);
            }
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int size = 80;
        int centerX = width / 2;
        int bottom = height / 5 * 4;
        InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, centerX - 60, bottom - 180, centerX + 60, bottom, size, 0.0625F, rotationH / 20.0F, rotationV / 20.0F, player());
        for (int index = 0; index < tools.size(); index++) {
            graphics.item(player().getInventory().getItem(tools.get(index).slot()), 2, 42 + index * 30);
        }
        if (selected >= 0 && selected < tools.size()) {
            ToolDisplay tool = tools.get(selected);
            for (int slot = 0; slot < ToolDisplay.SLOTS; slot++) {
                int x = width - 105 + slot % 9 * 10;
                int y = slot < 9 ? height - 200 : height - 245 + slot / 9 * 10;
                ItemStack stack = player().getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(x + 1, y + 1);
                    graphics.pose().scale(0.5F, 0.5F);
                    graphics.item(stack, 0, 0);
                    graphics.pose().popMatrix();
                }
                if (tool.slot() == slot) {
                    graphics.outline(x, y, 10, 10, 0xFF00AA00);
                }
            }
            graphics.text(font, Component.translatable("TG.guitext.translation"), width - 90, height - 180, 0xFFFFFFFF);
            graphics.text(font, Component.translatable("TG.guitext.rotation"), width - 90, height - 130, 0xFFFFFFFF);
            graphics.text(font, Component.translatable("TG.guitext.scale"), width - 90, height - 80, 0xFFFFFFFF);
            for (int axis = 0; axis < 3; axis++) {
                value(graphics, tool.translation().get(axis), height - 168 + axis * 10);
                value(graphics, tool.rotation().get(axis), height - 118 + axis * 10);
                value(graphics, tool.scale().get(axis), height - 68 + axis * 10);
            }
        }
    }

    private void value(GuiGraphicsExtractor graphics, float value, int y) {
        String text = FORMAT.format(value);
        graphics.text(font, text, width - 120 - font.width(text) / 2, y, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        sliding = super.mouseClicked(event, doubleClick);
        dragX = event.x();
        dragY = event.y();
        return sliding;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!sliding) {
            if (event.button() == 0) {
                rotationH -= (float) (dragX - event.x());
            } else if (event.button() == 1) {
                rotationV += (float) (dragY - event.y());
            }
            dragX = event.x();
            dragY = event.y();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public void removed() {
        WgClientNetwork.sendToServer(new SetToolDisplayPayload(List.copyOf(tools)));
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
