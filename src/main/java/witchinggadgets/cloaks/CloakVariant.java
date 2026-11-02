package witchinggadgets.cloaks;

public enum CloakVariant {
    STANDARD(0x1F2555, "cloak"),
    SPECTRAL(0x222222, "cloak"),
    STORAGE(0x888888, "cloak"),
    WOLF(0xFFFFFF, "cloakwolf");

    private final int color;
    private final String texture;

    CloakVariant(int color, String texture) {
        this.color = color;
        this.texture = texture;
    }

    public int color() {
        return color;
    }

    public String texture() {
        return texture;
    }
}
