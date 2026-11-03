package witchinggadgets.bags;

public enum BagKind {
    TRICKS(0x8A74BD),
    VOID(0x484848),
    ENDER(0x2D4741),
    HUNGRY(0x855172);

    private final int color;

    BagKind(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
