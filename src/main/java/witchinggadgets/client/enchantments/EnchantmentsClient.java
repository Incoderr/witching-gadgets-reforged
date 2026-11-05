package witchinggadgets.client.enchantments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import thaumcraft.fx.Fx;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.enchantments.EnchantmentsFeature;

import java.util.IdentityHashMap;
import java.util.Map;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class EnchantmentsClient {
    private static final Map<Player, ItemStack[]> HIDDEN = new IdentityHashMap<>();

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        NonNullList<ItemStack> armor = player.getInventory().armor;
        ItemStack[] saved = null;
        for (int slot = 0; slot < armor.size(); slot++) {
            if (hidesGear(player, armor.get(slot))) {
                if (saved == null) {
                    saved = new ItemStack[armor.size()];
                }
                saved[slot] = armor.get(slot);
                armor.set(slot, ItemStack.EMPTY);
            }
        }
        if (saved != null) {
            HIDDEN.put(player, saved);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        ItemStack[] saved = HIDDEN.remove(event.getEntity());
        if (saved == null) {
            return;
        }
        NonNullList<ItemStack> armor = event.getEntity().getInventory().armor;
        for (int slot = 0; slot < saved.length; slot++) {
            if (saved[slot] != null) {
                armor.set(slot, saved[slot]);
            }
        }
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
