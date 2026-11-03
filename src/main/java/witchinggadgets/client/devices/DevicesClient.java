package witchinggadgets.client.devices;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import thaumcraft.client.fx.ClientFx;
import thaumcraft.client.fx.WispFx;
import thaumcraft.client.platform.WorldFxRenderer;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.platform.WgGuiBlit;
import witchinggadgets.devices.SaunaStoveBlockEntity;
import witchinggadgets.fx.WgFx;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class DevicesClient implements WgFx {
    private static final Identifier STEAM = WitchingGadgets.id("textures/gui/steam_overlay.png");

    private DevicesClient() {
    }

    public static void init() {
        WgFx.set(new DevicesClient());
    }

    @Override
    public void saunaSteam(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (random.nextInt(9 - ClientFx.particleCount(2)) != 0) {
            return;
        }
        WispFx fx = new WispFx(random, pos.getX() + 0.5, pos.getY() + 0.875, pos.getZ() + 0.5, 0.5F, 4, null);
        fx.moveTo(pos.getX() + 0.3 + random.nextFloat() * 0.4, pos.getY() + 0.5, pos.getZ() + 0.3 + random.nextFloat() * 0.4);
        fx.shrink(true);
        fx.gravity(-0.025F);
        WorldFxRenderer.add(fx);
    }

    @Override
    public void sweat(Player player) {
        RandomSource random = player.getRandom();
        player.level().addParticle(
            ParticleTypes.FALLING_WATER,
            player.getX() - 0.2 + random.nextFloat() / 2.0,
            player.getBoundingBox().minY + 0.5 + random.nextFloat(),
            player.getZ() - 0.2 + random.nextFloat() / 2.0,
            0.0,
            0.0,
            0.0
        );
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, WitchingGadgets.id("sauna_steam"), DevicesClient::renderSteam);
    }

    private static void renderSteam(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        if (player == null || SaunaStoveBlockEntity.steaming(player) == null) {
            return;
        }
        WgGuiBlit.stretch(graphics, STEAM, 0, 0, graphics.guiWidth(), graphics.guiHeight(), 256, 256);
    }
}
