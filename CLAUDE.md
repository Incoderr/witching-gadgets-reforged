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

## Checks

```sh
python3 tools/fetch_wg.py
python3 tools/check_seams.py
./gradlew build
./gradlew runGameTestServer
```

- Visual checks in `runClient` and two-client play are done by the user.

## Code style

- Java: 4 spaces, no comments.
- Python: 4 spaces, double quotes, no comments, `pathlib`.
- Commits in English `feat:`/`fix:`, no co-author lines.
