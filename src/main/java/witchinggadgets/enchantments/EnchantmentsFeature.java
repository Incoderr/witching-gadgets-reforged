package witchinggadgets.enchantments;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.research.WgResearch;

import java.util.Map;

public final class EnchantmentsFeature {
    public static final ResourceKey<Enchantment> INVISIBLE_GEAR = key("invisible_gear");
    public static final ResourceKey<Enchantment> UNVEILING = key("unveiling");
    public static final ResourceKey<Enchantment> STEALTH = key("stealth");
    public static final ResourceKey<Enchantment> BACKSTAB = key("backstab");
    public static final ResourceKey<Enchantment> RIDE_PROTECT = key("ride_protect");

    private EnchantmentsFeature() {
    }

    public static void load() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, WitchingGadgets.id(name));
    }

    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().is(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static int level(LivingEntity entity, EquipmentSlot slot, ResourceKey<Enchantment> key) {
        return level(entity.getItemBySlot(slot), key);
    }

    public static boolean unveils(LivingEntity entity) {
        return level(entity, EquipmentSlot.HEAD, UNVEILING) > 0;
    }

    public static void recipes() {
        TcIngredient salis = TcIngredient.item("thaumcraft:salis_mundus");
        TcIngredient invisibility = TcIngredient.of(TcStack.withData("minecraft:potion", 1, Map.of(CloaksFeature.POTION_DATA, "minecraft:invisibility")));
        WgRecipes.enchantment(
            "ENCH_INVISIBLEGEAR", "ENCH_INVISIBLEGEAR", WitchingGadgets.id("invisible_gear"), 2,
            new AspectList().add(Aspect.DARKNESS, 4).add(Aspect.CRYSTAL, 8).add(Aspect.MAGIC, 8),
            TcIngredient.item("minecraft:quartz"), salis, TcIngredient.item("witchinggadgets:gemstone_dust")
        );
        WgRecipes.enchantment(
            "ENCH_UNVEILING", "ENCH_UNVEILING", WitchingGadgets.id("unveiling"), 2,
            new AspectList().add(Aspect.LIGHT, 4).add(Aspect.SENSES, 8).add(Aspect.MAGIC, 8),
            TcIngredient.item("minecraft:golden_carrot"), salis
        );
        WgRecipes.enchantment(
            "ENCH_STEALTH", "ENCH_STEALTH", WitchingGadgets.id("stealth"), 2,
            new AspectList().add(Aspect.MOTION, 6).add(Aspect.DARKNESS, 8).add(Aspect.MAGIC, 8),
            invisibility, salis
        );
        WgRecipes.enchantment(
            "ENCH_BACKSTAB", "ENCH_BACKSTAB", WitchingGadgets.id("backstab"), 3,
            new AspectList().add(Aspect.WEAPON, 12).add(Aspect.DARKNESS, 8).add(Aspect.MAGIC, 4),
            TcIngredient.item("minecraft:iron_sword"), invisibility, salis
        );
        WgRecipes.enchantment(
            "ENCH_RIDEPROTECT", "ENCH_RIDEPROTECT", WitchingGadgets.id("ride_protect"), 3,
            new AspectList().add(Aspect.ARMOR, 12).add(Aspect.TRAP, 8).add(Aspect.MAGIC, 4),
            salis, TcIngredient.item("minecraft:piston"), TcIngredient.item("minecraft:piston")
        );
    }

    public static void research() {
        research("ENCH_INVISIBLEGEAR", "invisgear", Aspect.CRYSTAL, Aspect.DARKNESS, -10, 1, "WGFAKEINFUSIONENCHANTMENT");
        research("ENCH_UNVEILING", "unveiling", Aspect.SENSES, Aspect.LIGHT, -10, 2, "WGFAKEINFUSIONENCHANTMENT");
        research("ENCH_STEALTH", "stealth", Aspect.MOTION, Aspect.DARKNESS, -9, 3, "WGFAKEINFUSIONENCHANTMENT");
        research("ENCH_BACKSTAB", "backstab", Aspect.WEAPON, Aspect.DARKNESS, -11, 4, "ENCH_STEALTH");
        research("ENCH_RIDEPROTECT", "rideprotect", Aspect.TRAP, Aspect.ARMOR, -11, 0, "WGFAKEINFUSIONENCHANTMENT");
    }

    private static void research(String key, String icon, Aspect first, Aspect second, int column, int row, String parent) {
        WgResearch.item(key, new AspectList().add(Aspect.MAGIC, 2).add(first, 4).add(second, 4), column, row, 2, WitchingGadgets.id("textures/gui/research/icon_ench_" + icon + ".png"))
            .setParents(parent)
            .setConcealed()
            .setSecondary()
            .setPages(WgResearch.text("1"), WgResearch.enchant(key))
            .registerResearchItem();
    }
}
