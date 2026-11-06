package witchinggadgets.client;

import net.neoforged.neoforge.common.ModConfigSpec;
import witchinggadgets.client.geom.CloakGeometry;

public final class WgClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CLOAK_ANIMATION = BUILDER
        .comment("0 = no animation, 1 = rotate cloak when legs move, 2 = stretch cloak when legs move")
        .defineInRange("cloakAnimationMode", CloakGeometry.ANIMATION_STRETCH, CloakGeometry.ANIMATION_NONE, CloakGeometry.ANIMATION_STRETCH);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private WgClientConfig() {
    }

    public static int cloakAnimation() {
        return SPEC.isLoaded() ? CLOAK_ANIMATION.getAsInt() : CLOAK_ANIMATION.getDefault();
    }
}
