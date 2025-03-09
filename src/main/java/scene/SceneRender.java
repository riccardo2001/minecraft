package scene;

import java.util.*;
import org.joml.Vector4f;
import core.Window;
import graphics.Mesh;
import graphics.Model;
import graphics.ShaderProgram;
import graphics.TextureAtlas;
import graphics.TextureCacheAtlas;
import graphics.UniformsMap;
import world.Block;
import world.Chunk;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SceneRender {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        scene.getCamera().getFrustum().update(scene.getCamera().getViewMatrix(), scene.getProjection().getProjMatrix());

        shaderProgram.bind();
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("txtSampler", 0);

        TextureCacheAtlas textureCache = scene.getTextureCacheAtlas();
        TextureAtlas textureAtlas = textureCache.getAtlasTexture();
        glActiveTexture(GL_TEXTURE0);
        textureAtlas.bind();

        Map<Model, Map<Integer, Map<Vector4f, List<Entity>>>> modelChunkMap = new HashMap<>();
        Collection<Model> models = scene.getModelMap().values();

        var loadedChunks = scene.getWorld().getLoadedChunks();

        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();
            for (Entity entity : entities) {
                if (Block.isBlockVisible(scene, entity)) {
                    int entityChunkX = (int) Math.floor(entity.getPosition().x / (Chunk.WIDTH * Block.BLOCK_SIZE));
                    int entityChunkZ = (int) Math.floor(entity.getPosition().z / (Chunk.DEPTH * Block.BLOCK_SIZE));

                    int chunkKey = entityChunkX * 10000 + entityChunkZ;

                    Map<Integer, Map<Vector4f, List<Entity>>> chunkMap = modelChunkMap.computeIfAbsent(model,
                            k -> new HashMap<>());

                    Map<Vector4f, List<Entity>> textureMap = chunkMap.computeIfAbsent(chunkKey, k -> new HashMap<>());

                    Vector4f region = entity.getTextureRegion();
                    textureMap.computeIfAbsent(region, k -> new ArrayList<>()).add(entity);
                }
            }
        }

        for (var entry : loadedChunks.entrySet()) {
            var chunk = entry.getValue();

            float chunkMinX = chunk.getChunkX() * Chunk.WIDTH * Block.BLOCK_SIZE;
            float chunkMinY = 0;
            float chunkMinZ = chunk.getChunkZ() * Chunk.DEPTH * Block.BLOCK_SIZE;
            float chunkMaxX = (chunk.getChunkX() + 1) * Chunk.WIDTH * Block.BLOCK_SIZE;
            float chunkMaxY = Chunk.HEIGHT * Block.BLOCK_SIZE;
            float chunkMaxZ = (chunk.getChunkZ() + 1) * Chunk.DEPTH * Block.BLOCK_SIZE;
            var chunkMin = new org.joml.Vector3f(chunkMinX, chunkMinY, chunkMinZ);
            var chunkMax = new org.joml.Vector3f(chunkMaxX, chunkMaxY, chunkMaxZ);

            if (!scene.getCamera().getFrustum().isBoxInFrustum(chunkMin, chunkMax)) {
                continue;
            }

            int chunkKey = chunk.getChunkX() * 10000 + chunk.getChunkZ();

            for (Model model : models) {
                Map<Integer, Map<Vector4f, List<Entity>>> chunkMap = modelChunkMap.get(model);
                if (chunkMap == null)
                    continue;

                Map<Vector4f, List<Entity>> textureMap = chunkMap.get(chunkKey);
                if (textureMap == null || textureMap.isEmpty())
                    continue;

                for (Mesh mesh : model.getMeshList()) {
                    glBindVertexArray(mesh.getVaoId());

                    for (Map.Entry<Vector4f, List<Entity>> groupEntry : textureMap.entrySet()) {
                        for (Entity entity : groupEntry.getValue()) {
                            entity.updateModelMatrix();
                            uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                            glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        }
                    }

                    glBindVertexArray(0);
                }
            }
        }

        shaderProgram.unbind();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");
        uniformsMap.createUniform("viewMatrix");
    }
    
}