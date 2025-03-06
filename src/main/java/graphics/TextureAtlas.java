package graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import core.Main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureAtlas {

    private int textureId;
    private String texturePath;
    private int atlasSize; // Es. 256 per un atlas 256x256
    private int tileSize; // Es. 16 per texture 16x16

    public TextureAtlas(String texturePath, int atlasSize, int tileSize) {
        this.texturePath = texturePath;
        this.atlasSize = atlasSize;
        this.tileSize = tileSize;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(texturePath);
            if (inputStream == null) {
                throw new RuntimeException("Texture not found: " + texturePath);
            }

            ByteBuffer imageBuffer;
            try {
                byte[] imageBytes = inputStream.readAllBytes();
                imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
                imageBuffer.put(imageBytes);
                imageBuffer.flip();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read texture: " + texturePath, e);
            }

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);

            if (buf == null) {
                throw new RuntimeException("Failed to load texture: " + stbi_failure_reason());
            }

            int width = w.get();
            int height = h.get();

            generateTexture(width, height, buf);

            stbi_image_free(buf);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    private void generateTexture(int width, int height, ByteBuffer buf) {
        textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    /**
     * Ottiene le coordinate UV di una texture nell'Atlas.
     *
     * @param x Posizione X della texture nella griglia (es. 0 per la prima
     *          colonna).
     * @param y Posizione Y della texture nella griglia (es. 1 per la seconda riga).
     * @return Un array con { uMin, vMin, uMax, vMax }.
     */
    public float[] getUVCoordinates(int x, int y) {
        float uMin = (x * (float) tileSize) / atlasSize;
        float vMin = (y * (float) tileSize) / atlasSize;
        float uMax = ((x + 1) * (float) tileSize) / atlasSize;
        float vMax = ((y + 1) * (float) tileSize) / atlasSize;

        return new float[] { uMin, vMin, uMax, vMax };
    }

    public String getTexturePath() {
        return texturePath;
    }
}
