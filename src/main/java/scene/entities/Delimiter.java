package scene.entities;

import org.joml.Vector3f;

public class Delimiter {
    public Vector3f min;
    public Vector3f max;

    public Delimiter(Vector3f position, float width, float height) {
        
        this.min = new Vector3f(
                position.x - width / 2,
                position.y - height, 
                position.z - width / 2);
        this.max = new Vector3f(
                position.x + width / 2,
                position.y, 
                position.z + width / 2);
    }

    public boolean intersects(Delimiter other) {
        return (min.x <= other.max.x && max.x >= other.min.x) &&
                (min.y <= other.max.y && max.y >= other.min.y) &&
                (min.z <= other.max.z && max.z >= other.min.z);
    }

    public Vector3f getPenetrationVector(Delimiter other) {
        if (!intersects(other)) {
            return null;
        }

        float overlapX = Math.min(max.x - other.min.x, other.max.x - min.x);
        float overlapY = Math.min(max.y - other.min.y, other.max.y - min.y);
        float overlapZ = Math.min(max.z - other.min.z, other.max.z - min.z);

        Vector3f penetration = new Vector3f();
        
        
        float epsilon = 0.001f;
        if (overlapX < epsilon) overlapX = epsilon;
        if (overlapY < epsilon) overlapY = epsilon;
        if (overlapZ < epsilon) overlapZ = epsilon;
        
        
        if (overlapX < overlapY && overlapX < overlapZ) {
            
            float centerX = (min.x + max.x) / 2;
            float otherCenterX = (other.min.x + other.max.x) / 2;
            penetration.x = centerX < otherCenterX ? -overlapX : overlapX;
        } else if (overlapY < overlapZ) {
            
            float centerY = (min.y + max.y) / 2;
            float otherCenterY = (other.min.y + other.max.y) / 2;
            penetration.y = centerY < otherCenterY ? -overlapY : overlapY;
        } else {
            
            float centerZ = (min.z + max.z) / 2;
            float otherCenterZ = (other.min.z + other.max.z) / 2;
            penetration.z = centerZ < otherCenterZ ? -overlapZ : overlapZ;
        }

        return penetration;
    }
}