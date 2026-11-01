package witchinggadgets.gear;

public enum GearSlot {
    CLOAK,
    PAULDRON,
    VAMBRACE,
    TITLE;

    public String descriptionKey() {
        return "wg.desc.gearSlot.tg." + ordinal();
    }
}
