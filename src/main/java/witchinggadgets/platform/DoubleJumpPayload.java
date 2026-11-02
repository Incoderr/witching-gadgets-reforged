package witchinggadgets.platform;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.baubles.BaubleEffects;
import witchinggadgets.baubles.BaublesFeature;
import witchinggadgets.gear.GearSlot;

public record DoubleJumpPayload() implements CustomPacketPayload {
    public static final Type<DoubleJumpPayload> TYPE = new Type<>(WitchingGadgets.id("double_jump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpPayload> CODEC = StreamCodec.unit(new DoubleJumpPayload());

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, DoubleJumpPayload::handle);
    }

    private static void handle(DoubleJumpPayload payload, Player player) {
        if (BaubleEffects.wearsGear(player, GearSlot.PAULDRON, BaublesFeature.DOUBLE_JUMP_SHOULDERS.get())) {
            player.resetFallDistance();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
