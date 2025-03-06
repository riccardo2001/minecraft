package world;

import graphics.Material;
import graphics.Mesh;
import graphics.Model;
import graphics.Texture;
import scene.Entity;
import scene.Scene;

public class Block {
    // Definisci costanti per le dimensioni standard del blocco
    public static final float BLOCK_SIZE = 1.0f;

    private static final float[] POSITIONS = new float[] {
            // V0
            -0.5f, 0.5f, 0.5f,
            // V1
            -0.5f, -0.5f, 0.5f,
            // V2
            0.5f, -0.5f, 0.5f,
            // V3
            0.5f, 0.5f, 0.5f,
            // V4
            -0.5f, 0.5f, -0.5f,
            // V5
            0.5f, 0.5f, -0.5f,
            // V6
            -0.5f, -0.5f, -0.5f,
            // V7
            0.5f, -0.5f, -0.5f,

            // For text coords in top face
            // V8: V4 repeated
            -0.5f, 0.5f, -0.5f,
            // V9: V5 repeated
            0.5f, 0.5f, -0.5f,
            // V10: V0 repeated
            -0.5f, 0.5f, 0.5f,
            // V11: V3 repeated
            0.5f, 0.5f, 0.5f,

            // For text coords in right face
            // V12: V3 repeated
            0.5f, 0.5f, 0.5f,
            // V13: V2 repeated
            0.5f, -0.5f, 0.5f,

            // For text coords in left face
            // V14: V0 repeated
            -0.5f, 0.5f, 0.5f,
            // V15: V1 repeated
            -0.5f, -0.5f, 0.5f,

            // For text coords in bottom face
            // V16: V6 repeated
            -0.5f, -0.5f, -0.5f,
            // V17: V7 repeated
            0.5f, -0.5f, -0.5f,
            // V18: V1 repeated
            -0.5f, -0.5f, 0.5f,
            // V19: V2 repeated
            0.5f, -0.5f, 0.5f,
    };

    private static final float[] TEXT_COORDS = new float[] {
            0.0f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.5f, 0.0f,

            0.0f, 0.0f,
            0.5f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,

            // For text coords in top face
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.0f, 1.0f,
            0.5f, 1.0f,

            // For text coords in right face
            0.0f, 0.0f,
            0.0f, 0.5f,

            // For text coords in left face
            0.5f, 0.0f,
            0.5f, 0.5f,

            // For text coords in bottom face
            0.5f, 0.0f,
            1.0f, 0.0f,
            0.5f, 0.5f,
            1.0f, 0.5f,
    };

    private static final int[] INDICES = new int[] {
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top Face
            8, 10, 11, 9, 8, 11,
            // Right face
            12, 13, 7, 5, 12, 7,
            // Left face
            14, 15, 6, 4, 14, 6,
            // Bottom face
            16, 18, 19, 17, 16, 19,
            // Back face
            4, 6, 7, 5, 4, 7
    };

    // Riferimento all'entità del blocco
    private Entity blockEntity;

    // Tipo di blocco (può essere utile per distinguere diversi tipi di blocchi)
    private BlockType type;

    // Enum per i tipi di blocco
    public enum BlockType {
        AIR,
        GRASS,
        STONE,
        WOOD
    }

    // Costruttore base
    public Block(Scene scene, BlockType type, String texturePath) {
        this.type = type;
        createBlockEntity(scene, texturePath);
    }

    // Metodo interno per creare l'entità del blocco
    private void createBlockEntity(Scene scene, String texturePath) {
        // Usa un ID basato sul tipo di blocco
        String modelId = "block-model-" + type.name().toLowerCase();
        String entityId = "block-entity-" + type.name().toLowerCase() + "-" +
                System.currentTimeMillis();

        // Controlla se il modello esiste già
        Model blockModel = scene.getModelMap().get(modelId);
        if (blockModel == null) {
            // Carica la texture
            Texture texture = scene.getTextureCache().createTexture(texturePath);

            // Crea il materiale
            Material material = new Material();
            material.setTexturePath(texture.getTexturePath());

            // Crea la mesh
            Mesh mesh = new Mesh(POSITIONS, TEXT_COORDS, INDICES);
            material.getMeshList().add(mesh);

            // Crea il modello
            blockModel = new Model(modelId, java.util.List.of(material));
            scene.addModel(blockModel);
        }

        // Crea l'entità
        blockEntity = new Entity(entityId, blockModel.getId());
        scene.addEntity(blockEntity);

        System.out.println("Created block: ModelID=" + modelId +
                ", EntityID=" + entityId +
                ", Type=" + type);
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

    public static String getTexturePathForBlockType(Block.BlockType blockType) {
        switch (blockType) {
            case GRASS:
                return "textures/grass.png";
            case STONE:
                return "textures/grass.png";
            default:
                return "textures/grass.png";
        }
    }
}