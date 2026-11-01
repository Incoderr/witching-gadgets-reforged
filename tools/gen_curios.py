NS = "witchinggadgets"
GEAR_SLOTS = ["cloak", "pauldron", "vambrace", "title"]
BAUBLES = {
    "belt": ["kama", "spectral_kama", "storage_kama", "wolf_kama"],
    "ring": ["luck_ring", "sniper_ring"],
}


def generate(ctx):
    if ctx.profile.modern:
        return
    for slot in GEAR_SLOTS:
        ctx.write(ctx.DATA / NS / "curios" / "slots" / f"{slot}.json", {"size": 1, "validators": [f"{NS}:gear"]})
    ctx.write(ctx.DATA / NS / "curios" / "entities" / "player.json", {
        "entities": ["minecraft:player"],
        "slots": [*GEAR_SLOTS, *BAUBLES],
    })
    for slot, items in BAUBLES.items():
        for item in items:
            ctx.item_tags[f"curios:{slot}"].add(f"{NS}:{item}")
