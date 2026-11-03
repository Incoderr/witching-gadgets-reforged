FRAME = [
    ("wood", (0, 0, 0, 1, 0.1875, 1), (0, 0, 1, 0.1875)),
    ("wood", (0.0625, 0.1875, 0.0625, 0.9375, 0.3125, 0.9375), (0, 0, 1, 0.125)),
    ("wood", (0, 0.8125, 0, 1, 0.9375, 1), (0, 0, 1, 0.1875)),
    ("stone", (0, 0.9375, 0, 1, 1, 1), (0, 0, 1, 1)),
    ("stone", (0.0625, 0.3125, 0.0625, 0.9375, 0.3125, 0.9375), (0.0625, 0.0625, 0.9375, 0.9375)),
    ("stone", (0, 0.8125, 0, 1, 0.8125, 1), (0, 0, 1, 1)),
    ("pillar", (0, 0.1875, 0, 0.125, 0.8125, 0.125), (0, 0, 0.125, 1)),
    ("pillar", (0, 0.1875, 0.875, 0.125, 0.8125, 1), (0, 0, 0.125, 1)),
    ("pillar", (0.875, 0.1875, 0, 1, 0.8125, 0.125), (0, 0, 0.125, 1)),
    ("pillar", (0.875, 0.1875, 0.875, 1, 0.8125, 1), (0, 0, 0.125, 1)),
    ("front", (0.125, 0.125, 0.0624, 0.875, 0.875, 0.0625), (0.125, 0.125, 0.875, 0.875)),
    ("side", (0.125, 0.125, 0.9375, 0.875, 0.875, 0.9376), (0.125, 0.125, 0.875, 0.875)),
    ("side", (0.9375, 0.125, 0.125, 0.9376, 0.625, 0.875), (0.125, 0.125, 0.875, 0.625)),
    ("side", (0.0624, 0.125, 0.125, 0.0625, 0.625, 0.875), (0.125, 0.125, 0.875, 0.625)),
    ("stone", (0.4075, 0.4075, 0, 0.5925, 0.5925, 0.0624), (0.4, 0.4, 0.6, 0.6)),
    ("stone", (0.375, 0.375, -0.125, 0.625, 0.625, 0), (0.375, 0.375, 0.625, 0.625)),
]
FLUIDS = [
    ("lava", (0.1875, 0.3125, 0.375, 0.3125, 0.8125, 0.625), (0.375, 0, 0.625, 0.5), None),
    ("water", (0.6875, 0.3125, 0.375, 0.8125, 0.8125, 0.625), (0.375, 0, 0.625, 0.5), 0),
]
TEXTURES = {
    "wood": "thaumcraft:block/woodplain",
    "stone": "thaumcraft:block/arcane_stone",
    "pillar": "thaumcraft:block/pedestal_top",
    "front": "thaumcraft:block/liftertop",
    "side": "thaumcraft:block/lifterside",
    "lava": "minecraft:block/lava_flow",
    "water": "minecraft:block/water_flow",
    "particle": "thaumcraft:block/woodplain",
}
ROTATIONS = {
    "north": {},
    "east": {"y": 90},
    "south": {"y": 180},
    "west": {"y": 270},
    "up": {"x": 270},
    "down": {"x": 90},
}


def element(texture, box, uv, tint=None):
    start = [round(value * 16, 4) for value in box[:3]]
    end = [round(value * 16, 4) for value in box[3:]]
    face = {"uv": [round(value * 16, 4) for value in uv], "texture": f"#{texture}"}
    if tint is not None:
        face["tintindex"] = tint
    return {"from": start, "to": end, "faces": {side: dict(face) for side in ("north", "south", "east", "west", "up", "down")}}


def extruder_model(fluids):
    elements = [element(texture, box, uv) for texture, box, uv in FRAME]
    if fluids:
        elements += [element(texture, box, uv, tint) for texture, box, uv, tint in FLUIDS]
    return {"textures": TEXTURES, "elements": elements}


def generate(ctx):
    ctx.cube_all("ethereal_wall", "etherealwall")
    fallback = {"model": ctx.ref("ethereal_wall")}
    ctx.blockstate("ethereal_wall", {"variants": {
        f"powered={value}": {"type": "thaumcraft:warded", "fallback": fallback} for value in ("false", "true")
    }})
    ctx.block_item("ethereal_wall")

    ctx.cube_all("ageing_stone", "timestone")
    ctx.simple_state("ageing_stone")
    ctx.block_item("ageing_stone")

    ctx.block_model("stone_extruder", extruder_model(True))
    ctx.block_model("stone_extruder_powered", extruder_model(False))
    ctx.blockstate("stone_extruder", {"variants": {
        f"facing={facing},powered={powered}": {"model": ctx.ref("stone_extruder_powered" if powered == "true" else "stone_extruder"), **rotation}
        for facing, rotation in ROTATIONS.items()
        for powered in ("false", "true")
    }})
    ctx.block_item("stone_extruder")

    for name in ("ethereal_wall", "ageing_stone", "stone_extruder"):
        ctx.self_drop(name)
    ctx.mineable("pickaxe", "ethereal_wall", "ageing_stone")
    ctx.mineable("axe", "stone_extruder")
