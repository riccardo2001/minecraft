package scene;

import org.joml.Matrix4f;

public class Projection {
    private static final float FOV = (float) Math.toRadians(90.0);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000f;
    private Matrix4f projMatrix;

    public Projection(int width, int height) {
        projMatrix = new Matrix4f();
        updateProjMatrix(width, height);
    }

    public Matrix4f getProjMatrix() {
        return projMatrix;
    }

    public void updateProjMatrix(int width, int height) {
        projMatrix.setPerspective(FOV, (float) width / height, Z_NEAR, Z_FAR);
    }
}