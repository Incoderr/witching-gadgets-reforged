package witchinggadgets.client.terraform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import thaumcraft.aspect.Aspect;
import witchinggadgets.terraform.TerraformerBlockEntity;

public class TerraformerRenderer implements BlockEntityRenderer<TerraformerBlockEntity> {
    public TerraformerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TerraformerBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Aspect suction = entity.suction();
        TerraformRendering.terraformer(poseStack, buffers, packedLight, suction == null ? 0xFFFFFFFF : 0xFF000000 | suction.getColor());
    }
}
