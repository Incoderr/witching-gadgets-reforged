BAG_DEFAULT = 0x8A74BD


def generate(ctx):
    ctx.generated_item("bag_of_tricks", "bag", [{"type": "minecraft:dye", "default": ctx.tint(BAG_DEFAULT)["value"]}])
    ctx.item_tags["minecraft:dyeable"].add("witchinggadgets:bag_of_tricks")
