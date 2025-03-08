package graphics;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector4f;

import world.Block.BlockType;

public class TextureCacheAtlas {
    private TextureAtlas atlas;
    private Map<String, float[]> uvCache;
    private Map<String, int[]> texturePositions;
    private Map<BlockType, Vector4f> textureRegionCache;

    public TextureCacheAtlas(String texturePath, int atlasSize, int tileSize) {
        this.atlas = new TextureAtlas(texturePath, atlasSize, tileSize);
        this.uvCache = new HashMap<>();
        this.texturePositions = initializeTexturePositions();
        this.textureRegionCache = new HashMap<>();
    }

    // Questo metodo potrebbe essere spostato in un file di configurazione esterno
    // per rendere il codice più manutenibile
    private Map<String, int[]> initializeTexturePositions() {
        Map<String, int[]> positions = new HashMap<>();
        positions.put("grass_top", new int[] { 1, 0 });
        positions.put("grass_side", new int[] { 3, 0 });
        positions.put("dirt", new int[] { 2, 0 });
        positions.put("stone", new int[] { 1, 1 });
        positions.put("wood", new int[] { 4, 1 });
        positions.put("default", new int[] { 0, 0 });
        return positions;
    }

    public TextureAtlas getAtlasTexture() {
        return atlas;
    }

    public void cleanup() {
        atlas.cleanup();
        uvCache.clear();
        texturePositions.clear();
        textureRegionCache.clear();
    }

    public Vector4f getTextureRegion(BlockType type) {
        return textureRegionCache.computeIfAbsent(type, t -> {
            // Mappa il tipo di blocco al nome della texture
            String textureName = switch (t) {
                case GRASS -> "grass_top"; // Per impostazione predefinita usiamo la texture superiore
                case DIRT -> "dirt";
                case STONE -> "stone";
                case WOOD -> "wood";
                default -> "default";
            };

            if (!texturePositions.containsKey(textureName)) {
                System.err.println("Warning: Texture '" + textureName + "' not found in atlas. Using default.");
                textureName = "default";
            }

            int[] position = texturePositions.get(textureName);
            float[] baseUV = atlas.getUVCoordinates(position[0], position[1]);

            // Creiamo un Vector4f con le coordinate UV (uMin, vMin, uMax, vMax)
            return new Vector4f(baseUV[0], baseUV[1], baseUV[2], baseUV[3]);
        });
    }

    public float[] getUVCoordinates(String textureName) {
        if (!uvCache.containsKey(textureName)) {
            if (!texturePositions.containsKey(textureName)) {
                System.err.println("Warning: Texture '" + textureName + "' not found in atlas. Using default.");
                textureName = "default";
            }

            int[] position = texturePositions.get(textureName);
            float[] baseUV = atlas.getUVCoordinates(position[0], position[1]);

            // Convert the UV format
            float[] formattedUV = new float[8];
            formattedUV[0] = baseUV[0]; // Top-left U
            formattedUV[1] = baseUV[1]; // Top-left V
            formattedUV[2] = baseUV[0]; // Bottom-left U
            formattedUV[3] = baseUV[3]; // Bottom-left V
            formattedUV[4] = baseUV[2]; // Bottom-right U
            formattedUV[5] = baseUV[3]; // Bottom-right V
            formattedUV[6] = baseUV[2]; // Top-right U
            formattedUV[7] = baseUV[1]; // Top-right V

            uvCache.put(textureName, formattedUV);
        }

        return uvCache.get(textureName);
    }
}