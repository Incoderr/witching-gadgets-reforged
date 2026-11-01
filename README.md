# Witching Gadgets: Reforged

Unofficial port of **Witching Gadgets 1.1.10** by BluSunrize from Minecraft 1.7.10 to **Minecraft 26.3** on **NeoForge 26.3**. It is an addon to [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) and needs it to run.

The goal is to recreate the original gameplay as closely as possible: the spinning wheel and weaving, cloaks, robes and other magical clothing, infused gems and primordial gear, the Thaumic blast furnace and ore clusters, essentia pumps, terraformers and the rest of the gadgets. Integrations with other mods (except JEI) are not ported. Travellers' Gear slots are not available on modern versions, so their items are moved to the base mod's bauble slots. The technical side is rebuilt on modern APIs, the original coremod (ASM transformers) is replaced by events or mixins.

The project is a scaffold for now: registries, network seams, config, game test harness and the Python tooling are in place, gameplay content is ported feature by feature.

> **Personal use only.** Witching Gadgets and all of its assets belong to BluSunrize. The original repository does not specify a license. This port is not intended for redistribution.

## Requirements to build

- Java 25
- Python 3
- A checkout of Thaumcraft 4: Reforged next to this repository (`../thaumcraft-4-reforged`)

Versions match the base mod:

- Mod `1.0`
- [Thaumcraft 4: Reforged](https://github.com/notlikethiss/thaumcraft-4-reforged) `1.0`
- Minecraft `26.3`
- NeoForge `26.3.0.57-beta`
- ModDevGradle `2.0.148`
- TerraBlender `26.3-26.3.0.0.9` (required by the base)
- JEI `31.9.0.58` (optional at runtime; included in development runs)

## Building and running

The base mod is included as a Gradle composite build (`includeBuild("../thaumcraft-4-reforged")`), so Gradle builds it automatically and both mods are loaded in every run.

```sh
export JAVA_HOME=/path/to/openjdk-25

./gradlew build               # build the addon jar into build/libs
./gradlew runClient           # start a development client
./gradlew runServer           # start a development server (game folder: run/)
./gradlew runGameTestServer   # run the addon game tests only
python3 tools/fetch_wg.py     # download the original source into reference/wg-src
python3 tools/convert_assets.py
python3 tools/gen_resources.py
python3 tools/inventory.py --check
python3 tools/check_seams.py  # check version-specific API boundaries
```

## Automatic releases

GitHub Actions builds pushes to `main` and `mc/**` and can also be started manually with **Run workflow**. Pushes that only change Markdown files or `tools/**` are skipped.

The workflow checks out Thaumcraft 4: Reforged alongside the addon, using the matching `mc/**` branch for Minecraft branches and `main` otherwise. Java, Minecraft and mod versions are read from `gradle.properties`.

After the seam check and Gradle build pass, the workflow replaces the release and tag named after the Minecraft version (currently `26.3`), attaches the addon JAR and uses the latest commit as release notes. Releases from `main` are marked as latest. This follows the release process used by Thaumcraft 4: Reforged.

## Project layout

```text
src/main/java/witchinggadgets/
  WitchingGadgets.java  mod entry point
  Config.java           synced config
  Features.java         feature list (load, research, recipes, aspects, tests)
  registry/             content registration
  network/              payloads
  platform/             version-specific API implementations
  client/               client entry point, screens, renderers
  test/                 automated game tests
src/main/templates/     mod metadata templates
src/main/resources/     textures and lang files
tools/                  Python fetch, conversion, generation and validation scripts
reference/              original mod source (git ignored)
```

## Tools

| Script                    | Purpose                                                                                       |
| ------------------------- | --------------------------------------------------------------------------------------------- |
| `tools/fetch_wg.py`       | Downloads the Witching Gadgets source (branch `master`) into `reference/wg-src`               |
| `tools/convert_assets.py` | Converts original textures, `.obj` models and `.lang` files, merges extra translations from `tools/lang/` |
| `tools/gen_resources.py`  | Generates blockstates, models, loot tables, recipes and tags from per-feature generators      |
| `tools/features.txt`      | List of per-feature resource generators                                                       |
| `tools/inventory.py`      | Checks `tools/id_map.json` against registrations, models and lang                             |
| `tools/id_map.json`       | Maps every original block and item (with metadata) to a modern id                             |
| `tools/check_seams.py`    | Checks version-specific API boundaries                                                        |

## Localization

The original English, German and Chinese language files are converted. The original has no Russian translation, so Russian is maintained by hand in `tools/lang/` together with English additions.

## Credits

- **BluSunrize** for the original Witching Gadgets
- **SlimeKnights**, **PatchouliHina** and the other contributors and translators of the original mod
- **incoder** for the port
- **Azanor** for Thaumcraft
