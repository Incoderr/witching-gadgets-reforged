package witchinggadgets.test;

import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchPage;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.research.WgResearch;
import witchinggadgets.research.WgResearchItem;

public final class CoreGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("core")
        .add("research_category_registered", CoreGameTests::researchCategoryRegistered)
        .add("text_pages_use_the_research_key", CoreGameTests::textPagesUseTheResearchKey)
        .add("duplicate_recipe_key_is_rejected", CoreGameTests::duplicateRecipeKeyIsRejected);

    private CoreGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void researchCategoryRegistered(GameTestHelper helper) {
        helper.assertTrue(ResearchCategories.get(WgResearch.CATEGORY) != null, "category " + WgResearch.CATEGORY + " is missing");
        helper.succeed();
    }

    private static void textPagesUseTheResearchKey(GameTestHelper helper) {
        WgResearchItem item = WgResearch.item("WG_CORE_TEST", new AspectList(), 0, 0, 1, TcStack.of("minecraft:stone"));
        item.setPages(WgResearch.text("1"), WgResearch.text("r2"), WgResearch.text("tc.research_page.OTHER.3"), WgResearch.arcane("SOME_RECIPE"));
        List<ResearchPage> pages = item.pages();
        helper.assertValueEqual(((ResearchPage.Text) pages.get(0)).text(), "tc.research_page.WG_CORE_TEST.1", "numbered page key");
        helper.assertValueEqual(((ResearchPage.Text) pages.get(1)).text(), "tc.research_page.WG_CORE_TEST.r2", "image page key");
        helper.assertValueEqual(((ResearchPage.Text) pages.get(2)).text(), "tc.research_page.OTHER.3", "full page key");
        helper.assertTrue(pages.get(3) instanceof ResearchPage.Arcane, "recipe page was converted");
        helper.assertTrue(ResearchCategories.getResearch("WG_CORE_TEST") == null, "test research leaked into the registry");
        helper.succeed();
    }

    private static void duplicateRecipeKeyIsRejected(GameTestHelper helper) {
        TcRecipe existing = TcRecipes.all().iterator().next();
        int before = TcRecipes.all().size();
        boolean rejected = false;
        try {
            WgRecipes.register(new CrucibleRecipe(existing.key(), existing.key(), TcStack.of("minecraft:stone"), TcIngredient.item("minecraft:stone"), new AspectList()));
        } catch (IllegalStateException exception) {
            rejected = true;
        }
        helper.assertTrue(rejected, "duplicate recipe key " + existing.key() + " was accepted");
        helper.assertValueEqual(TcRecipes.all().size(), before, "recipe count after a rejected registration");
        helper.assertTrue(TcRecipes.get(existing.key()) == existing, "original recipe was replaced");
        helper.succeed();
    }
}
