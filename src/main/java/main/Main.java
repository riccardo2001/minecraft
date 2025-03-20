package main;

import graphics.Render;
import scene.BlockOutline;
import scene.Camera;
import scene.RayCast;
import scene.Scene;
import world.World;
import org.joml.Vector2f;
import org.joml.Vector3f;

import core.Engine;
import core.IAppLogic;
import core.MouseInput;
import core.Window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main implements IAppLogic {
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    private RayCast rayCast;

    public static void main(String[] args) {
        Main main = new Main();
        Window.WindowOptions opts = new Window.WindowOptions();

        opts.width = 1280;
        opts.height = 720;
        opts.fps = 1000;
        opts.ups = Engine.TARGET_UPS;
        opts.compatibleProfile = false;

        Engine gameEng = new Engine("Minecraft", opts, main);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        scene.setWorld(new World());
        scene.getWorld().generateInitialWorld(0, 0);
        scene.getCamera().setPosition(0f, 75f, 0f);

        rayCast = new RayCast();

        System.out.println("World generated...");
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

        Vector3f moveDir = new Vector3f();

        if (!isPaused) {
            if (window.isKeyPressed(GLFW_KEY_W))
                moveDir.add(camera.getForward().mul(1));
            if (window.isKeyPressed(GLFW_KEY_S))
                moveDir.add(camera.getForward().mul(-1));
            if (window.isKeyPressed(GLFW_KEY_A))
                moveDir.add(camera.getLeft().mul(1));
            if (window.isKeyPressed(GLFW_KEY_D))
                moveDir.add(camera.getRight().mul(1));

            if (moveDir.lengthSquared() > 0) {
                moveDir.normalize();
            }

            camera.move(moveDir, move);

            if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && moveDir.lengthSquared() > 0) {
                camera.dash(moveDir, 0.04f);
            }

            if (window.isKeyPressed(GLFW_KEY_SPACE))
                camera.moveUp(move);
            if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT))
                camera.moveDown(move);

            camera.addRotation(
                    (float) Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(displVec.y * MOUSE_SENSITIVITY));
        }
    }

    @Override
    public void update(Window window, Scene scene, Render render) {
        float playerX = scene.getCamera().getPosition().x;
        float playerZ = scene.getCamera().getPosition().z;
        
        scene.updateWorldGeneration(playerX, playerZ);

        BlockOutline blockOutline = render.getSceneRender().getBlockOutline();

        rayCast.performRayCast(scene.getCamera(), scene.getWorld());

        if (rayCast.hasHit() && rayCast.getHitDistance() <= 8.0f) {
            blockOutline.setBlockPosition(rayCast.getBlockPosition());
            blockOutline.setVisible(true);
        } else {
            blockOutline.setVisible(false);
        }
    }

    @Override
    public void cleanup() {
    }
}