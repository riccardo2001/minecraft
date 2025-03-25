package scene.culling;

import org.joml.Vector3f;

public class Plane {
    public static final int OUTSIDE = -1;
    private float a, b, c, d;

    public void set(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public void normalize() {
        float mag = (float) Math.sqrt(a * a + b * b + c * c);
        a /= mag;
        b /= mag;
        c /= mag;
        d /= mag;
    }

    public int classifyBox(Vector3f min, Vector3f max) {
        float px = (a >= 0) ? max.x : min.x;
        float py = (b >= 0) ? max.y : min.y;
        float pz = (c >= 0) ? max.z : min.z;
        if (a * px + b * py + c * pz + d < 0)
            return OUTSIDE;
        return 1;
    }
}