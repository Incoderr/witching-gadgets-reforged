package witchinggadgets.client.robes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
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
    private static final int ROBE_DEFAULT = 6961280;
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
            public int getDefaultDyeColor(ItemStack stack) {
                return DyedItemColor.getOrDefault(stack, ROBE_DEFAULT) | 0xFF000000;
            }

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (slot != EquipmentSlot.CHEST) {
                    return original;
                }
                if (model == null) {
                    model = new SkirtedRobeModel(Minecraft.getInstance().getEntityModels().bakeLayer(SkirtedRobeModel.LAYER));
                }
                return model;
            }

            @Override
            public void setupModelAnimations(LivingEntity entity, ItemStack stack, EquipmentSlot slot, Model armor, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (armor instanceof SkirtedRobeModel robe) {
                    robe.animateArmor(entity, ageInTicks);
                }
            }
        }, RobesFeature.BEWITCHED_ROBE.get());
        event.registerItem(new IClientItemExtensions() {
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return DyedItemColor.getOrDefault(stack, ROBE_DEFAULT) | 0xFF000000;
            }
        }, RobesFeature.BEWITCHED_LEGGINGS.get());
    }
}
