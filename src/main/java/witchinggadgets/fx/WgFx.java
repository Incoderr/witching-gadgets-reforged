package witchinggadgets.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface WgFx {
    default void saunaSteam(Level level, BlockPos pos) {
    }

    default void sweat(Player player) {
    }

    static WgFx get() {
        return Holder.fx;
    }

    static void set(WgFx fx) {
        Holder.fx = fx;
    }

    final class Holder {
        private static WgFx fx = new WgFx() {
        };

        private Holder() {
        }
    }
}
