package witchinggadgets.gadgets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public record GuidePoint(double x, double y, double z, int facing) {
    public static final Codec<GuidePoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("x").forGetter(GuidePoint::x),
        Codec.DOUBLE.fieldOf("y").forGetter(GuidePoint::y),
        Codec.DOUBLE.fieldOf("z").forGetter(GuidePoint::z),
        Codec.INT.fieldOf("facing").forGetter(GuidePoint::facing)
    ).apply(instance, GuidePoint::new));
    public static final StreamCodec<ByteBuf, GuidePoint> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, GuidePoint::x,
        ByteBufCodecs.DOUBLE, GuidePoint::y,
        ByteBufCodecs.DOUBLE, GuidePoint::z,
        ByteBufCodecs.VAR_INT, GuidePoint::facing,
        GuidePoint::new
    );
    public static final Codec<List<GuidePoint>> LIST_CODEC = CODEC.listOf();
    public static final StreamCodec<ByteBuf, List<GuidePoint>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());

    public static GuidePoint of(Entity entity) {
        return new GuidePoint(entity.getX(), entity.getY(), entity.getZ(), facing(entity));
    }

    public static int facing(Entity entity) {
        return Mth.floor(entity.getYRot() * 8.0F / 360.0F + 0.5) & 7;
    }
}
