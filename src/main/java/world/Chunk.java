package world;

import scene.Entity;
import scene.Scene;
import org.joml.Vector4f;

public class Chunk {
    public static final int WIDTH = 32;
    public static final int HEIGHT = 256;
    public static final int DEPTH = 32;

    private final int chunkX;
    private final int chunkZ;
    private Block[][][] blocks;
    private boolean isDirty;
    private ChunkMesh chunkMesh;
    private Entity chunkEntity;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new Block[WIDTH][HEIGHT][DEPTH];
        this.isDirty = true;
        System.out.println("Generating Chunk at: " + chunkX + ", " + chunkZ);

        generateInitialTerrain();
    }

    private void generateInitialTerrain() {
        // Ciclo corretto: x da 0 a WIDTH, z da 0 a DEPTH, y da 0 a HEIGHT.
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int terrainHeight = calculateTerrainHeight(x, z);

                for (int y = 0; y < HEIGHT; y++) {
                    Block.BlockType blockType = determineBlockType(x, y, z, terrainHeight);

                    if (blockType != Block.BlockType.AIR) {
                        Block block = new Block(blockType);
                        setBlock(x, y, z, block);
                    }
                }
            }
        }
    }

    private int calculateTerrainHeight(int localX, int localZ) {
        double noise1 = Math.sin(localX * 0.1) * 2;
        double noise2 = Math.cos(localZ * 0.2) * 3;
        double noise3 = Math.sin(localX * 0.05 + localZ * 0.05) * 4;

        return 64 + (int) (noise1 + noise2 + noise3);
    }

    private Block.BlockType determineBlockType(int x, int y, int z, int terrainHeight) {
        if (y == 0)
            return Block.BlockType.STONE;
        if (y < terrainHeight - 3)
            return Block.BlockType.STONE;
        if (y < terrainHeight - 1)
            return Block.BlockType.STONE;
        if (y < terrainHeight)
            return Block.BlockType.GRASS;
        return Block.BlockType.AIR;
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (isValidPosition(x, y, z)) {
            blocks[x][y][z] = block;
            isDirty = true;
        }
    }

    public Block getBlock(int x, int y, int z) {
        return isValidPosition(x, y, z) ? blocks[x][y][z] : null;
    }

    private boolean isValidPosition(int x, int y, int z) {
        return x >= 0 && x < WIDTH &&
                y >= 0 && y < HEIGHT &&
                z >= 0 && z < DEPTH;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void resetDirtyFlag() {
        isDirty = false;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Block[][][] getBlocks() {
        return blocks;
    }
    
    public void buildMesh(World world, Scene scene) {
        if (chunkMesh == null) {
            chunkMesh = new ChunkMesh();
        }
        
        chunkMesh.buildMesh(this, world);
        
        if (chunkEntity == null) {
            // Crea un'entità per il chunk
            String entityId = "chunk_" + chunkX + "_" + chunkZ;
            chunkEntity = new Entity(entityId, "chunk", new Vector4f(0, 0, 1, 1));
            chunkEntity.setPosition(
                chunkX * WIDTH * Block.BLOCK_SIZE, 
                0, 
                chunkZ * DEPTH * Block.BLOCK_SIZE
            );
            chunkEntity.updateModelMatrix();
            
            // Aggiungi l'entità al model
            if (scene != null) {
                if (!scene.getModelMap().containsKey("chunk")) {
                    scene.registerChunkModel(chunkMesh.getMesh());
                }
                scene.addChunkEntity(chunkEntity);
            }
        } else if (scene != null) {
            // Aggiorna la mesh esistente
            scene.updateChunkMesh(chunkEntity.getId(), chunkMesh.getMesh());
        }
        
        resetDirtyFlag();
    }
    
    public Entity getChunkEntity() {
        return chunkEntity;
    }
}