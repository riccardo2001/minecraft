package core;

import org.lwjgl.glfw.GLFWVidMode;
import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final long windowHandle;
    private int width, height;
    private boolean isCursorVisible = false;
    private boolean isPaused = false;
    private String title;
    private Callable<Void> resizeFunc;
    private MouseInput mouseInput;

    public static class WindowOptions {
        public boolean compatibleProfile;
        public int fps;
        public int height;
        public int ups;
        public int width;
    }

    public Window(String title, WindowOptions opts, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (opts.compatibleProfile) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }
        if (opts.width > 0 && opts.height > 0) {
            width = opts.width;
            height = opts.height;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = vidMode.width();
            height = vidMode.height();
        }
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resized(w, h));

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                togglePause();
            }
        });

        glfwMakeContextCurrent(windowHandle);
        if (opts.fps > 0) {
            glfwSwapInterval(0);
        } else {
            glfwSwapInterval(1);
        }
        glfwShowWindow(windowHandle);

        int[] wArr = new int[1], hArr = new int[1];
        glfwGetFramebufferSize(windowHandle, wArr, hArr);
        width = wArr[0];
        height = hArr[0];

        mouseInput = new MouseInput(windowHandle);
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

            isCursorVisible = true;
        } else {
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            isCursorVisible = false;
        }
    }

    protected void resized(int w, int h) {
        width = w;
        height = h;
        try {
            resizeFunc.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void update() {
        renderPauseOverlay();
        glfwSwapBuffers(windowHandle);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public void setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(windowHandle, title);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }

    public String getTitle() {
        return title;
    }

    public void renderPauseOverlay() {
        if (isPaused) {
            glClearColor(0.3f, 0.3f, 0.3f, 0.7f);
            glClear(GL_COLOR_BUFFER_BIT);
        } else {
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    public boolean isCursorVisible() {
        return isCursorVisible;
    }
}
