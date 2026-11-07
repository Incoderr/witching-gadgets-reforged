package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public record ArmorStandItemRenderer() implements NoDataSpecialModelRenderer {
    private static final float SCALE = 0.46875F;

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        ArmorStandRenderer.renderStand(poseStack, collector, lightCoords, ArmorStandBlockEntity.ALL_FLAGS);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (float x : new float[] {0.0F, 1.0F}) {
            for (float y : new float[] {0.0F, 1.0F}) {
                for (float z : new float[] {0.0F, 1.0F}) {
                    output.accept(new Vector3f(x, y, z));
                }
            }
        }
    }

    @SubscribeEvent
    static void register(RegisterSpecialModelRendererEvent event) {
        event.register(WitchingGadgets.id("armor_stand"), Unbaked.MAP_CODEC);
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public ArmorStandItemRenderer bake(BakingContext context) {
            return new ArmorStandItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
