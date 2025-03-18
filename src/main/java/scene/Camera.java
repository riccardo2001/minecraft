package scene;

import org.joml.*;
import java.lang.Math;

public class Camera {
    private Vector3f position, up;
    private Vector2f rotation;
    private Matrix4f viewMatrix;
    private Frustum frustum;

    public Camera() {
        position = new Vector3f();
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

    public void dash(Vector3f direction, float intensity) {
        if (direction.lengthSquared() <= 0)
            return;
        direction.normalize().mul(intensity);
        position.add(direction);
        recalculate();
    }

    private void recalculate() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);
    }

    public void move(Vector3f direction, float amount) {
        if (direction.lengthSquared() <= 0)
            return;
        direction.normalize().mul(amount);
        position.add(direction);
        recalculate();
    }

    public Vector3f getForward() {
        Vector3f forward = new Vector3f();
        viewMatrix.positiveZ(forward).negate();
        forward.y = 0; 
        return forward.normalize();
    }

    public Vector3f getLeft() {
        Vector3f left = new Vector3f();
        viewMatrix.positiveX(left).negate();
        left.y = 0; 
        return left.normalize();
    }

    public Vector3f getRight() {
        Vector3f right = new Vector3f();
        viewMatrix.positiveX(right);
        right.y = 0; 
        return right.normalize();
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

    public Vector3f getFrontVector() {
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(rotation.x) * (float) Math.cos(rotation.y);
        front.y = (float) Math.sin(rotation.x);
        front.z = (float) Math.cos(rotation.x) * (float) Math.sin(rotation.y);
        return front.normalize();
    }
    
    public Vector2f getRotation() {
        return new Vector2f(rotation);
    }
}