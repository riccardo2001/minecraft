package graphics;

import java.util.*;
import static org.lwjgl.opengl.GL20.*;
import utils.Utils;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(List<ShaderModuleData> modules) {
        programId = glCreateProgram();
        if (programId == 0)
            throw new RuntimeException("Could not create Shader Program");
        List<Integer> shaderModules = new ArrayList<>();
        for (ShaderModuleData s : modules) {
            shaderModules.add(createShader(Utils.readFile(s.shaderFile), s.shaderType));
        }
        link(shaderModules);
    }

    protected int createShader(String code, int type) {
        int shaderId = glCreateShader(type);
        if (shaderId == 0)
            throw new RuntimeException("Error creating shader type: " + type);
        glShaderSource(shaderId, code);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0)
            throw new RuntimeException("Error compiling shader: " + glGetShaderInfoLog(shaderId, 1024));
        glAttachShader(programId, shaderId);
        return shaderId;
    }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0)
            throw new RuntimeException("Error linking Shader Program: " + glGetProgramInfoLog(programId, 1024));
        for (int shaderId : shaderModules) {
            glDetachShader(programId, shaderId);
            glDeleteShader(shaderId);
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0)
            glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public record ShaderModuleData(String shaderFile, int shaderType) {
    }
}