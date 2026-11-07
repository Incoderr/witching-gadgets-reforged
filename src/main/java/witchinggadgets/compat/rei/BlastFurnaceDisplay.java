package witchinggadgets.compat.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import witchinggadgets.recipeview.BlastFurnaceView;
import witchinggadgets.recipeview.WgRecipeCatalog;

public final class BlastFurnaceDisplay extends BasicDisplay {
    public static final DisplaySerializer<BlastFurnaceDisplay> SERIALIZER = WgRei.byKey(key -> {
        BlastFurnaceView view = WgRecipeCatalog.blastFurnace(key);
        return view == null ? null : new BlastFurnaceDisplay(view);
    }, display -> display.view.key());

    private final BlastFurnaceView view;

    public BlastFurnaceDisplay(BlastFurnaceView view) {
        super(List.of(WgRei.stacks(view.inputs())), outputs(view), Optional.empty());
        this.view = view;
    }

    private static List<EntryIngredient> outputs(BlastFurnaceView view) {
        List<EntryIngredient> outputs = new ArrayList<>();
        outputs.add(WgRei.stack(view.output()));
        if (!view.bonus().isEmpty()) {
            outputs.add(WgRei.stack(view.bonus()));
        }
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return WgRei.BLAST_FURNACE;
    }

}
