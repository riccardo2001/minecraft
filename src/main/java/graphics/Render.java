package graphics;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.opengl.GL;

import core.Window;
import scene.Scene;
import scene.SceneRender;

public class Render {
    private SceneRender sceneRender;

    public Render() {
        GL.createCapabilities();
        // Nel codice di inizializzazione
        glEnable(GL_DEPTH_TEST);
        sceneRender = new SceneRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        sceneRender.render(window, scene);
    }
}
