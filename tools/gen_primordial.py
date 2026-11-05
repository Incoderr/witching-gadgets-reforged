import shutil

PIECES = [
    ("primordial_helmet", 0, "minecraft:head_armor"),
    ("primordial_chestplate", 1, "minecraft:chest_armor"),
    ("primordial_leggings", 2, "minecraft:leg_armor"),
    ("primordial_boots", 3, "minecraft:foot_armor"),
]


def generate(ctx):
    for name, index, tag in PIECES:
        ctx.generated_item(name, ["primordialarmorrune", f"primordialarmor{index}"], [{"type": f"{ctx.NS}:primordial_ability"}])
        ctx.item_tags[tag].add(f"{ctx.NS}:{name}")
    layers = {}
    for layer in ("humanoid", "humanoid_leggings"):
        target = ctx.ASSETS / "textures" / "entity" / "equipment" / layer
        target.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ctx.SOURCE_TEXTURES / "models" / "primordialarmor.png", target / "primordial_armor.png")
        layers[layer] = [{"texture": f"{ctx.NS}:primordial_armor"}]
    ctx.write(ctx.ASSETS / "equipment" / "primordial_armor.json", {"layers": layers})
