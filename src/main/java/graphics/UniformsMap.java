package graphics;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL20.*;

public class UniformsMap {
    private int programId;
    private Map<String, Integer> uniforms;

    public UniformsMap(int programId) {
        this.programId = programId;
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) {
        int location = glGetUniformLocation(programId, uniformName);
        if (location < 0)
            throw new RuntimeException("Could not find uniform: " + uniformName);
        uniforms.put(uniformName, location);
    }

    private int getUniformLocation(String name) {
        Integer loc = uniforms.get(name);
        if (loc == null)
            throw new RuntimeException("Uniform not found: " + name);
        return loc;
    }

    public void setUniform(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }

    public void setUniform(String name, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(name), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String name, Vector4f value) {
        glUniform4f(getUniformLocation(name), value.x, value.y, value.z, value.w);
    }
}