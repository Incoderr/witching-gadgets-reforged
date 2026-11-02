package witchinggadgets.cloaks;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
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

public final class CloaksFeature {
    public static final String POTION_DATA = "witchinggadgets:potion";

    public static final WgStackKey<Boolean> SPECTRAL = WgStackKey.register("cloak_spectral", Codec.BOOL, ByteBufCodecs.BOOL);
    public static final WgStackKey<Integer> WOLF_POTION = WgStackKey.register("cloak_wolf_potion", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final WgStackKey<ItemContainerContents> STORAGE = WgStackKey.register("cloak_storage", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);

    public static final DeferredItem<CloakItem> CLOAK = cloak("cloak", CloakVariant.STANDARD, CloakItem::new);
    public static final DeferredItem<CloakItem> SPECTRAL_CLOAK = cloak("spectral_cloak", CloakVariant.SPECTRAL, CloakItem::new);
    public static final DeferredItem<CloakItem> STORAGE_CLOAK = cloak("storage_cloak", CloakVariant.STORAGE, CloakItem::new);
    public static final DeferredItem<CloakItem> WOLF_CLOAK = cloak("wolf_cloak", CloakVariant.WOLF, CloakItem::new);
    public static final DeferredItem<KamaItem> KAMA = cloak("kama", CloakVariant.STANDARD, KamaItem::new);
    public static final DeferredItem<KamaItem> SPECTRAL_KAMA = cloak("spectral_kama", CloakVariant.SPECTRAL, KamaItem::new);
    public static final DeferredItem<KamaItem> STORAGE_KAMA = cloak("storage_kama", CloakVariant.STORAGE, KamaItem::new);
    public static final DeferredItem<KamaItem> WOLF_KAMA = cloak("wolf_kama", CloakVariant.WOLF, KamaItem::new);

    public static final Supplier<MenuType<CloakStorageMenu>> STORAGE_MENU = ModMenus.MENUS.register(
        "cloak_storage",
        () -> Menus.simple(CloakStorageMenu::new)
    );

    private static final String[] CLOAKS = {"cloak", "spectral_cloak", "storage_cloak", "wolf_cloak"};
    private static final String[] KAMAS = {"kama", "spectral_kama", "storage_kama", "wolf_kama"};

    private CloaksFeature() {
    }

    private static <T extends Item> DeferredItem<T> cloak(String name, CloakVariant variant, BiFunction<CloakVariant, Item.Properties, T> factory) {
        return ModItems.ITEMS.registerItem(name, properties -> factory.apply(variant, properties), properties -> properties);
    }

    public static void load() {
        TcStack.registerDataMatcher(POTION_DATA, (stack, value) -> {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            return contents != null && contents.potion().map(potion -> potion.is(ResourceLocation.parse(value))).orElse(false);
        });
        TcStack.registerDataHandler(POTION_DATA, (stack, value) -> BuiltInRegistries.POTION.getHolder(ResourceLocation.parse(value))
            .ifPresent(potion -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion))));
    }

    public static void recipes() {
        TcIngredient cloak = TcIngredient.item("witchinggadgets:cloak");
        WgRecipes.arcane(
            "CLOAK", "CLOAK", TcStack.of("witchinggadgets:cloak"),
            new AspectList().add(Aspect.AIR, 7),
            new String[] {" F ", "FFF", "FFF"},
            'F', TcIngredient.item("thaumcraft:enchanted_fabric")
        );
        WgRecipes.arcane(
            "CLOAK_STORAGE", "CLOAK_STORAGE", TcStack.of("witchinggadgets:storage_cloak"),
            new AspectList().add(Aspect.AIR, 20).add(Aspect.ENTROPY, 15).add(Aspect.ORDER, 10),
            new String[] {"SCS", " B "},
            'C', cloak,
            'S', TcIngredient.item("witchinggadgets:spacious_cloth"),
            'B', TcIngredient.item("witchinggadgets:bag_of_tricks")
        );
        WgRecipes.arcane(
            "CLOAK_WOLF", "CLOAK_WOLF", TcStack.of("witchinggadgets:wolf_cloak"),
            new AspectList().add(Aspect.FIRE, 10).add(Aspect.ENTROPY, 20).add(Aspect.EARTH, 15),
            new String[] {" W ", "WCW"},
            'C', cloak,
            'W', TcIngredient.item("witchinggadgets:wolf_pelt")
        );
        WgRecipes.infusion(
            "CLOAK_SPECTRAL", TcStack.of("witchinggadgets:spectral_cloak"), 3,
            new AspectList().add(Aspect.SOUL, 8).add(Aspect.TRAVEL, 8).add(Aspect.ELDRITCH, 4).add(Aspect.SENSES, 4),
            cloak,
            TcIngredient.of(TcStack.withData("minecraft:potion", 1, Map.of(POTION_DATA, "minecraft:long_invisibility"))),
            TcIngredient.item("witchinggadgets:bewitched_cloth"),
            TcIngredient.item("minecraft:ender_pearl"),
            TcIngredient.item("witchinggadgets:bewitched_cloth")
        );
        for (int index = 0; index < CLOAKS.length; index++) {
            WgRecipes.arcane(
                "CLOAKKAMA_" + index, "CLOAKKAMA", TcStack.of("witchinggadgets:" + KAMAS[index]),
                new AspectList().add(Aspect.AIR, 5).add(Aspect.ORDER, 5),
                new String[] {"B", "C"},
                'B', TcIngredient.item("thaumcraft:mundane_belt"),
                'C', TcIngredient.item("witchinggadgets:" + CLOAKS[index])
            );
        }
    }

    public static void research() {
        WgResearch.item("CLOAK", new AspectList().add(Aspect.CLOTH, 1).add(Aspect.AIR, 1).add(Aspect.ARMOR, 1).add(Aspect.MAGIC, 1), 8, -2, 2, TcStack.of("witchinggadgets:cloak"))
            .setParentsHidden("ENCHFABRIC")
            .setConcealed()
            .setPages(WgResearch.text("1"), WgResearch.arcane("CLOAK"))
            .registerResearchItem();
        WgResearch.item("CLOAK_SPECTRAL", new AspectList().add(Aspect.CLOTH, 4).add(Aspect.TRAVEL, 2).add(Aspect.SOUL, 2).add(Aspect.DARKNESS, 4), 10, -1, 3, TcStack.of("witchinggadgets:spectral_cloak"))
            .setParents("CLOAK")
            .setParentsHidden("INFUSION")
            .setPages(WgResearch.text("1"), WgResearch.infusion("CLOAK_SPECTRAL"), WgResearch.text("2"))
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        WgResearch.item("CLOAK_STORAGE", new AspectList().add(Aspect.CLOTH, 4).add(Aspect.VOID, 6).add(Aspect.HUNGER, 4), 10, -3, 3, TcStack.of("witchinggadgets:storage_cloak"))
            .setParents("CLOAK")
            .setParentsHidden("BAGOFTRICKS")
            .setPages(WgResearch.text("1"), WgResearch.arcane("CLOAK_STORAGE"))
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        WgResearch.item("CLOAK_WOLF", new AspectList().add(Aspect.CLOTH, 4).add(Aspect.BEAST, 4).add(Aspect.HUNGER, 4), 6, -1, 3, TcStack.of("witchinggadgets:wolf_cloak"))
            .setParents("CLOAK")
            .setPages(WgResearch.text("1"), WgResearch.arcane("CLOAK_WOLF"))
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
        WgResearch.item("CLOAKKAMA", new AspectList().add(Aspect.CLOTH, 2).add(Aspect.ARMOR, 2), 9, -5, 1, TcStack.of("witchinggadgets:kama"))
            .setParents("WGBAUBLES", "CLOAK")
            .setConcealed()
            .setSecondary()
            .setPages(WgResearch.text("1"), WgResearch.arcane("CLOAKKAMA_0", "CLOAKKAMA_1", "CLOAKKAMA_2", "CLOAKKAMA_3"))
            .registerResearchItem();
    }
}
