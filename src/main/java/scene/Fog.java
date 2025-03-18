package scene;

import graphics.ShaderProgram;
import graphics.UniformsMap;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class Fog {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    
    private Vector3f fogColor;
    private float fogDensity;
    private float fogGradient;
    private boolean useFog;
    
    public Fog() {
        List<ShaderProgram.ShaderModuleData> fogModules = new ArrayList<>();
        fogModules.add(new ShaderProgram.ShaderModuleData("shaders/fog.vert", GL_VERTEX_SHADER));
        fogModules.add(new ShaderProgram.ShaderModuleData("shaders/fog.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(fogModules);
        
        fogColor = new Vector3f(0.5f, 0.6f, 0.7f);
        fogDensity = 0.015f;
        fogGradient = 1.5f;
        useFog = true;
        
        createUniforms();
    }
    
    public void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("fogColor");
        uniformsMap.createUniform("cameraPosition");
        uniformsMap.createUniform("fogDensity");
        uniformsMap.createUniform("fogGradient");
    }
    
    public void bind() {
        shaderProgram.bind();
    }
    
    public void unbind() {
        shaderProgram.unbind();
    }
    
    public void cleanup() {
        shaderProgram.cleanup();
    }
    
    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }
    
    public UniformsMap getUniformsMap() {
        return uniformsMap;
    }
    
    public Vector3f getFogColor() {
        return fogColor;
    }
    
    public void setFogColor(Vector3f fogColor) {
        this.fogColor = fogColor;
    }
    
    public float getFogDensity() {
        return fogDensity;
    }
    
    public void setFogDensity(float fogDensity) {
        this.fogDensity = fogDensity;
    }
    
    public float getFogGradient() {
        return fogGradient;
    }
    
    public void setFogGradient(float fogGradient) {
        this.fogGradient = fogGradient;
    }
    
    public boolean isUseFog() {
        return useFog;
    }
    
    public void setUseFog(boolean useFog) {
        this.useFog = useFog;
    }
}
