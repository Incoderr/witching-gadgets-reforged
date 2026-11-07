package witchinggadgets.client.travellersgear;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GuiEntities {
    private GuiEntities() {
    }

    public static void render(GuiGraphics graphics, int x0, int y0, int x1, int y1, int size, float rotationH, float rotationV, LivingEntity entity) {
        float centerX = (x0 + x1) / 2.0F;
        float centerY = (y0 + y1) / 2.0F;
        graphics.enableScissor(x0, y0, x1, y1);
        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI).rotateY((float) Math.toRadians(rotationH)).rotateX((float) Math.toRadians(rotationV));
        float bodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float headRotO = entity.yHeadRotO;
        float headRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F;
        entity.setYRot(180.0F);
        entity.setXRot(0.0F);
        entity.yHeadRot = 180.0F;
        entity.yHeadRotO = 180.0F;
        float scale = entity.getScale();
        Vector3f translate = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F * scale, 0.0F);
        InventoryScreen.renderEntityInInventory(graphics, centerX, centerY, size / scale, translate, pose, null, entity);
        entity.yBodyRot = bodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = headRotO;
        entity.yHeadRot = headRot;
        graphics.disableScissor();
    }
}
