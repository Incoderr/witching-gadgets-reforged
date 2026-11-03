BAGS = {
    "bag_of_tricks": (0x8A74BD, None),
    "void_bag": (0x484848, "bagoverlay_void"),
    "ender_bag": (0x2D4741, "bagoverlay_ender"),
    "hungry_bag": (0x855172, "bagoverlay_hungry"),
}


def generate(ctx):
    for name, (color, overlay) in BAGS.items():
        layers = ["bag"] if overlay is None else ["bag", overlay]
        ctx.generated_item(name, layers, [{"type": "minecraft:dye", "default": ctx.tint(color)["value"]}])
        ctx.item_tags["minecraft:dyeable"].add(f"witchinggadgets:{name}")
