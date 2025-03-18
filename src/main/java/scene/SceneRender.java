package scene;

import java.util.*;
import core.Window;
import graphics.Mesh;
import graphics.Model;
import graphics.ShaderProgram;
import graphics.TextureAtlas;
import graphics.TextureCacheAtlas;
import graphics.UniformsMap;
import world.Block;
import world.Chunk;
import world.ChunkPosition;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SceneRender {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    
    private Fog fog;
    private Crosshair crosshair;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);

        fog = new Fog();
        crosshair = new Crosshair();

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        fog.cleanup();
        crosshair.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        scene.getCamera().getFrustum().update(scene.getCamera().getViewMatrix(), scene.getProjection().getProjMatrix());

        if (fog.isUseFog()) {
            Vector3f fogColor = fog.getFogColor();
            glClearColor(fogColor.x, fogColor.y, fogColor.z, 1.0f);
        }

        ShaderProgram activeShader = fog.isUseFog() ? fog.getShaderProgram() : shaderProgram;
        UniformsMap activeUniformsMap = fog.isUseFog() ? fog.getUniformsMap() : uniformsMap;

        activeShader.bind();
        activeUniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        activeUniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        activeUniformsMap.setUniform("txtSampler", 0);

        if (fog.isUseFog()) {
            activeUniformsMap.setUniform("fogColor", fog.getFogColor());
            activeUniformsMap.setUniform("cameraPosition", scene.getCamera().getPosition());
            activeUniformsMap.setUniform("fogDensity", fog.getFogDensity());
            activeUniformsMap.setUniform("fogGradient", fog.getFogGradient());
        }

        TextureCacheAtlas textureCache = scene.getTextureCacheAtlas();
        TextureAtlas textureAtlas = textureCache.getAtlasTexture();
        glActiveTexture(GL_TEXTURE0);
        textureAtlas.bind();

        var loadedChunks = scene.getWorld().getLoadedChunks();
        var modelMap = scene.getModelMap();

        for (var entry : loadedChunks.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            Chunk chunk = entry.getValue();

            if (!Block.isChunkVisible(scene, chunk))
                continue;

            String modelId = "chunk_model_" + chunkPos.getX() + "_" + chunkPos.getZ();
            Model chunkModel = modelMap.get(modelId);

            if (chunkModel == null) {
                if (chunk.isDirty()) {
                    chunk.buildMesh(scene.getWorld(), scene);
                }
                continue;
            }

            Entity chunkEntity = chunk.getChunkEntity();
            if (chunkEntity != null) {
                for (Mesh mesh : chunkModel.getMeshList()) {
                    glBindVertexArray(mesh.getVaoId());
                    chunkEntity.updateModelMatrix();
                    activeUniformsMap.setUniform("modelMatrix", chunkEntity.getModelMatrix());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    glBindVertexArray(0);
                }
            }
        }

        renderOtherEntities(scene, activeUniformsMap);

        activeShader.unbind();
        
        if (!window.isCursorVisible()) {
            crosshair.render(window);
        }
    }

    private void renderOtherEntities(Scene scene, UniformsMap activeUniformsMap) {
        Map<Model, List<Entity>> modelEntityMap = new HashMap<>();

        for (var entry : scene.getModelMap().entrySet()) {
            String modelId = entry.getKey();
            if (modelId.startsWith("chunk_model_"))
                continue;

            Model model = entry.getValue();
            List<Entity> entities = model.getEntitiesList();
            if (!entities.isEmpty()) {
                modelEntityMap.put(model, entities);
            }
        }

        for (var entry : modelEntityMap.entrySet()) {
            Model model = entry.getKey();
            List<Entity> entities = entry.getValue();

            for (Mesh mesh : model.getMeshList()) {
                glBindVertexArray(mesh.getVaoId());

                for (Entity entity : entities) {
                    entity.updateModelMatrix();
                    activeUniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }

                glBindVertexArray(0);
            }
        }
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");
        uniformsMap.createUniform("viewMatrix");
    }

    public void setFogColor(Vector3f fogColor) {
        fog.setFogColor(fogColor);
    }

    public void setFogDensity(float fogDensity) {
        fog.setFogDensity(fogDensity);
    }

    public void setFogGradient(float fogGradient) {
        fog.setFogGradient(fogGradient);
    }

    public void setUseFog(boolean useFog) {
        fog.setUseFog(useFog);
    }

    public boolean isUsingFog() {
        return fog.isUseFog();
    }
}