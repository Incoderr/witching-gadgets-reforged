package witchinggadgets.baubles;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.WgRegistries;
import witchinggadgets.platform.WgStackKey;
import witchinggadgets.registry.ModItems;
import witchinggadgets.research.WgResearch;

public final class BaublesFeature {
    public static final String RESEARCH = "WGBAUBLES";
    public static final String LUCKY_COIN = "witchinggadgets:lucky_coin";

    public static final WgStackKey<String> TITLE = WgStackKey.register("title", Codec.STRING, ByteBufCodecs.STRING_UTF8);

    public static final DeferredItem<WearableItem> DOUBLE_JUMP_SHOULDERS = wearable("double_jump_shoulders", GearSlot.PAULDRON);
    public static final DeferredItem<WearableItem> KNOCKBACK_SHOULDERS = wearable("knockback_shoulders", GearSlot.PAULDRON);
    public static final DeferredItem<WearableItem> STRENGTH_VAMBRACES = wearable("strength_vambraces", GearSlot.VAMBRACE);
    public static final DeferredItem<WearableItem> HASTE_VAMBRACES = wearable("haste_vambraces", GearSlot.VAMBRACE);
    public static final DeferredItem<TitleItem> CRIMSON_CULT_TITLE = register("crimson_cult_title", TitleItem::new);
    public static final DeferredItem<RingItem> LUCK_RING = register("luck_ring", RingItem::new);
    public static final DeferredItem<RingItem> SNIPER_RING = register("sniper_ring", RingItem::new);

    private BaublesFeature() {
    }

    private static DeferredItem<WearableItem> wearable(String name, GearSlot slot) {
        return register(name, properties -> new WearableItem(slot, properties));
    }

    private static <T extends Item> DeferredItem<T> register(String name, Function<Item.Properties, T> factory) {
        return ModItems.ITEMS.registerItem(name, factory, properties -> properties.rarity(Rarity.RARE));
    }

    public static void load() {
        TcStack.registerDataMatcher(LUCKY_COIN, (stack, value) -> hasEnchantment(stack, Enchantments.FORTUNE) && hasEnchantment(stack, Enchantments.LOOTING));
        TcStack.registerDataHandler(LUCKY_COIN, (stack, value) -> {
            enchant(stack, Enchantments.FORTUNE);
            enchant(stack, Enchantments.LOOTING);
        });
    }

    private static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> key) {
        for (Holder<Enchantment> holder : stack.getEnchantments().keySet()) {
            if (holder.is(key)) {
                return true;
            }
        }
        return false;
    }

    private static void enchant(ItemStack stack, ResourceKey<Enchantment> key) {
        WgRegistries.enchantment(key).ifPresent(holder -> stack.enchant(holder, 1));
    }

    public static TcStack luckyCoin() {
        return TcStack.withData("thaumcraft:gold_coin", 1, Map.of(LUCKY_COIN, "1"));
    }

    public static void recipes() {
        TcIngredient ingot = TcIngredient.item("minecraft:iron_ingot");
        TcIngredient feather = TcIngredient.item("minecraft:feather");
        TcIngredient sugar = TcIngredient.item("minecraft:sugar");
        TcIngredient lapis = TcIngredient.item("minecraft:lapis_lazuli");
        WgRecipes.arcane(
            "WGBAUBLES_WOLFVAMBRACES", RESEARCH, TcStack.of("witchinggadgets:strength_vambraces"),
            new AspectList().add(Aspect.ENTROPY, 10).add(Aspect.FIRE, 10),
            new String[] {" P ", "PVP"},
            'P', TcIngredient.item("witchinggadgets:wolf_pelt"),
            'V', TcIngredient.item("witchinggadgets:vambrace")
        );
        WgRecipes.arcane(
            "WGBAUBLES_KNOCKBACKSHOULDERS", RESEARCH, TcStack.of("witchinggadgets:knockback_shoulders"),
            new AspectList().add(Aspect.ORDER, 10).add(Aspect.EARTH, 10),
            new String[] {" S ", "ETE"},
            'E', TcIngredient.item("thaumcraft:earth_shard"),
            'S', TcIngredient.item("witchinggadgets:pauldron"),
            'T', TcIngredient.tag("c:ingots/thaumium")
        );
        WgRecipes.arcane(
            "WGBAUBLES_COIN", RESEARCH, luckyCoin(),
            new AspectList().add(Aspect.ORDER, 30),
            new String[] {"BCB", "CCC", "BCB"},
            'C', TcIngredient.item("thaumcraft:gold_coin"),
            'B', TcIngredient.item("minecraft:enchanted_book")
        );
        WgRecipes.infusion(
            "WGBAUBLES_HASTEVAMBRACES", RESEARCH, TcStack.of("witchinggadgets:haste_vambraces"), 2,
            new AspectList().add(Aspect.MINE, 8).add(Aspect.TOOL, 4).add(Aspect.MOTION, 4).add(Aspect.AIR, 8),
            TcIngredient.item("witchinggadgets:vambrace"),
            TcIngredient.item("minecraft:gold_ingot"), sugar,
            TcIngredient.of(TcStack.withData("minecraft:potion", 1, Map.of(CloaksFeature.POTION_DATA, "minecraft:swiftness"))), sugar
        );
        WgRecipes.infusion(
            "WGBAUBLES_DOUBLEJUMPSHOULDERS", RESEARCH, TcStack.of("witchinggadgets:double_jump_shoulders"), 2,
            new AspectList().add(Aspect.FLIGHT, 16).add(Aspect.MOTION, 8).add(Aspect.AIR, 16),
            TcIngredient.item("witchinggadgets:pauldron"),
            feather, ingot, feather, TcIngredient.item("thaumcraft:air_shard"), feather, ingot
        );
        WgRecipes.infusion(
            "WGBAUBLES_SNIPERRING", RESEARCH, TcStack.of("witchinggadgets:sniper_ring"), 2,
            new AspectList().add(Aspect.AIR, 16).add(Aspect.WEAPON, 8).add(Aspect.ORDER, 8),
            TcIngredient.item("thaumcraft:mundane_ring"),
            TcIngredient.item("thaumcraft:air_arrow"), TcIngredient.item("thaumcraft:fire_arrow"), TcIngredient.item("thaumcraft:water_arrow"),
            TcIngredient.item("thaumcraft:earth_arrow"), TcIngredient.item("thaumcraft:order_arrow"), TcIngredient.item("thaumcraft:entropy_arrow")
        );
        WgRecipes.infusion(
            "WGBAUBLES_LUCKRING", RESEARCH, TcStack.of("witchinggadgets:luck_ring"), 3,
            new AspectList().add(Aspect.GREED, 32).add(Aspect.TOOL, 16),
            TcIngredient.of(luckyCoin()),
            TcIngredient.item("minecraft:gold_ingot"), lapis, ingot, lapis, ingot, lapis, ingot, lapis
        );
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList().add(Aspect.CLOTH, 1).add(Aspect.MAGIC, 1).add(Aspect.ARMOR, 1), 7, -5, 1, TcStack.of("witchinggadgets:strength_vambraces"))
            .setParents("THAUMIUM")
            .setPages(
                WgResearch.text("1"), WgResearch.arcane("WGBAUBLES_KNOCKBACKSHOULDERS"),
                WgResearch.text("2"), WgResearch.arcane("WGBAUBLES_WOLFVAMBRACES"),
                WgResearch.text("3"), WgResearch.infusion("WGBAUBLES_HASTEVAMBRACES"),
                WgResearch.text("4"), WgResearch.infusion("WGBAUBLES_DOUBLEJUMPSHOULDERS"),
                WgResearch.text("5"), WgResearch.infusion("WGBAUBLES_SNIPERRING"),
                WgResearch.text("6"), WgResearch.infusion("WGBAUBLES_LUCKRING"), WgResearch.arcane("WGBAUBLES_COIN")
            )
            .registerResearchItem();
    }
}
