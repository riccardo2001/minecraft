package graphics;

import java.util.HashMap;
import java.util.Map;

public class TextureCacheAtlas {
    private TextureAtlas atlas;
    private Map<String, float[]> uvCache;

    // Mapping di nomi di texture a coordinate x,y nell'atlas
    private Map<String, int[]> texturePositions;

    public TextureCacheAtlas(String texturePath, int atlasSize, int tileSize) {
        this.atlas = new TextureAtlas(texturePath, atlasSize, tileSize);
        this.uvCache = new HashMap<>();
        this.texturePositions = initializeTexturePositions();
    }

    private Map<String, int[]> initializeTexturePositions() {
        Map<String, int[]> positions = new HashMap<>();

        // Mappa le posizioni in base all'immagine dell'atlas
        // Nota: queste sono posizioni approssimative basate sull'immagine, potrebbe
        // essere necessario aggiustarle
        positions.put("grass_top", new int[] { 1, 0 }); // Erba superiore (verde)
        positions.put("grass_side", new int[] { 3, 0 }); // Erba laterale (marrone con verde)
        positions.put("dirt", new int[] { 2, 0 }); // Terra (marrone)
        positions.put("stone", new int[] { 1, 1 }); // Pietra (grigia)
        positions.put("wood", new int[] { 4, 1 }); // Legno
        positions.put("default", new int[] { 0, 0 }); // Texture di default

        // Aggiungi altre mappature in base all'immagine

        return positions;
    }

    public float[] getUVCoordinates(String textureName) {
        if (!uvCache.containsKey(textureName)) {
            if (!texturePositions.containsKey(textureName)) {
                System.err.println("Warning: Texture '" + textureName + "' not found in atlas. Using default.");
                textureName = "default";
            }

            int[] position = texturePositions.get(textureName);
            float[] baseUV = atlas.getUVCoordinates(position[0], position[1]);

            // Convert the UV format from [uMin, vMin, uMax, vMax] to
            // [TopLeft_U, TopLeft_V, BottomLeft_U, BottomLeft_V, BottomRight_U,
            // BottomRight_V, TopRight_U, TopRight_V]
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

    public String getTexturePath() {
        return atlas.getTexturePath();
    }

    public void bind() {
        atlas.bind();
    }

    public void cleanup() {
        atlas.cleanup();
    }
}
