package scene;

import org.joml.*;

public class Entity {
    private final String id;
    private final String modelId;
    private Matrix4f modelMatrix;
    private Vector4f textureRegion;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;

    public Entity(String id, String modelId, Vector4f textureRegion) {
        this.id = id;
        this.modelId = modelId;
        this.textureRegion = textureRegion;
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
        scale = 1;
    }

    public String getId() {
        return id;
    }

    public String getModelId() {
        return modelId;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void setRotation(float x, float y, float z, float angle) {
        rotation.fromAxisAngleRad(x, y, z, angle);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void updateModelMatrix() {
        modelMatrix.translationRotateScale(position, rotation, scale);
    }

    public Vector4f getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(Vector4f textureRegion) {
        this.textureRegion = textureRegion;
    }
}