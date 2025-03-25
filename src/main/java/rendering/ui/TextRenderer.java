package rendering.ui;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import core.Window;
import rendering.shaders.ShaderProgram;
import rendering.shaders.UniformsMap;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextRenderer {

        public static class TextEntry {
                public final String text;
                public final float x;
                public final float y;
                public final float scale;

                public TextEntry(String text, float x, float y, float scale) {
                        this.text = text;
                        this.x = x;
                        this.y = y;
                        this.scale = scale;
                }
        }

        private ShaderProgram shader;
        private UniformsMap uniforms;
        private int vao;
        private int vbo;
        private int maxVertices;

        private static final float CHAR_WIDTH = 10f;
        private static final float CHAR_HEIGHT = 16f;
        private static final float SPACING = 5f;

        private int bufferSize;

        public TextRenderer() {
                initShader();
                initBuffers();
        }

        private void initShader() {
                shader = new ShaderProgram(List.of(
                                new ShaderProgram.ShaderModuleData("shaders/text.vert", GL_VERTEX_SHADER),
                                new ShaderProgram.ShaderModuleData("shaders/text.frag", GL_FRAGMENT_SHADER)));

                uniforms = new UniformsMap(shader.getProgramId());
                uniforms.createUniform("projection");
                uniforms.createUniform("color");
        }

        private void initBuffers() {
                maxVertices = 1024;
                bufferSize = maxVertices * 4 * 4;

                vao = glGenVertexArrays();
                glBindVertexArray(vao);

                vbo = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, maxVertices * 4 * 4, GL_DYNAMIC_DRAW);

                glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
                glEnableVertexAttribArray(0);

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
        }

        public void renderBatch(List<TextEntry> entries, Vector3f color, int windowWidth, int windowHeight) {
                List<Float> vertices = new ArrayList<>();
                for (TextEntry entry : entries) {
                        processText(entry.text, entry.x, entry.y, entry.scale, vertices);
                }
                renderVertices(vertices, color, windowWidth, windowHeight);
        }

        private void processText(String text, float x, float y, float scale, List<Float> vertices) {
                float cursorX = x;
                for (char c : text.toCharArray()) {
                        float[][] segments = getCharSegments(c);
                        for (float[] seg : segments) {
                                vertices.add(cursorX + seg[0] * scale);
                                vertices.add(y + seg[1] * scale);
                                vertices.add(cursorX + seg[2] * scale);
                                vertices.add(y + seg[3] * scale);
                        }
                        cursorX += (CHAR_WIDTH * scale) + (SPACING * scale);
                }
        }

        private void renderVertices(List<Float> vertices, Vector3f color, int windowWidth, int windowHeight) {
                if (vertices.isEmpty())
                        return;

                java.nio.FloatBuffer floatBuffer = toFloatBuffer(vertices);
                int requiredBytes = floatBuffer.remaining() * 4;

                glBindVertexArray(vao);
                glBindBuffer(GL_ARRAY_BUFFER, vbo);

                if (requiredBytes > bufferSize) {
                        bufferSize = Math.max(requiredBytes * 2, bufferSize * 2);
                        glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_DYNAMIC_DRAW);
                }
                glBufferSubData(GL_ARRAY_BUFFER, 0, floatBuffer);

                shader.bind();
                uniforms.setUniform("color", color);
                Matrix4f projection = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);
                uniforms.setUniform("projection", projection);

                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(1.5f);

                glDrawArrays(GL_LINES, 0, vertices.size() / 2);

                glDisable(GL_BLEND);
                glEnable(GL_DEPTH_TEST);

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
                shader.unbind();
        }

        public void renderText(String text, float x, float y, float scale, Window window) {
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                render(text, x, y, scale, new Vector3f(1, 1, 1), window.getWidth(), window.getHeight());
                glDisable(GL_BLEND);
                glEnable(GL_DEPTH_TEST);
        }

        public void render(String text, float x, float y, float scale, Vector3f color, int windowWidth,
                        int windowHeight) {
                List<Float> vertices = new ArrayList<>();

                float cursorX = x;
                for (char c : text.toCharArray()) {
                        float[][] segments = getCharSegments(c);
                        for (float[] seg : segments) {
                                vertices.add(cursorX + seg[0] * scale);
                                vertices.add(y + seg[1] * scale);
                                vertices.add(cursorX + seg[2] * scale);
                                vertices.add(y + seg[3] * scale);
                        }
                        cursorX += (CHAR_WIDTH * scale) + (SPACING * scale);
                }

                glBindVertexArray(vao);
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferSubData(GL_ARRAY_BUFFER, 0, toFloatBuffer(vertices));

                shader.bind();
                uniforms.setUniform("color", color);

                Matrix4f projection = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);
                uniforms.setUniform("projection", projection);

                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glLineWidth(1.5f);

                glDrawArrays(GL_LINES, 0, vertices.size() / 2);

                glDisable(GL_BLEND);
                glEnable(GL_DEPTH_TEST);

                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(0);
                shader.unbind();
        }

        private float[][] getCharSegments(char c) {
                final float LINE_THICKNESS = 2.5f;
                final float CENTER_Y = CHAR_HEIGHT / 2 - LINE_THICKNESS / 2;
                final float MARGIN = 0.5f;

                return switch (c) {
                        case 'X' -> new float[][] {
                                        { MARGIN, MARGIN, CHAR_WIDTH - MARGIN, CHAR_HEIGHT - MARGIN },
                                        { CHAR_WIDTH - MARGIN, MARGIN, MARGIN, CHAR_HEIGHT - MARGIN }
                        };

                        case 'Y' -> new float[][] {
                                        { MARGIN, MARGIN, CHAR_WIDTH / 2, CHAR_HEIGHT / 2 },
                                        { CHAR_WIDTH - MARGIN, MARGIN, CHAR_WIDTH / 2, CHAR_HEIGHT / 2 },
                                        { CHAR_WIDTH / 2, CHAR_HEIGHT / 2, CHAR_WIDTH / 2, CHAR_HEIGHT - MARGIN }
                        };

                        case 'Z' -> new float[][] {
                                        { MARGIN, MARGIN, CHAR_WIDTH - MARGIN, MARGIN },
                                        { CHAR_WIDTH - MARGIN, MARGIN, MARGIN, CHAR_HEIGHT - MARGIN },
                                        { MARGIN, CHAR_HEIGHT - MARGIN, CHAR_WIDTH - MARGIN, CHAR_HEIGHT - MARGIN }
                        };

                        case '0' -> new float[][] {
                                        { 0, 0, CHAR_WIDTH, 0 },
                                        { CHAR_WIDTH, 0, CHAR_WIDTH, CHAR_HEIGHT },
                                        { CHAR_WIDTH, CHAR_HEIGHT, 0, CHAR_HEIGHT },
                                        { 0, CHAR_HEIGHT, 0, 0 }
                        };
                        case '1' -> new float[][] {
                                        { CHAR_WIDTH / 2, 0, CHAR_WIDTH / 2, CHAR_HEIGHT },
                                        { CHAR_WIDTH / 4, CHAR_HEIGHT, 3 * CHAR_WIDTH / 4, CHAR_HEIGHT }
                        };
                        case '2' -> new float[][] {
                                        { CHAR_WIDTH, CHAR_HEIGHT, 0, CHAR_HEIGHT },
                                        { 0, CHAR_HEIGHT, 0, CHAR_HEIGHT / 2 },
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 },
                                        { CHAR_WIDTH, CHAR_HEIGHT / 2, CHAR_WIDTH, 0 },
                                        { 0, 0, CHAR_WIDTH, 0 }
                        };
                        case '3' -> new float[][] {
                                        { 0, CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT },
                                        { CHAR_WIDTH, CHAR_HEIGHT, CHAR_WIDTH, 0 },
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 },
                                        { 0, 0, CHAR_WIDTH, 0 }
                        };
                        case '4' -> new float[][] {
                                        { 0, 0, 0, CHAR_HEIGHT / 2 },
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 },
                                        { CHAR_WIDTH, 0, CHAR_WIDTH, CHAR_HEIGHT }
                        };
                        case '5' -> new float[][] {
                                        { 0, CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT },
                                        { CHAR_WIDTH, CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT / 2 },
                                        { CHAR_WIDTH, CHAR_HEIGHT / 2, 0, CHAR_HEIGHT / 2 },
                                        { 0, CHAR_HEIGHT / 2, 0, 0 },
                                        { 0, 0, CHAR_WIDTH, 0 }
                        };

                        case '6' -> new float[][] {
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 },
                                        { 0, CHAR_HEIGHT / 2, 0, CHAR_HEIGHT },
                                        { CHAR_WIDTH, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT },
                                        { 0, CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT },

                                        { 0, CHAR_HEIGHT / 2, 0, 0 },
                                        { 0, 0, CHAR_WIDTH / 2, 0 },
                                        { CHAR_WIDTH / 2, 0, CHAR_WIDTH, 0 }
                        };

                        case '7' -> new float[][] {
                                        { 0, 0, CHAR_WIDTH, 0 },
                                        { CHAR_WIDTH, CHAR_HEIGHT, CHAR_WIDTH, 0 }
                        };

                        case '8' -> new float[][] {
                                        { 0, CHAR_HEIGHT, CHAR_WIDTH, CHAR_HEIGHT },
                                        { CHAR_WIDTH, CHAR_HEIGHT, CHAR_WIDTH, 0 },
                                        { 0, 0, CHAR_WIDTH, 0 },
                                        { 0, CHAR_HEIGHT, 0, 0 },
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 }
                        };

                        case '9' -> new float[][] {
                                        { CHAR_WIDTH, CHAR_HEIGHT, 0, CHAR_HEIGHT },
                                        { 0, CHAR_HEIGHT / 2, 0, 0 },
                                        { 0, 0, CHAR_WIDTH, 0 },
                                        { CHAR_WIDTH, 0, CHAR_WIDTH, CHAR_HEIGHT },
                                        { 0, CHAR_HEIGHT / 2, CHAR_WIDTH, CHAR_HEIGHT / 2 }
                        };
                        case ',' -> new float[][] {
                                        { CHAR_WIDTH / 2 - 1, CHAR_HEIGHT - 3, CHAR_WIDTH / 2 + 1, CHAR_HEIGHT - 1 }
                        };
                        case ':' -> new float[][] {
                                        { CHAR_WIDTH / 2 - 1, CHAR_HEIGHT / 2 - 4, CHAR_WIDTH / 2 + 1,
                                                        CHAR_HEIGHT / 2 - 2 },
                                        { CHAR_WIDTH / 2 - 1, CHAR_HEIGHT / 2 + 2, CHAR_WIDTH / 2 + 1,
                                                        CHAR_HEIGHT / 2 + 4 }
                        };
                        case '-' -> new float[][] {
                                        { 1, CENTER_Y, CHAR_WIDTH - 1, CENTER_Y }
                        };
                        default -> new float[0][];
                };
        }

        private FloatBuffer toFloatBuffer(List<Float> data) {
                FloatBuffer buffer = java.nio.ByteBuffer
                                .allocateDirect(data.size() * 4)
                                .order(java.nio.ByteOrder.nativeOrder())
                                .asFloatBuffer();
                for (float f : data)
                        buffer.put(f);
                buffer.flip();
                return buffer;
        }

        public void cleanup() {
                glDeleteVertexArrays(vao);
                glDeleteBuffers(vbo);
                shader.cleanup();
        }
}