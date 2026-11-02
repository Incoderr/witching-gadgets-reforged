package witchinggadgets.client.baubles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.common.CommonHooks;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaubleEffects;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.client.platform.WgClientNetwork;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.DoubleJumpPayload;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class BaubleClientEvents {
    private static boolean jumpHeld;
    private static int airJumps;

    private BaubleClientEvents() {
    }

    // Seraph's Shoulders give one extra jump in the air like the WG key handler.
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        if (player.onGround() || player.onClimbable() || player.isInWater() || player.getAbilities().flying) {
            airJumps = BaubleEffects.wearsGear(player, GearSlot.PAULDRON, BaublesFeature.DOUBLE_JUMP_SHOULDERS.get()) ? 1 : 0;
        }
        boolean pressed = minecraft.options.keyJump.isDown() && minecraft.gui.screen() == null;
        if (pressed && !jumpHeld && !player.onGround() && airJumps > 0 && !player.getAbilities().flying) {
            double motion = 0.42;
            if (player.hasEffect(MobEffects.JUMP_BOOST)) {
                motion += (player.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
            }
            Vec3 movement = player.getDeltaMovement();
            player.setDeltaMovement(movement.x, motion, movement.z);
            player.resetFallDistance();
            CommonHooks.onLivingJump(player);
            WgClientNetwork.sendToServer(new DoubleJumpPayload());
            airJumps--;
        }
        jumpHeld = pressed;
    }

    @SubscribeEvent
    static void onFov(ComputeFovModifierEvent event) {
        if (event.getPlayer().isShiftKeyDown() && BaubleEffects.isDrawingBow(event.getPlayer())
            && BaubleEffects.wearsRing(event.getPlayer(), BaublesFeature.SNIPER_RING.get())) {
            event.setNewFovModifier(0.25F);
        }
    }
}
