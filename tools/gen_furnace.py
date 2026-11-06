HEATS = ("off", "on", "special")
SIDES = ("down", "up", "north", "south", "west", "east")


def tex(name):
    return f"witchinggadgets:block/blastfurnace_{name}" if name else "witchinggadgets:block/blastfurnace"


def texture(position, side, heat):
    i = HEATS[heat]
    bricks = tex(None)
    s = SIDES.index(side)
    if position == 0:
        return tex(f"cornerbottomr_{i}") if s == 2 else tex(f"cornerbottoml_{i}") if s == 4 else tex("cornertopl_off") if s == 0 else bricks
    if position == 1:
        return tex(f"sidebottom_{i}") if s == 2 else tex("bottomt") if s == 0 else bricks
    if position == 2:
        return tex(f"cornerbottoml_{i}") if s == 2 else tex(f"cornerbottomr_{i}") if s == 5 else tex("cornertopr_off") if s == 0 else bricks
    if position == 3:
        return tex(f"sidebottom_{i}") if s == 4 else tex("bottoml") if s == 0 else bricks
    if position == 4:
        return tex("bottom")
    if position == 5:
        return tex(f"sidebottom_{i}") if s == 5 else tex("bottomr") if s == 0 else bricks
    if position == 6:
        return tex(f"cornerbottoml_{i}") if s == 3 else tex(f"cornerbottomr_{i}") if s == 4 else tex("cornerbottoml_off") if s == 0 else bricks
    if position == 7:
        return tex(f"sidebottom_{i}") if s == 3 else tex("bottomb") if s == 0 else bricks
    if position == 8:
        return tex(f"cornerbottomr_{i}") if s == 3 else tex(f"cornerbottoml_{i}") if s == 5 else tex("cornerbottomr_off") if s == 0 else bricks
    internal = tex("internal") if heat == 1 else bricks
    if position == 9:
        return tex(f"cornertopr_{i}") if s == 2 else tex(f"cornertopl_{i}")
    if position == 10:
        return tex(f"sidetop_{i}") if s == 2 else internal
    if position == 11:
        return tex(f"cornertopl_{i}") if s == 2 else tex(f"cornertopr_{i}")
    if position == 12:
        return tex(f"sidetop_{i}") if s == 4 else internal
    if position == 14:
        return tex(f"sidetop_{i}") if s == 5 else internal
    if position == 15:
        return tex(f"cornertopl_{i}") if s == 3 else tex(f"cornertopr_{i}")
    if position == 16:
        return tex(f"sidetop_{i}") if s == 3 else internal
    if position == 17:
        return tex(f"cornertopr_{i}") if s == 3 else tex(f"cornertopl_{i}")
    if position == 22:
        return tex("lava")
    return bricks


def box(start, end, faces):
    return {"from": start, "to": end, "faces": {side: {"texture": f"#{side}"} for side in faces}}


def elements(position):
    if position == 22:
        return [box([0, 0, 0], [16, 14, 16], SIDES)]
    if position >= 18:
        p = position - 18
        return [
            box([0, 0, 0], [16, 8, 16], SIDES),
            box([8 if p % 3 == 0 else 0, 8, 8 if p < 3 else 0], [8 if (p + 1) % 3 == 0 else 16, 16, 8 if p > 5 else 16], SIDES),
        ]
    return [box([0, 0, 0], [16, 16, 16], SIDES)]


def condition(positions):
    return {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:block_state_property", "block": "witchinggadgets:infernal_blast_furnace", "properties": {"position": str(p)}}
        for p in positions
    ]}


def generate(ctx):
    variants = {}
    for position in range(27):
        for heat in range(3):
            name = f"infernal_blast_furnace_{position}_{HEATS[heat]}"
            textures = {side: texture(position, side, heat) for side in SIDES}
            textures["particle"] = tex(None)
            ctx.block_model(name, {"textures": textures, "elements": elements(position)})
            variants[f"heat={heat},position={position}"] = {"model": ctx.ref(name)}
    ctx.blockstate("infernal_blast_furnace", {"variants": variants})
    groups = {
        "minecraft:nether_bricks": [p for p in range(9) if p != 4],
        "minecraft:obsidian": [4, 9, 11, 15, 17],
        "minecraft:soul_sand": [10, 12, 13, 14, 16],
        "minecraft:nether_brick_stairs": [p for p in range(18, 27) if p != 22],
    }
    ctx.write_loot("infernal_blast_furnace", [{
        "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": item, "conditions": [condition(positions)]} for item, positions in groups.items()],
        "conditions": [{"condition": "minecraft:survives_explosion"}],
    }])
    ctx.mineable("pickaxe", "infernal_blast_furnace")
