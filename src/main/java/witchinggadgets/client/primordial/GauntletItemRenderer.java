package witchinggadgets.client.primordial;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.TcRenderTypes;
import thaumcraft.client.render.model.ObjModel;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.primordial.PrimordialGauntletItem;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public class GauntletItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = WitchingGadgets.id("obj/primordial_gauntlet.obj");
    private static final ResourceLocation BRACELET = WitchingGadgets.id("textures/models/primordialbracelet.png");
    private static final ResourceLocation RUNES = WitchingGadgets.id("textures/gui/bracelet.png");
    private static final ResourceLocation WHITE = WitchingGadgets.id("textures/models/white.png");
    private static final String[] FINGERS = {"Thumb", "Index", "Middle", "Ring", "Pinky"};
    private static final int[][] OVERLAY = {
        {93, 25, 79}, {103, 39, 90}, {113, 52, 100}, {123, 67, 111}, {132, 80, 122}, {141, 94, 132}, {152, 108, 142}, {161, 122, 153},
        {171, 135, 164}, {161, 122, 153}, {152, 108, 142}, {141, 94, 132}, {132, 80, 122}, {123, 67, 111}, {113, 52, 100}, {103, 39, 90},
    };
    private static GauntletItemRenderer instance;

    public GauntletItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private static void part(MultiBufferSource buffers, PoseStack poseStack, ResourceLocation texture, String name, int color, int light) {
        ObjModel.get(MODEL).renderPart(name, poseStack.last(), buffers.getBuffer(TcRenderTypes.entityCutoutNoCull(texture)), color, light);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        int ticks = Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount;
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.1F, 0.5F);
        int grey = rgb(204, 204, 204);
        part(buffers, poseStack, BRACELET, "Guard_Arm_04", grey, light);
        part(buffers, poseStack, BRACELET, "Guard_Hand_05", grey, light);
        int[] tint = OVERLAY[ticks % 32 / 2];
        for (int f = 0; f < FINGERS.length; f++) {
            part(buffers, poseStack, BRACELET, "Finger_" + FINGERS[f] + "_f" + f, 0xFFFFFFFF, light);
            part(buffers, poseStack, BRACELET, "Finger_" + FINGERS[f] + "_Overlay_f" + f + "_1", rgb(tint[0], tint[1], tint[2]), light);
        }
        float rune = ticks % 32 + 1;
        if (rune > 15) {
            rune = 16 - (rune - 16);
        }
        int runeLevel = Math.max(0, Math.min(255, (int) (rune * 0.2F * 255)));
        part(buffers, poseStack, RUNES, "Runes_06", rgb(runeLevel, runeLevel, runeLevel), light);
        NonNullList<ItemStack> gems = PrimordialGauntletItem.gems(stack);
        for (int g = 1; g <= 3; g++) {
            ItemStack gem = gems.get(g);
            Aspect aspect = InfusedGemItem.aspect(gem);
            if (gem.isEmpty() || aspect == null) {
                continue;
            }
            int color = aspect.getColor();
            int divisor = gem.isDamaged() ? 2 : 1;
            part(buffers, poseStack, WHITE, "Gem0" + g + "_0" + g, rgb((color >> 16 & 255) / divisor, (color >> 8 & 255) / divisor, (color & 255) / divisor), light);
        }
        poseStack.popPose();
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (instance == null) {
                    instance = new GauntletItemRenderer();
                }
                return instance;
            }
        }, PrimordialFeature.PRIMORDIAL_GAUNTLET.get());
    }
}
