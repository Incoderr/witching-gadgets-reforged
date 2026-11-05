import shutil

TEXTURES = {
    "simple_thread": "mat_threadsimple",
    "golden_thread": "mat_threadgold",
    "thaumium_thread": "mat_threadthaumium",
    "wolf_pelt": "mat_wolfpelt",
    "arcane_abacus": "mat_calculator",
    "gemcutting_tools": "mat_cuttingtools",
    "photo_plate": "mat_photoplate",
    "developed_photo": "mat_developedphoto",
    "guiding_string": "mat_guidingstring",
    "powerless_pearl": "mat_powerlesspearl",
    "gemstone_dust": "mat_gemstonedust",
}
CLOTHS = {
    "spacious_cloth": 0x888888,
    "golden_cloth": 0xE6BE44,
    "bewitched_cloth": 0x9700C0,
}


def generate(ctx):
    icon = ctx.ASSETS / "textures" / "item" / "tab_icon.png"
    icon.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ctx.SOURCE_TEXTURES / "gui" / "research" / "wgicon.png", icon)
    ctx.generated_item("tab_icon", "tab_icon")
    for name, texture in TEXTURES.items():
        ctx.generated_item(name, texture)
    for name, color in CLOTHS.items():
        ctx.generated_item(name, "mat_cloth", [ctx.tint(color)])

    ctx.cube_all("void_metal_block", "voidmetalblock")
    ctx.simple_state("void_metal_block")
    ctx.block_item("void_metal_block")
    ctx.self_drop("void_metal_block")
    ctx.mineable("pickaxe", "void_metal_block")
    ctx.tag_both("c:storage_blocks/void_metal", "void_metal_block")
    ctx.tag_both("c:storage_blocks", "void_metal_block")

    ctx.shaped(
        "void_metal_block", "witchinggadgets:void_metal_block", 1,
        ["vvv", "vvv", "vvv"],
        {"v": "#c:ingots/void_metal"},
        "building",
    )
    ctx.shapeless(
        "void_metal_ingot_from_block", "thaumcraft:void_metal_ingot", 9,
        ["#c:storage_blocks/void_metal"],
    )
