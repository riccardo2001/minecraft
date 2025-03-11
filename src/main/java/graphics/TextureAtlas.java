package graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import core.Main;
import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureAtlas {
    private int textureId;
    private int atlasWidth;
    private int atlasHeight;
    private int tileSize;
    private String texturePath;

    public TextureAtlas(String texturePath, int atlasWidth, int atlasHeight, int tileSize) {
        this.texturePath = texturePath;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.tileSize = tileSize;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            InputStream in = Main.class.getClassLoader().getResourceAsStream(texturePath);
            if (in == null) {
                throw new RuntimeException("Texture not found: " + texturePath);
            }
            byte[] bytes = in.readAllBytes();
            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(bytes.length);
            imageBuffer.put(bytes).flip();
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);
            ByteBuffer buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buf == null) {
                throw new RuntimeException("Failed to load texture: " + stbi_failure_reason());
            }
            generateTexture(w.get(), h.get(), buf);
            stbi_image_free(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateTexture(int width, int height, ByteBuffer buf) {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public float[] getUVCoordinates(int x, int y) {
        // Calcola le coordinate UV normalizzate (0.0 - 1.0)
        // Aggiungiamo un piccolo margine per evitare problemi di bleeding
        float margin = 0.001f;
        
        float uMin = (x * tileSize + margin) / (float) atlasWidth;
        float vMin = (y * tileSize + margin) / (float) atlasHeight;
        float uMax = ((x + 1) * tileSize - margin) / (float) atlasWidth;
        float vMax = ((y + 1) * tileSize - margin) / (float) atlasHeight;

        // Debug output per verificare i valori
        System.out.println("Texture UV coordinates: [" + uMin + ", " + vMin + ", " + uMax + ", " + vMax + "]");
        
        return new float[] { uMin, vMin, uMax, vMax };
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }
}
