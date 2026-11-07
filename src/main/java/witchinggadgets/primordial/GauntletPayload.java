package witchinggadgets.primordial;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

public record GauntletPayload(int action) implements CustomPacketPayload {
    public static final Type<GauntletPayload> TYPE = new Type<>(WitchingGadgets.id("gauntlet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GauntletPayload> CODEC = ByteBufCodecs.VAR_INT.map(GauntletPayload::new, GauntletPayload::action).cast();

    public static final int OPEN_MENU = -1;

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, GauntletPayload::handle);
    }

    private static void handle(GauntletPayload payload, Player player) {
        ItemStack gauntlet = player.getMainHandItem();
        if (!(gauntlet.getItem() instanceof PrimordialGauntletItem)) {
            return;
        }
        if (payload.action() == OPEN_MENU) {
            if (player instanceof ServerPlayer serverPlayer) {
                GauntletMenu.open(serverPlayer, InteractionHand.MAIN_HAND);
            }
        } else if (payload.action() >= 0 && payload.action() < PrimordialGauntletItem.SLOTS) {
            PrimordialGauntletItem.select(gauntlet, payload.action());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
