FOCI = ("plains", "cold_taiga", "desert", "jungle", "hell", "taint", "mushroom")


def device(ctx, name, focus):
    ctx.block_model(name, {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/metalbase"}})
    ctx.simple_state(name)
    ctx.item_model(name, {"parent": "minecraft:block/block", "textures": {"particle": "thaumcraft:block/metalbase"}})
    model = {"type": f"{ctx.NS}:terraform"}
    if focus:
        model["focus"] = focus
    ctx.write(ctx.ASSETS / "items" / f"{name}.json", {"model": {"type": "minecraft:special", "base": ctx.ref(name, "item"), "model": model}})
    ctx.self_drop(name)
    ctx.mineable("pickaxe", name)


def generate(ctx):
    device(ctx, "terraformer", None)
    for focus in FOCI:
        device(ctx, f"terraform_focus_{focus}", focus)
