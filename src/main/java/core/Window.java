package core;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorWorkarea;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

public class Window {
    private long window;
    private int width = 1280;
    private int height = 720;

    public void create() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Errore nell'inizializzazione di GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // Hide until centered
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, "Minecraft", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Errore nella creazione della finestra");

        // Set up resize callback
        glfwSetFramebufferSizeCallback(window, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            glViewport(0, 0, newWidth, newHeight);

            // Update projection matrix when window is resized
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            setPerspective(90.0f, (float) newWidth / newHeight, 0.1f, 100.0f);
            glMatrixMode(GL_MODELVIEW); // Switch back to modelview for rendering
        });

        // Center the window
        long monitor = glfwGetPrimaryMonitor();
        if (monitor != NULL) {
            int[] monitorX = new int[1];
            int[] monitorY = new int[1];
            int[] monitorWidth = new int[1];
            int[] monitorHeight = new int[1];
            glfwGetMonitorWorkarea(monitor, monitorX, monitorY, monitorWidth, monitorHeight);

            int xPos = (monitorWidth[0] - width) / 2 + monitorX[0];
            int yPos = (monitorHeight[0] - height) / 2 + monitorY[0];

            glfwSetWindowPos(window, xPos, yPos);
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW
        GL.createCapabilities();

        // Configure OpenGL
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        // Initial viewport setup
        glViewport(0, 0, width, height);

        // Set up projection matrix
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        setPerspective(90.0f, (float) width / height, 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW); // Switch back to modelview
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

    public int getWidth() {
        return width*2;
    }

    public int getHeight() {
        return height*2;
    }
}