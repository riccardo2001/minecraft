package core;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.DoubleBuffer;

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

    public void input(long windowHandle) {
        displVec.set(0, 0);

        DoubleBuffer xPosBuffer = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer yPosBuffer = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(windowHandle, xPosBuffer, yPosBuffer);

        float newX = (float) xPosBuffer.get(0);
        float newY = (float) yPosBuffer.get(0);

        if (previousPos.x < 0 || previousPos.y < 0) {
            previousPos.set(newX, newY);
        }

        displVec.x = newX - previousPos.x;
        displVec.y = newY - previousPos.y;

        glfwSetCursorPos(windowHandle, 1280 / 2.0, 720 / 2.0);

        previousPos.set(1280 / 2.0f, 720 / 2.0f);
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}