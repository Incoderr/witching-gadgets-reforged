package witchinggadgets.client.travellersgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.travellersgear.ArmorStandBlockEntity;
import witchinggadgets.travellersgear.TravellersGearFeature;

public class ArmorStandItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float SCALE = 0.46875F;
    private static ArmorStandItemRenderer instance;

    public ArmorStandItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(SCALE, SCALE, SCALE);
        ArmorStandRenderer.renderStand(poseStack, buffers, packedLight, ArmorStandBlockEntity.ALL_FLAGS);
        poseStack.popPose();
    }

    @EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        static void registerExtensions(RegisterClientExtensionsEvent event) {
            event.registerItem(new IClientItemExtensions() {
                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    if (instance == null) {
                        instance = new ArmorStandItemRenderer();
                    }
                    return instance;
                }
            }, TravellersGearFeature.ARMOR_STAND.get().asItem());
        }
    }
}
