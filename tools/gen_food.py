def generate(ctx):
    for name, texture in (("thaumium_shears", "thaumiumshears"), ("sweetwart", "food_sweetwart"), ("nethercake", "food_nethercake"), ("brain_jerky", "food_brainjerky")):
        ctx.generated_item(name, texture)
    ctx.item_tags["c:tools/shear"].add(f"{ctx.NS}:thaumium_shears")
    ctx.item_tags["minecraft:enchantable/durability"].add(f"{ctx.NS}:thaumium_shears")
    ctx.shaped("thaumium_shears", "witchinggadgets:thaumium_shears", 1, [" t", "t "], {"t": "thaumcraft:thaumium_ingot"}, category="equipment")
    ctx.shapeless("sweetwart", "witchinggadgets:sweetwart", 1, ["minecraft:nether_wart", "minecraft:sugar"], category="misc")
    ctx.shaped("nethercake", "witchinggadgets:nethercake", 1, ["nnn", "www"], {"n": "witchinggadgets:sweetwart", "w": "minecraft:wheat"}, category="misc")
