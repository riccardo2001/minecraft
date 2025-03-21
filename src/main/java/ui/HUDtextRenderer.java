package ui;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import graphics.ShaderProgram;
import graphics.UniformsMap;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class HUDtextRenderer {
    private static final float CHAR_WIDTH = 10f;
    private static final float CHAR_HEIGHT = 16f;

    private final ShaderProgram shader;
    private final UniformsMap uniforms;
    private final int vao;
    private final int vbo;

    private float currentScale = 0.5f;
    private final float spacing = 2f;
    private Vector2f screenSize = new Vector2f(1280, 720);

    public HUDtextRenderer() {
        this.shader = createShader();
        this.uniforms = new UniformsMap(shader.getProgramId());
        this.vao = setupVAO();
        this.vbo = setupVBO();
        initUniforms();
    }

    private ShaderProgram createShader() {
        return new ShaderProgram(List.of(
                new ShaderProgram.ShaderModuleData("shaders/hud_text.vert", GL_VERTEX_SHADER),
                new ShaderProgram.ShaderModuleData("shaders/hud_text.frag", GL_FRAGMENT_SHADER)));
    }

    private int setupVAO() {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        return vao;
    }

    private int setupVBO() {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 512 * 4 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    private void initUniforms() {
        uniforms.createUniform("projection");
        uniforms.createUniform("color");
    }

    public void updateScreenSize(int width, int height) {
        this.screenSize = new Vector2f(width, height);
    }

    public void renderText(String text, float x, float y, float scaleMultiplier, Vector3f color) {
        List<Float> vertices = new ArrayList<>();
        float finalScale = currentScale * scaleMultiplier;
        float cursorX = x;

        for (char c : text.toCharArray()) {
            for (float[] seg : getCharSegments(c)) {
                addSegmentVertices(vertices, cursorX, y, finalScale, seg);
            }
            cursorX += (CHAR_WIDTH * finalScale) + spacing;
        }

        prepareRenderState(color);
        uploadVertices(vertices);
        drawText(vertices.size());
        cleanupRenderState();
    }

    private void addSegmentVertices(List<Float> vertices, float x, float y, float scale, float[] segment) {
        float yOffset = screenSize.y - y - (CHAR_HEIGHT * scale);
        vertices.add(x + segment[0] * scale);
        vertices.add(yOffset - segment[1] * scale);
        vertices.add(x + segment[2] * scale);
        vertices.add(yOffset - segment[3] * scale);
    }

    private void prepareRenderState(Vector3f color) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        uniforms.setUniform("color", color);
        uniforms.setUniform("projection",
                new Matrix4f().ortho(0, screenSize.x, screenSize.y, 0, -1, 1));
    }

    private void uploadVertices(List<Float> vertices) {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, toFloatBuffer(vertices));
    }

    private void drawText(int vertexCount) {
        glLineWidth(1.5f);
        glDrawArrays(GL_LINES, 0, vertexCount / 2);
    }

    private void cleanupRenderState() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private float[][] getCharSegments(char c) {
        return switch (c) {
            case '0' -> new float[][] { { 0, 0, 10, 0 }, { 10, 0, 10, 16 }, { 10, 16, 0, 16 }, { 0, 16, 0, 0 } };
            case '1' -> new float[][] { { 4, 0, 4, 16 }, { 2, 16, 8, 16 } };
            case '2' -> new float[][] { { 0, 16, 10, 16 }, { 10, 16, 10, 8 }, { 10, 8, 0, 8 }, { 0, 8, 0, 0 },
                    { 0, 0, 10, 0 } };
            // Aggiungi gli altri caratteri seguendo lo stesso schema...
            default -> new float[0][];
        };
    }

    private java.nio.FloatBuffer toFloatBuffer(List<Float> data) {
        java.nio.FloatBuffer buffer = java.nio.ByteBuffer
                .allocateDirect(data.size() * 4)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        for (float f : data)
            buffer.put(f);
        buffer.flip();
        return buffer;
    }

    public void setScale(float scale) {
        this.currentScale = scale;
    }

    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        shader.cleanup();
    }
}