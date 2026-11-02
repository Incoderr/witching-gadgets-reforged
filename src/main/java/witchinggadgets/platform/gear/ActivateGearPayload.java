package witchinggadgets.platform.gear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.cloaks.CloakEvents;
import witchinggadgets.gear.GearAbility;
import witchinggadgets.platform.WgNetwork;

public record ActivateGearPayload() implements CustomPacketPayload {
    public static final Type<ActivateGearPayload> TYPE = new Type<>(WitchingGadgets.id("activate_gear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActivateGearPayload> CODEC = StreamCodec.unit(new ActivateGearPayload());

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, ActivateGearPayload::handle);
    }

    private static void handle(ActivateGearPayload payload, Player player) {
        ItemStack cloak = CloakEvents.activeCloak(player);
        if (cloak.getItem() instanceof GearAbility ability && ability.canActivate(player, cloak)) {
            ability.activate(player, cloak);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
