package witchinggadgets.research;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchPage;
import witchinggadgets.WitchingGadgets;

public final class WgResearch {
    public static final String CATEGORY = "WITCHGADG";

    private WgResearch() {
    }

    public static void init() {
        ResearchCategories.registerCategory(
            CATEGORY,
            WitchingGadgets.id("textures/gui/research/wgicon.png"),
            WitchingGadgets.id("textures/gui/research/wgresearchback.png")
        );
    }

    public static WgResearchItem item(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        return new WgResearchItem(key, tags, column, row, complexity, icon);
    }

    public static WgResearchItem item(String key, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        return new WgResearchItem(key, tags, column, row, complexity, icon);
    }

    public static WgFakeResearchItem fake(String original, String originalCategory, int column, int row, TcStack icon) {
        return new WgFakeResearchItem(original, originalCategory, column, row, icon);
    }

    public static WgFakeResearchItem fake(String original, String originalCategory, int column, int row, ResourceLocation icon) {
        return new WgFakeResearchItem(original, originalCategory, column, row, icon);
    }

    public static ResearchPage text(String page) {
        return ResearchPage.text(page);
    }

    public static ResearchPage recipe(String... recipes) {
        return ResearchPage.crafting(recipes);
    }

    public static ResearchPage arcane(String... recipes) {
        return ResearchPage.arcane(recipes);
    }

    public static ResearchPage infusion(String... recipes) {
        return ResearchPage.infusion(recipes);
    }

    public static ResearchPage enchant(String recipe) {
        return ResearchPage.infusionEnchantment(recipe);
    }

    public static ResearchPage crucible(String... recipes) {
        return ResearchPage.crucible(recipes);
    }

    public static ResearchPage compound(String recipe) {
        return ResearchPage.compound(recipe);
    }
}
