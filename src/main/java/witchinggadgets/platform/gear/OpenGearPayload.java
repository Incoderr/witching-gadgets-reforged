package witchinggadgets.platform.gear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

public record OpenGearPayload(long hidden) implements CustomPacketPayload {
    public static final Type<OpenGearPayload> TYPE = new Type<>(WitchingGadgets.id("open_gear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGearPayload> CODEC = ByteBufCodecs.VAR_LONG.<RegistryFriendlyByteBuf>cast().map(OpenGearPayload::new, OpenGearPayload::hidden);

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, OpenGearPayload::handle);
    }

    private static void handle(OpenGearPayload payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, user) -> new GearMenu(containerId, inventory, payload.hidden()), Component.translatable("container.crafting")), buffer -> buffer.writeVarLong(payload.hidden()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
