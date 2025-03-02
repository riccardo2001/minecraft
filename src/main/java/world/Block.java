package world;

import static org.lwjgl.opengl.GL11.*;

public class Block {
    private float x, y, z;

    public Block(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void render() {
        glPushMatrix();
        glTranslatef(x, y, z);
        
        glBegin(GL_QUADS);
        
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
        
        glEnd();
        
        glPopMatrix();
    }
}