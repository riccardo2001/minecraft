package world;

import java.util.List;

import org.joml.Vector3f;

import graphics.Material;
import graphics.Mesh;
import graphics.Model;
import graphics.TextureCacheAtlas;
import scene.Camera;
import scene.Entity;
import scene.Scene;

public class Block {
    public static final float BLOCK_SIZE = 1.0f;

    private static final float[] POSITIONS = new float[] {
            // Front face
            -0.5f, 0.5f, 0.5f, // V0
            -0.5f, -0.5f, 0.5f, // V1
            0.5f, -0.5f, 0.5f, // V2
            0.5f, 0.5f, 0.5f, // V3

            // Back face
            -0.5f, 0.5f, -0.5f, // V4
            0.5f, 0.5f, -0.5f, // V5
            -0.5f, -0.5f, -0.5f, // V6
            0.5f, -0.5f, -0.5f, // V7
    };

    private static final int[] INDICES = new int[] {
            // Front face
            0, 1, 3, 3, 1, 2,
            // Back face
            4, 6, 5, 5, 6, 7,
            // Top face
            4, 5, 0, 0, 5, 3,
            // Bottom face
            6, 7, 1, 1, 7, 2,
            // Left face
            4, 6, 0, 0, 6, 1,
            // Right face
            5, 7, 3, 3, 7, 2
    };

    private Entity blockEntity;
    private BlockType type;

    public enum BlockType {
        AIR,
        GRASS,
        DIRT,
        STONE,
        WOOD
    }

    public enum Face {
        TOP, BOTTOM, FRONT, BACK, LEFT, RIGHT
    }

    public Block(Scene scene, BlockType type, TextureCacheAtlas textureCache) {
        this.type = type;
        createBlockEntity(scene, textureCache);
    }

    private void createBlockEntity(Scene scene, TextureCacheAtlas textureCache) {
        String modelId = "block-model-" + type.name().toLowerCase();
        String entityId = "block-entity-" + type.name().toLowerCase() + "-" + System.currentTimeMillis();

        Model blockModel = scene.getModelMap().get(modelId);
        if (blockModel == null) {
            Material material = new Material();
            material.setTexturePath(textureCache.getTexturePath());

            float[] textCoords = generateUVCoordinates(textureCache);
            Mesh mesh = new Mesh(POSITIONS, textCoords, INDICES);
            material.getMeshList().add(mesh);

            blockModel = new Model(modelId, List.of(material));
            scene.addModel(blockModel);
        }

        blockEntity = new Entity(entityId, blockModel.getId());
        scene.addEntity(blockEntity);
    }

    private float[] getUVForFace(BlockType blockType, Face face, TextureCacheAtlas textureCache) {
        String textureName = switch (blockType) {
            case GRASS -> switch (face) {
                case TOP -> "grass_top";
                case BOTTOM -> "dirt";
                default -> "grass_side";
            };
            case DIRT -> "dirt";
            case STONE -> "stone";
            case WOOD -> "wood";
            default -> "default";
        };

        // Recupera le coordinate UV dalla textureCache
        float[] uv = textureCache.getUVCoordinates(textureName);

        // Assicurati che getUVCoordinates restituisca un array con 8 valori
        if (uv.length != 8) {
            // Se non ci sono 8 valori, imposta valori predefiniti o gestisci l'errore
            System.err.println(
                    "Warning: UV coordinates for texture '" + textureName + "' are not valid. Returning default UV.");
            return new float[] { 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f };
        }

        return uv;
    }

    private float[] generateUVCoordinates(TextureCacheAtlas textureCache) {
        float[] textCoords = new float[6 * 4 * 2];

        // Front face (V0, V1, V2, V3)
        float[] frontUV = getUVForFace(type, Face.FRONT, textureCache);
        System.arraycopy(frontUV, 0, textCoords, 0, 8); // Copia tutti e 4 i vertici

        // Back face (V4, V6, V7, V5)
        float[] backUV = getUVForFace(type, Face.BACK, textureCache);
        textCoords[8] = backUV[6];
        textCoords[9] = backUV[7]; // V4
        textCoords[10] = backUV[0];
        textCoords[11] = backUV[1]; // V6
        textCoords[12] = backUV[2];
        textCoords[13] = backUV[3]; // V7
        textCoords[14] = backUV[4];
        textCoords[15] = backUV[5]; // V5

        // Top face (V4, V5, V0, V3)
        float[] topUV = getUVForFace(type, Face.TOP, textureCache);
        textCoords[16] = topUV[0];
        textCoords[17] = topUV[1]; // V4
        textCoords[18] = topUV[4];
        textCoords[19] = topUV[5]; // V5
        textCoords[20] = topUV[6];
        textCoords[21] = topUV[7]; // V0
        textCoords[22] = topUV[2];
        textCoords[23] = topUV[3]; // V3

        // Bottom face (V6, V1, V2, V7)
        float[] bottomUV = getUVForFace(type, Face.BOTTOM, textureCache);
        textCoords[24] = bottomUV[6];
        textCoords[25] = bottomUV[7]; // V6
        textCoords[26] = bottomUV[0];
        textCoords[27] = bottomUV[1]; // V1
        textCoords[28] = bottomUV[2];
        textCoords[29] = bottomUV[3]; // V2
        textCoords[30] = bottomUV[4];
        textCoords[31] = bottomUV[5]; // V7

        // Left face (V4, V0, V1, V6)
        float[] leftUV = getUVForFace(type, Face.LEFT, textureCache);
        textCoords[32] = leftUV[6];
        textCoords[33] = leftUV[7]; // V4
        textCoords[34] = leftUV[0];
        textCoords[35] = leftUV[1]; // V0
        textCoords[36] = leftUV[2];
        textCoords[37] = leftUV[3]; // V1
        textCoords[38] = leftUV[4];
        textCoords[39] = leftUV[5]; // V6

        // Right face (V5, V3, V2, V7)
        float[] rightUV = getUVForFace(type, Face.RIGHT, textureCache);
        textCoords[40] = rightUV[6];
        textCoords[41] = rightUV[7]; // V5
        textCoords[42] = rightUV[0];
        textCoords[43] = rightUV[1]; // V3
        textCoords[44] = rightUV[2];
        textCoords[45] = rightUV[3]; // V2
        textCoords[46] = rightUV[4];
        textCoords[47] = rightUV[5]; // V7

        return textCoords;
    }

    public void setWorldPosition(float x, float y, float z) {
        blockEntity.setPosition(x, y, z);
    }

    public Entity getEntity() {
        return blockEntity;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return type != BlockType.AIR;
    }

    public void updateModelMatrix() {
        blockEntity.updateModelMatrix();
    }

    public static boolean isBlockVisible(Scene scene, Entity entity) {
        Camera camera = scene.getCamera();

        // 1. Frustum Culling - verifica se il blocco è nel campo visivo della camera
        if (!isInFrustum(camera, entity)) {
            return false;
        }

        // 2. Distance Culling - ignora blocchi troppo lontani
        float maxDistance = 100.0f; // Distanza massima di rendering
        Vector3f cameraPos = camera.getPosition();
        Vector3f blockPos = entity.getPosition();

        float distSquared = cameraPos.distanceSquared(blockPos);
        if (distSquared > maxDistance * maxDistance) {
            return false;
        }

        // 3. Occlusion Culling - verifica se il blocco è coperto
        // Questo è più complesso - implementazione semplificata
        if (isOccluded(scene, entity)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se un blocco è all'interno del frustum della camera
     */
    private static boolean isInFrustum(Camera camera, Entity entity) {
        // Ottieni le dimensioni dell'AABB del blocco (Axis-Aligned Bounding Box)
        Vector3f position = entity.getPosition();
        Vector3f min = new Vector3f(position.x - BLOCK_SIZE / 2,
                position.y - BLOCK_SIZE / 2,
                position.z - BLOCK_SIZE / 2);
        Vector3f max = new Vector3f(position.x + BLOCK_SIZE / 2,
                position.y + BLOCK_SIZE / 2,
                position.z + BLOCK_SIZE / 2);

        // Controlla se l'AABB è all'interno del frustum
        return camera.getFrustum().isBoxInFrustum(min, max);
    }

    /**
     * Verifica se un blocco è completamente coperto da altri blocchi
     * Questa è un'implementazione semplificata
     */
    private static boolean isOccluded(Scene scene, Entity entity) {
        // Implementazione semplice: un blocco è occluso se tutti i 6 lati sono coperti
        // da altri blocchi opachi (non trasparenti come vetro, acqua, ecc.)

        World world = scene.getWorld();
        Vector3f pos = entity.getPosition();
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        int z = Math.round(pos.z);

        // Verifica tutti i 6 lati adiacenti
        boolean allSidesCovered = true;

        // Direzioni: su, giù, nord, sud, est, ovest
        int[][] directions = {
                { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 }, { 1, 0, 0 }, { -1, 0, 0 }
        };

        for (int[] dir : directions) {
            Block adjacent = world.getBlock(x + dir[0], y + dir[1], z + dir[2]);
            if (adjacent == null || !adjacent.isOpaque()) {
                allSidesCovered = false;
                break;
            }
        }

        return allSidesCovered;
    }
    private boolean isOpaque(){
        return type!= BlockType.AIR;
    }
}
