package scene;

import core.Window;
import graphics.ShaderProgram;
import graphics.UniformsMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Crosshair {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    private int vaoId;
    private int vboId;
    private Vector3f color;

    public Crosshair() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/crosshair.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/crosshair.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);

        color = new Vector3f(0.8f, 0.8f, 0.8f);
        createUniforms();
        createBuffers();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("crosshairColor");
    }

    private void createBuffers() {
        float size = 0.016f;
        float thickness = 0.003f;

        float[] horizontalLine = {
                -size, -thickness / 2, 0.0f,
                -size, thickness / 2, 0.0f,
                size, -thickness / 2, 0.0f,
                size, thickness / 2, 0.0f
        };

        float[] verticalLine = {
                -thickness / 2, -size, 0.0f,
                -thickness / 2, size, 0.0f,
                thickness / 2, -size, 0.0f,
                thickness / 2, size, 0.0f
        };

        float[] vertices = new float[horizontalLine.length + verticalLine.length];
        System.arraycopy(horizontalLine, 0, vertices, 0, horizontalLine.length);
        System.arraycopy(verticalLine, 0, vertices, horizontalLine.length, verticalLine.length);

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = 3 * 4;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

    public void render(Window window) {
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        if (depthEnabled)
            glDisable(GL_DEPTH_TEST);

        shaderProgram.bind();

        float aspect = (float) window.getWidth() / window.getHeight();
        Matrix4f ortho = new Matrix4f().ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);

        uniformsMap.setUniform("projectionMatrix", ortho);
        uniformsMap.setUniform("crosshairColor", color);

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);
        glBindVertexArray(0);

        shaderProgram.unbind();
        if (depthEnabled)
            glEnable(GL_DEPTH_TEST);
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}