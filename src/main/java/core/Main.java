package core;

import graphics.Render;
import scene.Camera;
import scene.Scene;
import world.World;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class Main implements IAppLogic {
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    public static void main(String[] args) {
        Main main = new Main();
        Window.WindowOptions opts = new Window.WindowOptions();
        opts.width = 1280;
        opts.height = 720;
        opts.fps = 100;
        opts.ups = Engine.TARGET_UPS;
        opts.compatibleProfile = false;
        Engine gameEng = new Engine("Minecraft", opts, main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        // Cleanup eventuale
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        scene.setWorld(new World());
        scene.getWorld().generateInitialWorld(0, 0);
        scene.getCamera().setPosition(0f, 75f, 0f);
        System.out.println("World generated");

        System.out.println("OpenGL Vendor: " + glGetString(GL_VENDOR));
        System.out.println("OpenGL Renderer: " + glGetString(GL_RENDERER));
        System.out.println("OpenGL Version: " + glGetString(GL_VERSION));

    }

    @Override
    public void input(Window window, Scene scene, float diffTimeMillis) {
        boolean isPaused = window.isCursorVisible();

        if (!isPaused) {
            window.getMouseInput().input(window.getWindowHandle(), false); 
        }

        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();

        if (!isPaused) {
            if (window.isKeyPressed(GLFW_KEY_W)) {
                camera.moveForward(move);
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                camera.moveBackwards(move);
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                camera.moveLeft(move);
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                camera.moveRight(move);
            }
            if (window.isKeyPressed(GLFW_KEY_SPACE)) {
                camera.moveUp(move);
            } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
                camera.moveDown(move);
            }

            camera.addRotation(
                    (float) Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(displVec.y * MOUSE_SENSITIVITY));
        }
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        float playerX = scene.getCamera().getPosition().x;
        float playerZ = scene.getCamera().getPosition().z;
        scene.updateWorldGeneration(playerX, playerZ);
    }
}