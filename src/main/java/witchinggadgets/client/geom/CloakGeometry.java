package witchinggadgets.client.geom;

public final class CloakGeometry {
    public interface Sink {
        void vertex(float x, float y, float z, float u, float v, int argb, int light);
    }

    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_ROTATE = 1;
    public static final int ANIMATION_STRETCH = 2;

    private static final double[] CIRCLE = {
        0.5, 0.49039, 0.46194, 0.41573, 0.35355, 0.27779, 0.19134, 0.09755,
        0.0, -0.09755, -0.19134, -0.27779, -0.35355, -0.41573, -0.46194, -0.49039,
        -0.5, -0.49039, -0.46194, -0.41573, -0.35355, -0.27779, -0.19134, -0.09755,
        0.0, 0.09755, 0.19134, 0.27779, 0.35355, 0.41573, 0.46194, 0.49039
    };
    private static final double[] DIVIDERS = {0.3, 0.725, 0.75, 0.8, 0.825, 0.9, 1.0, 1.1};

    private CloakGeometry() {
    }

    public static float rotationDegrees(int mode, float leftLegDegrees, float rightLegDegrees) {
        if (mode != ANIMATION_ROTATE) {
            return 0.0F;
        }
        float rotation = 0.0F;
        if (leftLegDegrees > 0) {
            rotation += leftLegDegrees * 0.15F;
        }
        if (rightLegDegrees > 0) {
            rotation += rightLegDegrees * 0.15F;
        }
        return rotation;
    }

    public static double stretch(int mode, float leftLegDegrees, float rightLegDegrees) {
        double angle = Math.max(leftLegDegrees, rightLegDegrees);
        return mode == ANIMATION_STRETCH && angle > 1 ? 1.0 + 0.3 * angle / 90.0 : 1.0;
    }

    // Vertices are in the original model space after translate(0, 1.45, 0) scale(1, -1, 1).
    public static void cloak(Sink sink, double stretch, int argb, int light) {
        double prev0 = CIRCLE[0];
        double prev1 = CIRCLE[1];
        double prev2 = CIRCLE[24];
        double prev3 = CIRCLE[25];
        for (int i = 0; i < 15; i++) {
            int it1 = wrap(i + 1);
            int it2 = wrap(i + 24);
            int it3 = wrap(it2 + 1);
            for (int j = 0; j < 8; j++) {
                double h0 = CIRCLE[j] * CIRCLE[j] * 7;
                double h1 = CIRCLE[j + 1] * CIRCLE[j + 1] * 7;
                double divider = DIVIDERS[j];
                double d0 = CIRCLE[i] * 1.5 * divider;
                double d1 = CIRCLE[it1] * 1.5 * divider;
                double d2 = CIRCLE[it2] * 1.5 * divider;
                double d3 = CIRCLE[it3] * 1.5 * divider;
                float u0 = i * 0.0625F;
                float u1 = (i + 1) * 0.0625F;
                float v0 = j * 0.125F;
                float v1 = (j + 1) * 0.125F;
                if (j == 2) {
                    h0 *= 0.975;
                }
                if (j == 1) {
                    h1 *= 0.975;
                    h0 *= 0.9;
                }
                if (j == 0) {
                    d0 = d1 = d2 = d3 = 0.0;
                    prev0 = prev1 = prev2 = prev3 = 0.0;
                    h1 *= 0.9;
                    h0 *= 0.9;
                }
                d2 *= stretch;
                d3 *= stretch;
                sink.vertex((float) prev0, (float) h0, (float) prev2, u0, v0, argb, light);
                sink.vertex((float) d0, (float) h1, (float) d2, u0, v1, argb, light);
                sink.vertex((float) d1, (float) h1, (float) d3, u1, v1, argb, light);
                sink.vertex((float) prev1, (float) h0, (float) prev3, u1, v0, argb, light);
                prev0 = d0;
                prev1 = d1;
                prev2 = d2;
                prev3 = d3;
            }
        }
    }

    // Kama cloth around the hips; legDegrees > 0 lifts the matching seam.
    public static void kama(Sink sink, boolean sprinting, float leftLegDegrees, float rightLegDegrees, int argb, int light) {
        double angleMax = sprinting ? 60 : 70;
        double ap = 0.8125;
        double hL = 0;
        double hR = 0;
        double dL = 0.25;
        double dR = 0.25;
        double h = 0.5625;
        double w = 0.038;
        if (leftLegDegrees > 0) {
            double mod = leftLegDegrees / angleMax;
            dL += 0.4375 * mod;
            hL += 0.4375 * mod;
        }
        if (rightLegDegrees > 0) {
            double mod = rightLegDegrees / angleMax;
            dR += 0.4375 * mod;
            hR += 0.4375 * mod;
        }
        quad(sink, argb, light,
            0.4375, ap - h + 0.1, -0.125, 0, 1,
            0.25 + w, ap, -0.125, 0, 0,
            0.25 + w, ap, 0.125 + w, 0.25, 0,
            0.375, ap - h + hL, dL, 0.25, 1);
        quad(sink, argb, light,
            0.375, ap - h + hL, dL, 0.25, 1,
            0, ap - h - 0.1 + hL, dL, 0.515625, 1,
            -0.03125, ap, 0.125 + w, 0.515625, 0,
            0.25 + w, ap, 0.125 + w, 0.25, 0);
        quad(sink, argb, light,
            -0.0376, ap - h + hR, dR, 0.515625, 1,
            -0.0376, ap, 0.125 + w, 0.515625, 0,
            -0.0376, ap, 0.125 + w, 0.484375, 0,
            -0.0376, ap - h + hL, dL, 0.484375, 1);
        quad(sink, argb, light,
            0.0376, ap - h + hL, dL, 0.515625, 1,
            0.0376, ap, 0.125 + w, 0.515625, 0,
            0.0376, ap, 0.125 + w, 0.484375, 0,
            0.0376, ap - h + hL, dL, 0.484375, 1);
        quad(sink, argb, light,
            -0.375, ap - h + hR, dR, 0.75, 1,
            -0.25 - w, ap, 0.125 + w, 0.75, 0,
            0.03125, ap, 0.125 + w, 0.484375, 0,
            0, ap - h - 0.1 + hR, dR, 0.484375, 1);
        quad(sink, argb, light,
            -0.375, ap - h + hR, dR, 0.75, 1,
            -0.25 - w, ap, 0.125 + w, 0.75, 0,
            -0.25 - w, ap, -0.125, 1, 0,
            -0.4375, ap - h + 0.1, -0.125, 1, 1);
    }

    public static void belt(Sink sink, int argb, int light) {
        double ap = 0.8125;
        double w = 0.038;
        quad(sink, argb, light,
            -0.25 - w, ap - 0.125, 0.125 + w, 0, 1,
            -0.25 - w, ap + 0.125, 0.125 + w, 0, 0.875,
            -0.25 - w, ap + 0.125, -0.125 - w, 0.0625, 0.875,
            -0.25 - w, ap - 0.125, -0.125 - w, 0.0625, 1);
        quad(sink, argb, light,
            -0.25 - w, ap - 0.125, -0.125 - w, 0.0625, 1,
            -0.25 - w, ap + 0.125, -0.125 - w, 0.0625, 0.875,
            0.25 + w, ap + 0.125, -0.125 - w, 0.1875, 0.875,
            0.25 + w, ap - 0.125, -0.125 - w, 0.1875, 1);
        quad(sink, argb, light,
            0.25 + w, ap - 0.125, -0.125 - w, 0.1875, 1,
            0.25 + w, ap + 0.125, -0.125 - w, 0.1875, 0.875,
            0.25 + w, ap + 0.125, 0.125 + w, 0.25, 0.875,
            0.25 + w, ap - 0.125, 0.125 + w, 0.25, 1);
        quad(sink, argb, light,
            0.25 + w, ap - 0.125, 0.125 + w, 0.25, 1,
            0.25 + w, ap + 0.125, 0.125 + w, 0.25, 0.875,
            -0.25 - w, ap + 0.125, 0.125 + w, 0.375, 0.875,
            -0.25 - w, ap - 0.125, 0.125 + w, 0.375, 1);
    }

    private static int wrap(int index) {
        return index > 31 ? index - 31 : index;
    }

    private static void quad(Sink sink, int argb, int light, double... values) {
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 5;
            sink.vertex((float) values[offset], (float) values[offset + 1], (float) values[offset + 2], (float) values[offset + 3], (float) values[offset + 4], argb, light);
        }
    }
}
