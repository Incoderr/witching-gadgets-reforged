package witchinggadgets.gems;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;

public final class GemAffinities {
    private record Entry(List<Aspect> affinities, List<Aspect> aversions) {
    }

    private static final Map<TagKey<Item>, Entry> GEMS = new LinkedHashMap<>();

    static {
        gem("c:gems/diamond", List.of(Aspect.ORDER, Aspect.ENTROPY), List.of());
        gem("c:gems/emerald", List.of(Aspect.ORDER, Aspect.EARTH), List.of(Aspect.WATER, Aspect.ENTROPY));
        gem("c:gems/quartz", List.of(Aspect.ENTROPY, Aspect.FIRE), List.of(Aspect.ORDER, Aspect.WATER));
        gem("c:gems/certus_quartz", List.of(Aspect.ORDER, Aspect.AIR), List.of(Aspect.ENTROPY, Aspect.FIRE));
        gem("c:gems/topaz", List.of(Aspect.AIR, Aspect.FIRE), List.of(Aspect.ENTROPY, Aspect.WATER));
        gem("c:gems/amber", List.of(Aspect.AIR, Aspect.EARTH), List.of(Aspect.ENTROPY));
        gem("c:gems/peridot", List.of(Aspect.EARTH), List.of(Aspect.ENTROPY, Aspect.FIRE));
        gem("c:gems/malachite", List.of(Aspect.EARTH, Aspect.WATER), List.of(Aspect.ENTROPY, Aspect.FIRE));
        gem("c:gems/ruby", List.of(Aspect.FIRE), List.of(Aspect.ENTROPY, Aspect.WATER));
        gem("c:gems/sapphire", List.of(Aspect.WATER), List.of(Aspect.ENTROPY, Aspect.FIRE));
        gem("c:gems/tanzanite", List.of(Aspect.WATER), List.of(Aspect.ENTROPY, Aspect.FIRE));
    }

    private GemAffinities() {
    }

    private static void gem(String tag, List<Aspect> affinities, List<Aspect> aversions) {
        GEMS.put(TagKey.create(Registries.ITEM, ResourceLocation.parse(tag)), new Entry(affinities, aversions));
    }

    private static Entry entry(ItemStack stack) {
        for (Map.Entry<TagKey<Item>, Entry> gem : GEMS.entrySet()) {
            if (stack.is(gem.getKey())) {
                return gem.getValue();
            }
        }
        return null;
    }

    public static boolean isGem(ItemStack stack) {
        return !stack.isEmpty() && entry(stack) != null;
    }

    public static List<Aspect> affinities(ItemStack stack) {
        Entry entry = entry(stack);
        return entry == null ? List.of() : entry.affinities();
    }

    public static List<Aspect> aversions(ItemStack stack) {
        Entry entry = entry(stack);
        return entry == null ? List.of() : entry.aversions();
    }
}
