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

    /**
     * Aggiorna i piani del frustum combinando le matrici view e projection.
     */
    public void update(Matrix4f viewMatrix, Matrix4f projMatrix) {
        Matrix4f clipMatrix = new Matrix4f();
        projMatrix.mul(viewMatrix, clipMatrix);

        // Estrai il piano sinistro
        planes[0].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 0),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 0),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 0),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 0));
        planes[0].normalize();

        // Estrai il piano destro
        planes[1].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 0),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 0),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 0),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 0));
        planes[1].normalize();

        // Estrai il piano inferiore
        planes[2].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 1),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 1),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 1),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 1));
        planes[2].normalize();

        // Estrai il piano superiore
        planes[3].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 1),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 1),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 1),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 1));
        planes[3].normalize();

        // Estrai il piano near
        planes[4].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 2),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 2),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 2),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 2));
        planes[4].normalize();

        // Estrai il piano far
        planes[5].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 2),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 2),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 2),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 2));
        planes[5].normalize();
    }

    /**
     * Ritorna true se l'AABB (definito da min e max) è almeno parzialmente
     * all'interno del frustum.
     */
    public boolean isBoxInFrustum(Vector3f min, Vector3f max) {
        for (Plane plane : planes) {
            if (plane.classifyBox(min, max) == Plane.OUTSIDE) {
                return false;
            }
        }
        return true;
    }
}