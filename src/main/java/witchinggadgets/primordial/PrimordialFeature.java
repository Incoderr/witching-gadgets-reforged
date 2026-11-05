package witchinggadgets.primordial;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
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
import thaumcraft.platform.TcToolMaterial;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.platform.WgBlocking;
import witchinggadgets.platform.WgStackKey;
import witchinggadgets.registry.ModItems;
import witchinggadgets.research.WgResearch;

import java.util.Map;

public final class PrimordialFeature {
    public static final String PEARLS_DATA = "witchinggadgets:primordial_pearls";

    public static final WgStackKey<Integer> ABILITY = WgStackKey.register("primordial_ability", Codec.INT, ByteBufCodecs.VAR_INT);
    public static final WgStackKey<Boolean> PEARLS_PENDING = WgStackKey.register("primordial_pearls", Codec.BOOL, ByteBufCodecs.BOOL);

    public static final TcArmorMaterial PRIMORDIAL_ARMOR = new TcArmorMaterial(
        40, 3, 7, 6, 3, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, TcMaterials.REPAIRS_VOID_METAL, WitchingGadgets.id("primordial_armor")
    );

    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_HELMET = armor("primordial_helmet", ArmorSlot.HELMET);
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_CHESTPLATE = armor("primordial_chestplate", ArmorSlot.CHESTPLATE);
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_LEGGINGS = armor("primordial_leggings", ArmorSlot.LEGGINGS);
    public static final DeferredItem<PrimordialArmorItem> PRIMORDIAL_BOOTS = armor("primordial_boots", ArmorSlot.BOOTS);

    public static final TcToolMaterial PRIMORDIAL_TOOL = new TcToolMaterial(1500, 8.0F, 6.0F, 25, TcMaterials.REPAIRS_VOID_METAL, BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

    public static final DeferredItem<PrimordialWeaponItem> PRIMORDIAL_SWORD = ModItems.ITEMS.registerItem(
        "primordial_sword", properties -> new PrimordialWeaponItem(PrimordialWeaponItem.Kind.CLAYMORE, properties),
        properties -> WgBlocking.halfBlock(Registration.sword(properties.rarity(Rarity.EPIC), PRIMORDIAL_TOOL, 4.0F, -2.4F))
    );
    public static final DeferredItem<PrimordialWeaponItem> PRIMORDIAL_HAMMER = ModItems.ITEMS.registerItem(
        "primordial_hammer", properties -> new PrimordialWeaponItem(PrimordialWeaponItem.Kind.HAMMER, properties),
        properties -> WgBlocking.halfBlock(Registration.pickaxe(properties.rarity(Rarity.EPIC), PRIMORDIAL_TOOL, 2.0F, -2.8F))
    );
    public static final DeferredItem<PrimordialWeaponItem> PRIMORDIAL_AXE = ModItems.ITEMS.registerItem(
        "primordial_axe", properties -> new PrimordialWeaponItem(PrimordialWeaponItem.Kind.GREATAXE, properties),
        properties -> WgBlocking.halfBlock(Registration.axe(properties.rarity(Rarity.EPIC), PRIMORDIAL_TOOL, 3.0F, -3.0F))
    );

    private PrimordialFeature() {
    }

    private static DeferredItem<PrimordialArmorItem> armor(String name, ArmorSlot slot) {
        return ModItems.ITEMS.registerItem(name, properties -> new PrimordialArmorItem(PRIMORDIAL_ARMOR, slot, properties), properties -> Registration.armor(properties.rarity(Rarity.EPIC), PRIMORDIAL_ARMOR, slot));
    }

    public static void load() {
        Registration.armorMaterial(PRIMORDIAL_ARMOR);
        TcStack.registerDataMatcher(PEARLS_DATA, (stack, value) -> PEARLS_PENDING.getOrDefault(stack, false) == Boolean.parseBoolean(value));
        TcStack.registerDataHandler(PEARLS_DATA, (stack, value) -> PEARLS_PENDING.set(stack, Boolean.parseBoolean(value) ? Boolean.TRUE : null));
    }

    public static void recipes() {
        WgRecipes.infusion(
            "EMPOWERPEARL", "EMPOWERPEARL", TcStack.of("thaumcraft:primordial_pearl"), 3,
            new AspectList().add(Aspect.AIR, 16).add(Aspect.FIRE, 16).add(Aspect.EARTH, 16).add(Aspect.WATER, 16).add(Aspect.ORDER, 16).add(Aspect.ENTROPY, 16),
            TcIngredient.item("witchinggadgets:powerless_pearl"),
            TcIngredient.item("thaumcraft:air_shard"), TcIngredient.item("thaumcraft:fire_shard"), TcIngredient.item("thaumcraft:water_shard"),
            TcIngredient.item("thaumcraft:earth_shard"), TcIngredient.item("thaumcraft:order_shard"), TcIngredient.item("thaumcraft:entropy_shard")
        );
        armorRecipe("_HELMET", "witchinggadgets:primordial_helmet", "thaumcraft:fortress_helmet");
        armorRecipe("_CUIRASS", "witchinggadgets:primordial_chestplate", "thaumcraft:fortress_chestplate");
        armorRecipe("_GREAVES", "witchinggadgets:primordial_leggings", "thaumcraft:fortress_leggings");
        armorRecipe("_BOOTS", "witchinggadgets:primordial_boots", "thaumcraft:boots_traveller");
        weaponRecipe("_CLAYMORE", "witchinggadgets:primordial_sword", Aspect.WEAPON, 64, null, "thaumcraft:void_sword", "thaumcraft:void_sword");
        weaponRecipe("_HAMMER", "witchinggadgets:primordial_hammer", Aspect.TOOL, 32, Aspect.WEAPON, "thaumcraft:void_pickaxe", "thaumcraft:void_sword");
        weaponRecipe("_GREATAXE", "witchinggadgets:primordial_axe", Aspect.TOOL, 32, Aspect.WEAPON, "thaumcraft:void_axe", "thaumcraft:void_axe");
    }

    private static void weaponRecipe(String suffix, String result, Aspect first, int amount, Aspect second, String tool, String last) {
        TcIngredient pearl = TcIngredient.item("thaumcraft:primordial_pearl");
        TcIngredient wisp = TcIngredient.item("thaumcraft:wisp_essence");
        TcIngredient charm = TcIngredient.item("thaumcraft:primal_charm");
        AspectList aspects = new AspectList().add(Aspect.MAGIC, 64).add(Aspect.METAL, 128).add(first, amount);
        if (second != null) {
            aspects.add(second, 32);
        }
        aspects.add(Aspect.AIR, 32).add(Aspect.FIRE, 32).add(Aspect.WATER, 32).add(Aspect.EARTH, 32).add(Aspect.ORDER, 32).add(Aspect.ENTROPY, 32);
        WgRecipes.infusion(
            "PRIMORDIALWEAPONRY" + suffix, "PRIMORDIALWEAPONRY", TcStack.withData(result, 1, Map.of(PEARLS_DATA, "true")), 10, aspects,
            TcIngredient.item("witchinggadgets:void_metal_block"),
            wisp, TcIngredient.item(tool), wisp, pearl, wisp, charm, TcIngredient.item("thaumcraft:greatwood_rod"), charm, wisp, pearl, wisp, TcIngredient.item(last)
        );
    }

    private static void armorRecipe(String suffix, String result, String base) {
        TcIngredient pearl = TcIngredient.item("thaumcraft:primordial_pearl");
        TcIngredient wisp = TcIngredient.item("thaumcraft:wisp_essence");
        TcIngredient charm = TcIngredient.item("thaumcraft:primal_charm");
        TcIngredient ingot = TcIngredient.item("thaumcraft:void_metal_ingot");
        WgRecipes.infusion(
            "PRIMORDIALARMOR" + suffix, "PRIMORDIALARMOR", TcStack.withData(result, 1, Map.of(PEARLS_DATA, "true")), 10,
            new AspectList().add(Aspect.MAGIC, 64).add(Aspect.METAL, 128).add(Aspect.ARMOR, 32).add(Aspect.AIR, 32).add(Aspect.FIRE, 32)
                .add(Aspect.WATER, 32).add(Aspect.EARTH, 32).add(Aspect.ORDER, 32).add(Aspect.ENTROPY, 32),
            TcIngredient.item("witchinggadgets:void_metal_block"),
            pearl, wisp, charm, wisp, pearl, ingot, TcIngredient.item(base), ingot, pearl, wisp, charm, wisp
        );
    }

    public static void research() {
        WgResearch.item("PRIMORDIALGEARSET", primals(new AspectList().add(Aspect.MAGIC, 1).add(Aspect.ENERGY, 1)), -2, 3, 3, WitchingGadgets.id("textures/gui/research/icon_primordialgear.png"))
            .setParents("VOIDMETAL", "WGFAKEPRIMPEARL")
            .setPages(WgResearch.text("1"), WgResearch.text("2"))
            .registerResearchItem();
        WgResearch.item("EMPOWERPEARL", new AspectList().add(Aspect.MAGIC, 8).add(Aspect.CRYSTAL, 4).add(Aspect.VOID, 4).add(Aspect.ENERGY, 4), -3, 5, 1, TcStack.of("witchinggadgets:powerless_pearl"))
            .setSecondary()
            .setParents("WGFAKEPRIMPEARL")
            .setItemTriggers("witchinggadgets:powerless_pearl")
            .setHidden()
            .setPages(WgResearch.text("1"), WgResearch.infusion("EMPOWERPEARL"))
            .registerResearchItem();
        WgResearch.item("PRIMORDIALWEAPONRY", new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.WEAPON, 1).add(Aspect.TOOL, 1).add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.MAGIC, 2).add(Aspect.EARTH, 1).add(Aspect.ORDER, 1), -4, 2, 3, WitchingGadgets.id("textures/gui/research/icon_primordialweaponry.png"))
            .setParents("PRIMORDIALGEARSET")
            .setConcealed()
            .setPages(
                WgResearch.text("1"),
                WgResearch.text("2"),
                WgResearch.text("3"),
                WgResearch.infusion("PRIMORDIALWEAPONRY_CLAYMORE"),
                WgResearch.infusion("PRIMORDIALWEAPONRY_HAMMER"),
                WgResearch.infusion("PRIMORDIALWEAPONRY_GREATAXE")
            )
            .registerResearchItem();
        WgResearch.item("PRIMORDIALARMOR", primals(new AspectList().add(Aspect.ARMOR, 1).add(Aspect.MAGIC, 1)), -1, 5, 3, WitchingGadgets.id("textures/gui/research/icon_primordialarmor.png"))
            .setParents("PRIMORDIALGEARSET", "ARMORFORTRESS")
            .setConcealed()
            .setPages(
                WgResearch.text("1"),
                WgResearch.text("2"),
                WgResearch.infusion("PRIMORDIALARMOR_HELMET"),
                WgResearch.infusion("PRIMORDIALARMOR_CUIRASS"),
                WgResearch.infusion("PRIMORDIALARMOR_GREAVES"),
                WgResearch.infusion("PRIMORDIALARMOR_BOOTS")
            )
            .registerResearchItem();
    }

    private static AspectList primals(AspectList list) {
        for (Aspect aspect : PrimordialGear.ABILITIES) {
            list.add(aspect, 1);
        }
        return list;
    }
}
