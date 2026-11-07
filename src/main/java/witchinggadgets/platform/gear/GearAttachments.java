package witchinggadgets.platform.gear;

import java.util.List;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.travellersgear.ToolDisplay;

public final class GearAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WitchingGadgets.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<ToolDisplay>>> TOOL_DISPLAY = ATTACHMENTS.register("tool_display", () -> AttachmentType
        .<List<ToolDisplay>>builder(() -> List.of())
        .serialize(ToolDisplay.LIST_CODEC)
        .copyOnDeath()
        .sync(ToolDisplay.LIST_STREAM_CODEC)
        .build());

    private GearAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
