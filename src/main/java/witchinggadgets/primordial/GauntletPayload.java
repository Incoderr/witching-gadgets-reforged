package witchinggadgets.primordial;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

public record GauntletPayload(boolean openMenu) implements CustomPacketPayload {
    public static final Type<GauntletPayload> TYPE = new Type<>(WitchingGadgets.id("gauntlet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GauntletPayload> CODEC = ByteBufCodecs.BOOL.map(GauntletPayload::new, GauntletPayload::openMenu).cast();

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, GauntletPayload::handle);
    }

    private static void handle(GauntletPayload payload, Player player) {
        ItemStack gauntlet = player.getMainHandItem();
        if (!(gauntlet.getItem() instanceof PrimordialGauntletItem)) {
            return;
        }
        if (payload.openMenu()) {
            if (player instanceof ServerPlayer serverPlayer) {
                GauntletMenu.open(serverPlayer, InteractionHand.MAIN_HAND);
            }
            return;
        }
        int next = (PrimordialGauntletItem.selected(gauntlet) + 1) % PrimordialGauntletItem.SLOTS;
        PrimordialGauntletItem.select(gauntlet, next);
        ItemStack gem = PrimordialGauntletItem.gems(gauntlet).get(next);
        Component name = gem.isEmpty() ? Component.translatable("wg.chat.gauntlet.empty") : gem.getHoverName();
        player.displayClientMessage(Component.translatable("wg.chat.gauntlet.selected", next + 1, name), true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
