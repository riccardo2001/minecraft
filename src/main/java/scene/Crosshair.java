package scene;

import core.Window;
import graphics.ShaderProgram;
import graphics.UniformsMap;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Crosshair {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    private int vaoId;
    private int vboId;
    
    public Crosshair() {
        List<ShaderProgram.ShaderModuleData> modules = new ArrayList<>();
        modules.add(new ShaderProgram.ShaderModuleData("shaders/crosshair.vert", GL_VERTEX_SHADER));
        modules.add(new ShaderProgram.ShaderModuleData("shaders/crosshair.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(modules);
        
        createUniforms();
        createBuffers();
    }
    
    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
    }
    
    private void createBuffers() {
        float size = 0.025f;
        float thickness = 0.005f;
        float sizeBottom = 0.022f;
        
        float[] horizontalLine = new float[] {
            -size, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            -size, thickness, 0.0f, 1.0f, 1.0f, 1.0f,
            size, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            size, thickness, 0.0f, 1.0f, 1.0f, 1.0f
        };
        
        float[] verticalLine = new float[] {
            -thickness/2, -sizeBottom, 0.0f, 1.0f, 1.0f, 1.0f,
            -thickness/2, size, 0.0f, 1.0f, 1.0f, 1.0f,
            thickness/2, -sizeBottom, 0.0f, 1.0f, 1.0f, 1.0f,
            thickness/2, size, 0.0f, 1.0f, 1.0f, 1.0f
        };
        
        float[] vertices = new float[horizontalLine.length + verticalLine.length];
        System.arraycopy(horizontalLine, 0, vertices, 0, horizontalLine.length);
        System.arraycopy(verticalLine, 0, vertices, horizontalLine.length, verticalLine.length);
        
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        
        int stride = 6 * 4;
        int positionOffset = 0;
        int colorOffset = 3 * 4;
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, positionOffset);
        glEnableVertexAttribArray(0);
        
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, colorOffset);
        glEnableVertexAttribArray(1);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void render(Window window) {
        boolean depthEnabled = glIsEnabled(GL_DEPTH_TEST);
        if (depthEnabled) {
            glDisable(GL_DEPTH_TEST);
        }
        
        shaderProgram.bind();
        
        float aspect = (float)window.getWidth() / window.getHeight();
        Matrix4f ortho = new Matrix4f().ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);
        uniformsMap.setUniform("projectionMatrix", ortho);
        
        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4); 
        glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);
        glBindVertexArray(0);
        
        shaderProgram.unbind();
        
        if (depthEnabled) {
            glEnable(GL_DEPTH_TEST);
        }
    }
    
    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(vboId);
        glDeleteVertexArrays(vaoId);
    }
}
