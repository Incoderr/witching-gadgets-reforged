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

    public static final DeferredItem<BagItem> BAG_OF_TRICKS = ModItems.ITEMS.registerItem("bag_of_tricks", BagItem::new, properties -> properties.rarity(Rarity.RARE));

    public static final Supplier<MenuType<BagMenu>> BAG_MENU = ModMenus.MENUS.register("bag", () -> Menus.simple(BagMenu::new));

    private BagsFeature() {
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
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList().add(Aspect.CLOTH, 2).add(Aspect.VOID, 2).add(Aspect.HUNGER, 2), 7, 2, 1, TcStack.of("witchinggadgets:bag_of_tricks"))
            .setParents("SPINNINGWHEEL")
            .setConcealed()
            .setPages(WgResearch.text("1"), WgResearch.arcane("BAGOFTRICKS_CLOTH"), WgResearch.arcane("BAGOFTRICKS_BAG"))
            .registerResearchItem();
    }
}
