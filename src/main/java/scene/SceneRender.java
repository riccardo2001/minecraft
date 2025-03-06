package scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import core.Window;
import graphics.Material;
import graphics.Mesh;
import graphics.Model;
import graphics.ShaderProgram;
import graphics.Texture;
import graphics.TextureCache;
import graphics.UniformsMap;
import world.Block;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList
                .add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        shaderProgram.bind();

        // Imposta le matrici uniform
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("txtSampler", 0);

        TextureCache textureCache = scene.getTextureCache();
        Collection<Model> models = scene.getModelMap().values();

        // Riduci il numero di cambi di texture
        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();

            for (Material material : model.getMaterialList()) {
                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                // Gruppo le entità per modello e materiale
                List<Mesh> meshes = material.getMeshList();
                for (Mesh mesh : meshes) {
                    glBindVertexArray(mesh.getVaoId());

                    // Batching: disegna tutte le entità con lo stesso materiale
                    for (Entity entity : entities) {
                        if (Block.isBlockVisible(scene, entity)) {
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