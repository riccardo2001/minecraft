package scene;

import org.joml.Vector3f;

public class Plane {
    public static final int OUTSIDE = 0;
    public static final int INTERSECT = 1;
    public static final int INSIDE = 2;

    private Vector3f normal;
    private float distance;

    public Plane() {
        normal = new Vector3f();
        distance = 0;
    }

    public void setNormalAndDistance(Vector3f normal, float distance) {
        this.normal.set(normal);
        this.distance = distance;
    }

    public int classifyBox(Vector3f min, Vector3f max) {
        // Determina quale vertice della box è più distante lungo la normale del piano
        Vector3f p = new Vector3f();

        if (normal.x >= 0) {
            p.x = max.x;
        } else {
            p.x = min.x;
        }

        if (normal.y >= 0) {
            p.y = max.y;
        } else {
            p.y = min.y;
        }

        if (normal.z >= 0) {
            p.z = max.z;
        } else {
            p.z = min.z;
        }

        // Calcola la distanza del punto dal piano
        float d = normal.dot(p) + distance;

        if (d < 0) {
            return OUTSIDE;
        }

        // Se il punto più distante è all'interno, la box potrebbe essere all'interno o
        // intersecare
        Vector3f n = new Vector3f();

        if (normal.x >= 0) {
            n.x = min.x;
        } else {
            n.x = max.x;
        }

        if (normal.y >= 0) {
            n.y = min.y;
        } else {
            n.y = max.y;
        }

        if (normal.z >= 0) {
            n.z = min.z;
        } else {
            n.z = max.z;
        }

        d = normal.dot(n) + distance;

        if (d < 0) {
            return INTERSECT;
        }

        return INSIDE;
    }
}
