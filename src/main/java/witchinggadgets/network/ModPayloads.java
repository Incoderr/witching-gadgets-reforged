package witchinggadgets.network;

import witchinggadgets.primordial.GauntletPayload;
import witchinggadgets.travellersgear.ArmorStandFlagsPayload;

public final class ModPayloads {
    private ModPayloads() {
    }

    public static void register() {
        GauntletPayload.register();
        ArmorStandFlagsPayload.register();
    }
}
