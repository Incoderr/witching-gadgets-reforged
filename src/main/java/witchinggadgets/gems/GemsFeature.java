package witchinggadgets.gems;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.platform.BaseConfig;
import witchinggadgets.platform.WgStackKey;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModEffects;
import witchinggadgets.registry.ModItems;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.research.WgResearch;

public final class GemsFeature {
    public static final WgStackKey<String> GEM_CUT = WgStackKey.register("gem_cut", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final WgStackKey<String> GEM_ASPECT = WgStackKey.register("gem_aspect", Codec.STRING, ByteBufCodecs.STRING_UTF8);
    public static final WgStackKey<String> CAPSULE_FLUID = WgStackKey.register("capsule_fluid", Codec.STRING, ByteBufCodecs.STRING_UTF8);

    public static final ResourceKey<Enchantment> GEMSTONE_POTENCY = ResourceKey.create(Registries.ENCHANTMENT, WitchingGadgets.id("gemstone_potency"));
    public static final ResourceKey<Enchantment> GEMSTONE_BRITTLE = ResourceKey.create(Registries.ENCHANTMENT, WitchingGadgets.id("gemstone_brittle"));

    public static final Holder<MobEffect> KNOCKBACK_RESISTANCE = ModEffects.EFFECTS.register(
        "knockback_resistance",
        () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x6E6E6E) {
        }.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, WitchingGadgets.id("effect.knockback_resistance"), 0.34, AttributeModifier.Operation.ADD_VALUE)
    );

    public static final DeferredItem<InfusedGemItem> INFUSED_GEM = ModItems.ITEMS.registerItem("infused_gem", InfusedGemItem::new, properties -> properties.durability(InfusedGemItem.MAX_CHARGE));
    public static final DeferredItem<CrystalCapsuleItem> CRYSTAL_CAPSULE = ModItems.ITEMS.registerItem("crystal_capsule", CrystalCapsuleItem::new);

    public static final DeferredBlock<CuttingTableBlock> CUTTING_TABLE = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "cutting_table",
        CuttingTableBlock::new,
        properties -> properties.mapColor(MapColor.WOOD).strength(2.5F, 10.0F).sound(SoundType.WOOD)
    ));
    public static final DeferredBlock<GlimmerBlock> GLIMMER_OF_LIGHT = ModBlocks.BLOCKS.registerBlock(
        "glimmer_of_light",
        GlimmerBlock::new,
        properties -> properties.noCollission().instabreak().noLootTable().replaceable().lightLevel(state -> 14)
    );

    public static final Supplier<BlockEntityType<CuttingTableBlockEntity>> CUTTING_TABLE_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "cutting_table", () -> BlockEntityType.Builder.of(CuttingTableBlockEntity::new, CUTTING_TABLE.get()).build(null)
    );
    public static final Supplier<BlockEntityType<GlimmerBlockEntity>> GLIMMER_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "glimmer_of_light", () -> BlockEntityType.Builder.of(GlimmerBlockEntity::new, GLIMMER_OF_LIGHT.get()).build(null)
    );

    public static final Supplier<MenuType<CuttingTableMenu>> CUTTING_TABLE_MENU = ModMenus.MENUS.register(
        "cutting_table", () -> Menus.simple(CuttingTableMenu::new)
    );

    private GemsFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        WgRecipes.vanilla("GEMCUTTING_TOOLS", "GEMCUTTING", "gemcutting_tools", TcStack.of("witchinggadgets:gemcutting_tools"));
        WgRecipes.compound("GEMCUTTING", "GEMCUTTING", new AspectList(), 1, 2, 1, TcStack.of("witchinggadgets:gemcutting_tools"), TcStack.of("thaumcraft:table"));
        WgRecipes.crucible(
            "CRYSTALCAPSULE", "CRYSTALCAPSULE", TcStack.of("witchinggadgets:crystal_capsule"),
            TcIngredient.item("minecraft:bucket"),
            new AspectList().add(Aspect.VOID, 2).add(Aspect.CRYSTAL, 4)
        );
    }

    public static void research() {
        String last = BaseConfig.allowMirrors() ? "7" : "tc.research_page.GEMCUTTING.7.noMirrors";
        WgResearch.item("GEMCUTTING", new AspectList().add(Aspect.CRYSTAL, 1).add(Aspect.ORDER, 1).add(Aspect.MAGIC, 1).add(Aspect.CRAFT, 1), 1, -2, 2, TcStack.of("witchinggadgets:cutting_table"))
            .setPages(
                WgResearch.text("1"),
                WgResearch.recipe("GEMCUTTING_TOOLS"),
                WgResearch.compound("GEMCUTTING"),
                WgResearch.text("2"),
                WgResearch.text("3"),
                WgResearch.text("4"),
                WgResearch.text("5"),
                WgResearch.text("6"),
                WgResearch.text(last)
            )
            .registerResearchItem();
        WgResearch.item("CRYSTALCAPSULE", new AspectList().add(Aspect.CRYSTAL, 3).add(Aspect.ORDER, 2).add(Aspect.VOID, 4), 2, -2, 2, TcStack.of("witchinggadgets:crystal_capsule"))
            .setParents("GEMCUTTING")
            .setPages(WgResearch.text("1"), WgResearch.crucible("CRYSTALCAPSULE"))
            .setSecondary()
            .registerResearchItem();
    }
}
