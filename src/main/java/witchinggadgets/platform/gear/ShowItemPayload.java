package witchinggadgets.platform.gear;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

public record ShowItemPayload(int slot) implements CustomPacketPayload {
    public static final Type<ShowItemPayload> TYPE = new Type<>(WitchingGadgets.id("show_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowItemPayload> CODEC = ByteBufCodecs.VAR_INT.<RegistryFriendlyByteBuf>cast().map(ShowItemPayload::new, ShowItemPayload::slot);
    private static final long COOLDOWN = 20L;
    private static final Map<UUID, Long> LAST = new HashMap<>();

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, ShowItemPayload::handle);
    }

    private static void handle(ShowItemPayload payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(player.containerMenu instanceof GearMenu menu)) {
            return;
        }
        if (payload.slot() < 0 || payload.slot() >= menu.slots.size()) {
            return;
        }
        Slot slot = menu.slots.get(payload.slot());
        if (!slot.isActive() || !slot.hasItem()) {
            return;
        }
        long time = player.level().getGameTime();
        Long last = LAST.get(player.getUUID());
        if (last != null && time - last < COOLDOWN) {
            return;
        }
        LAST.put(player.getUUID(), time);
        serverPlayer.level().getServer().getPlayerList().broadcastSystemMessage(Component.translatable("TG.chattext.showItem", player.getDisplayName(), slot.getItem().getDisplayName()), false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
