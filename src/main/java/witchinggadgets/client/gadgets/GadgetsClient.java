package witchinggadgets.client.gadgets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.client.platform.WorldFxRenderer;
import thaumcraft.research.ScanManager;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gadgets.GadgetsFeature;
import witchinggadgets.gadgets.GuidePoint;
import witchinggadgets.gadgets.GuidingStringItem;
import witchinggadgets.gadgets.PhotoItem;
import witchinggadgets.gadgets.ScanSnapshot;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GadgetsClient {
    private static final Map<String, GuideBeamFx> BEAMS = new HashMap<>();

    private GadgetsClient() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            BEAMS.clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof GuidingStringItem)) {
            return;
        }
        List<GuidePoint> path = GuidingStringItem.path(held);
        if (path.size() < 2) {
            return;
        }
        for (int index = 1; index < path.size(); index++) {
            GuidePoint last = path.get(index - 1);
            GuidePoint point = path.get(index);
            String ident = Math.floor(point.x()) + "," + Math.floor(point.y()) + "," + Math.floor(point.z());
            beam(ident, point.x(), point.y() + 1, point.z(), last.x(), last.y() + 1, last.z());
        }
        if (GuidingStringItem.active(held)) {
            GuidePoint last = path.getLast();
            Vec3 hand = handPos(player);
            beam("player", hand.x, hand.y, hand.z, last.x(), last.y() + 1, last.z());
        }
        BEAMS.values().removeIf(GuideBeamFx::isDead);
    }

    private static void beam(String ident, double x, double y, double z, double targetX, double targetY, double targetZ) {
        GuideBeamFx beam = BEAMS.get(ident);
        if (beam == null || beam.isDead()) {
            beam = new GuideBeamFx(x, y, z, targetX, targetY, targetZ);
            WorldFxRenderer.add(beam);
            BEAMS.put(ident, beam);
        } else {
            beam.update(x, y, z, targetX, targetY, targetZ);
        }
    }

    private static Vec3 handPos(Player player) {
        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float yaw = player.yBodyRot * Mth.DEG_TO_RAD;
        double x = player.getX() - Mth.cos(yaw) * 0.35 * side;
        double z = player.getZ() - Mth.sin(yaw) * 0.35 * side;
        return new Vec3(x, player.getY() + player.getBbHeight() * 0.6, z);
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof PhotoItem)) {
            return;
        }
        ScanSnapshot scan = GadgetsFeature.SCAN.get(stack);
        Player player = event.getEntity();
        if (scan == null) {
            return;
        }
        List<Component> lines = event.getToolTip();
        lines.add(Component.literal(" ").append(scan.name()));
        if (Screen.hasControlDown()) {
            for (Aspect aspect : scan.aspects().getAspectsSorted()) {
                if (scan.aspects().getAmount(aspect) > 0) {
                    lines.add(Component.literal("  " + aspect.getName() + ": " + scan.aspects().getAmount(aspect)).withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            lines.add(Component.literal("  ").append(Component.translatable("wg.desc.ctrlForAspectList")));
        }
        if (player != null) {
            lines.add(Component.literal(" ").append(Component.translatable("wg.desc.scanCompleted")).append(": " + ScanManager.hasBeenScanned(player, scan.target())));
        }
    }
}
