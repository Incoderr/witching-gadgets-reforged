package witchinggadgets.network;

import witchinggadgets.primordial.GauntletPayload;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        GauntletPayload.register();
    }
}
