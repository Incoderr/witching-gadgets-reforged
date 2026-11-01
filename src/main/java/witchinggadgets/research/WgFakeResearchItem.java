package witchinggadgets.research;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public class WgFakeResearchItem extends ResearchItem {
    public static final String PREFIX = "WGFAKE";

    private final String original;

    public WgFakeResearchItem(String original, String originalCategory, int column, int row, TcStack icon) {
        super(PREFIX + original, WgResearch.CATEGORY, new AspectList(), column, row, 1, icon);
        this.original = original;
        link(originalCategory);
    }

    public WgFakeResearchItem(String original, String originalCategory, int column, int row, Identifier icon) {
        super(PREFIX + original, WgResearch.CATEGORY, new AspectList(), column, row, 1, icon);
        this.original = original;
        link(originalCategory);
    }

    public ResearchItem original() {
        ResearchItem item = ResearchCategories.getResearch(original);
        if (item == null) {
            throw new IllegalStateException("Research " + original + " for " + key() + " is not registered");
        }
        return item;
    }

    @Override
    public String nameKey() {
        return original().nameKey();
    }

    @Override
    public String textKey() {
        return original().textKey();
    }

    @Override
    public List<ResearchPage> pages() {
        return original().pages();
    }

    private void link(String originalCategory) {
        ResearchItem item = original();
        if (!originalCategory.equals(item.category())) {
            throw new IllegalStateException("Research " + original + " is in " + item.category() + ", not " + originalCategory);
        }
        List<String> siblings = new ArrayList<>(item.siblings());
        siblings.add(key());
        item.setSiblings(siblings.toArray(String[]::new));
        setStub();
        setHidden();
    }
}
