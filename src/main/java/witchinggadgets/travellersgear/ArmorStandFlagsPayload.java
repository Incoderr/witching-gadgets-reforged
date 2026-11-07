package witchinggadgets.travellersgear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

public record ArmorStandFlagsPayload(int containerId, int flags) implements CustomPacketPayload {
    public static final Type<ArmorStandFlagsPayload> TYPE = new Type<>(WitchingGadgets.id("armor_stand_flags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorStandFlagsPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ArmorStandFlagsPayload::containerId,
        ByteBufCodecs.VAR_INT, ArmorStandFlagsPayload::flags,
        ArmorStandFlagsPayload::new
    );

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, ArmorStandFlagsPayload::handle);
    }

    private static void handle(ArmorStandFlagsPayload payload, Player player) {
        if (player.containerMenu instanceof ArmorStandMenu menu && menu.containerId == payload.containerId() && menu.stillValid(player) && menu.blockEntity() != null) {
            menu.blockEntity().setFlags(payload.flags());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
