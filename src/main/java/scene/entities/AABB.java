package scene.entities;

import org.joml.Vector3f;

public class AABB {
    public Vector3f min;
    public Vector3f max;

    public AABB(Vector3f position, float width, float height) {
        this.min = new Vector3f(
                position.x - width / 2,
                position.y,
                position.z - width / 2);
        this.max = new Vector3f(
                position.x + width / 2,
                position.y + height,
                position.z + width / 2);
    }

    public boolean intersects(AABB other) {
        return (min.x <= other.max.x && max.x >= other.min.x) &&
                (min.y <= other.max.y && max.y >= other.min.y) &&
                (min.z <= other.max.z && max.z >= other.min.z);
    }

    public Vector3f getPenetrationVector(AABB other) {
        if (!intersects(other)) {
            return null;
        }

        float overlapX = Math.min(max.x - other.min.x, other.max.x - min.x);
        float overlapY = Math.min(max.y - other.min.y, other.max.y - min.y);
        float overlapZ = Math.min(max.z - other.min.z, other.max.z - min.z);

        Vector3f penetration = new Vector3f();
        if (overlapX < overlapY && overlapX < overlapZ) {

            penetration.x = (min.x < other.min.x) ? -overlapX : overlapX;
        } else if (overlapY < overlapZ) {

            penetration.y = (min.y < other.min.y) ? -overlapY : overlapY;
        } else {

            penetration.z = (min.z < other.min.z) ? -overlapZ : overlapZ;
        }

        return penetration;
    }
}