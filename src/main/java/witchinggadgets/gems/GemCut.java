package witchinggadgets.gems;

import java.util.Locale;

public enum GemCut {
    POINT,
    OVAL;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static GemCut byIndex(int index) {
        return index >= 0 && index < values().length ? values()[index] : values()[0];
    }

    public static GemCut byId(String id) {
        for (GemCut cut : values()) {
            if (cut.id().equals(id)) {
                return cut;
            }
        }
        return POINT;
    }
}
