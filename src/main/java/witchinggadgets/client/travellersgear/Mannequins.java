package witchinggadgets.client.travellersgear;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

public final class Mannequins {
    private static final GameProfile PROFILE = new GameProfile(Util.NIL_UUID, "TG_ArmorStand");
    private static final int ID = Integer.MIN_VALUE + 7;
    @Nullable
    private static RemotePlayer mannequin;

    private Mannequins() {
    }

    public static RemotePlayer of(ClientLevel level) {
        if (mannequin == null || mannequin.level() != level) {
            mannequin = new RemotePlayer(level, PROFILE);
            mannequin.setId(ID);
            mannequin.setInvisible(true);
        }
        return mannequin;
    }

    public static boolean is(Entity entity) {
        return mannequin != null && entity == mannequin;
    }
}
