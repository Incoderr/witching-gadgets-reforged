package witchinggadgets.client.platform;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import thaumcraft.platform.CamouflagedBlockEntity;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.arcanestone.ArcaneStoneFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class CamoModels {
    private CamoModels() {
    }

    @SubscribeEvent
    static void wrap(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Block block : List.of(ArcaneStoneFeature.ETHEREAL_WALL.get())) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                ModelResourceLocation location = BlockModelShaper.stateToModelLocation(state);
                BakedModel model = models.get(location);
                if (model != null) {
                    models.put(location, new Camo(model));
                }
            }
        }
    }

    private static final class Camo extends BakedModelWrapper<BakedModel> {
        private Camo(BakedModel original) {
            super(original);
        }

        private static @Nullable BlockState disguise(ModelData data) {
            return data.get(CamouflagedBlockEntity.DISGUISE);
        }

        private BakedModel model(@Nullable BlockState disguise) {
            return disguise == null ? originalModel : Minecraft.getInstance().getBlockRenderer().getBlockModel(disguise);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
            BlockState disguise = disguise(data);
            return model(disguise).getQuads(disguise == null ? state : disguise, side, random, ModelData.EMPTY, renderType);
        }

        @Override
        public TextureAtlasSprite getParticleIcon(ModelData data) {
            return model(disguise(data)).getParticleIcon(ModelData.EMPTY);
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
            BlockState disguise = disguise(data);
            return disguise == null ? super.getRenderTypes(state, random, data) : model(disguise).getRenderTypes(disguise, random, ModelData.EMPTY);
        }
    }
}
