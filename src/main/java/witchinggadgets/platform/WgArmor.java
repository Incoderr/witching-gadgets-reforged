package witchinggadgets.platform;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.platform.TcArmorMaterial;
import witchinggadgets.WitchingGadgets;

public final class WgArmor {
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, WitchingGadgets.MODID);
    private static final Map<String, Holder<ArmorMaterial>> CACHE = new HashMap<>();

    private WgArmor() {
    }

    public static void register(IEventBus bus) {
        MATERIALS.register(bus);
    }

    public static synchronized Holder<ArmorMaterial> dyeable(TcArmorMaterial material) {
        return CACHE.computeIfAbsent(material.assetId().getPath(), name -> {
            Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
            defense.put(ArmorItem.Type.HELMET, material.helmetDefense());
            defense.put(ArmorItem.Type.CHESTPLATE, material.chestplateDefense());
            defense.put(ArmorItem.Type.LEGGINGS, material.leggingsDefense());
            defense.put(ArmorItem.Type.BOOTS, material.bootsDefense());
            defense.put(ArmorItem.Type.BODY, material.chestplateDefense());
            return MATERIALS.register(name, () -> new ArmorMaterial(
                defense,
                material.enchantability(),
                material.equipSound(),
                () -> Ingredient.of(material.repairItems()),
                List.of(new ArmorMaterial.Layer(material.assetId(), "", true), new ArmorMaterial.Layer(material.assetId(), "_overlay", false)),
                material.toughness(),
                material.knockbackResistance()
            ));
        });
    }
}
