package witchinggadgets.platform;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

// Data-driven registry lookups for code that has no level at hand (recipe outputs).
public final class WgRegistries {
    private WgRegistries() {
    }

    public static Optional<Holder.Reference<Enchantment>> enchantment(ResourceKey<Enchantment> key) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Optional.empty();
        }
        return server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }
}
