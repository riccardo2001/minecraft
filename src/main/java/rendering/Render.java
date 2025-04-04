package rendering;

import static org.lwjgl.opengl.GL11.*;

import core.Window;
import scene.Scene;

public class Render {
    private SceneRender sceneRender;

    public Render() {
        org.lwjgl.opengl.GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        sceneRender = new SceneRender();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        sceneRender.render(window, scene);
    }

    public void updateBlockOutline(Scene scene) {
        scene.getRayCast().performRayCast(scene.getCamera(), scene.getWorld());

        if (scene.getRayCast().hasHit() && scene.getRayCast().getHitDistance() <= 8.0f) {
            this.sceneRender.getBlockOutline().setBlockPosition(scene.getRayCast().getBlockPosition());
            this.sceneRender.getBlockOutline().setVisible(true);
        } else {
            this.sceneRender.getBlockOutline().setVisible(false);
        }
    }

    public SceneRender getSceneRender() {
        return sceneRender;
    }

    public void cleanup() {
        sceneRender.cleanup();
    }
}