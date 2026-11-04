package witchinggadgets.gadgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.aspect.AspectList;
import thaumcraft.research.ScanKind;
import thaumcraft.research.ScanManager;

public record ScanSnapshot(String kind, String key, AspectList aspects, ItemContainerContents clue, String clueEntity, Component name) {
    public static final Codec<ScanSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("kind").forGetter(ScanSnapshot::kind),
        Codec.STRING.fieldOf("key").forGetter(ScanSnapshot::key),
        AspectList.CODEC.fieldOf("aspects").forGetter(ScanSnapshot::aspects),
        ItemContainerContents.CODEC.optionalFieldOf("clue", ItemContainerContents.EMPTY).forGetter(ScanSnapshot::clue),
        Codec.STRING.optionalFieldOf("clue_entity", "").forGetter(ScanSnapshot::clueEntity),
        ComponentSerialization.CODEC.fieldOf("name").forGetter(ScanSnapshot::name)
    ).apply(instance, ScanSnapshot::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanSnapshot> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ScanSnapshot::kind,
        ByteBufCodecs.STRING_UTF8, ScanSnapshot::key,
        AspectList.STREAM_CODEC, ScanSnapshot::aspects,
        ItemContainerContents.STREAM_CODEC, ScanSnapshot::clue,
        ByteBufCodecs.STRING_UTF8, ScanSnapshot::clueEntity,
        ComponentSerialization.STREAM_CODEC, ScanSnapshot::name,
        ScanSnapshot::new
    );

    public static ScanSnapshot of(ScanManager.Target target, Component name) {
        AspectList aspects = target.aspects() == null ? new AspectList() : target.aspects().copy();
        ItemContainerContents clue = target.clueStack().isEmpty() ? ItemContainerContents.EMPTY : ItemContainerContents.fromItems(List.of(target.clueStack().copy()));
        String entity = target.clueEntity() == null ? "" : target.clueEntity().toString();
        return new ScanSnapshot(target.kind().name(), target.key(), aspects, clue, entity, name);
    }

    public static Component nameOf(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return nameOf(item.getItem());
        }
        return entity.getName().copy();
    }

    public static Component nameOf(ItemStack stack) {
        return stack.getHoverName().copy().withStyle(stack.getRarity().color());
    }

    public ScanManager.Target target() {
        ItemStack clueStack = clue.copyOne();
        ResourceLocation entity = clueEntity.isEmpty() ? null : ResourceLocation.tryParse(clueEntity);
        return new ScanManager.Target(ScanKind.valueOf(kind), key, aspects.copy(), clueStack, entity);
    }
}
