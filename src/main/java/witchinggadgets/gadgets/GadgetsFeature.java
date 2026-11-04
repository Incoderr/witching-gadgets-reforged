package witchinggadgets.gadgets;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.ArcaneCrafting;
import thaumcraft.crafting.DynamicArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.platform.WgStackKey;
import witchinggadgets.registry.ModItems;
import witchinggadgets.research.WgResearch;

public final class GadgetsFeature {
    public static final String SCAN_DATA = "witchinggadgets:scan";

    public static final WgStackKey<Boolean> GUIDE_ACTIVE = WgStackKey.register("guide_active", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final WgStackKey<List<GuidePoint>> GUIDE_PATH = WgStackKey.register("guide_path", GuidePoint.LIST_CODEC, GuidePoint.LIST_STREAM_CODEC);
    public static final WgStackKey<ScanSnapshot> SCAN = WgStackKey.register("scan", ScanSnapshot.CODEC, ScanSnapshot.STREAM_CODEC);

    public static final DeferredItem<ScanCameraItem> SCAN_CAMERA = ModItems.ITEMS.registerItem("scan_camera", ScanCameraItem::new, properties -> properties.stacksTo(1));

    private static final TcIngredient BLACK_DYE = TcIngredient.anyOf(TcIngredient.tag("c:dyes/black"), TcIngredient.item("minecraft:ink_sac"));

    private GadgetsFeature() {
    }

    public static void load() {
        TcStack.registerDataMatcher(SCAN_DATA, (stack, value) -> SCAN.has(stack));
        ArcaneCrafting.registerDynamic(new PhotoDeveloping());
    }

    public static Item photoPlate() {
        return MaterialsFeature.PHOTO_PLATE.get();
    }

    public static void recipes() {
        TcIngredient stick = TcIngredient.tag("c:rods/wooden");
        WgRecipes.arcane(
            "CALCULATOR", "CALCULATOR", TcStack.of("witchinggadgets:arcane_abacus"),
            new AspectList().add(Aspect.ORDER, 10),
            new String[] {"srs", "sbs", "sgs"},
            's', stick,
            'r', TcIngredient.tag("c:dyes/red"),
            'b', TcIngredient.tag("c:dyes/blue"),
            'g', TcIngredient.tag("c:dyes/green")
        );
        WgRecipes.arcane(
            "SCANCAMERA", "SCANCAMERA", TcStack.of("witchinggadgets:scan_camera"),
            new AspectList().add(Aspect.AIR, 20).add(Aspect.EARTH, 20).add(Aspect.ORDER, 10),
            new String[] {"wl ", "pmt", "wl "},
            't', TcIngredient.item("thaumcraft:thaumometer"),
            'm', TcIngredient.item("thaumcraft:mirrored_glass"),
            'p', TcIngredient.item("minecraft:glass_pane"),
            'w', TcIngredient.item("thaumcraft:greatwood_planks"),
            'l', TcIngredient.item("minecraft:leather")
        );
        WgRecipes.arcaneShapeless(
            "SCANCAMERA_DEVELOP", "SCANCAMERA", TcStack.of("witchinggadgets:developed_photo"),
            PhotoDeveloping.aspects(1),
            TcIngredient.of(TcStack.withData("witchinggadgets:photo_plate", 1, Map.of(SCAN_DATA, "1"))),
            BLACK_DYE,
            TcIngredient.item("minecraft:paper")
        );
        WgRecipes.vanilla("SCANCAMERA_CLEARPLATE", "SCANCAMERA", "clear_photo_plate", TcStack.of("thaumcraft:mirrored_glass"));
        WgRecipes.infusion(
            "LABYRINTHSTRING", "LABYRINTHSTRING", TcStack.of("witchinggadgets:guiding_string"), 2,
            new AspectList().add(Aspect.TRAVEL, 4).add(Aspect.MIND, 6).add(Aspect.TOOL, 2),
            TcIngredient.item("thaumcraft:greatwood_log"),
            TcIngredient.item("minecraft:ender_pearl"),
            TcIngredient.item("witchinggadgets:simple_thread"),
            TcIngredient.item("witchinggadgets:simple_thread"),
            TcIngredient.item("witchinggadgets:simple_thread")
        );
    }

    public static void research() {
        WgResearch.item("SCANCAMERA", new AspectList().add(Aspect.SENSES, 1).add(Aspect.MIND, 1).add(Aspect.SOUL, 1).add(Aspect.CRYSTAL, 1), 2, -4, 2, TcStack.of("witchinggadgets:scan_camera"))
            .setPages(WgResearch.text("1"), WgResearch.arcane("SCANCAMERA"), WgResearch.text("2"), WgResearch.arcane("SCANCAMERA_DEVELOP"), WgResearch.recipe("SCANCAMERA_CLEARPLATE"))
            .registerResearchItem();
        WgResearch.item("CALCULATOR", new AspectList().add(Aspect.TOOL, 1).add(Aspect.MIND, 1).add(Aspect.MECHANISM, 1), 1, -5, 1, TcStack.of("witchinggadgets:arcane_abacus"))
            .setParents("INFUSION")
            .setPages(WgResearch.text("1"), WgResearch.arcane("CALCULATOR"))
            .registerResearchItem();
        WgResearch.item("LABYRINTHSTRING", new AspectList().add(Aspect.TOOL, 1).add(Aspect.MIND, 1).add(Aspect.TRAVEL, 1), 2, -6, 1, TcStack.of("witchinggadgets:guiding_string"))
            .setParents("INFUSION")
            .setPages(WgResearch.text("1"), WgResearch.infusion("LABYRINTHSTRING"))
            .registerResearchItem();
    }

    public static final class PhotoDeveloping implements DynamicArcaneRecipe {
        public static AspectList aspects(int paper) {
            return new AspectList().add(Aspect.AIR, 5 * paper).add(Aspect.WATER, 5 * paper).add(Aspect.ORDER, 5 * paper);
        }

        @Override
        public boolean matches(List<ItemStack> grid, Player player) {
            ItemStack plate = ItemStack.EMPTY;
            int paper = 0;
            int ink = 0;
            for (ItemStack stack : grid) {
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.is(photoPlate())) {
                    if (!SCAN.has(stack) || !plate.isEmpty()) {
                        return false;
                    }
                    plate = stack;
                } else if (stack.is(Items.PAPER)) {
                    paper++;
                } else if (BLACK_DYE.test(stack)) {
                    ink++;
                } else {
                    return false;
                }
            }
            return !plate.isEmpty() && paper > 0 && ink > 0 && paper == ink;
        }

        @Override
        public ItemStack result(List<ItemStack> grid) {
            ItemStack plate = ItemStack.EMPTY;
            List<ItemStack> paper = new ArrayList<>();
            for (ItemStack stack : grid) {
                if (stack.is(photoPlate())) {
                    plate = stack;
                } else if (stack.is(Items.PAPER)) {
                    paper.add(stack);
                }
            }
            ItemStack developed = new ItemStack(MaterialsFeature.DEVELOPED_PHOTO.get(), paper.size());
            ScanSnapshot scan = SCAN.get(plate);
            if (scan != null) {
                SCAN.set(developed, scan);
            }
            return developed;
        }

        @Override
        public AspectList aspects(List<ItemStack> grid) {
            int paper = 0;
            for (ItemStack stack : grid) {
                if (stack.is(Items.PAPER)) {
                    paper++;
                }
            }
            return aspects(paper);
        }
    }
}
