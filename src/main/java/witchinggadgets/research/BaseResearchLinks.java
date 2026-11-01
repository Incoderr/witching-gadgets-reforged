package witchinggadgets.research;

import net.minecraft.resources.Identifier;
import thaumcraft.crafting.TcStack;

public final class BaseResearchLinks {
    private BaseResearchLinks() {
    }

    public static void register() {
        WgResearch.fake("ENCHFABRIC", "ARTIFICE", 10, 1, TcStack.of("thaumcraft:enchanted_fabric")).registerResearchItem();
        WgResearch.fake("ARCANESTONE", "ARTIFICE", -3, -5, TcStack.of("thaumcraft:arcane_stone")).registerResearchItem();
        WgResearch.fake("BATHSALTS", "ALCHEMY", -1, -3, TcStack.of("thaumcraft:bath_salts")).registerResearchItem();
        WgResearch.fake("JARLABEL", "ALCHEMY", 4, -1, TcStack.of("thaumcraft:warded_jar")).registerResearchItem();
        WgResearch.fake("MIRRORESSENTIA", "ARTIFICE", 1, -7, TcStack.of("thaumcraft:essentia_mirror")).registerResearchItem();
        WgResearch.fake("ALCHEMICALMANUFACTURE", "ALCHEMY", -4, -4, thaumcraft("textures/misc/r_alchman.png")).registerResearchItem();
        WgResearch.fake("PUREIRON", "ALCHEMY", -5, -2, TcStack.of("thaumcraft:native_iron_cluster")).registerResearchItem();
        WgResearch.fake("INFERNALFURNACE", "ARTIFICE", 3, -5, thaumcraft("textures/misc/r_infernalfurnace.png")).registerResearchItem();
        WgResearch.fake("CENTRIFUGE", "ALCHEMY", 5, -5, TcStack.of("thaumcraft:alchemical_centrifuge")).registerResearchItem();
        WgResearch.fake("INFUSIONENCHANTMENT", "ARTIFICE", -8, 1, thaumcraft("textures/misc/r_enchant.png")).setSiblings().registerResearchItem();
        WgResearch.fake("ELDRITCHMINOR", "ELDRITCH", 1, 3, thaumcraft("textures/misc/r_eldritchminor.png")).setSpecial().registerResearchItem();
        WgResearch.fake("PRIMPEARL", "ELDRITCH", 0, 1, TcStack.of("thaumcraft:primordial_pearl")).setSpecial().registerResearchItem();
    }

    private static Identifier thaumcraft(String path) {
        return Identifier.fromNamespaceAndPath("thaumcraft", path);
    }
}
