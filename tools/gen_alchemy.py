import json

WALLS = {
    "north": (False, False),
    "south": (False, True),
    "west": (True, False),
    "east": (True, True),
}
PUMP_FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}


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
    ctx.generated_item("rose_vine", "rosevine", texture_kind="block")
    ctx.shear_only("rose_vine")


def faces(texture, uv=None):
    result = {}
    for face in ("down", "up", "north", "south", "west", "east"):
        entry = {"texture": texture}
        if uv:
            entry["uv"] = uv
        result[face] = entry
    return result


def pump(ctx):
    elements = [
        {"from": [2, 2, 8], "to": [14, 14, 12], "faces": faces("#body")},
        {"from": [4, 4, 4], "to": [12, 12, 8], "faces": faces("#body")},
        {"from": [5.17, 5.17, 0], "to": [10.83, 10.83, 4], "rotation": {"angle": 45, "axis": "z", "origin": [8, 8, 2]}, "faces": faces("#nozzle")},
    ]
    for x, y in ((0, 0), (12, 0), (0, 12), (12, 12)):
        elements.append({"from": [x, y, 10], "to": [x + 4, y + 4, 16], "faces": faces("#bellows")})
    ctx.block_model("essentia_pump", {
        "textures": {
            "body": "thaumcraft:block/metalbase",
            "nozzle": "thaumcraft:block/alchemyblock",
            "bellows": "thaumcraft:block/planks_greatwood",
            "particle": "thaumcraft:block/metalbase",
        },
        "elements": elements,
    })
    ctx.blockstate("essentia_pump", {"variants": {
        f"facing={facing}": {"model": ctx.ref("essentia_pump"), **({"y": angle} if angle else {})}
        for facing, angle in PUMP_FACINGS.items()
    }})
    ctx.block_item("essentia_pump")
    ctx.self_drop("essentia_pump")
    ctx.mineable("pickaxe", "essentia_pump")


def generate(ctx):
    rose_vine(ctx)
    pump(ctx)
