package core;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Vector2f;

import graphics.Render;
import scene.Camera;
import scene.Scene;
import world.World;

public class Main implements IAppLogic {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("Minecraft ", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        scene.setWorld(new World());
        scene.getWorld().generateInitialWorld(0, 0);
        scene.getCamera().setPosition(0f, 0f, 0f);
        System.out.println("World generated");
    }

    @Override
    public void input(Window window, Scene scene, float diffTimeMillis) {
        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
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

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        float playerX = scene.getCamera().getPosition().x;
        float playerZ = scene.getCamera().getPosition().z;
        scene.updateWorldGeneration(playerX, playerZ);
    }
}
