package core;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import static org.lwjgl.opengl.GL11.*;

import graphics.Camera;
import input.InputHandler;
import world.Block;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Window window;
    private Camera camera;
    private List<Block> blocks;

    public void run() {
        window = new Window();
        window.create();
        camera = new Camera(0, 1, 5);

        blocks = new ArrayList<>();
        blocks.add(new Block(0, 0, 0));
        blocks.add(new Block(1, 0, 0));
        blocks.add(new Block(0, 0, 1));

        while (!window.shouldClose()) {
            update();
            render();
            window.swapBuffers();
        }
    }

    private void update() {
        long windowHandle = window.getHandle();

        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_W))
            camera.move(0, 0, -0.1f);
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_S))
            camera.move(0, 0, 0.1f);
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_A))
            camera.move(-0.1f, 0, 0);
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_D))
            camera.move(0.1f, 0, 0);
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        lookAt(
                camera.getX(), camera.getY(), camera.getZ(),
                camera.getX(), camera.getY(), camera.getZ() - 1,
                0, 1, 0);

        // Renderizza i blocchi
        for (Block block : blocks) {
            block.render();
        }
    }

    private void lookAt(float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ) {
        // Calcola i vettori di direzione della camera
        float dirX = centerX - eyeX;
        float dirY = centerY - eyeY;
        float dirZ = centerZ - eyeZ;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= length;
        dirY /= length;
        dirZ /= length;

        float rightX = dirY * upZ - dirZ * upY;
        float rightY = dirZ * upX - dirX * upZ;
        float rightZ = dirX * upY - dirY * upX;

        length = (float) Math.sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ);
        rightX /= length;
        rightY /= length;
        rightZ /= length;

        float newUpX = rightY * dirZ - rightZ * dirY;
        float newUpY = rightZ * dirX - rightX * dirZ;
        float newUpZ = rightX * dirY - rightY * dirX;

        float[] m = new float[16];
        m[0] = rightX;
        m[1] = newUpX;
        m[2] = -dirX;
        m[3] = 0;
        m[4] = rightY;
        m[5] = newUpY;
        m[6] = -dirY;
        m[7] = 0;
        m[8] = rightZ;
        m[9] = newUpZ;
        m[10] = -dirZ;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;

        glMultMatrixf(m);
        glTranslatef(-eyeX, -eyeY, -eyeZ);
    }
}