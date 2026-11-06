package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.model.ObjModel;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.primordial.PrimordialGauntletItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public class GauntletItemRenderer implements SpecialModelRenderer<ItemStack> {
    private static final Identifier MODEL = WitchingGadgets.id("obj/primordial_gauntlet.obj");
    private static final Identifier BRACELET = WitchingGadgets.id("textures/models/primordialbracelet.png");
    private static final Identifier RUNES = WitchingGadgets.id("textures/gui/bracelet.png");
    private static final Identifier WHITE = WitchingGadgets.id("textures/models/white.png");
    private static final String[] FINGERS = {"Thumb", "Index", "Middle", "Ring", "Pinky"};
    private static final int[][] OVERLAY = {
        {93, 25, 79}, {103, 39, 90}, {113, 52, 100}, {123, 67, 111}, {132, 80, 122}, {141, 94, 132}, {152, 108, 142}, {161, 122, 153},
        {171, 135, 164}, {161, 122, 153}, {152, 108, 142}, {141, 94, 132}, {132, 80, 122}, {123, 67, 111}, {113, 52, 100}, {103, 39, 90},
    };

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    private static void part(SubmitNodeCollector collector, PoseStack poseStack, Identifier texture, String name, int color, int light) {
        ObjModel obj = ObjModel.get(MODEL);
        collector.submitCustomGeometry(poseStack, TcRenderTypes.entityCutoutNoCull(texture), (pose, buffer) -> obj.renderPart(name, pose, buffer, color, light));
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        int ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.1F, 0.5F);
        int grey = rgb(204, 204, 204);
        part(collector, poseStack, BRACELET, "Guard_Arm_04", grey, lightCoords);
        part(collector, poseStack, BRACELET, "Guard_Hand_05", grey, lightCoords);
        int[] overlay = OVERLAY[ticks % 32 / 2];
        for (int f = 0; f < FINGERS.length; f++) {
            part(collector, poseStack, BRACELET, "Finger_" + FINGERS[f] + "_f" + f, 0xFFFFFFFF, lightCoords);
            part(collector, poseStack, BRACELET, "Finger_" + FINGERS[f] + "_Overlay_f" + f + "_1", rgb(overlay[0], overlay[1], overlay[2]), lightCoords);
        }
        float rune = ticks % 32 + 1;
        if (rune > 15) {
            rune = 16 - (rune - 16);
        }
        int runeLevel = Math.max(0, Math.min(255, (int) (rune * 0.2F * 255)));
        part(collector, poseStack, RUNES, "Runes_06", rgb(runeLevel, runeLevel, runeLevel), lightCoords);
        if (stack != null) {
            NonNullList<ItemStack> gems = PrimordialGauntletItem.gems(stack);
            for (int g = 1; g <= 3; g++) {
                ItemStack gem = gems.get(g);
                Aspect aspect = InfusedGemItem.aspect(gem);
                if (gem.isEmpty() || aspect == null) {
                    continue;
                }
                int color = aspect.getColor();
                int divisor = gem.isDamaged() ? 2 : 1;
                part(collector, poseStack, WHITE, "Gem0" + g + "_0" + g, rgb((color >> 16 & 255) / divisor, (color >> 8 & 255) / divisor, (color & 255) / divisor), lightCoords);
            }
        }
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
        event.register(WitchingGadgets.id("primordial_gauntlet"), Unbaked.MAP_CODEC);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public GauntletItemRenderer bake(BakingContext context) {
            return new GauntletItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
