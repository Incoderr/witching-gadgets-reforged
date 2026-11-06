TEXTURES = {
    "double_jump_shoulders": "bauble_shouldersdoublejump",
    "knockback_shoulders": "bauble_shouldersknockback",
    "strength_vambraces": "bauble_vambracestrength",
    "haste_vambraces": "bauble_vambracehaste",
    "crimson_cult_title": "bauble_titlecrimsoncult",
    "luck_ring": "bauble_ringluck",
    "sniper_ring": "bauble_ringsniper",
}


def generate(ctx):
    for name, texture in TEXTURES.items():
        ctx.generated_item(name, texture)
