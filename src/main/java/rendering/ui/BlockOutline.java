package rendering.ui;

import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import rendering.shaders.ShaderProgram;
import rendering.shaders.UniformsMap;
import scene.Scene;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class BlockOutline {
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
        uniformsMap.createUniform("color");

    }

    private void createBuffers() {
        float s = 1.0f;

        float[] vertices = {
            0,0,0, s,0,0,
            s,0,0, s,0,s,
            s,0,s, 0,0,s,
            0,0,s, 0,0,0,
            
            0,0,0, 0,s,0,
            s,0,0, s,s,0,
            s,0,s, s,s,s,
            0,0,s, 0,s,s,
            
            0,s,0, s,s,0,
            s,s,0, s,s,s,
            s,s,s, 0,s,s,
            0,s,s, 0,s,0
        };

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(Scene scene) {
        if (!visible || blockPos == null) return;
    
        boolean prevDepthTest = glIsEnabled(GL_DEPTH_TEST);
        boolean prevCullFace = glIsEnabled(GL_CULL_FACE);
        boolean prevBlend = glIsEnabled(GL_BLEND);
    
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    
        Vector4f outlineColor = new Vector4f(1.0f, 1.0f, 0.0f, 0.9f);
        Matrix4f modelMatrix = new Matrix4f()
            .translate(blockPos.x, blockPos.y, blockPos.z)
            .scale(1.005f);
        
        shaderProgram.bind();
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("modelMatrix", modelMatrix);
        uniformsMap.setUniform("color", outlineColor);
        
        glBindVertexArray(vaoId);
        glLineWidth(3.0f);
        glDrawArrays(GL_LINES, 0, 24);
        glBindVertexArray(0);
        
        shaderProgram.unbind();
    
        if (prevDepthTest) glEnable(GL_DEPTH_TEST);
        if (prevCullFace) glEnable(GL_CULL_FACE);
        if (!prevBlend) glDisable(GL_BLEND);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setBlockPosition(Vector3i blockPos) {
        this.blockPos.set(blockPos);
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}