package scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Frustum {
    private Plane[] planes;

    public Frustum() {
        planes = new Plane[6];
        for (int i = 0; i < 6; i++) {
            planes[i] = new Plane();
        }
    }

    public void update(Matrix4f viewMatrix, Matrix4f projMatrix) {
        Matrix4f clipMatrix = new Matrix4f();
        projMatrix.mul(viewMatrix, clipMatrix);

        planes[0].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 0),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 0),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 0),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 0));
        planes[0].normalize();

        planes[1].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 0),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 0),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 0),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 0));
        planes[1].normalize();

        planes[2].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 1),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 1),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 1),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 1));
        planes[2].normalize();

        planes[3].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 1),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 1),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 1),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 1));
        planes[3].normalize();

        planes[4].set(
                clipMatrix.get(0, 3) + clipMatrix.get(0, 2),
                clipMatrix.get(1, 3) + clipMatrix.get(1, 2),
                clipMatrix.get(2, 3) + clipMatrix.get(2, 2),
                clipMatrix.get(3, 3) + clipMatrix.get(3, 2));
        planes[4].normalize();

        planes[5].set(
                clipMatrix.get(0, 3) - clipMatrix.get(0, 2),
                clipMatrix.get(1, 3) - clipMatrix.get(1, 2),
                clipMatrix.get(2, 3) - clipMatrix.get(2, 2),
                clipMatrix.get(3, 3) - clipMatrix.get(3, 2));
        planes[5].normalize();
    }

    public boolean isBoxInFrustum(Vector3f min, Vector3f max) {
        for (Plane plane : planes) {
            if (plane.classifyBox(min, max) == Plane.OUTSIDE) {
                return false;
            }
        }
        return true;
    }
}