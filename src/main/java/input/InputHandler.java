package input;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    public static boolean isKeyPressed(long window, int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}