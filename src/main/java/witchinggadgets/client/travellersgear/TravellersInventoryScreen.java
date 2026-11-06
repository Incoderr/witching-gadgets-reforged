package witchinggadgets.client.travellersgear;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.aspect.AspectRenderer;
import thaumcraft.wand.WandManager;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.platform.gear.ShowItemPayload;
import witchinggadgets.client.platform.gear.GearClientEvents;
import witchinggadgets.platform.gear.GearMenu;
import witchinggadgets.platform.gear.GearSlotView;

public class TravellersInventoryScreen extends AbstractContainerScreen<GearMenu> {
    private static final int[][] OVERLAYS = {
        {53, 202}, {21, 202}, {37, 202}, {21, 218}, {37, 218},
        {221, 19}, {221, 37}, {239, 18}, {239, 37},
        {239, 55}, {239, 73}, {239, 91}, {239, 109},
        {221, 55}, {221, 73}, {221, 73}, {221, 91},
    };
    private static final String[] VIS = {"aer", "ignis", "terra", "aqua", "ordo", "perditio"};
    private static final int GREY = 0xFF777777;

    private float playerRotation;

    public TravellersInventoryScreen(GearMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 218, 200);
    }

    private static TgLayout.Element element(String name) {
        return TgLayout.element(name);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        var texture = TgLayout.textureId();
        WgGuiBlit.blit(graphics, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        List<TgLayout.Element> elements = TgLayout.elements();
        for (int index = 0; index < TgLayout.SLOT_ELEMENTS; index++) {
            if (menu.isHidden(index) || index >= menu.slots.size()) {
                continue;
            }
            TgLayout.Element element = elements.get(index);
            WgGuiBlit.blit(graphics, texture, leftPos + element.x, topPos + element.y, 220, 0, 18, 18);
            if (index < OVERLAYS.length && !menu.slots.get(index).hasItem()) {
                WgGuiBlit.blit(graphics, texture, leftPos + element.x + 1, topPos + element.y + 1, OVERLAYS[index][0], OVERLAYS[index][1], 16, 16);
            }
        }
        TgLayout.Element player = element("Player");
        if (!player.hidden) {
            WgGuiBlit.blit(graphics, texture, leftPos + player.x, topPos + player.y + player.height - 9, 202, 247, 54, 9);
            renderPlayer(graphics, leftPos + player.x, topPos + player.y, leftPos + player.x + player.width, topPos + player.y + player.height - 7, minecraft.player);
        }
    }

    private void renderPlayer(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, LivingEntity entity) {
        EntityRenderer<? super LivingEntity, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        EntityRenderState state = renderer.createRenderState(entity, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + playerRotation;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
        graphics.entity(state, 30, translation, rotation, new Quaternionf(), x0, y0, x1, y1);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Player player = minecraft.player;
        var texture = TgLayout.textureId();
        TgLayout.Element health = element("Health");
        if (!health.hidden) {
            WgGuiBlit.blit(graphics, texture, health.x, health.y, 220, 162, 9, 9);
            graphics.text(font, "x" + player.getMaxHealth() / 2.0F, health.x + 10, health.y, GREY, false);
        }
        TgLayout.Element armor = element("Armor");
        if (!armor.hidden) {
            WgGuiBlit.blit(graphics, texture, armor.x, armor.y, 229, 162, 9, 9);
            graphics.text(font, "x" + player.getArmorValue(), armor.x + 10, armor.y, GREY, false);
        }
        TgLayout.Element experience = element("Experience");
        if (!experience.hidden && player.getXpNeededForNextLevel() > 0) {
            WgGuiBlit.blit(graphics, texture, experience.x, experience.y + 6, 186, 200, 70, 5);
            int filled = (int) (player.experienceProgress * 71.0F);
            if (filled > 0) {
                WgGuiBlit.blit(graphics, texture, experience.x, experience.y + 6, 186, 205, Math.min(filled, 70), 5);
            }
            int center = experience.x + experience.width / 2;
            small(graphics, Component.translatable("TG.guitext.lvl").getString() + ": " + player.experienceLevel, center, experience.y, 0xFF33AA66);
            small(graphics, (int) Math.floor(player.experienceProgress * player.getXpNeededForNextLevel()) + "/" + player.getXpNeededForNextLevel(), center, experience.y + 12, GREY);
        }
        TgLayout.Element speed = element("Speed");
        if (!speed.hidden) {
            WgGuiBlit.blit(graphics, texture, speed.x, speed.y, 238, 162, 9, 9);
            graphics.text(font, (int) (player.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1000) + "%", speed.x + 10, speed.y, GREY, false);
        }
        TgLayout.Element damage = element("Attack Strength");
        if (!damage.hidden) {
            WgGuiBlit.blit(graphics, texture, damage.x, damage.y, 247, 162, 9, 9);
            graphics.text(font, Math.round(player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 100) + "%", damage.x + 10, damage.y, GREY, false);
        }
        TgLayout.Element vis = element("Vis Discounts");
        if (!vis.hidden) {
            graphics.text(font, Component.translatable("TG.guitext.visDiscount").getString() + ":", vis.x, vis.y, GREY, false);
            for (int index = 0; index < VIS.length; index++) {
                Aspect aspect = Aspect.getAspect(VIS[index]);
                int x = vis.x + index / 2 * 20;
                int y = vis.y + 10 + index % 2 * 10;
                graphics.pose().pushMatrix();
                graphics.pose().translate(x, y);
                graphics.pose().scale(0.5F, 0.5F);
                AspectRenderer.drawTag(graphics, 0, 0, aspect);
                graphics.text(font, (int) (WandManager.getTotalVisDiscount(player, aspect) * 100) + "%", 20, 4, GREY, false);
                graphics.pose().popMatrix();
            }
        }
        TgLayout.Element name = element("Name");
        if (!name.hidden) {
            String text = player.getName().getString();
            graphics.text(font, text, name.x + name.width / 2 - font.width(text) / 2, name.y, GREY, false);
        }
        TgLayout.Element title = element("Tile");
        String playerTitle = TitleRenderer.title(player);
        if (!title.hidden && !playerTitle.isEmpty()) {
            small(graphics, Component.translatable(playerTitle).getString(), title.x + title.width / 2, title.y, GREY);
        }
        TgLayout.Element effects = element("Potion Effects");
        if (!effects.hidden) {
            int y = effects.y;
            List<Component> tooltip = new ArrayList<>();
            for (MobEffectInstance effect : player.getActiveEffects()) {
                if (!effect.showIcon()) {
                    continue;
                }
                WgGuiBlit.sprite(graphics, Hud.getMobEffectSprite(effect.getEffect()), effects.x, y, 18, 18);
                int localX = mouseX - leftPos;
                int localY = mouseY - topPos;
                if (localX > effects.x && localX <= effects.x + 18 && localY > y && localY <= y + 18) {
                    Component label = effect.getEffect().value().getDisplayName();
                    if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
                        label = label.copy().append(" ").append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
                    }
                    tooltip.add(label);
                    tooltip.add(MobEffectUtil.formatDuration(effect, 1.0F, minecraft.level.tickRateManager().tickrate()));
                }
                y += 18;
            }
            if (!tooltip.isEmpty()) {
                graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            }
        }
    }

    private void small(GuiGraphicsExtractor graphics, String text, int centerX, int y, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y);
        graphics.pose().scale(0.5F, 0.5F);
        graphics.text(font, text, -font.width(text) / 2, 0, color, false);
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot instanceof GearSlotView view && !view.hasItem() && menu.getCarried().isEmpty()) {
            graphics.setTooltipForNextFrame(Component.translatable(view.type().descriptionKey()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 1 && event.hasControlDown() && hoveredSlot != null && hoveredSlot.hasItem()) {
            WgClientNetwork.sendToServer(new ShowItemPayload(hoveredSlot.index));
            return true;
        }
        TgLayout.Element player = element("Player");
        double x = event.x() - leftPos - player.x;
        double y = event.y() - topPos - player.y;
        if (!player.hidden && y >= player.height - 9 && y <= player.height) {
            if (x >= 0 && x <= 16) {
                playerRotation += 22.5F;
            }
            if (x >= player.width - 16 && x <= player.width) {
                playerRotation -= 22.5F;
            }
        }
        return super.mouseClicked(event, doubleClick);
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
