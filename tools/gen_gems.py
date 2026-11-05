import boxmodel
import pngtools

TEXTURE_SIZE = (64, 32)
TABLE = [
    (0, 0, (16, 2, 16), (-8.0, 0.0, -8.0), None),
    (0, 0, (16, 4, 16), (-8.0, 10.0, -8.0), None),
    (-32, 0, (16, 0, 16), (-8.0, 14.05, -8.0), None),
    (0, 20, (3, 8, 3), (-7.0, 2.0, -7.0), None),
    (0, 20, (3, 8, 3), (4.0, 2.0, -7.0), None),
    (0, 20, (3, 8, 3), (-7.0, 2.0, 4.0), None),
    (0, 20, (3, 8, 3), (4.0, 2.0, 4.0), None),
    (52, 0, (1, 1, 1), (4.8, 13.9, 2.2), 0.4),
    (52, 0, (1, 1, 5), (7.0, 14.0, -2.5), -0.4),
    (52, 6, (1, 1, 2), (-6.0, 13.9, -0.8), -0.1),
    (52, 0, (1, 1, 5), (-6.5, 14.0, -5.5), 0.1),
    (52, 1, (1, 1, 4), (4.5, 14.0, -5.5), None),
    (58, 6, (1, 1, 2), (4.5, 13.9, -2.8), -0.1),
]
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
ICON = (18, 198, 18, 18)


def element(part):
    u, v, size, pivot, yaw = part
    origin = [pivot[0] + 8, pivot[1], pivot[2] + 8]
    rotation = ("y", round(yaw * 180.0 / 3.141592653589793, 4)) if yaw else None
    content = boxmodel.unflipped(u, v, (0.0, 0.0, 0.0), size, origin, TEXTURE_SIZE, "#table", rotation)
    content["faces"] = {name: face for name, face in content["faces"].items() if min(face["uv"]) >= 0}
    return content


def effect_icon(ctx):
    source = ctx.ROOT / "src" / "main" / "resources" / "assets" / ctx.NS / "textures" / "gui" / "potioneffects.png"
    width, height, rows = pngtools.read_rgba(source)
    x, y, w, h = ICON
    cropped = [row[x * 4:(x + w) * 4] for row in rows[y:y + h]]
    target = ctx.ASSETS / "textures" / "mob_effect" / "knockback_resistance.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    pngtools.write_rgba(target, w, h, cropped)


def enchantment(ctx, name, weight, min_base, min_step):
    ctx.write(ctx.DATA / ctx.NS / "enchantment" / f"{name}.json", {
        "anvil_cost": 1,
        "description": {"translate": f"enchantment.{ctx.NS}.{name}"},
        "effects": {},
        "max_cost": {"base": 11 + 50, "per_level_above_first": 10},
        "max_level": 3,
        "min_cost": {"base": min_base, "per_level_above_first": min_step},
        "slots": ["any"],
        "supported_items": f"{ctx.NS}:infused_gem",
        "weight": weight,
    })


def generate(ctx):
    notes = []
    ctx.block_model("cutting_table", {
        "textures": {"table": ctx.block_texture("models/cuttingtable", "cutting_table"), "particle": "thaumcraft:block/planks_greatwood"},
        "elements": [ctx.profile.model_element(element(part), "cutting_table", notes) for part in TABLE],
    })
    ctx.blockstate("cutting_table", {"variants": {
        f"facing={facing}": {"model": ctx.ref("cutting_table"), **({"y": angle} if angle else {})}
        for facing, angle in FACINGS.items()
    }})
    ctx.block_item("cutting_table")
    ctx.block_model("glimmer_of_light", {"textures": {"particle": "minecraft:block/glowstone"}})
    ctx.simple_state("glimmer_of_light")
    ctx.self_drop("cutting_table")
    ctx.mineable("axe", "cutting_table")

    for cut in ("point", "oval"):
        ctx.item_model(f"infused_gem_{cut}", {"parent": "minecraft:item/generated", "textures": {"layer0": ctx.ref(f"infusedgem_{cut}", "item")}})
    gem_tint = [{"type": f"{ctx.NS}:gem_aspect"}]
    ctx.write(ctx.ASSETS / "items" / "infused_gem.json", {"model": {
        "type": "minecraft:select",
        "property": "minecraft:component",
        "component": f"{ctx.NS}:gem_cut",
        "cases": [{"when": "oval", "model": {"type": "minecraft:model", "model": ctx.ref("infused_gem_oval", "item"), "tints": gem_tint}}],
        "fallback": {"type": "minecraft:model", "model": ctx.ref("infused_gem_point", "item"), "tints": gem_tint},
    }})

    ctx.item_model("crystal_capsule", {"parent": "minecraft:item/generated", "textures": {
        "layer0": ctx.ref("crystalcapsule_mask", "item"),
        "layer1": ctx.ref("crystalcapsule", "item"),
        "layer2": ctx.ref("crystalcapsule_overlay", "item"),
    }})
    ctx.item_definition("crystal_capsule", ctx.ref("crystal_capsule", "item"), [{"type": f"{ctx.NS}:capsule_fluid"}])

    ctx.shaped(
        "gemcutting_tools", "witchinggadgets:gemcutting_tools", 1,
        ["qfi", "sss"],
        {"q": "#c:gems/quartz", "f": "minecraft:flint", "i": "#c:ingots/iron", "s": "#c:rods/wooden"},
    )
    enchantment(ctx, "gemstone_potency", 4, 10, 11)
    enchantment(ctx, "gemstone_brittle", 1, 10, 11)
    effect_icon(ctx)
