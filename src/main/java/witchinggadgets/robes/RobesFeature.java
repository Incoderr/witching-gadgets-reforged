package witchinggadgets.robes;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.ArmorSlot;
import thaumcraft.platform.Registration;
import thaumcraft.platform.TcArmorMaterial;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModItems;
import witchinggadgets.research.WgResearch;

public final class RobesFeature {
    public static final String RESEARCH = "ADVANCEDROBES";

    public static final TcArmorMaterial BEWITCHED_ROBE_ARMOR = new TcArmorMaterial(
        25, 2, 4, 3, 2, 25, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, TcMaterials.REPAIRS_ENCHANTED_FABRIC, WitchingGadgets.id("bewitched_robe")
    );

    public static final DeferredItem<BewitchedRobeItem> BEWITCHED_ROBE = ModItems.ITEMS.registerItem(
        "bewitched_robe", BewitchedRobeItem::new, properties -> Registration.armor(properties.rarity(Rarity.UNCOMMON), BEWITCHED_ROBE_ARMOR, ArmorSlot.CHESTPLATE)
    );
    public static final DeferredItem<BewitchedRobeItem> BEWITCHED_LEGGINGS = ModItems.ITEMS.registerItem(
        "bewitched_leggings", BewitchedRobeItem::new, properties -> Registration.armor(properties.rarity(Rarity.UNCOMMON), BEWITCHED_ROBE_ARMOR, ArmorSlot.LEGGINGS)
    );

    private RobesFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient cloth = TcIngredient.item("witchinggadgets:bewitched_cloth");
        TcIngredient thaumium = TcIngredient.item("witchinggadgets:thaumium_thread");
        WgRecipes.arcaneShapeless(
            "ADVANCEDROBES_CLOTH", RESEARCH, TcStack.of("witchinggadgets:bewitched_cloth"),
            new AspectList().add(Aspect.ENTROPY, 5).add(Aspect.ORDER, 5),
            TcIngredient.item("witchinggadgets:simple_thread"), thaumium, thaumium, TcIngredient.item("witchinggadgets:golden_thread")
        );
        WgRecipes.arcane(
            "ADVANCEDROBES_CHEST", RESEARCH, TcStack.of("witchinggadgets:bewitched_robe"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10),
            new String[] {" C ", "CRC"},
            'C', cloth,
            'R', TcIngredient.item("thaumcraft:robe_chestplate")
        );
        WgRecipes.arcane(
            "ADVANCEDROBES_LEGS", RESEARCH, TcStack.of("witchinggadgets:bewitched_leggings"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10),
            new String[] {" C ", "CRC"},
            'C', cloth,
            'R', TcIngredient.item("thaumcraft:robe_leggings")
        );
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList().add(Aspect.CLOTH, 3).add(Aspect.MAGIC, 4).add(Aspect.TAINT, 1), 9, 2, 3, TcStack.of("witchinggadgets:bewitched_cloth"))
            .setParents("WGFAKEENCHFABRIC", "SPINNINGWHEEL")
            .setPages(WgResearch.text("1"), WgResearch.arcane("ADVANCEDROBES_CLOTH"), WgResearch.arcane("ADVANCEDROBES_CHEST"), WgResearch.arcane("ADVANCEDROBES_LEGS"))
            .setConcealed()
            .setSecondary()
            .registerResearchItem();
    }
}
