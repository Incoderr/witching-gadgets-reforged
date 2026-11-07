package witchinggadgets.travellersgear;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record ToolDisplay(int slot, List<Float> translation, List<Float> rotation, List<Float> scale, boolean rotateWithHead, boolean hideWhenEquipped) {
    public static final int MAX = 6;
    public static final int SLOTS = 36;
    private static final Codec<List<Float>> VECTOR = Codec.FLOAT.listOf(3, 3);
    public static final Codec<ToolDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("slot").forGetter(ToolDisplay::slot),
        VECTOR.fieldOf("translation").forGetter(ToolDisplay::translation),
        VECTOR.fieldOf("rotation").forGetter(ToolDisplay::rotation),
        VECTOR.fieldOf("scale").forGetter(ToolDisplay::scale),
        Codec.BOOL.fieldOf("rotate_with_head").forGetter(ToolDisplay::rotateWithHead),
        Codec.BOOL.fieldOf("hide_when_equipped").forGetter(ToolDisplay::hideWhenEquipped)
    ).apply(instance, ToolDisplay::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, List<Float>> VECTOR_STREAM = ByteBufCodecs.FLOAT.<RegistryFriendlyByteBuf>cast().apply(ByteBufCodecs.list(3));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolDisplay> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ToolDisplay::slot,
        VECTOR_STREAM, ToolDisplay::translation,
        VECTOR_STREAM, ToolDisplay::rotation,
        VECTOR_STREAM, ToolDisplay::scale,
        ByteBufCodecs.BOOL, ToolDisplay::rotateWithHead,
        ByteBufCodecs.BOOL, ToolDisplay::hideWhenEquipped,
        ToolDisplay::new
    );
    public static final Codec<List<ToolDisplay>> LIST_CODEC = CODEC.listOf();
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ToolDisplay>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list(MAX));

    public static ToolDisplay create() {
        return new ToolDisplay(0, List.of(0.0F, 0.0F, 0.0F), List.of(0.0F, 0.0F, 0.0F), List.of(1.0F, 1.0F, 1.0F), false, false);
    }

    public ToolDisplay sanitized() {
        return new ToolDisplay(Mth.clamp(slot, 0, SLOTS - 1), clamp(translation, -1.0F, 1.0F), clamp(rotation, -180.0F, 180.0F), clamp(scale, -2.0F, 2.0F), rotateWithHead, hideWhenEquipped);
    }

    private static List<Float> clamp(List<Float> values, float min, float max) {
        return List.of(Mth.clamp(values.get(0), min, max), Mth.clamp(values.get(1), min, max), Mth.clamp(values.get(2), min, max));
    }
}
