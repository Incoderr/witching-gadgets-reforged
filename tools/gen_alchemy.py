import json

WALLS = {
    "north": (False, False),
    "south": (False, True),
    "west": (True, False),
    "east": (True, True),
}
PUMP_FACINGS = ("north", "east", "south", "west")


def box_element(box, invert, mirror, texture):
    x0, y0, z0, x1, y1, z1 = box
    if mirror:
        x0, x1, z0, z1 = 16 - x0, 16 - x1, 16 - z0, 16 - z1
    if invert:
        x0, z0, x1, z1 = z0, x0, z1, x1
    return {
        "from": [min(x0, x1), min(y0, y1), min(z0, z1)],
        "to": [max(x0, x1), max(y0, y1), max(z0, z1)],
        "faces": {face: {"texture": texture} for face in ("down", "up", "north", "south", "west", "east")},
    }


def rose_vine(ctx):
    data = json.loads((ctx.ROOT / "tools" / "rose_vine.json").read_text(encoding="utf-8"))
    texture = ctx.ref("rosevineblock")
    parts = []
    for age in range(6):
        ctx.block_model(f"rose_vine_floor_{age}", {
            "textures": {"vine": texture, "particle": texture},
            "elements": [box_element(box, False, False, "#vine") for box in data["floor"][age]],
        })
        parts.append({"when": {"age": str(age), "down": "true"}, "apply": {"model": ctx.ref(f"rose_vine_floor_{age}")}})
        for side, (invert, mirror) in WALLS.items():
            ctx.block_model(f"rose_vine_{side}_{age}", {
                "textures": {"vine": texture, "particle": texture},
                "elements": [box_element(box, invert, mirror, "#vine") for box in data["wall"][age]],
            })
            parts.append({"when": {"age": str(age), side: "true"}, "apply": {"model": ctx.ref(f"rose_vine_{side}_{age}")}})
    ctx.blockstate("rose_vine", {"multipart": parts})
    ctx.block_item("rose_vine", model=ctx.ref("rose_vine_west_5"))
    ctx.shear_only("rose_vine")


def pump(ctx):
    ctx.block_model("essentia_pump", {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/metalbase"}})
    ctx.blockstate("essentia_pump", {"variants": {f"facing={facing}": {"model": ctx.ref("essentia_pump")} for facing in PUMP_FACINGS}})
    ctx.item_model("essentia_pump", {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/metalbase"}})
    ctx.write(ctx.ASSETS / "items" / "essentia_pump.json", {"model": {
        "type": "minecraft:special",
        "base": ctx.ref("essentia_pump", "item"),
        "model": {"type": f"{ctx.NS}:essentia_pump"},
    }})
    ctx.self_drop("essentia_pump")
    ctx.mineable("pickaxe", "essentia_pump")


def generate(ctx):
    rose_vine(ctx)
    pump(ctx)
