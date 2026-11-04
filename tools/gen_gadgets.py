import json
import shutil

PARTS = {
    "Gold_01": "gold",
    "Leather_02": "leather",
    "Scanner_03": "scanner",
    "Wood_04": "wood",
}
HIDDEN = {"Display_00"}
CENTER = 0.5


def camera_obj(source):
    lines = []
    current = None
    for line in source.read_text(encoding="utf-8").splitlines():
        if line.startswith("mtllib") or line.startswith("usemtl"):
            continue
        if line.startswith("o "):
            current = line[2:].strip()
            lines.append(line)
            if current in PARTS:
                lines.append(f"usemtl {current}")
            continue
        if line.startswith("v "):
            x, y, z = (float(value) for value in line.split()[1:4])
            lines.append(f"v {x + CENTER:.6f} {y + CENTER:.6f} {z + CENTER:.6f}")
            continue
        if line.startswith("f ") and current in HIDDEN:
            continue
        lines.append(line)
    return "mtllib scan_camera.mtl\n" + "\n".join(lines) + "\n"


def camera_mtl():
    return "".join(f"newmtl {part}\nmap_Kd #{texture}\n\n" for part, texture in PARTS.items())


def copy_texture(ctx, source, name):
    target = ctx.ASSETS / "textures" / "block" / f"{name}.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ctx.SOURCE_TEXTURES / f"{source}.png", target)
    meta = ctx.SOURCE_TEXTURES / f"{source}.png.mcmeta"
    if meta.exists():
        shutil.copyfile(meta, target.with_name(f"{name}.png.mcmeta"))
    return ctx.ref(name)


def generate(ctx):
    models = ctx.ASSETS / "models" / "item"
    models.mkdir(parents=True, exist_ok=True)
    source = ctx.ROOT / "src" / "main" / "resources" / "assets" / ctx.NS / "obj" / "scan_camera.obj"
    (models / "scan_camera.obj").write_text(camera_obj(source), encoding="utf-8", newline="\n")
    (models / "scan_camera.mtl").write_text(camera_mtl(), encoding="utf-8", newline="\n")
    gold = copy_texture(ctx, "models/scanner", "scan_camera_gold")
    textures = {
        "gold": gold,
        "leather": copy_texture(ctx, "models/cameraleather", "scan_camera_leather"),
        "scanner": copy_texture(ctx, "models/scanscreen", "scan_camera_screen"),
        "wood": "thaumcraft:block/planks_greatwood",
        "particle": gold,
    }
    ctx.item_model("scan_camera", {
        "loader": "neoforge:obj",
        "model": f"{ctx.NS}:models/item/scan_camera.obj",
        "mtl_override": f"{ctx.NS}:models/item/scan_camera.mtl",
        "flip_v": True,
        "textures": textures,
        "display": {
            "gui": {"rotation": [30, 225, 0], "scale": [1.25, 1.25, 1.25]},
            "ground": {"translation": [0, 2, 0], "scale": [1.0, 1.0, 1.0]},
            "fixed": {"rotation": [0, 90, 0], "scale": [1.25, 1.25, 1.25]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [1.0, 1.0, 1.0]},
            "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [1.0, 1.0, 1.0]},
            "firstperson_righthand": {"rotation": [0, 135, 0], "translation": [0, 2, 0], "scale": [1.2, 1.2, 1.2]},
            "firstperson_lefthand": {"rotation": [0, 135, 0], "translation": [0, 2, 0], "scale": [1.2, 1.2, 1.2]},
        },
    })
    ctx.item_definition("scan_camera", ctx.ref("scan_camera", "item"))

    ctx.shapeless("clear_photo_plate", "thaumcraft:mirrored_glass", 1, ["witchinggadgets:photo_plate"])
