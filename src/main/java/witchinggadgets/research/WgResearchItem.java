package witchinggadgets.research;

import java.util.function.BooleanSupplier;
import net.minecraft.resources.Identifier;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public class WgResearchItem extends ResearchItem {
    private static final String PAGE_PREFIX = "tc.research_page.";

    private BooleanSupplier condition = () -> true;

    public WgResearchItem(String key, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, WgResearch.CATEGORY, tags, column, row, complexity, icon);
    }

    public WgResearchItem(String key, AspectList tags, int column, int row, int complexity, Identifier icon) {
        super(key, WgResearch.CATEGORY, tags, column, row, complexity, icon);
    }

    public WgResearchItem when(BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public boolean isAvailable() {
        return condition.getAsBoolean() && super.isAvailable();
    }

    @Override
    public ResearchItem setPages(ResearchPage... pages) {
        ResearchPage[] converted = new ResearchPage[pages.length];
        for (int index = 0; index < pages.length; index++) {
            ResearchPage page = pages[index];
            if (page instanceof ResearchPage.Text text && !text.text().contains(".")) {
                page = ResearchPage.text(PAGE_PREFIX + key() + "." + text.text());
            }
            converted[index] = page;
        }
        return super.setPages(converted);
    }
}
