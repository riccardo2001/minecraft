package rendering.textures;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import main.Main;

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

        if (glGetError() != GL_NO_ERROR) {
            throw new RuntimeException("Error binding texture");
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        if (glGetError() != GL_NO_ERROR) {
            throw new RuntimeException("Error loading texture data");
        }

        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public float[] getUVCoordinates(int x, int y) {
        float margin = 0.000f;

        float uMin = (x * tileSize + margin) / (float) atlasWidth;
        float vMin = (y * tileSize + margin) / (float) atlasHeight;
        float uMax = ((x + 1) * tileSize - margin) / (float) atlasWidth;
        float vMax = ((y + 1) * tileSize - margin) / (float) atlasHeight;

        return new float[] { uMin, vMin, uMax, vMax };
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }
}
