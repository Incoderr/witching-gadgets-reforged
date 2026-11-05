package witchinggadgets.client.devices;

import thaumcraft.client.geom.QuadSink;

public final class PumpGeometry {
    private PumpGeometry() {
    }

    private static void vertex(QuadSink sink, float x, float y, float z, float u, float v, int light) {
        sink.vertex(x, y, z, u / 64.0F, v / 64.0F, 0xFFFFFFFF, light);
    }

    private static void triangle(QuadSink sink, float[] a, float[] b, float[] c, int light) {
        vertex(sink, a[0], a[1], a[2], a[3], a[4], light);
        vertex(sink, b[0], b[1], b[2], b[3], b[4], light);
        vertex(sink, c[0], c[1], c[2], c[3], c[4], light);
        vertex(sink, c[0], c[1], c[2], c[3], c[4], light);
    }

    private static void quad(QuadSink sink, float[] a, float[] b, float[] c, float[] d, int light) {
        vertex(sink, a[0], a[1], a[2], a[3], a[4], light);
        vertex(sink, b[0], b[1], b[2], b[3], b[4], light);
        vertex(sink, c[0], c[1], c[2], c[3], c[4], light);
        vertex(sink, d[0], d[1], d[2], d[3], d[4], light);
    }

    private static float[] p(float x, float y, float z, float u, float v) {
        return new float[] {x, y, z, u, v};
    }

    public static void funnel(QuadSink sink, int light) {
        triangle(sink, p(.375F, .75F, .25F, 59, 42), p(.375F, .25F, .25F, 59, 22), p(.25F, .5F, .25F, 64, 32), light);
        quad(sink, p(.375F, .75F, .25F, 59, 22), p(.625F, .75F, .25F, 45, 22), p(.625F, .25F, .25F, 45, 42), p(.375F, .25F, .25F, 59, 42), light);
        triangle(sink, p(.625F, .75F, .25F, 45, 42), p(.75F, .5F, .25F, 40, 32), p(.625F, .25F, .25F, 45, 22), light);

        triangle(sink, p(0, .5F, .75F, 64, 32), p(.25F, 0, .75F, 59, 22), p(.25F, 1, .75F, 59, 42), light);
        quad(sink, p(.75F, 1, .75F, 45, 22), p(.25F, 1, .75F, 59, 22), p(.25F, 0, .75F, 59, 42), p(.75F, 0, .75F, 45, 42), light);
        triangle(sink, p(.75F, 0, .75F, 45, 22), p(1, .5F, .75F, 40, 32), p(.75F, 1, .75F, 45, 42), light);

        quad(sink, p(.25F, 1, .75F, 18, 46), p(.75F, 1, .75F, 18, 24), p(.625F, .75F, .25F, 30, 30), p(.375F, .75F, .25F, 30, 40), light);
        quad(sink, p(.75F, 0, .75F, 18, 24), p(.25F, 0, .75F, 18, 46), p(.375F, .25F, .25F, 30, 40), p(.625F, .25F, .25F, 30, 30), light);
        quad(sink, p(0, .5F, .75F, 18, 46), p(.25F, 1, .75F, 18, 24), p(.375F, .75F, .25F, 30, 30), p(.25F, .5F, .25F, 30, 40), light);
        quad(sink, p(.25F, 0, .75F, 18, 24), p(0, .5F, .75F, 18, 46), p(.25F, .5F, .25F, 30, 40), p(.375F, .25F, .25F, 30, 30), light);
        quad(sink, p(.75F, 1, .75F, 18, 46), p(1, .5F, .75F, 18, 24), p(.75F, .5F, .25F, 30, 30), p(.625F, .75F, .25F, 30, 40), light);
        quad(sink, p(1, .5F, .75F, 18, 24), p(.75F, 0, .75F, 18, 46), p(.625F, .25F, .25F, 30, 40), p(.75F, .5F, .25F, 30, 30), light);
    }

    public static void nozzle(QuadSink sink, int light) {
        quad(sink, p(.5F, .75F, .25F, 0, 64), p(.75F, .5F, .25F, 64, 64), p(.75F, .5F, 0, 64, 0), p(.5F, .75F, 0, 0, 0), light);
        quad(sink, p(.5F, .25F, .25F, 64, 64), p(.25F, .5F, .25F, 0, 64), p(.25F, .5F, 0, 0, 0), p(.5F, .25F, 0, 64, 0), light);
        quad(sink, p(.5F, .75F, 0, 64, 0), p(.75F, .5F, 0, 0, 0), p(.5F, .25F, 0, 0, 64), p(.25F, .5F, 0, 64, 64), light);
        quad(sink, p(.25F, .5F, 0, 0, 64), p(.25F, .5F, .25F, 64, 64), p(.5F, .75F, .25F, 64, 0), p(.5F, .75F, 0, 0, 0), light);
        quad(sink, p(.5F, .25F, .25F, 0, 64), p(.5F, .25F, 0, 64, 64), p(.75F, .5F, 0, 64, 0), p(.75F, .5F, .25F, 0, 0), light);
    }
}
