package witchinggadgets.client.robes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.robes.RobesFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class RobeModels {
    private static SkirtedRobeModel model;

    private RobeModels() {
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SkirtedRobeModel.LAYER, SkirtedRobeModel::createLayer);
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        model = new SkirtedRobeModel(event.getEntityModels().bakeLayer(SkirtedRobeModel.LAYER));
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (layerType != EquipmentClientInfo.LayerType.HUMANOID) {
                    return original;
                }
                if (model == null) {
                    model = new SkirtedRobeModel(Minecraft.getInstance().getEntityModels().bakeLayer(SkirtedRobeModel.LAYER));
                }
                return model;
            }
        }, RobesFeature.BEWITCHED_ROBE.get());
    }
}
