package scene;

import org.joml.Vector3f;

public class BoundingBox {
    private Vector3f min;
    private Vector3f max;
    
    public BoundingBox(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }
    
    public Vector3f getMin() {
        return min;
    }
    
    public Vector3f getMax() {
        return max;
    }
    
    // Calcola la BoundingBox a partire dalla posizione e dimensione di un blocco
    public static BoundingBox fromBlock(Vector3f position, float size) {
        Vector3f min = new Vector3f(
            position.x - size/2,
            position.y - size/2,
            position.z - size/2
        );
        Vector3f max = new Vector3f(
            position.x + size/2,
            position.y + size/2,
            position.z + size/2
        );
        return new BoundingBox(min, max);
    }
}

