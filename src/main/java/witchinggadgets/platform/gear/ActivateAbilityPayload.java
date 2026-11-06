package witchinggadgets.platform.gear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gear.AbilitySlots;
import witchinggadgets.platform.WgNetwork;

public record ActivateAbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<ActivateAbilityPayload> TYPE = new Type<>(WitchingGadgets.id("activate_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateAbilityPayload> CODEC = ByteBufCodecs.VAR_INT.<RegistryFriendlyByteBuf>cast().map(ActivateAbilityPayload::new, ActivateAbilityPayload::slot);

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, ActivateAbilityPayload::handle);
    }

    private static void handle(ActivateAbilityPayload payload, Player player) {
        AbilitySlots.activate(player, payload.slot());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
