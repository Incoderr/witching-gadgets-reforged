ENCHANTMENTS = [
    ("invisible_gear", 2, 6, 0, "#minecraft:enchantable/durability", ["armor"], None),
    ("unveiling", 1, 6, 0, "#witchinggadgets:enchantable/unveiling", ["head"], None),
    ("stealth", 5, 5, 11, "#witchinggadgets:enchantable/stealth", ["legs", "feet"], None),
    ("backstab", 3, 5, 11, "#witchinggadgets:enchantable/backstab", ["mainhand"], ["minecraft:smite", "minecraft:bane_of_arthropods"]),
    ("ride_protect", 1, 9, 0, "#minecraft:enchantable/armor", ["armor"], None),
]
TAGS = {
    "witchinggadgets:enchantable/unveiling": ["thaumcraft:goggles_of_revealing", "thaumcraft:fortress_helmet", "thaumcraft:void_robe_helmet"],
    "witchinggadgets:enchantable/stealth": ["#minecraft:enchantable/leg_armor", "#minecraft:enchantable/foot_armor"],
    "witchinggadgets:enchantable/backstab": ["#minecraft:swords", "#minecraft:axes"],
}


def generate(ctx):
    for name, max_level, min_base, min_step, supported, slots, exclusive in ENCHANTMENTS:
        content = {
            "anvil_cost": 8,
            "description": {"translate": f"enchantment.{ctx.NS}.{name}"},
            "effects": {},
            "max_cost": {"base": min_base + 20, "per_level_above_first": min_step},
            "max_level": max_level,
            "min_cost": {"base": min_base, "per_level_above_first": min_step},
            "slots": slots,
            "supported_items": supported,
            "weight": 1,
        }
        if exclusive:
            content["exclusive_set"] = exclusive
        ctx.write(ctx.DATA / ctx.NS / "enchantment" / f"{name}.json", content)
    for tag, values in TAGS.items():
        ctx.item_tags[tag].update(values)
