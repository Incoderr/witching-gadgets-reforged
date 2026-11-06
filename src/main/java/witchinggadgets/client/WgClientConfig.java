package witchinggadgets.client;

import net.neoforged.neoforge.common.ModConfigSpec;
import witchinggadgets.client.geom.CloakGeometry;

public final class WgClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CLOAK_ANIMATION = BUILDER
        .comment("0 = no animation, 1 = rotate cloak when legs move, 2 = stretch cloak when legs move")
        .defineInRange("cloakAnimationMode", CloakGeometry.ANIMATION_STRETCH, CloakGeometry.ANIMATION_NONE, CloakGeometry.ANIMATION_STRETCH);

    public static final ModConfigSpec.DoubleValue ABILITY_RADIAL_SPEED = BUILDER
        .comment("The speed at which the radial for active abilities opens. Default is 15% per tick, minimum is 5%, maximum is 100%")
        .defineInRange("abilityRadialSpeed", 0.15, 0.05, 1.0);

    public static final ModConfigSpec.BooleanValue LIMIT_BOOK_SEARCH = BUILDER
        .comment("Thaumonomicon Search to currently active category")
        .define("limitBookSearch", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private WgClientConfig() {
    }

    public static int cloakAnimation() {
        return SPEC.isLoaded() ? CLOAK_ANIMATION.getAsInt() : CLOAK_ANIMATION.getDefault();
    }

    public static float abilityRadialSpeed() {
        return (float) (SPEC.isLoaded() ? ABILITY_RADIAL_SPEED.getAsDouble() : ABILITY_RADIAL_SPEED.getDefault());
    }

    public static boolean limitBookSearch() {
        return SPEC.isLoaded() ? LIMIT_BOOK_SEARCH.getAsBoolean() : LIMIT_BOOK_SEARCH.getDefault();
    }
}
