import boxmodel

TEXTURE_WIDTH = 64
TEXTURE_HEIGHT = 32
PARTS = [
    (0, 0, (0.0, 0.0, 0.0), (2, 8, 2), (1.0, 0.0, 14.0), None),
    (0, 0, (0.0, 0.0, 0.0), (2, 8, 2), (-3.0, 0.0, 14.0), None),
    (0, 0, (0.0, 0.0, 0.0), (2, 6, 2), (-1.0, 0.0, 1.0), None),
    (0, 0, (0.0, 0.0, 0.0), (6, 2, 16), (-3.0, 5.0, 0.25), ("x", -10.0)),
    (8, 0, (0.0, 0.0, 0.0), (1, 3, 1), (-0.5, 12.0, 0.0), None),
    (8, 0, (0.0, 0.0, 0.0), (1, 3, 1), (-0.5, 12.0, 6.35), None),
    (8, 4, (0.0, 0.0, 0.0), (1, 1, 3), (-0.5, 9.9, 2.2), None),
    (8, 4, (0.0, 0.0, 0.0), (1, 1, 3), (-0.5, 16.1, 2.2), None),
    (8, 4, (0.0, 0.0, -1.5), (1, 1, 3), (-0.5, 15.3, 5.6), ("x", 45.0)),
    (8, 4, (0.0, 0.0, -1.5), (1, 1, 3), (-0.5, 11.0, 6.3), ("x", -45.0)),
    (8, 4, (0.0, 0.0, -1.5), (1, 1, 3), (-0.5, 15.3, 1.8), ("x", -45.0)),
    (8, 4, (0.0, 0.0, -1.5), (1, 1, 3), (-0.5, 11.0, 1.1), ("x", 45.0)),
    (28, 0, (0.0, 13.0, 3.7), (1, 6, 1), (-0.5, -2.5, -0.5), None),
    (28, 8, (0.0, 13.6, 3.0), (1, 1, 6), (-0.5, -0.5, -2.5), None),
    (36, 0, (5.0, 10.0, 3.7), (1, 7, 2), (-0.5, -4.0, -1.0), ("z", 15.0)),
    (28, 0, (0.0, 9.0, 13.0), (1, 4, 1), (-0.5, 0.0, -0.5), None),
    (0, 18, (0.0, 11.0, 13.0), (2, 3, 2), (-1.0, 0.0, -1.0), None),
]
FACINGS = {"east": 0, "south": 90, "west": 180, "north": 270}


def element(part):
    u, v, offset, size, pivot, rotation = part
    origin = [pivot[0] + 8, pivot[1], pivot[2]]
    return boxmodel.unflipped(u, v, offset, size, origin, (TEXTURE_WIDTH, TEXTURE_HEIGHT), "#wheel", rotation)


def generate(ctx):
    notes = []
    elements = [ctx.profile.model_element(element(part), "spinning_wheel", notes) for part in PARTS]
    ctx.block_model("spinning_wheel", {
        "textures": {"wheel": ctx.block_texture("models/spinningwheel", "spinning_wheel"), "particle": "thaumcraft:block/woodplain"},
        "elements": elements,
    })
    ctx.blockstate("spinning_wheel", {"variants": {
        f"facing={facing}": {"model": ctx.ref("spinning_wheel"), **({"y": rotation} if rotation else {})}
        for facing, rotation in FACINGS.items()
    }})
    ctx.block_item("spinning_wheel")
    ctx.self_drop("spinning_wheel")
    ctx.mineable("axe", "spinning_wheel")
