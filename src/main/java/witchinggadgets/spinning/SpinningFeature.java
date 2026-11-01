package witchinggadgets.spinning;

import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.platform.Menus;
import thaumcraft.platform.Transfer;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.registry.ModBlockEntities;
import witchinggadgets.registry.ModBlocks;
import witchinggadgets.registry.ModMenus;
import witchinggadgets.research.WgResearch;

public final class SpinningFeature {
    public static final String RESEARCH = "SPINNINGWHEEL";

    public static final DeferredBlock<SpinningWheelBlock> SPINNING_WHEEL = ModBlocks.withItem(ModBlocks.BLOCKS.registerBlock(
        "spinning_wheel",
        SpinningWheelBlock::new,
        properties -> properties
            .mapColor(MapColor.WOOD)
            .strength(2.5F)
            .sound(SoundType.WOOD)
    ));

    public static final Supplier<BlockEntityType<SpinningWheelBlockEntity>> SPINNING_WHEEL_ENTITY = ModBlockEntities.BLOCK_ENTITIES.register(
        "spinning_wheel",
        () -> new BlockEntityType<>(SpinningWheelBlockEntity::new, SPINNING_WHEEL.get())
    );

    public static final Supplier<MenuType<SpinningWheelMenu>> SPINNING_WHEEL_MENU = ModMenus.MENUS.register(
        "spinning_wheel",
        () -> Menus.simple(SpinningWheelMenu::new)
    );

    private SpinningFeature() {
    }

    public static void load() {
        Transfer.exposeItems(SPINNING_WHEEL_ENTITY, (entity, side) -> entity);
    }

    public static void recipes() {
        TcIngredient string = TcIngredient.item("minecraft:string");
        SpinningRecipe.register("SPINNING_THREAD", TcStack.of("witchinggadgets:simple_thread", 2), string, string, string, string);
        SpinningRecipe.register(
            "SPINNING_GOLD_THREAD", TcStack.of("witchinggadgets:golden_thread", 2),
            string, string, TcIngredient.item("minecraft:gold_nugget"), TcIngredient.item("minecraft:gold_nugget")
        );
        SpinningRecipe.register(
            "SPINNING_THAUMIUM_THREAD", TcStack.of("witchinggadgets:thaumium_thread", 2),
            string, string, TcIngredient.tag("c:nuggets/thaumium"), TcIngredient.tag("c:nuggets/thaumium")
        );
        WgRecipes.arcane(
            RESEARCH, RESEARCH, TcStack.of("witchinggadgets:spinning_wheel"),
            new AspectList().add(Aspect.ORDER, 5).add(Aspect.AIR, 5),
            new String[] {"I W", " T "},
            'T', TcIngredient.item("thaumcraft:table"),
            'I', TcIngredient.item("minecraft:iron_ingot"),
            'W', TcIngredient.tag("minecraft:planks")
        );
    }

    public static void research() {
        WgResearch.item(RESEARCH, new AspectList(), 8, 4, 0, TcStack.of("witchinggadgets:spinning_wheel"))
            .setRound()
            .setAutoUnlock()
            .setPages(
                WgResearch.text("1"),
                WgResearch.text("2"),
                WgResearch.arcane(RESEARCH),
                WgResearch.text("r1"),
                WgResearch.text("r2"),
                WgResearch.text("r3")
            )
            .registerResearchItem();
    }
}
