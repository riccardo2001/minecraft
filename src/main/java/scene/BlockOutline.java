package scene;

import org.joml.Matrix4f;
import org.joml.Vector3i;
import graphics.ShaderProgram;
import graphics.UniformsMap;
import java.util.ArrayList;
import java.util.List;
import world.Block;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class BlockOutline {
    private static final float LINE_WIDTH = 8.0f; // Spessore aumentato
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    private int vaoId;
    private int vboId;
    private boolean visible;
    private Vector3i blockPos;
    
    public BlockOutline() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/outline.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/outline.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);
        
        createUniforms();
        createBuffers();
        
        visible = false;
        blockPos = new Vector3i();
    }
    
    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("modelMatrix");
    }
    
    private void createBuffers() {
        float s = Block.BLOCK_SIZE;
        
        // Lines for a cube (8 vertices, 12 lines with 2 vertices each)
        float[] vertices = new float[] {
            // Bottom face - 4 lines
            0, 0, 0,    s, 0, 0,
            s, 0, 0,    s, 0, s,
            s, 0, s,    0, 0, s,
            0, 0, s,    0, 0, 0,
            
            // Top face - 4 lines
            0, s, 0,    s, s, 0,
            s, s, 0,    s, s, s,
            s, s, s,    0, s, s,
            0, s, s,    0, s, 0,
            
            // Vertical edges - 4 lines
            0, 0, 0,    0, s, 0,
            s, 0, 0,    s, s, 0,
            s, 0, s,    s, s, s,
            0, 0, s,    0, s, s
        };
        
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
    
    public void render(Scene scene) {
        if (!visible) {
            return;
        }
        
        // Salva lo stato corrente
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        boolean blendEnabled = glIsEnabled(GL_BLEND);
        
        // Disattiva depth test e attiva blending per migliore visibilità
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        shaderProgram.bind();
        
        glLineWidth(LINE_WIDTH);
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        
        // Aggiungi un piccolo offset e scala
        Matrix4f modelMatrix = new Matrix4f().identity().translate(
            blockPos.x - 0.01f, 
            blockPos.y - 0.01f, 
            blockPos.z - 0.01f
        ).scale(1.02f); 
        
        uniformsMap.setUniform("modelMatrix", modelMatrix);
        
        glBindVertexArray(vaoId);
        glDrawArrays(GL_LINES, 0, 24);
        glBindVertexArray(0);
        
        glLineWidth(1.0f);
        shaderProgram.unbind();
        
        // Ripristina lo stato
        if (!blendEnabled) {
            glDisable(GL_BLEND);
        }
        if (depthEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
        
        // Meno output di debug per non intasare la console
        System.out.println("Outline block at: " + blockPos);
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public void setBlockPosition(Vector3i blockPos) {
        this.blockPos = blockPos;
    }
    
    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}
