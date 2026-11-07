package witchinggadgets.platform.gear;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.platform.Gear;
import witchinggadgets.platform.WgNetwork;
import witchinggadgets.travellersgear.ToolDisplay;

public record SetToolDisplayPayload(List<ToolDisplay> displays) implements CustomPacketPayload {
    public static final Type<SetToolDisplayPayload> TYPE = new Type<>(WitchingGadgets.id("set_tool_display"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetToolDisplayPayload> CODEC = ToolDisplay.LIST_STREAM_CODEC.map(SetToolDisplayPayload::new, SetToolDisplayPayload::displays);

    public static void register() {
        WgNetwork.toServer(TYPE, CODEC, SetToolDisplayPayload::handle);
    }

    private static void handle(SetToolDisplayPayload payload, Player player) {
        List<ToolDisplay> displays = new ArrayList<>();
        for (ToolDisplay display : payload.displays()) {
            if (displays.size() < ToolDisplay.MAX) {
                displays.add(display.sanitized());
            }
        }
        Gear.setToolDisplay(player, displays);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
