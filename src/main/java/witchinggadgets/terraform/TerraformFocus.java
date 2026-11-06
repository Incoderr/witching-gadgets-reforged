package witchinggadgets.terraform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import thaumcraft.aspect.Aspect;

public enum TerraformFocus {
    PLAINS("plains", Aspect.PLANT, Biomes.PLAINS, "minecraft:grass_block"),
    COLD_TAIGA("cold_taiga", Aspect.COLD, Biomes.SNOWY_TAIGA, "minecraft:ice"),
    DESERT("desert", Aspect.FIRE, Biomes.DESERT, "minecraft:sand"),
    JUNGLE("jungle", Aspect.TREE, Biomes.JUNGLE, "minecraft:jungle_log"),
    HELL("hell", Aspect.DARKNESS, Biomes.NETHER_WASTES, "minecraft:nether_bricks"),
    TAINT("taint", Aspect.TAINT, ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("thaumcraft", "tainted_land")), "thaumcraft:crusted_taint"),
    MUSHROOM("mushroom", Aspect.SLIME, Biomes.MUSHROOM_FIELDS, "minecraft:mycelium");

    private final String name;
    private final Aspect aspect;
    private final ResourceKey<Biome> biome;
    private final String displayed;

    TerraformFocus(String name, Aspect aspect, ResourceKey<Biome> biome, String displayed) {
        this.name = name;
        this.aspect = aspect;
        this.biome = biome;
        this.displayed = displayed;
    }

    public String id() {
        return "terraform_focus_" + name;
    }

    public Aspect aspect() {
        return aspect;
    }

    public ResourceKey<Biome> biome() {
        return biome;
    }

    public String displayed() {
        return displayed;
    }
}
