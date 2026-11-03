import boxmodel

LABEL_TEXTURE_SIZE = (128, 64)
LABEL_PARTS = [
    (0, 0, (-8, 0, -8), (16, 8, 16), (0, 0, 0)),
    (0, 32, (-8, 12, -8), (16, 4, 16), (0, 0, 0)),
    (72, 0, (3, 8, -7), (4, 4, 4), (0, 0, 0)),
    (72, 0, (-7, 8, 3), (4, 4, 4), (0, 0, 0)),
    (72, 0, (3, 8, 3), (4, 4, 4), (0, 0, 0)),
    (72, 0, (-7, 8, -7), (4, 4, 4), (0, 0, 0)),
    (72, 8, (0, 0, 0), (3, 8, 6), (-7, 16, 0)),
    (72, 22, (0, 0, 0), (3, 7, 5), (-7, 16, 1)),
    (90, 8, (0, 0, 0), (6, 3, 8), (1, 16.0001, -7)),
    (90, 19, (0, 0, 0), (5, 3, 7), (2, 16.0001, -6.5)),
]
LABEL_FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
SAUNA_RIM = [
    ((0, 12, 0), (16, 16, 3)),
    ((0, 12, 13), (16, 16, 16)),
    ((0, 12, 3), (3, 16, 13)),
    ((13, 12, 3), (16, 16, 13)),
]
SAUNA_COALS = [
    (3, 5, 3, 6, 0.125), (5, 7, 3, 6, 0.1875), (7, 9, 3, 6, 0.15625), (9, 12, 3, 7, 0.125),
    (12, 13, 3, 6, 0.09375), (3, 4, 6, 7, 0.15625), (4, 6, 6, 7, 0.1875), (6, 7, 6, 7, 0.15625),
    (7, 9, 6, 7, 0.125), (12, 13, 6, 9, 0.15625), (3, 4, 7, 9, 0.15625), (4, 6, 7, 9, 0.125),
    (6, 9, 7, 8, 0.09375), (9, 10, 7, 9, 0.1875), (10, 12, 7, 8, 0.125), (6, 9, 8, 10, 0.15625),
    (10, 12, 8, 9, 0.15625), (3, 6, 9, 11, 0.09375), (9, 13, 9, 10, 0.09375), (6, 8, 10, 11, 0.125),
    (8, 10, 10, 11, 0.1875), (10, 13, 10, 11, 0.125), (3, 4, 11, 13, 0.1875), (4, 6, 11, 12, 0.15625),
    (6, 9, 11, 13, 0.125), (9, 11, 11, 13, 0.15625), (11, 13, 11, 13, 0.09375), (4, 6, 12, 13, 0.09375),
]
SIDES = ("north", "south", "east", "west", "up", "down")


def cuboid(start, end, textures):
    return {"from": list(start), "to": list(end), "faces": {side: {"texture": textures.get(side, textures["side"])} for side in SIDES}}


def sauna_model():
    body = {"side": "#side", "up": "#top"}
    elements = [cuboid((0, 0, 0), (16, 12, 16), body)]
    elements += [cuboid(start, end, body) for start, end in SAUNA_RIM]
    for x1, x2, z1, z2, height in SAUNA_COALS:
        elements.append(cuboid((x1, 12, z1), (x2, round(12 + height * 16, 4), z2), {"side": "#coal"}))
    return elements


def generate(ctx):
    ctx.block_model("sauna_stove", {
        "parent": "minecraft:block/block",
        "textures": {
            "particle": ctx.ref("saunastove_side"),
            "side": ctx.ref("saunastove_side"),
            "top": ctx.ref("saunastove_top"),
            "coal": ctx.ref("saunastove_coal"),
        },
        "elements": sauna_model(),
    })
    ctx.simple_state("sauna_stove")
    ctx.block_item("sauna_stove")

    texture = ctx.block_texture("models/labellib", "label_library")
    ctx.block_model("label_library", {
        "parent": "minecraft:block/block",
        "textures": {"particle": texture, "t": texture},
        "elements": [boxmodel.unflipped(u, v, offset, size, (8 + pivot[0], pivot[1], 8 + pivot[2]), LABEL_TEXTURE_SIZE, "#t") for u, v, offset, size, pivot in LABEL_PARTS],
    })
    ctx.blockstate("label_library", {"variants": {
        f"facing={facing}": {"model": ctx.ref("label_library"), **({"y": rotation} if rotation else {})}
        for facing, rotation in LABEL_FACINGS.items()
    }})
    ctx.block_item("label_library")

    for name in ("sauna_stove", "label_library"):
        ctx.self_drop(name)
    ctx.mineable("axe", "sauna_stove", "label_library")
