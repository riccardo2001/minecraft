package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Frustum {
    private Plane[] planes; // 6 piani che formano il frustum
    
    public Frustum() {
        planes = new Plane[6];
        for (int i = 0; i < 6; i++) {
            planes[i] = new Plane();
        }
    }
    
    public void update(Matrix4f viewMatrix, Matrix4f projMatrix) {
        // Calcola i piani del frustum combinando le matrici view e projection
        Matrix4f clipMatrix = new Matrix4f();
        projMatrix.mul(viewMatrix, clipMatrix);
        
        // Estrai i piani (left, right, bottom, top, near, far)
        // [codice per estrarre i piani...]
    }
    
    public boolean isBoxInFrustum(Vector3f min, Vector3f max) {
        // Verifica se il box è completamente fuori da uno qualsiasi dei piani
        for (Plane plane : planes) {
            if (plane.classifyBox(min, max) == Plane.OUTSIDE) {
                return false;
            }
        }
        return true;
    }
}
