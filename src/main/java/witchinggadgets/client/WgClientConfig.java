package witchinggadgets.client;

import net.neoforged.neoforge.common.ModConfigSpec;
import witchinggadgets.client.geom.CloakGeometry;

public final class WgClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CLOAK_ANIMATION = BUILDER
        .comment("0 = no animation, 1 = rotate cloak when legs move, 2 = stretch cloak when legs move")
        .defineInRange("cloakAnimationMode", CloakGeometry.ANIMATION_STRETCH, CloakGeometry.ANIMATION_NONE, CloakGeometry.ANIMATION_STRETCH);

    public static final ModConfigSpec.DoubleValue GAUNTLET_X = gauntlet("x", 0.58);
    public static final ModConfigSpec.DoubleValue GAUNTLET_Y = gauntlet("y", -0.476);
    public static final ModConfigSpec.DoubleValue GAUNTLET_Z = gauntlet("z", -0.658);
    public static final ModConfigSpec.DoubleValue GAUNTLET_ROT_X = gauntlet("rotX", 8.53);
    public static final ModConfigSpec.DoubleValue GAUNTLET_ROT_Y = gauntlet("rotY", -23.04);
    public static final ModConfigSpec.DoubleValue GAUNTLET_ROT_Z = gauntlet("rotZ", 82.74);
    public static final ModConfigSpec.DoubleValue GAUNTLET_SCALE = gauntlet("scale", 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static ModConfigSpec.DoubleValue gauntlet(String name, double value) {
        return BUILDER.comment("First person primordial gauntlet pose for the right hand (blocks, degrees), the left hand is mirrored")
            .defineInRange("gauntletFirstPerson." + name, value, -360.0, 360.0);
    }

    public static float gauntlet(ModConfigSpec.DoubleValue value) {
        return (float) (SPEC.isLoaded() ? value.getAsDouble() : value.getDefault());
    }

    private WgClientConfig() {
    }

    public static int cloakAnimation() {
        return SPEC.isLoaded() ? CLOAK_ANIMATION.getAsInt() : CLOAK_ANIMATION.getDefault();
    }
}
