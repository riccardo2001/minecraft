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

        // Rendi tutti i chunk visibili
        var loadedChunks = scene.getWorld().getLoadedChunks();
        
        Model chunkModel = scene.getModelMap().get("chunk");
        if (chunkModel != null) {
            for (var entry : loadedChunks.entrySet()) {
                var chunk = entry.getValue();
                
                if (!Block.isChunkVisible(scene, chunk)) {
                    continue;
                }
                
                // Se il chunk è stato modificato, costruisci la sua mesh
                if (chunk.isDirty()) {
                    chunk.buildMesh(scene.getWorld(), scene);
                }
                
                // Ottieni l'entità del chunk
                Entity chunkEntity = chunk.getChunkEntity();
                if (chunkEntity != null) {
                    // Renderizza l'entità del chunk
                    for (Mesh mesh : chunkModel.getMeshList()) {
                        glBindVertexArray(mesh.getVaoId());
                        
                        chunkEntity.updateModelMatrix();
                        uniformsMap.setUniform("modelMatrix", chunkEntity.getModelMatrix());
                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        
                        glBindVertexArray(0);
                    }
                }
            }
        }

        // Renderizza eventuali altre entità non relative ai chunk
        renderOtherEntities(scene);

        shaderProgram.unbind();
    }
    
    private void renderOtherEntities(Scene scene) {
        // Mappa modelli -> entità per le entità che non sono chunk
        Map<Model, List<Entity>> modelEntityMap = new HashMap<>();
        
        for (var entry : scene.getModelMap().entrySet()) {
            if (!entry.getKey().equals("chunk")) {
                Model model = entry.getValue();
                List<Entity> entities = model.getEntitiesList();
                if (!entities.isEmpty()) {
                    modelEntityMap.put(model, entities);
                }
            }
        }
        
        // Renderizza ogni modello con tutte le sue entità
        for (var entry : modelEntityMap.entrySet()) {
            Model model = entry.getKey();
            List<Entity> entities = entry.getValue();
            
            for (Mesh mesh : model.getMeshList()) {
                glBindVertexArray(mesh.getVaoId());
                
                for (Entity entity : entities) {
                    entity.updateModelMatrix();
                    uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
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
}