import shutil

import pngtools

PIECES = [
    ("primordial_helmet", 0, "minecraft:head_armor"),
    ("primordial_chestplate", 1, "minecraft:chest_armor"),
    ("primordial_leggings", 2, "minecraft:leg_armor"),
    ("primordial_boots", 3, "minecraft:foot_armor"),
]


WEAPONS = {
    "primordial_sword": "primordialsword",
    "primordial_hammer": "primordialhammer",
    "primordial_axe": "primordialaxe",
}
EFFECTS = {"dissolve": (36, 198), "cinder_coat": (54, 198)}
HAMMER_MINEABLE = ["#minecraft:mineable/pickaxe", "#minecraft:mineable/shovel", "#c:glass_blocks", "#minecraft:ice"]
GREATAXE_MINEABLE = ["#minecraft:mineable/axe", "#minecraft:leaves", "#minecraft:replaceable_by_trees"]


def effect_icons(ctx):
    source = ctx.ROOT / "src" / "main" / "resources" / "assets" / ctx.NS / "textures" / "gui" / "potioneffects.png"
    width, height, rows = pngtools.read_rgba(source)
    for name, (x, y) in EFFECTS.items():
        cropped = [row[x * 4:(x + 18) * 4] for row in rows[y:y + 18]]
        target = ctx.ASSETS / "textures" / "mob_effect" / f"{name}.png"
        target.parent.mkdir(parents=True, exist_ok=True)
        pngtools.write_rgba(target, 18, 18, cropped)


def generate(ctx):
    tint = [{"type": "minecraft:constant", "value": -1}, {"type": f"{ctx.NS}:primordial_ability"}]
    for name, texture in WEAPONS.items():
        ctx.item_model(name, {"parent": "minecraft:item/handheld", "textures": {
            "layer0": ctx.ref(texture, "item"),
            "layer1": ctx.ref(f"{texture}_overlay", "item"),
        }})
        ctx.item_definition(name, ctx.ref(name, "item"), tint)
    ctx.item_tags["minecraft:swords"].add(f"{ctx.NS}:primordial_sword")
    ctx.item_tags["minecraft:pickaxes"].add(f"{ctx.NS}:primordial_hammer")
    ctx.item_tags["minecraft:axes"].add(f"{ctx.NS}:primordial_axe")
    ctx.block_tags[f"{ctx.NS}:primordial_hammer_mineable"].update(HAMMER_MINEABLE)
    ctx.block_tags[f"{ctx.NS}:primordial_greataxe_mineable"].update(GREATAXE_MINEABLE)
    effect_icons(ctx)
    ctx.item_model("primordial_gauntlet", {
        "parent": "minecraft:item/generated",
        "textures": {"particle": "witchinggadgets:item/primordialarmorrune"},
        "display": {
            "gui": {"rotation": [-161.94, -34.26, -38.54], "translation": [0.28, -0.26, -0.31], "scale": [1.1562, 1.3489, 1.1562]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.375, 0.4375, 0.375]},
            "fixed": {"rotation": [-161.94, -34.26, -38.54], "translation": [0.28, -0.26, -0.31], "scale": [1.1562, 1.3489, 1.1562]},
            "thirdperson_righthand": {"rotation": [0, -90, 90], "translation": [0, -2, 5.6], "scale": [1, 1, 1]},
            "thirdperson_lefthand": {"rotation": [0, -90, 90], "translation": [0, -2, 5.6], "scale": [1, 1, 1]},
        },
    })
    ctx.write(ctx.ASSETS / "items" / "primordial_gauntlet.json", {"model": {
        "type": "minecraft:special",
        "base": ctx.ref("primordial_gauntlet", "item"),
        "model": {"type": f"{ctx.NS}:primordial_gauntlet"},
    }})

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
