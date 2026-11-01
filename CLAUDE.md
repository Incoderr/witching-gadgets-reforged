# Witching Gadgets: Reforged

Port of Witching Gadgets 1.1.10 (MC 1.7.10, BluSunrize) as an addon to Thaumcraft 4: Reforged (`../thaumcraft-4-reforged`, "the base" below) for Minecraft 26.3 on NeoForge. Logic, numbers, recipes, texts and assets of the original are carried over 1:1, the technical side is written on modern APIs.

This file describes how to develop and port the mod. Phase status, test counts, run dates and log paths do not belong here. Add a line only if an agent would make a mistake without it: a non-obvious API rule, a required order of steps, or a deliberate deviation from WG that would otherwise get "fixed" back.

## Principles

- Source of truth for behavior: the WG 1.1.10 sources in `reference/wg-src` (branch `master`, `tools/fetch_wg.py`). WG code is read as data, nothing from it is executed.
- Integrations with other mods (TConstruct, Twilight Forest, Mystcraft, Magic Bees, ForgeMultipart, MineTweaker) are excluded, except JEI.
- Travellers' Gear does not exist on modern versions: its items move to the base's bauble slots (`thaumcraft.platform.Baubles`).
- The WG coremod (`witchinggadgets/asm`) is not ported as ASM, the required behavior is done with NeoForge events or mixins.
- WG bugs are fixed only if they break the game (dupes, item loss, NPE, desync, server crash). Gameplay quirks stay.
- The server validates what WG trusted from the client. Client classes never leak to the server, everything must work on a dedicated server.
- Resources are made by Python generators in `tools/`, datagen is not used.
- The original has no license specified, the port is `All Rights Reserved` for personal use.

## Environment

- Mod `witchinggadgets`, package `witchinggadgets`, asset namespace `witchinggadgets`, archive `witching-gadgets-reforged`, version 1.0.
- MC 26.3, NeoForge 26.3.0.57-beta, ModDevGradle 2.0.148, JEI 31.9.0.58, TerraBlender 26.3-26.3.0.0.9, Java 25. Versions in `gradle.properties` match the base.
- Python 3, `tools/` scripts have no external dependencies.
- The base is included as a composite build: `includeBuild("../thaumcraft-4-reforged")` in `settings.gradle`, dependency `implementation "thaumcraft:thaumcraft-4-reforged:1.0"`. The base repository must sit next to this one. The base is not edited from the addon.
- `neoforge.mods.toml`: the `thaumcraft` dependency is required with `ordering="AFTER"`, so the addon constructor runs after the base constructor. Research and recipes are registered from the addon constructor.
- `runGameTestServer` runs with `--tests witchinggadgets:*`, base tests are not run. The test structure `thaumcraft:test/platform` comes from the base.
- `run/` needs `eula.txt` with `eula=true` (gitignored).
- There is no root `icon.png` or `poster.png` yet, `logoFile`/`iconFile`/`bannerFile` in `neoforge.mods.toml` are added together with them.

## References (gitignored)

- `reference/wg-src/` WG sources, `tools/fetch_wg.py` downloads the archive into `tools/cache/` and extracts it without the top-level folder.
- MC, NeoForge sources and the decompiled TC4 live in `../thaumcraft-4-reforged/reference`.
- For the base addon API and layout examples see `../thaumic-tinkerer-reforged/CLAUDE.md`.

## Architecture

- `platform/` and `client/platform/` hold everything where version APIs diverge. The addon has its own seams in the `witchinggadgets` namespace: `WgNetwork` (its own `RegisterPayloadHandlersEvent`), `client/platform/WgClientNetwork`. Gameplay code goes only through them and through the base seams. `tools/check_seams.py` fails on forbidden imports outside the seams and on client class imports outside `client/`.
- Registration: `registry/` (`ModItems`, `ModBlocks` with `withItem`, `ModBlockEntities`, `ModMenus`, `ModEntities`, `ModEffects`, `ModSounds`, `ModCreativeTabs`), payloads in `network/ModPayloads`, client handlers in `client/ClientPayloadHandlers`, client entry point `client/WitchingGadgetsClient`.
- `Config` is SYNCED only, keys and defaults are carried over from `WGConfig`.
- The creative tab is the addon's own, its icon is the base's thaumium ingot for now.

## Feature layout

- A feature lives in `witchinggadgets/<feature>/` and exposes one `<Feature>Feature` class: static `DeferredHolder`s in the shared registries, methods `load()` (empty, needed to load the class), `research()`, `recipes()`, and `aspects(AspectRegistry)` when needed.
- A feature adds one line each to the shared files: `Features.java`, `client/ClientFeatures.java`, `tools/features.txt`, `network/ModPayloads.java`, `client/ClientPayloadHandlers.java`. These files use `merge=union` in `.gitattributes`, write lines as separate statements.
- A feature's GameTests live in `test/<Feature>GameTests.java` on `WgGameTestSuite` (`new WgGameTestSuite("name").add("test", body)`, registered with one line in `Features.tests`).

## Research

- `research/WgResearch`: category `WITCHGADG` (registered in `WgResearch.init()` from the constructor), factories `item(...)` and `fake(original, originalCategory, column, row, icon)`, pages `text(page)`, `recipe`, `arcane`, `infusion`, `enchant`, `crucible`, `compound`. The WG code ports almost line by line: `new ResearchPage("witchinggadgets_research_page.KEY.1")` is `WgResearch.text("1")`, `(ShapedArcaneRecipe) recipeList.get("KEY")` is `WgResearch.arcane("KEY")`.
- `WgResearchItem.setPages` turns a text page without a dot into `tc.research_page.<key>.<page>` (`1`, `2`, `r1` like WG, no renumbering), a full key stays. `.when(BooleanSupplier)` hides research by config (mirror research uses `thaumcraft.Config::allowMirrors` like WG `Config.allowMirrors`), `.setWarp(n)` is WG `addWarp(n)`. WG `setParents` validated parents and dropped the research on a typo; the port does not, the table test catches it.
- `WgFakeResearchItem` is WG `WGFakeResearchItem`: key `WGFAKE<original>`, stub, hidden, complexity 1, no aspects, name, text and pages delegated to the base research, appended to the original's siblings in the constructor. All 12 links live in `research/BaseResearchLinks` and are registered before `Features.research()`, feature research uses them as parents.
- `tools/extract_wg_research.py` builds `tools/wg_research.json` from `WGResearch.registerResearch()` with a small statement interpreter over `tools/wgjava.py` (the Thaumic Tinkerer expression parser). `CONDITIONS` fixes WG branches: Twilight Forest absent (no `CLOAK_RAVEN`), mirrors on, native clusters and transmutations of other mods' ores off (`METALLURGICPERFECTION_CLUSTERS`, `METALLURGICPERFECTION_TRANSMUTATION`, `WGFAKETRANSIRON` are not ported), no `tempus` aspect. `CLOAKKAMA_4` (raven) is dropped from the kama page. Aspects are stored as base tags (`praecantatio`), read from the base `Aspect.java`.
- GameTest `research_matches_wg_table` compares every registered table entry (position, complexity clamped to 1..3, flags, parents, siblings, triggers, aspects, warp, pages, fake target) and fails on WG research missing from the table. Entries that are not ported yet are only logged; when the last feature lands, make missing entries fail. Do not edit the table by hand, rerun the extractor and compare with WG.

## Recipes

- `crafting/WgRecipes` on top of `TcRecipes.register`: `arcane(key, research, result, aspects, pattern, 'A', ingredient, ...)`, `arcaneShapeless`, `infusion(key[, research], result, instability, aspects, central, components...)`, `enchantment(key, research, enchantment, instability, aspects, components...)`, `compound(key, research, aspects, width, height, depth, blocks...)`, `crucible`, `vanilla(key, research, recipePath, result)`. A repeated key (including base keys) throws `IllegalStateException`.
- Recipe keys are WG `tag + tagAddon` (`BAGOFTRICKS_CLOTH`, `CLOAKKAMA_0`), the research of a recipe is WG `tag`.
- Base ids of TC4 references come from `tools/tc4_refs.json` (copied from Thaumic Tinkerer, built by `tools/tc4_ref.py --all` from the base `reference/decompiled`): `ConfigItems.itemResource` meta 7 is `thaumcraft:enchanted_fabric`, `ConfigBlocks.blockCosmeticSolid` meta 6 `thaumcraft:arcane_stone`.

## Generated files (do not edit by hand)

| File | Generator |
|---|---|
| `src/main/resources/assets/witchinggadgets` (textures, `obj`, lang) | `tools/convert_assets.py` (+ `tools/assets_extra/`, `tools/lang/`) |
| `src/generated/resources` (rewritten completely) | `tools/gen_resources.py` |
| `reference/wg-src` | `tools/fetch_wg.py` |
| `tools/wg_research.json` | `tools/extract_wg_research.py` |

- After a lang change: `convert_assets.py`, then `gen_resources.py`. Manual translations only in `tools/lang/<lang>.json` and `tools/lang/<feature>/<lang>.json`. WG has no Russian, `ru_ru` exists only from `tools/lang/` and is translated by us; base terms are reused (Arcane is «магический», Primordial and Primal «сингулярный», Taint «заражённый», Void «пустотный», Thaumium «таум-»).
- The WG `.lang` files are UTF-8 with a few cp1252 lines (comments, umlauts in `de_DE.lang`), `convert_assets.py` and `inventory.py` decode UTF-8 with a `cp1252_fallback` error handler (invalid bytes are read as cp1252, also inside a mixed line).
- Some WG `.mcmeta` files are lenient JSON (`{index:0, time:120}`), `convert_assets.py` quotes the keys.
- Lang keys: blocks `block.witchinggadgets.<id>`, items `item.witchinggadgets.<id>`, a `variant` row in `id_map.json` gives `item.witchinggadgets.<id>.<variant>` (`infused_gem.inert`). Research `witchinggadgets_research_name.KEY` becomes `tc.research_name.KEY`, `_text.KEY` `tc.research_text.KEY`, `_page.KEY.N` `tc.research_page.KEY.N` with the WG suffix kept (`1`, `2`, `r1`), pages are referenced by the full key. Category `tc.research_category.WITCHGADG`. Enchantments `enchantment.wg.camelCase` become `enchantment.witchinggadgets.snake_case`. `wg.*` keys stay as they are, anything else gets the `witchinggadgets.` prefix.
- Textures are copied with `blocks` renamed to `block`, `items` to `item`, names lowercased; `models` and `gui` keep their folder names. `.obj` models go to `assets/witchinggadgets/obj/<snake_case>.obj`.
- The resource format profile lives in `tools/mcformat.py`.

## id_map.json

- Edited by hand. A row: `old` (WG registration name), `meta`, `lang` (en_US.lang key or null), `id`, `state` (blocks only), `skip` (reason or null), optional `no_item` (block without an item) and `variant` (an extra lang key of an existing id).
- Ids are the snake_case registration sub-name when it is meaningful, otherwise the lang name: `timeStone` is `ageing_stone`, `cobbleGen` `stone_extruder`, `calculator` `arcane_abacus`, `CrystalFlask` `crystal_capsule`, `blastFurnace` `infernal_blast_furnace`. The three `obsidianDecoration` metas are `obsidian_rune_0..2`.
- Skipped: the hidden `snowGen`, raven cloak and kama (need Twilight Forest), native clusters of other mods' ores (`WG_Cluster`), and stale lang keys of content WG 1.1.10 does not register (loom, armor stand, elite armor, scribing tools, primordial and life protection rings, old material clusters). Every tile/item `.name` key of en_US.lang must be covered by a row, `inventory.py --check` verifies it.

## Checks

```sh
python3 tools/fetch_wg.py
python3 tools/convert_assets.py
python3 tools/gen_resources.py && git status --short src/generated
python3 tools/extract_wg_research.py && git status --short tools/wg_research.json
python3 tools/inventory.py --check
python3 tools/check_seams.py
./gradlew build
./gradlew runGameTestServer
```

- Regeneration must not change files.
- `inventory.py --check` prints what is missing (registrations, models, lang) and exits 0. `--strict` fails on anything missing, the `id_map.json` schema is always checked. `--all` prints full lists.

- Visual checks in `runClient` and two-client play are done by the user.

## Code style

- Java: 4 spaces, no comments.
- Python: 4 spaces, double quotes, no comments, `pathlib`.
- Commits in English `feat:`/`fix:`, no co-author lines.
