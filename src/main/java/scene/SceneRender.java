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
    private ShaderProgram fogShaderProgram;
    private UniformsMap uniformsMap;
    private UniformsMap fogUniformsMap;

    private Vector3f fogColor;
    private float fogDensity;
    private float fogGradient;
    private boolean useFog;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);

        List<ShaderProgram.ShaderModuleData> fogModules = new ArrayList<>();
        fogModules.add(new ShaderProgram.ShaderModuleData("shaders/fog.vert", GL_VERTEX_SHADER));
        fogModules.add(new ShaderProgram.ShaderModuleData("shaders/fog.frag", GL_FRAGMENT_SHADER));
        fogShaderProgram = new ShaderProgram(fogModules);

        fogColor = new Vector3f(0.5f, 0.6f, 0.7f);
        fogDensity = 0.015f;
        fogGradient = 1.5f;
        useFog = true;

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        fogShaderProgram.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        scene.getCamera().getFrustum().update(scene.getCamera().getViewMatrix(), scene.getProjection().getProjMatrix());

        if (useFog) {
            glClearColor(fogColor.x, fogColor.y, fogColor.z, 1.0f);
        }

        ShaderProgram activeShader = useFog ? fogShaderProgram : shaderProgram;
        UniformsMap activeUniformsMap = useFog ? fogUniformsMap : uniformsMap;

        activeShader.bind();
        activeUniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        activeUniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        activeUniformsMap.setUniform("txtSampler", 0);

        if (useFog) {
            activeUniformsMap.setUniform("fogColor", fogColor);
            activeUniformsMap.setUniform("cameraPosition", scene.getCamera().getPosition());
            activeUniformsMap.setUniform("fogDensity", fogDensity);
            activeUniformsMap.setUniform("fogGradient", fogGradient);
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

        fogUniformsMap = new UniformsMap(fogShaderProgram.getProgramId());
        fogUniformsMap.createUniform("projectionMatrix");
        fogUniformsMap.createUniform("modelMatrix");
        fogUniformsMap.createUniform("txtSampler");
        fogUniformsMap.createUniform("viewMatrix");
        fogUniformsMap.createUniform("fogColor");
        fogUniformsMap.createUniform("cameraPosition");
        fogUniformsMap.createUniform("fogDensity");
        fogUniformsMap.createUniform("fogGradient");
    }

    public void setFogColor(Vector3f fogColor) {
        this.fogColor = fogColor;
    }

    public void setFogDensity(float fogDensity) {
        this.fogDensity = fogDensity;
    }

    public void setFogGradient(float fogGradient) {
        this.fogGradient = fogGradient;
    }

    public void setUseFog(boolean useFog) {
        this.useFog = useFog;
    }

    public boolean isUsingFog() {
        return useFog;
    }
}