package core;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {
    private Vector2f currentPos, displVec, previousPos;
    private boolean leftButtonPressed, rightButtonPressed;

    public MouseInput(long windowHandle) {
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        previousPos = new Vector2f(-1, -1);
        currentPos = new Vector2f();
        displVec = new Vector2f();
        glfwSetCursorPosCallback(windowHandle, (handle, xpos, ypos) -> {
            currentPos.x = (float) xpos;
            currentPos.y = (float) ypos;
        });
        glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getCurrentPos() {
        return currentPos;
    }

    public Vector2f getDisplVec() {
        return displVec;
    }

    public void input() {
        displVec.set(0, 0);
        if (previousPos.x < 0 || previousPos.y < 0) {
            previousPos.set(currentPos);
            return;
        }
        displVec.x = currentPos.x - previousPos.x;
        displVec.y = currentPos.y - previousPos.y;
        previousPos.set(currentPos);
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}