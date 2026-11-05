package witchinggadgets.client.primordial;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.primordial.PrimordialFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class PrimordialClient {
    private static final Map<EquipmentSlot, PrimordialArmorModel> MODELS = new EnumMap<>(EquipmentSlot.class);

    private PrimordialClient() {
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PrimordialArmorModel.LAYER, PrimordialArmorModel::createLayer);
        event.registerLayerDefinition(PrimordialArmorModel.BOOTS_LAYER, PrimordialArmorModel::createBootsLayer);
    }

    @SubscribeEvent
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        bake(event.getEntityModels());
    }

    private static void bake(EntityModelSet models) {
        MODELS.clear();
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}) {
            MODELS.put(slot, new PrimordialArmorModel(models.bakeLayer(PrimordialArmorModel.LAYER), slot));
        }
        MODELS.put(EquipmentSlot.FEET, new PrimordialArmorModel(models.bakeLayer(PrimordialArmorModel.BOOTS_LAYER), EquipmentSlot.FEET));
    }

    private static IClientItemExtensions extensions(EquipmentSlot slot) {
        return new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (MODELS.isEmpty()) {
                    bake(Minecraft.getInstance().getEntityModels());
                }
                return MODELS.get(slot);
            }

            @Override
            public void setupModelAnimations(LivingEntity entity, ItemStack stack, EquipmentSlot equipmentSlot, Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (model instanceof PrimordialArmorModel armor) {
                    armor.animateArmor(entity);
                }
            }
        };
    }

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(extensions(EquipmentSlot.HEAD), PrimordialFeature.PRIMORDIAL_HELMET.get());
        event.registerItem(extensions(EquipmentSlot.CHEST), PrimordialFeature.PRIMORDIAL_CHESTPLATE.get());
        event.registerItem(extensions(EquipmentSlot.LEGS), PrimordialFeature.PRIMORDIAL_LEGGINGS.get());
        event.registerItem(extensions(EquipmentSlot.FEET), PrimordialFeature.PRIMORDIAL_BOOTS.get());
    }
}
