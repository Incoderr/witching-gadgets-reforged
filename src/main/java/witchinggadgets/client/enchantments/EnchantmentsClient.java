package witchinggadgets.client.enchantments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import thaumcraft.fx.Fx;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.enchantments.EnchantmentsFeature;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class EnchantmentsClient {
    private EnchantmentsClient() {
    }

    public static boolean hidesGear(LivingEntity wearer, ItemStack stack) {
        int translucency = EnchantmentsFeature.level(stack, EnchantmentsFeature.INVISIBLE_GEAR);
        if (translucency > 1 || translucency > 0 && wearer.isInvisible()) {
            Player viewer = Minecraft.getInstance().player;
            return viewer == null || wearer == viewer || !EnchantmentsFeature.unveils(viewer);
        }
        return false;
    }

    private static ItemStack visible(LivingEntity wearer, EquipmentSlot slot, ItemStack shown) {
        return hidesGear(wearer, wearer.getItemBySlot(slot)) ? ItemStack.EMPTY : shown;
    }

    @SubscribeEvent
    static void onRegisterModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                state.headEquipment = visible(avatar, EquipmentSlot.HEAD, state.headEquipment);
                state.chestEquipment = visible(avatar, EquipmentSlot.CHEST, state.chestEquipment);
                state.legsEquipment = visible(avatar, EquipmentSlot.LEGS, state.legsEquipment);
                state.feetEquipment = visible(avatar, EquipmentSlot.FEET, state.feetEquipment);
            }
        });
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player viewer = minecraft.player;
        if (level == null || viewer == null || minecraft.isPaused() || !EnchantmentsFeature.unveils(viewer)) {
            return;
        }
        for (Player player : level.players()) {
            if (player != viewer && player.isInvisibleTo(viewer)) {
                RandomSource random = player.getRandom();
                for (int count = 0; count < 3; count++) {
                    float x = (float) player.getX() + random.nextFloat() - 0.5F;
                    float y = (float) player.getY() + 1.0F + random.nextFloat() - 0.5F;
                    float z = (float) player.getZ() + random.nextFloat() - 0.5F;
                    Fx.get().sparkle(level, x, y, z, 1.0F, 0, 0.0F);
                }
            }
        }
    }
}
