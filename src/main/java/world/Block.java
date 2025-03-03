package world;

import static org.lwjgl.opengl.GL11.*;

public class Block {
    private int type; // You could use this to differentiate block types
    private float x, y, z; // Position for standalone blocks

    // Constructor for standalone blocks (existing code)
    public Block(float x, float y, float z, int type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    // Constructor for chunk-based blocks
    public Block(int type) {
        this.type = type;
    }

    // Original render method for standalone blocks
    public void render() {
        glPushMatrix();
        glTranslatef(x, y, z);

        renderCubeByType(type);

        glPopMatrix();
    }

    // New render method for chunk-based blocks
    public void render(float x, float y, float z) {
        glPushMatrix();
        glTranslatef(x, y, z);

        renderCubeByType(type);

        glPopMatrix();
    }

    // Helper method to avoid code duplication
    private void renderCubeByType(int type) {
        glBegin(GL_QUADS);

        // Apply colors based on block type
        if (type == 1) { // Stone
            renderCubeWithStoneColors();
        } else if (type == 0) { // Grass
            renderCubeWithGrassColors();
        } else { // Default - use original multicolored cube
            renderColoredCube();
        }

        glEnd();
    }

    private void renderColoredCube() {
        // Faccia anteriore (Z+)
        glColor3f(1.0f, 0.0f, 0.0f); // Rosso
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);

        // Faccia posteriore (Z-)
        glColor3f(0.0f, 1.0f, 0.0f); // Verde
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        // Faccia superiore (Y+)
        glColor3f(0.0f, 0.0f, 1.0f); // Blu
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);

        // Faccia inferiore (Y-)
        glColor3f(1.0f, 1.0f, 0.0f); // Giallo
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);

        // Faccia destra (X+)
        glColor3f(1.0f, 0.0f, 1.0f); // Magenta
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);

        // Faccia sinistra (X-)
        glColor3f(0.0f, 1.0f, 1.0f); // Ciano
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
    }

    private void renderCubeWithStoneColors() {
        // Apply stone color to all faces (gray)
        glColor3f(0.5f, 0.5f, 0.5f);

        // Front face (Z+)
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);

        // Back face (Z-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        // Top face (Y+)
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);

        // Bottom face (Y-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);

        // Right face (X+)
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);

        // Left face (X-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
    }

    private void renderCubeWithGrassColors() {
        // Top face (Y+) - green for grass
        glColor3f(0.0f, 0.8f, 0.0f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);

        // Side faces - brown for dirt
        glColor3f(0.6f, 0.3f, 0.0f);

        // Front face (Z+)
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);

        // Back face (Z-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        // Bottom face (Y-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);

        // Right face (X+)
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);

        // Left face (X-)
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
    }

    // Getters for type and position
    public int getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}