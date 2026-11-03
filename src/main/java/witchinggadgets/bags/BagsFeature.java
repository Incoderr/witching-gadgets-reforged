package witchinggadgets.bags;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.platform.WgStackKey;
import witchinggadgets.registry.ModItems;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.research.WgResearch;

public final class BagsFeature {
    public static final String RESEARCH = "BAGOFTRICKS";

    public static final WgStackKey<ItemContainerContents> CONTENTS = WgStackKey.register("bag_contents", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);
    public static final WgStackKey<String> OWNER = WgStackKey.register("bag_owner", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final WgStackKey<Boolean> UNLOCKED = WgStackKey.register("bag_unlocked", Codec.BOOL, ByteBufCodecs.BOOL);

    public static final DeferredItem<BagItem> BAG_OF_TRICKS = bag("bag_of_tricks", BagKind.TRICKS, Rarity.RARE);
    public static final DeferredItem<BagItem> VOID_BAG = bag("void_bag", BagKind.VOID, Rarity.EPIC);
    public static final DeferredItem<BagItem> ENDER_BAG = bag("ender_bag", BagKind.ENDER, Rarity.RARE);
    public static final DeferredItem<BagItem> HUNGRY_BAG = bag("hungry_bag", BagKind.HUNGRY, Rarity.RARE);

    public static final Supplier<MenuType<BagMenu>> BAG_MENU = ModMenus.MENUS.register("bag", () -> Menus.simple(BagMenu::new));
    public static final Supplier<MenuType<VoidBagMenu>> VOID_BAG_MENU = ModMenus.MENUS.register("void_bag", () -> Menus.simple(VoidBagMenu::new));

    private BagsFeature() {
    }

    private static DeferredItem<BagItem> bag(String name, BagKind kind, Rarity rarity) {
        return ModItems.ITEMS.registerItem(name, properties -> new BagItem(kind, properties), properties -> properties.rarity(rarity));
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient thread = TcIngredient.item("witchinggadgets:simple_thread");
        WgRecipes.arcaneShapeless(
            "BAGOFTRICKS_CLOTH", RESEARCH, TcStack.of("witchinggadgets:spacious_cloth", 2),
            new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.AIR, 5),
            thread, thread, thread, TcIngredient.item("witchinggadgets:thaumium_thread")
        );
        WgRecipes.arcane(
            "BAGOFTRICKS_BAG", RESEARCH, TcStack.of("witchinggadgets:bag_of_tricks"),
            new AspectList().add(Aspect.ORDER, 20).add(Aspect.AIR, 20),
            new String[] {"C C", "C C", "CCC"},
            'C', TcIngredient.item("witchinggadgets:spacious_cloth")
        );
        TcIngredient bag = TcIngredient.item("witchinggadgets:bag_of_tricks");
        TcIngredient spacious = TcIngredient.item("witchinggadgets:spacious_cloth");
        TcIngredient bewitched = TcIngredient.item("witchinggadgets:bewitched_cloth");
        TcIngredient voidSeed = TcIngredient.item("thaumcraft:void_seed");
        WgRecipes.arcane(
            "HUNGERBAG", "HUNGERBAG", TcStack.of("witchinggadgets:hungry_bag"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.AIR, 10),
            new String[] {" H ", "CBC"},
            'C', spacious,
            'H', TcIngredient.item("thaumcraft:hungry_chest"),
            'B', bag
        );
        WgRecipes.infusion(
            "ENDERBAG", TcStack.of("witchinggadgets:ender_bag"), 3,
            new AspectList().add(Aspect.VOID, 8).add(Aspect.ELDRITCH, 4).add(Aspect.MAGIC, 4),
            bag,
            TcIngredient.item("minecraft:ender_chest"), bewitched, TcIngredient.item("minecraft:ender_eye"), bewitched
        );
        WgRecipes.infusion(
            "VOIDBAG", TcStack.of("witchinggadgets:void_bag"), 4,
            new AspectList().add(Aspect.VOID, 16).add(Aspect.ELDRITCH, 16).add(Aspect.ENTROPY, 32),
            bag,
            voidSeed, spacious, voidSeed, spacious
        );
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList().add(Aspect.CLOTH, 2).add(Aspect.VOID, 2).add(Aspect.HUNGER, 2), 7, 2, 1, TcStack.of("witchinggadgets:bag_of_tricks"))
            .setParents("SPINNINGWHEEL")
            .setConcealed()
            .setPages(WgResearch.text("1"), WgResearch.arcane("BAGOFTRICKS_CLOTH"), WgResearch.arcane("BAGOFTRICKS_BAG"))
            .registerResearchItem();
        WgResearch.item("HUNGERBAG", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.HUNGER, 3).add(Aspect.VOID, 3), 6, 1, 1, TcStack.of("witchinggadgets:hungry_bag"))
            .setParents(RESEARCH, "HUNGRYCHEST")
            .setConcealed()
            .setSecondary()
            .setPages(WgResearch.text("1"), WgResearch.arcane("HUNGERBAG"))
            .registerResearchItem();
        WgResearch.item("ENDERBAG", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.ELDRITCH, 3).add(Aspect.VOID, 3), 7, 4, 1, TcStack.of("witchinggadgets:ender_bag"))
            .setParents(RESEARCH, "INFUSION")
            .setHidden()
            .setSecondary()
            .setItemTriggers("minecraft:ender_chest")
            .setAspectTriggers(Aspect.ELDRITCH)
            .setPages(WgResearch.text("1"), WgResearch.infusion("ENDERBAG"))
            .registerResearchItem();
        WgResearch.item("VOIDBAG", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.VOID, 3).add(Aspect.ENTROPY, 5).add(Aspect.ELDRITCH, 3), 2, 2, 1, TcStack.of("witchinggadgets:void_bag"))
            .setParents(RESEARCH, "WGFAKEELDRITCHMINOR")
            .setConcealed()
            .setSecondary()
            .setPages(WgResearch.text("1"), WgResearch.infusion("VOIDBAG"))
            .registerResearchItem();
    }
}
