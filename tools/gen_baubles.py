import pngtools

TEXTURES = {
    "double_jump_shoulders": "bauble_shouldersdoublejump",
    "knockback_shoulders": "bauble_shouldersknockback",
    "strength_vambraces": "bauble_vambracestrength",
    "haste_vambraces": "bauble_vambracehaste",
    "crimson_cult_title": "bauble_titlecrimsoncult",
    "luck_ring": "bauble_ringluck",
    "sniper_ring": "bauble_ringsniper",
}
# Travellers' Gear bases do not exist without TG: grey copies of the WG pieces stand in for them.
BASES = {
    "pauldron": "bauble_shouldersknockback",
    "vambrace": "bauble_vambracestrength",
}


def generate(ctx):
    for name, texture in TEXTURES.items():
        ctx.generated_item(name, texture)
    for name, source in BASES.items():
        pngtools.desaturate(ctx.SOURCE_TEXTURES / "item" / f"{source}.png", ctx.ASSETS / "textures" / "item" / f"{name}.png")
        ctx.generated_item(name, name)
    ctx.shaped("pauldron", "witchinggadgets:pauldron", 1, ["LIL", "L L"], {"L": "minecraft:leather", "I": "minecraft:iron_ingot"}, "equipment")
    ctx.shaped("vambrace", "witchinggadgets:vambrace", 1, [" L ", "LIL"], {"L": "minecraft:leather", "I": "minecraft:iron_ingot"}, "equipment")
