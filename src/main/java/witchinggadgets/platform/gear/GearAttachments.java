package witchinggadgets.platform.gear;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;
import witchinggadgets.WitchingGadgets;

public final class GearAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WitchingGadgets.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GearInventory>> GEAR = ATTACHMENTS.register("gear", () -> AttachmentType
        .serializable(holder -> new GearInventory((Player) holder))
        .copyOnDeath()
        .sync(new AttachmentSyncHandler<GearInventory>() {
            @Override
            public void write(RegistryFriendlyByteBuf buffer, GearInventory attachment, boolean initialSync) {
                attachment.write(buffer);
            }

            @Override
            public @Nullable GearInventory read(IAttachmentHolder holder, RegistryFriendlyByteBuf buffer, @Nullable GearInventory previousValue) {
                GearInventory inventory = previousValue != null ? previousValue : new GearInventory((Player) holder);
                inventory.read(buffer);
                return inventory;
            }
        })
        .build());

    private GearAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static GearInventory inventory(Player player) {
        return player.getData(GEAR);
    }
}
