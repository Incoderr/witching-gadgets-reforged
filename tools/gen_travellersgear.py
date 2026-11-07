ITEMS = {
    "belt": "simplegear_belt",
    "gold_ring": "simplegear_ringgold",
    "silver_ring": "simplegear_ringsilver",
    "pauldron": "simplegear_pauldrons",
    "vambrace": "simplegear_vambraces",
    "title_scroll": "simplegear_title",
}
WOOL = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
DYE_COLOURS = [0xFFFFFF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533, 0x7FCC19, 0xF27FA5, 0x4C4C4C, 0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0x191919]
TITLES = ["treepuncher", "titan", "librarian", "bursar", "archchancellor", "justicar", "explorer", "defender", "seeker", "boxFox", "freshPrince"]
LOOT_TARGETS = [
    ("simple_dungeon", "minecraft:chests/simple_dungeon", (8, 8), 120),
    ("abandoned_mineshaft", "minecraft:chests/abandoned_mineshaft", (3, 7), 80),
    ("desert_pyramid", "minecraft:chests/desert_pyramid", (2, 7), 73),
    ("jungle_temple", "minecraft:chests/jungle_temple", (2, 7), 73),
    ("stronghold_library", "minecraft:chests/stronghold_library", (1, 5), 44),
]
TITLE_WEIGHT = 8


def texture(name):
    return f"travellersgear/{name}"


def generate(ctx):
    for name, source in ITEMS.items():
        ctx.generated_item(name, texture(source))
    cloak = "traveller_cloak"
    ctx.generated_item(cloak, texture("simplegear_cloak"), [{"type": "minecraft:dye", "default": ctx.tint(0xFFFFFF)["value"]}])
    ctx.item_tags["minecraft:dyeable"].add(f"{ctx.NS}:{cloak}")
    for wool, colour in zip(WOOL, DYE_COLOURS):
        ctx.shaped(f"traveller_cloak_{wool}", f"{ctx.NS}:{cloak}", 1, [" s ", "www", "www"], {"s": "minecraft:string", "w": f"minecraft:{wool}_wool"}, "equipment", components={"minecraft:dyed_color": colour})
    ctx.shaped("gold_ring", f"{ctx.NS}:gold_ring", 1, ["gg ", "g g", " gg"], {"g": "#c:nuggets/gold"}, "equipment")
    ctx.shaped("silver_ring", f"{ctx.NS}:silver_ring", 1, ["ss ", "s s", " ss"], {"s": "#c:nuggets/silver"}, "equipment", conditions=ctx.not_tag_empty("c:nuggets/silver"))
    ctx.shaped("belt", f"{ctx.NS}:belt", 1, [" l ", "l l", " i "], {"l": "minecraft:leather", "i": "#c:ingots/iron"}, "equipment")
    ctx.shaped("pauldron", f"{ctx.NS}:pauldron", 1, ["ll ", "ill", " i "], {"l": "minecraft:leather", "i": "#c:nuggets/iron"}, "equipment")
    ctx.shaped("vambrace", f"{ctx.NS}:vambrace", 1, [" l ", "lil", " l "], {"l": "minecraft:leather", "i": "#c:nuggets/iron"}, "equipment")
    ctx.shaped("title_scroll", f"{ctx.NS}:title_scroll", 1, [" l ", "pbp", " l "], {"l": "#c:gems/lapis", "p": "minecraft:paper", "b": "minecraft:enchanted_book"}, "misc")
    stand = "armor_stand"
    ctx.block_model(stand, {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/dark_oak_planks"}})
    ctx.blockstate(stand, {"variants": {f"facing={facing}": {"model": ctx.ref(stand)} for facing in ("north", "south", "west", "east")}})
    ctx.item_model(stand, {"parent": "minecraft:block/block", "textures": {"particle": "minecraft:block/dark_oak_planks"}})
    ctx.write(ctx.ASSETS / "items" / f"{stand}.json", {"model": {"type": "minecraft:special", "base": ctx.ref(stand, "item"), "model": {"type": f"{ctx.NS}:armor_stand"}}})
    ctx.self_drop(stand)
    ctx.mineable("axe", stand)
    ctx.shaped(stand, f"{ctx.NS}:{stand}", 1, ["sfs", " f ", "ppp"], {"s": "#c:rods/wooden", "f": "minecraft:oak_fence", "p": "#minecraft:wooden_slabs"}, "misc")
    titles = [
        {
            "type": "minecraft:item",
            "name": f"{ctx.NS}:title_scroll",
            "weight": TITLE_WEIGHT,
            "functions": [{"function": "minecraft:set_components", "components": {f"{ctx.NS}:title": f"TG.personaltitle.{title}"}}],
        }
        for title in TITLES
    ]
    for name, target, rolls, vanilla_weight in LOOT_TARGETS:
        ctx.inject_loot(f"title_scroll_{name}", [target], rolls, vanilla_weight * len(TITLES), titles)
