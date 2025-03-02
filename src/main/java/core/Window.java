package core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;

    public void create() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Errore nell'inizializzazione di GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(1280, 720, "Minecraft", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Errore nella creazione della finestra");

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();

        // Configure OpenGL
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        // Set up viewport
        glViewport(0, 0, 1280, 720);

        // Set up projection matrix
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        setPerspective(45.0f, 1280.0f / 720.0f, 0.1f, 100.0f);
    }

    private void setPerspective(float fov, float aspect, float near, float far) {
        float fH = (float) Math.tan(Math.toRadians(fov / 2)) * near;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, near, far);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void swapBuffers() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public long getHandle() {
        return window;
    }
}