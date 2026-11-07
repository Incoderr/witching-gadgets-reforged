package witchinggadgets.client.travellersgear;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class Mannequins {
    private static final GameProfile PROFILE = new GameProfile(Util.NIL_UUID, "TG_ArmorStand");
    private static @Nullable RemotePlayer mannequin;

    private Mannequins() {
    }

    public static RemotePlayer of(ClientLevel level) {
        if (mannequin == null || mannequin.level() != level) {
            mannequin = new RemotePlayer(level, PROFILE);
        }
        return mannequin;
    }

    public static boolean is(int id) {
        return mannequin != null && mannequin.getId() == id;
    }

    public static @Nullable Entity entity(int id) {
        if (is(id)) {
            return mannequin;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.getEntity(id);
    }
}
