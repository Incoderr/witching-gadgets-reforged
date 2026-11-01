package witchinggadgets.client.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.WgNetwork;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgClientNetwork {
    private static final List<Consumer<RegisterClientPayloadHandlersEvent>> HANDLERS = new ArrayList<>();

    private WgClientNetwork() {
    }

    public static <T extends CustomPacketPayload> void handle(CustomPacketPayload.Type<T> type, WgNetwork.Handler<T> handler) {
        HANDLERS.add(event -> event.register(type, (payload, context) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                handler.handle(payload, player);
            }
        }));
    }

    @SubscribeEvent
    static void register(RegisterClientPayloadHandlersEvent event) {
        for (Consumer<RegisterClientPayloadHandlersEvent> handler : HANDLERS) {
            handler.accept(event);
        }
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
