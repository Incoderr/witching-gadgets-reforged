package witchinggadgets.client.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public final class WgGuiQuads {
    private WgGuiQuads() {
    }

    public static void quads(GuiGraphicsExtractor graphics, float[] points, int[] colors) {
        if (points.length < 8) {
            return;
        }
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = graphics.peekScissorStack();
        graphics.submitGuiElementRenderState(new Quads(RenderPipelines.GUI, pose, points.clone(), colors.clone(), scissor, bounds(points, pose, scissor)));
    }

    private static @Nullable ScreenRectangle bounds(float[] points, Matrix3x2fc pose, @Nullable ScreenRectangle scissor) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (int index = 0; index + 1 < points.length; index += 2) {
            minX = Math.min(minX, points[index]);
            minY = Math.min(minY, points[index + 1]);
            maxX = Math.max(maxX, points[index]);
            maxY = Math.max(maxY, points[index + 1]);
        }
        int left = (int) Math.floor(minX);
        int top = (int) Math.floor(minY);
        ScreenRectangle area = new ScreenRectangle(left, top, (int) Math.ceil(maxX) - left, (int) Math.ceil(maxY) - top).transformMaxBounds(pose);
        return scissor != null ? scissor.intersection(area) : area;
    }

    private record Quads(
        RenderPipeline pipeline,
        Matrix3x2fc pose,
        float[] points,
        int[] colors,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (int vertex = 0; vertex * 2 + 1 < points.length; vertex++) {
                consumer.addVertexWith2DPose(pose, points[vertex * 2], points[vertex * 2 + 1]).setColor(colors[vertex]);
            }
        }
    }
}
