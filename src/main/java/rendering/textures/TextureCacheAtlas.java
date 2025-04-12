package rendering.textures;

import java.util.HashMap;
import java.util.Map;
import org.joml.Vector4f;

import world.blocks.Block.BlockType;

public class TextureCacheAtlas {
    private TextureAtlas atlas;
    private Map<String, float[]> uvCache;
    private Map<String, int[]> texturePositions;
    private Map<BlockType, Vector4f> textureRegionCache;
    private Map<String, Vector4f> textureRegionStringCache;

    public TextureCacheAtlas(String texturePath, int atlasWidth, int atlasHeight, int tileSize) {
        atlas = new TextureAtlas(texturePath, atlasWidth, atlasHeight, tileSize);
        uvCache = new HashMap<>();
        texturePositions = initializeTexturePositions();
        textureRegionCache = new HashMap<>();
        textureRegionStringCache = new HashMap<>();
    }

    private Map<String, int[]> initializeTexturePositions() {
        Map<String, int[]> positions = new HashMap<>();

        positions.put("grass_top", new int[] { 7, 14 });
        positions.put("grass_side", new int[] { 6, 14 });
        positions.put("dirt", new int[] { 8, 5 });
        positions.put("stone", new int[] { 19, 6 });
        positions.put("wood", new int[] { 1, 13 });
        positions.put("leaves", new int[] { 9, 12 });
        positions.put("bedrock", new int[] { 1, 1 });
        positions.put("water", new int[] { 0, 2 });
        positions.put("default", new int[] { 0, 1 });
        return positions;
    }

    public Vector4f getTextureRegion(BlockType type) {
        return textureRegionCache.computeIfAbsent(type, t -> {
            String textureName = switch (t) {
                case GRASS -> "grass_top";
                case DIRT -> "dirt";
                case STONE -> "stone";
                case WATER -> "water";
                case WOOD -> "wood";
                case LEAVES -> "leaves";
                default -> "default";
            };
            if (!texturePositions.containsKey(textureName)) {
                System.err.println("Warning: Texture '" + textureName + "' not found. Using default.");
                textureName = "default";
            }
            int[] pos = texturePositions.get(textureName);
            float[] baseUV = atlas.getUVCoordinates(pos[0], pos[1]);

            return new Vector4f(baseUV[0], baseUV[1], baseUV[2], baseUV[3]);
        });
    }

    public Vector4f getTextureRegion(String textureName) {
        return textureRegionStringCache.computeIfAbsent(textureName, t -> {
            String actualTextureName = textureName;

            if (!texturePositions.containsKey(actualTextureName)) {
                System.err.println("Warning: Texture '" + actualTextureName + "' not found. Using default.");
                actualTextureName = "default";
            }

            int[] pos = texturePositions.get(actualTextureName);
            float[] baseUV = atlas.getUVCoordinates(pos[0], pos[1]);

            return new Vector4f(baseUV[0], baseUV[1], baseUV[2], baseUV[3]);
        });
    }

    public float[] getUVCoordinates(String textureName) {
        if (!uvCache.containsKey(textureName)) {
            if (!texturePositions.containsKey(textureName)) {
                System.err.println("Warning: Texture '" + textureName + "' not found. Using default.");
                textureName = "default";
            }
            int[] pos = texturePositions.get(textureName);
            float[] baseUV = atlas.getUVCoordinates(pos[0], pos[1]);
            float[] formattedUV = new float[8];
            formattedUV[0] = baseUV[0];
            formattedUV[1] = baseUV[1];
            formattedUV[2] = baseUV[0];
            formattedUV[3] = baseUV[3];
            formattedUV[4] = baseUV[2];
            formattedUV[5] = baseUV[3];
            formattedUV[6] = baseUV[2];
            formattedUV[7] = baseUV[1];
            uvCache.put(textureName, formattedUV);
        }
        return uvCache.get(textureName);
    }

    public void cleanup() {
        atlas.cleanup();
        uvCache.clear();
        texturePositions.clear();
        textureRegionCache.clear();
        textureRegionStringCache.clear();
    }

    public TextureAtlas getAtlasTexture() {
        return atlas;
    }
}