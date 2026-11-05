import pngtools

N = None
COLORS = {
    "g01": (243, 184, 56),
    "g03": (220, 155, 14),
    "g04": (156, 109, 9),
    "g05": (132, 92, 8),
    "g06": (241, 170, 14),
    "g07": (179, 126, 11),
    "g08": (241, 176, 36),
    "g09": (135, 94, 7),
    "g10": (203, 143, 13),
    "g11": (190, 134, 12),
    "g12": (242, 181, 48),
    "w": (208, 224, 248),
}
SHAPE = [
    [N, N, N, N, N, "g11", "g11", "g10"],
    [N, "g04", "g09", N, "g07", "g01", "g10", N],
    ["g10", "w", "w", "g05", "g07", N, N, N],
    ["g06", "w", "g01", "g03", "g01", N, N, N],
    [N, "g04", "g04", "g11", N, N, N, N],
    [N, N, "g05", "g11", N, N, N, N],
    [N, N, "g11", "g04", N, N, N, N],
    [N, N, "g04", "g11", N, N, N, N],
    [N, N, "g03", "g04", N, N, N, N],
    [N, N, "g03", "g01", N, N, N, N],
    [N, N, "g04", "g03", N, N, N, N],
    [N, "g03", "g04", N, N, N, N, N],
    [N, "g03", "g10", N, N, N, N, N],
    [N, "g01", "g10", N, N, N, N, N],
    [N, "g08", "g12", N, N, N, N, N],
    ["g08", "g08", "g12", N, N, N, N, N],
]
GLASS_REGION = (0, 0, 64, 128)


def palette(ctx):
    names = list(COLORS)
    rows = []
    for _ in range(16):
        row = bytearray()
        for x in range(16):
            r, g, b = COLORS[names[x]] if x < len(names) else (255, 255, 255)
            row += bytes((r, g, b, 255))
        rows.append(bytes(row))
    target = ctx.ASSETS / "textures" / "item" / "mirror_frame.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    pngtools.write_rgba(target, 16, 16, rows)
    return {name: index for index, name in enumerate(names)}


def glass(ctx):
    width, height, rows = pngtools.read_rgba(ctx.SOURCE_TEXTURES / "models" / "glass.png")
    x, y, w, h = GLASS_REGION
    cropped = [row[x * 4:(x + w) * 4] for row in rows[y:y + h]]
    pngtools.write_rgba(ctx.ASSETS / "textures" / "item" / "mirror_glass.png", w, h, cropped)


def pixel(y, z, index):
    uv = [index, 0, index + 1, 1]
    return {
        "from": [7.5, y - 8, z],
        "to": [8.5, y - 7, z + 1],
        "faces": {face: {"texture": "#frame", "uv": uv} for face in ("down", "up", "north", "south", "west", "east")},
    }


def generate(ctx):
    indices = palette(ctx)
    glass(ctx)
    elements = [{
        "from": [8, -8, 0],
        "to": [8, 24, 16],
        "faces": {
            "east": {"texture": "#glass", "uv": [0, 0, 16, 16]},
            "west": {"texture": "#glass", "uv": [0, 0, 16, 16]},
        },
    }]
    for i, row in enumerate(SHAPE):
        for j, color in enumerate(row):
            if color is None:
                continue
            index = indices[color]
            for y, z in ((i, j), (i, 15 - j), (31 - i, j), (31 - i, 15 - j)):
                elements.append(pixel(y, z, index))
    ctx.item_model("tab_icon", {
        "textures": {"frame": ctx.ref("mirror_frame", "item"), "glass": ctx.ref("mirror_glass", "item"), "particle": ctx.ref("mirror_frame", "item")},
        "elements": elements,
        "display": {
            "gui": {"rotation": [15, -60, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
            "fixed": {"rotation": [0, 90, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
            "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [0, 2, 1], "scale": [0.3, 0.3, 0.3]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
        },
    })
    ctx.item_definition("tab_icon", ctx.ref("tab_icon", "item"))
