package ui;

import graphics.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import core.Window;
import entities.Inventory;
import world.Block.BlockType;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

public class HUD {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    private HUDmesh quadMesh;
    private HUDtextRenderer textRenderer;
    private final int slotSize = 64;
    private final int slotSpacing = 8;

    public HUD() {
        initShader();
        createQuadMesh();
        textRenderer = new HUDtextRenderer();
    }

    private void initShader() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/hud.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/hud.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);

        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("txtSampler");
        uniformsMap.createUniform("color");
        uniformsMap.createUniform("texAtlasCoords");
    }

    private void createQuadMesh() {
        float[] positions = {
                0, 0,
                1, 0,
                1, 1,
                0, 1
        };

        float[] texCoords = {
                0, 0,
                1, 0,
                1, 1,
                0, 1
        };

        int[] indices = { 0, 1, 2, 0, 2, 3 };
        quadMesh = new HUDmesh(positions, texCoords, indices);
    }

    public void render(Inventory inventory, TextureCacheAtlas textureCache, Window window) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.bind();
        uniformsMap.setUniform("txtSampler", 0);

        Matrix4f projectionMatrix = new Matrix4f().ortho(0, window.getWidth(), 0, window.getHeight(), -1, 1);
        uniformsMap.setUniform("projectionMatrix", projectionMatrix);

        renderHotbar(inventory, textureCache, window);

        shaderProgram.unbind();
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderHotbar(Inventory inventory, TextureCacheAtlas textureCache, Window window) {
        BlockType[] hotbar = inventory.getHotbar();
        int selectedSlot = inventory.getSelectedSlot();
        int startX = (window.getWidth() - (9 * (slotSize + slotSpacing))) / 2;
        int y = 20;

        for (int i = 0; i < 9; i++) {
            int slotX = startX + i * (slotSize + slotSpacing);
            
            renderQuad(slotX, y, slotSize, slotSize, new Vector4f(0.2f, 0.2f, 0.2f, 0.7f));

            if (i == selectedSlot) {
                renderQuad(slotX - 2, y - 2, slotSize + 4, slotSize + 4, new Vector4f(1, 1, 1, 0.8f));
            }

            if (hotbar[i] != null) {
                Vector4f uv = textureCache.getTextureRegion(hotbar[i]);
                TextureAtlas atlas = textureCache.getAtlasTexture();
                atlas.bind();

                renderTexturedQuad(
                    slotX + (slotSize - (slotSize - 8)) / 2, 
                    y + (slotSize - (slotSize - 8)) / 2,
                    slotSize - 8, 
                    slotSize - 8, 
                    uv
                );
            }

            if (hotbar[i] != null) {
                int count = inventory.getItemCount(hotbar[i]);
                if(count > 1) {
                    String text = String.valueOf(count);
                    float textX = slotX + slotSize - 24; 
                    float textY = y + slotSize - 20;
                    renderText(text, textX, textY, 0.7f);
                }
            }
        }
    }

    private void renderText(String text, float x, float y, float scale) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        textRenderer.renderText(text, x, y, scale, new Vector3f(1, 1, 1));
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderQuad(int x, int y, int width, int height, Vector4f color) {
        glBindTexture(GL_TEXTURE_2D, 0);
        uniformsMap.setUniform("color", color);
        uniformsMap.setUniform("texAtlasCoords", new Vector4f(0, 0, 0, 0));

        Matrix4f modelMatrix = new Matrix4f()
                .translate(x, y, 0)
                .scale(width, height, 1);

        uniformsMap.setUniform("modelMatrix", modelMatrix);

        glBindVertexArray(quadMesh.getVaoId());
        glDrawElements(GL_TRIANGLES, quadMesh.getNumVertices(), GL_UNSIGNED_INT, 0);
    }

    private void renderTexturedQuad(int x, int y, int width, int height, Vector4f uv) {
        glActiveTexture(GL_TEXTURE0);
        uniformsMap.setUniform("txtSampler", 0);
        uniformsMap.setUniform("color", new Vector4f(1, 1, 1, 1));

        uniformsMap.setUniform("texAtlasCoords", uv);

        Matrix4f modelMatrix = new Matrix4f()
                .translate(x, y, 0)
                .scale(width, height, 1);

        uniformsMap.setUniform("modelMatrix", modelMatrix);

        glBindVertexArray(quadMesh.getVaoId());
        glDrawElements(GL_TRIANGLES, quadMesh.getNumVertices(), GL_UNSIGNED_INT, 0);
    }

    public void cleanup() {
        quadMesh.cleanup();
        shaderProgram.cleanup();
        textRenderer.cleanup();
    }
}