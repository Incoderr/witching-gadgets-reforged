DYE_DEFAULT = 0x1F2555
CLOAKS = {
    "cloak": ("cloak", "dye"),
    "spectral_cloak": ("cloak", 0x222222),
    "storage_cloak": ("cloak", 0x888888),
    "wolf_cloak": ("cloak_wolf", None),
}
KAMAS = {
    "kama": (["kama", "kama_overlay"], "dye"),
    "spectral_kama": (["kama", "kama_overlay"], 0x222222),
    "storage_kama": (["kama", "kama_overlay"], 0x888888),
    "wolf_kama": ("kama_wolf", None),
}


def tints(ctx, tint):
    if tint is None:
        return None
    if tint == "dye":
        value = ctx.tint(DYE_DEFAULT)["value"]
        return [{"type": "minecraft:dye", "default": value}]
    return [ctx.tint(tint)]


def generate(ctx):
    for table in (CLOAKS, KAMAS):
        for name, (layers, tint) in table.items():
            ctx.generated_item(name, layers, tints(ctx, tint))
    ctx.item_tags["minecraft:dyeable"].update({"witchinggadgets:cloak", "witchinggadgets:kama"})
