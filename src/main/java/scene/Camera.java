package scene;

import org.joml.*;
import java.lang.Math;

public class Camera {
    private Vector3f position, direction, right, up;
    private Vector2f rotation;
    private Matrix4f viewMatrix;
    private Frustum frustum;

    public Camera() {
        position = new Vector3f();
        direction = new Vector3f();
        right = new Vector3f();
        up = new Vector3f();
        rotation = new Vector2f();
        viewMatrix = new Matrix4f();
    }

    public void addRotation(float x, float y) {
        rotation.y += x;
        rotation.x += y;
        float pitchLimit = (float) Math.toRadians(89.0);
        if (rotation.x > pitchLimit)
            rotation.x = pitchLimit;
        if (rotation.x < -pitchLimit)
            rotation.x = -pitchLimit;
        recalculate();
    }

    public void moveForward(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.add(direction);
        recalculate();
    }

    public void moveBackwards(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.sub(direction);
        recalculate();
    }

    public void moveLeft(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.sub(right);
        recalculate();
    }

    public void moveRight(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.add(right);
        recalculate();
    }

    public void moveUp(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.add(up);
        recalculate();
    }

    public void moveDown(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.sub(up);
        recalculate();
    }

    private void recalculate() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Frustum getFrustum() {
        if (frustum == null)
            frustum = new Frustum();
        return frustum;
    }
}