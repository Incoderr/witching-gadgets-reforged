package witchinggadgets.platform;

public final class BaseConfig {
    private BaseConfig() {
    }

    public static boolean allowMirrors() {
        return thaumcraft.Config.allowMirrors();
    }
}
