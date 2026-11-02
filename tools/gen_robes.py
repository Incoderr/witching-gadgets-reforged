import shutil

ROBE_DEFAULT = 6961280
LAYERS = {
    "humanoid": "advancedrobes_1",
    "humanoid_leggings": "advancedrobes_2",
}
ITEMS = {
    "bewitched_robe": "chestplaterobeadvanced",
    "bewitched_leggings": "leggingsrobeadvanced",
}


def generate(ctx):
    dye = [{"type": "minecraft:dye", "default": ctx.tint(ROBE_DEFAULT)["value"]}]
    for name, texture in ITEMS.items():
        ctx.generated_item(name, [texture, f"{texture}_overlay"], dye)
        ctx.item_tags["minecraft:dyeable"].add(f"witchinggadgets:{name}")
    layers = {}
    for layer, texture in LAYERS.items():
        target = ctx.ASSETS / "textures" / "entity" / "equipment" / layer
        target.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ctx.SOURCE_TEXTURES / "models" / f"{texture}.png", target / "bewitched_robe.png")
        shutil.copyfile(ctx.SOURCE_TEXTURES / "models" / f"{texture}_overlay.png", target / "bewitched_robe_overlay.png")
        layers[layer] = [
            {"texture": "witchinggadgets:bewitched_robe", "dyeable": {"color_when_undyed": ROBE_DEFAULT}},
            {"texture": "witchinggadgets:bewitched_robe_overlay"},
        ]
    ctx.write(ctx.ASSETS / "equipment" / "bewitched_robe.json", {"layers": layers})
