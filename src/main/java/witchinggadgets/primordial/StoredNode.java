package witchinggadgets.primordial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import thaumcraft.aspect.AspectList;

public record StoredNode(int type, int modifier, AspectList aspects) {
    public static final Codec<StoredNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("type").forGetter(StoredNode::type),
        Codec.INT.optionalFieldOf("modifier", -1).forGetter(StoredNode::modifier),
        AspectList.CODEC.fieldOf("aspects").forGetter(StoredNode::aspects)
    ).apply(instance, StoredNode::new));
    public static final StreamCodec<ByteBuf, StoredNode> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, StoredNode::type,
        ByteBufCodecs.VAR_INT, StoredNode::modifier,
        AspectList.STREAM_CODEC, StoredNode::aspects,
        StoredNode::new
    );
}
