package witchinggadgets.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import witchinggadgets.platform.WgNetwork;

public final class WgClientNetwork {
    private WgClientNetwork() {
    }

    public static <T extends CustomPacketPayload> void handle(CustomPacketPayload.Type<T> type, WgNetwork.Handler<T> handler) {
        WgNetwork.setClientHandler(type, payload -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                handler.handle(payload, player);
            }
        });
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
