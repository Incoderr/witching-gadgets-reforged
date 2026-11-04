package witchinggadgets.gadgets;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import witchinggadgets.crafting.WgRecipes;
import witchinggadgets.research.WgResearch;

public final class GadgetsFeature {
    private GadgetsFeature() {
    }

    public static void load() {
    }

    public static void recipes() {
        TcIngredient stick = TcIngredient.tag("c:rods/wooden");
        WgRecipes.arcane(
            "CALCULATOR", "CALCULATOR", TcStack.of("witchinggadgets:arcane_abacus"),
            new AspectList().add(Aspect.ORDER, 10),
            new String[] {"srs", "sbs", "sgs"},
            's', stick,
            'r', TcIngredient.tag("c:dyes/red"),
            'b', TcIngredient.tag("c:dyes/blue"),
            'g', TcIngredient.tag("c:dyes/green")
        );
    }

    public static void research() {
        WgResearch.item("CALCULATOR", new AspectList().add(Aspect.TOOL, 1).add(Aspect.MIND, 1).add(Aspect.MECHANISM, 1), 1, -5, 1, TcStack.of("witchinggadgets:arcane_abacus"))
            .setParents("INFUSION")
            .setPages(WgResearch.text("1"), WgResearch.arcane("CALCULATOR"))
            .registerResearchItem();
    }
}
