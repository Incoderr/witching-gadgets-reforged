package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.terraform.TerraformFeature;
import witchinggadgets.terraform.TerraformFocusBlock;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public class TerraformItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static TerraformItemRenderer instance;

    public TerraformItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (Block.byItem(stack.getItem()) instanceof TerraformFocusBlock focus) {
            TerraformRendering.focus(poseStack, buffers, packedLight, 0xFF000000 | focus.focus().aspect().getColor());
        } else {
            TerraformRendering.terraformer(poseStack, buffers, packedLight, 0xFFFFFFFF);
        }
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extensions = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (instance == null) {
                    instance = new TerraformItemRenderer();
                }
                return instance;
            }
        };
        event.registerItem(extensions, TerraformFeature.TERRAFORMER.get().asItem());
        TerraformFeature.FOCI.values().forEach(focus -> event.registerItem(extensions, focus.get().asItem()));
    }
}
