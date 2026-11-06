# Witching Gadgets: Reforged (mc/1.21.1)

Backport of the Witching Gadgets port from `main` (MC 26.3) to Minecraft 1.21.1. Gameplay logic comes from `main`, this branch only changes the version-specific parts.

This file describes how to sync and maintain the backport. Status, test counts, run dates and log paths do not belong here. Add a line only if an agent would make a mistake without it. The general port rules (principles, generators, feature layout) are in the `CLAUDE.md` of `main`.

## Versions

- Minecraft 1.21.1, NeoForge 21.1.256, Java 21, Parchment 1.21.1 / 2024.11.17, ModDevGradle 2.0.148
- JEI 19.57.0.451, TerraBlender 1.21.1-4.1.0.1, Curios 9.5.1+1.21.1

## Environment

- Worktree `.worktrees/mc-1.21.1` of the main checkout `witching-gadgets-reforged`.
- The base is the `mc/1.21.1` branch of Thaumcraft 4: Reforged: `settings.gradle` takes the first of `../thaumcraft-4-reforged`, `../thaumcraft-4-reforged/.worktrees/mc-1.21.1`, `../thaumcraft-4-reforged-1.21.1` whose `minecraft_version` is `1.21.1`, or `-PthaumcraftDir`. From this worktree that is `../../../thaumcraft-4-reforged/.worktrees/mc-1.21.1`, found by walking up the folders.
- References are gitignored and not shared between worktrees: run `python3 tools/fetch_wg.py` in the worktree to get `reference/wg-src`.
- The `mc/1.21.1` branch of `../thaumic-tinkerer-reforged` is the reference for 1.21.1 addon code.

## Syncing with main

Last synced `main` commit: `9f2954d`.

1. `git log --reverse <sha>..main`.
2. A commit with only resources, lang or `tools/`: `git cherry-pick`, then `python3 tools/convert_assets.py` and `python3 tools/gen_resources.py` (the `mcformat.py` profile comes from `minecraft_version` in `gradle.properties`).
3. A commit with Java: `git cherry-pick` or `git show <sha> | git apply -3`, adapt the 1.21.1 API by hand, `./gradlew compileJava`.
4. Commit with the same message, update the sha above.

## Where the branch differs

- Only `platform/`, `client/platform/`, registration, tests and resource formats change here. A gameplay change here means `main` is missing a seam: add the seam on `main`, then sync.
- 1.21.1 API: `ResourceLocation` instead of `Identifier`, `ModConfig.Type.SERVER` instead of `SYNCED`, `WgRegistration.Items`/`Blocks` provide the 26.3-style `registerItem`/`registerBlock(name, factory, properties)`, no `DeferredRegister.createEntities`.
- `WgNetwork` dispatches client handlers through `setClientHandler`, 1.21.1 has no client payload registration event.
- GameTests use `@GameTestHolder` + `@GameTestGenerator` in `WgGameTestSuite`, filtered by `neoforge.enabledGameTestNamespaces`. The structure `witchinggadgets:test/platform` comes from `tools/gen_testing.py` like on `main`, `WgGameTestHelper` shifts positions one block up onto it. `runGameTestServer` fails unless the log reports a successful run.
- Run folders are `run/client`, `run/server`, `run/gametest`.
- `tools/mcformat.py` has the `1.21.1` profile (pack format 34, legacy recipe ingredients and `neoforge:item_exists` conditions, loot `conditions`/`functions`, explicit `render_type` in block models). `gen_resources.py` writes 26.3 item definitions as `models/item` with `overrides` and copies `equipment` textures to `textures/models/armor`, `inventory.py` looks for item models in `models/item`.
- Baubles go through Curios via the base's `platform/BaublesCurios`.
- The four WG gear slots are Curios slot types `cloak`, `pauldron`, `vambrace`, `title` (`tools/gen_curios.py`, validator `witchinggadgets:gear` registered in `platform/gear/GearCurios`, which also adapts `GearItem` to `ICurioItem`). `platform/Gear` reads them through `CuriosApi`; there is no own gear menu, screen, key or attachment, and `Gear.sync` is empty because Curios resends changed stacks. Kama and ring tags `curios:belt`/`curios:ring` come from `gen_curios.py` too. Slot names are in `tools/lang/curios`.
- 1.21.1 armor items extend `ArmorItem` and take the material in the constructor. The base only gives its own robes a dyeable layer plus overlay, so `platform/WgArmor.dyeable` registers WG armor materials with those two layers; `RobesFeature.load` creates it before item registration. `client/robes/RobeModels` sets the default dye colour of the bewitched robes and the skirted chest model through one `IClientItemExtensions` per item (a second registration for the same item would replace it).
- Tests: mock server players come from `test/TestSupport` (the base dropped its `thaumcraft.test` package); spawn invulnerability is not bypassed there, so damage in tests uses `fellOutOfWorld` or is not asserted. Curios fires equip/unequip only on its tick, so tests call `onGearUnequipped` directly.
- Disguised blocks (ethereal wall): `platform/CamoBlockEntity` extends the base `CamouflagedBlockEntity`; the base 1.21.1 `thaumcraft:warded` loader always falls back to stone, so `client/platform/CamoModels` wraps the plain baked models of the listed blocks in `ModelEvent.ModifyBakingResult` instead, and `gen_arcanestone.py` writes a plain blockstate for the legacy profile. Block tints go through `RegisterColorHandlersEvent.Block` in `client/platform/WgBlockTints`.
- Item tints from 26.3 item definitions are lost in legacy `models/item`; `client/platform/WgItemTints` registers them with `RegisterColorHandlersEvent.Item` (the cloths). A new tinted item on `main` needs a line there (cloths, cloaks and kamas, bags, robes, infused gems, crystal capsules, primordial gear, rose vine); `main`'s `client/platform/WgItemTintSources` does not exist here.
- A `minecraft:select` on `minecraft:component` becomes `overrides` with the predicate `witchinggadgets:<component>` (case index + 1); `client/platform/WgItemProperties` registers that property (`gem_cut`).
- 1.21.1 has no render state modifiers: `client/enchantments/EnchantmentsClient` hides Translucency armor by emptying `Inventory.armor` in `RenderPlayerEvent.Pre` (lowest priority) and restoring it in `Post`; it writes the list directly so no equip sound plays.
- Addon armor materials that are not dyeable go through the base `Registration.armorMaterial`, called from the feature `load()` (primordial armor): calling it first from the item constructor registers too late. Custom armor models set part visibility in `setupModelAnimations` (`PrimordialArmorModel.animateArmor`). The 1.21.1 server does not know the forward input, so primordial boots move and raise the step height only on the client; fall distance is reduced on both sides.
- 1.21.1 has no `blocks_attacks` component: `platform/WgBlocking.halfBlock` returns the properties unchanged, `PrimordialWeaponItem` here starts using on use with the block animation and `ItemAbilities.SHIELD_BLOCK`, and `WgBlocking` turns `LivingShieldBlockEvent` into the 26.3 behaviour (any angle, half damage, no item damage). The block break event is `BlockEvent.BreakEvent` here (`BreakBlockEvent` on `main`).
- Removal hooks: `main` uses `affectNeighborsAfterRemoval`, here `onRemove` with the new state (infernal blast furnace disassembly). There is no `getEntityInsideCollisionShape`; `entityInside` fires for any entity overlapping the block.
- The base 1.21.1 has no TC4 config, so `platform/BaseConfig.allowMirrors()` returns `true` here.
- 1.21.1 block model rotations only allow 0, ±22.5 and ±45 degrees, `mcformat.py` snaps the spinning wheel's -10 and 15 degree boxes to ±22.5.
- 1.21.1 API in ported code: `GuiGraphics` with `renderBg`/`renderLabels` and a `render` override that draws the background and tooltip, `BlockEntityType.Builder.of(...).build(null)`, `ContainerHelper.loadAllItems(input.tag(), items, input.lookup())` over the base `compat` value IO, `RecipeManager.byKey(ResourceLocation)`, `GameTestHelper.getBlockEntity(pos)`, `ItemInteractionResult` from `useItemOn`, `BucketItem.content`, `javax.annotation.Nullable` (not a type-use annotation: `@Nullable Transfer.FluidTank`). The essentia pump item goes through a `BlockEntityWithoutLevelRenderer` registered in `client/devices/DeviceItemRenderers` (a `minecraft:special` item on `main`), and the pump renderer writes its own vertices with overlay and normal for the entity render types. Block entity renderers keep the old `render(entity, partialTick, poseStack, buffers, light, overlay)` and draw items through the base `client/render/ItemSlot`.

## Checks

```sh
python3 tools/check_seams.py
./gradlew build
./gradlew runGameTestServer
```

## Code style

- Java: 4 spaces, no comments.
- Python: 4 spaces, double quotes, no comments, `pathlib`.
- Commits in English `feat:`/`fix:`, no co-author lines.
