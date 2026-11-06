package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Locale;
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
import witchinggadgets.terraform.TerraformFocus;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public record TerraformItemRenderer(String focus) implements NoDataSpecialModelRenderer {
    public static final String NONE = "none";

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (NONE.equals(focus)) {
            TerraformRendering.terraformer(poseStack, collector, lightCoords, 0xFFFFFFFF);
            return;
        }
        TerraformFocus value = TerraformFocus.valueOf(focus.toUpperCase(Locale.ROOT));
        TerraformRendering.focus(poseStack, collector, lightCoords, 0xFF000000 | value.aspect().getColor());
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
        event.register(WitchingGadgets.id("terraform"), Unbaked.MAP_CODEC);
    }

    public record Unbaked(String focus) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = Codec.STRING.optionalFieldOf("focus", NONE).xmap(Unbaked::new, Unbaked::focus);

        @Override
        public TerraformItemRenderer bake(BakingContext context) {
            return new TerraformItemRenderer(focus);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
