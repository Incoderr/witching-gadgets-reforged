package witchinggadgets.client.gadgets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import thaumcraft.Thaumcraft;
import thaumcraft.client.geom.PowerBeamGeometry;
import thaumcraft.client.geom.QuadSink;
import thaumcraft.client.platform.WorldFx;

public final class GuideBeamFx extends WorldFx {
    private static final ResourceLocation BEAM = Thaumcraft.id("textures/misc/beam1.png");
    private static final int COLOR = 0xFFFF7F;
    private static final float OPACITY = 0.4F;

    private double targetX;
    private double targetY;
    private double targetZ;
    private double prevTargetX;
    private double prevTargetY;
    private double prevTargetZ;

    public GuideBeamFx(double x, double y, double z, double targetX, double targetY, double targetZ) {
        super(x, y, z, 8);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        prevTargetX = targetX;
        prevTargetY = targetY;
        prevTargetZ = targetZ;
    }

    public void update(double x, double y, double z, double targetX, double targetY, double targetZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        age = 0;
    }

    @Override
    public void tick() {
        super.tick();
        prevTargetX = targetX;
        prevTargetY = targetY;
        prevTargetZ = targetZ;
    }

    @Override
    public ResourceLocation texture() {
        return BEAM;
    }

    @Override
    public int blendMode() {
        return 1;
    }

    @Override
    public void render(QuadSink sink, float partialTick, Vec3 camera) {
        PowerBeamGeometry.render(
            renderX(partialTick, camera),
            renderY(partialTick, camera),
            renderZ(partialTick, camera),
            (float) (Mth.lerp(partialTick, prevTargetX, targetX) - camera.x),
            (float) (Mth.lerp(partialTick, prevTargetY, targetY) - camera.y),
            (float) (Mth.lerp(partialTick, prevTargetZ, targetZ) - camera.z),
            age + partialTick,
            OPACITY,
            COLOR,
            sink
        );
    }
}
