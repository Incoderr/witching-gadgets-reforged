package witchinggadgets.devices;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.registry.ModEffects;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.fx.WgFx;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class SaunaEvents {
    private SaunaEvents() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (SaunaStoveBlockEntity.steaming(player) == null) {
            return;
        }
        RandomSource random = player.level().getRandom();
        if (player.level().isClientSide()) {
            if (random.nextInt(3) == 0) {
                WgFx.get().sweat(player);
            }
            return;
        }
        steam(player, random);
    }

    public static void steam(Player player, RandomSource random) {
        if (random.nextInt(100) == 0 && player.getFoodData().getFoodLevel() > 6) {
            player.getFoodData().eat(-1, 0.1F);
        }
        if (!player.isInWater()) {
            player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, 20, 0, true, true));
        }
        if (random.nextInt(200) == 0 && player instanceof ServerPlayer serverPlayer) {
            Warp.add(serverPlayer, -1, WarpType.TEMPORARY);
        }
    }
}
