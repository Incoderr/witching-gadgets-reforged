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
import net.minecraft.world.phys.Vec3;
import thaumcraft.aspect.Aspect;
import thaumcraft.fx.Fx;
import thaumcraft.node.Node;
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

    @Override
    public void glimmer(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int x1 = pos.getX() + random.nextInt(2) - random.nextInt(2);
        int y1 = pos.getY() + random.nextInt(2) - random.nextInt(2);
        int z1 = pos.getZ() + random.nextInt(2) - random.nextInt(2);
        int x2 = x1 + random.nextInt(2) - random.nextInt(2);
        int y2 = y1 + random.nextInt(2) - random.nextInt(2);
        int z2 = z1 + random.nextInt(2) - random.nextInt(2);
        WispFx fx = new WispFx(random, x1, y1, z1, 0.1F + random.nextFloat() * 0.1F, 7, null);
        fx.moveTo(x2, y2, z2);
        fx.gravity(random.nextBoolean() ? -0.033F : 0.033F);
        WorldFxRenderer.add(fx);
    }

    @Override
    public void gemFizz(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        for (int index = 0; index < 8; index++) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void gemOres(Level level, BlockPos pos) {
        Fx.get().oreScan(level, pos, 5, System.currentTimeMillis() + 50000L);
    }

    @Override
    public void gemNodeWisp(Level level, Player player, BlockPos node) {
        int color = 0xFFFFFF;
        if (level.getBlockEntity(node) instanceof Node source) {
            for (Aspect aspect : source.getAspects().getAspects()) {
                color = blend(color, aspect.getColor());
            }
        }
        Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.5)).subtract(0.0, 0.3, 0.0);
        WispFx fx = new WispFx(level.getRandom(), hand.x, hand.y, hand.z, 0.5F, 0, null);
        fx.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
        fx.moveTo(node.getX() + 0.5, node.getY() + 0.5, node.getZ() + 0.5);
        WorldFxRenderer.add(fx);
    }

    private static int blend(int first, int second) {
        int red = ((first >> 16 & 255) + (second >> 16 & 255)) / 2;
        int green = ((first >> 8 & 255) + (second >> 8 & 255)) / 2;
        int blue = ((first & 255) + (second & 255)) / 2;
        return red << 16 | green << 8 | blue;
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
