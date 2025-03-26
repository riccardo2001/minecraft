package world.chunks;

import scene.Entity;
import scene.Scene;
import world.World;
import world.blocks.Block;
import world.generation.WorldGenerator;

import org.joml.Vector4f;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 256;
    public static final int DEPTH = 16;

    private final int chunkX;
    private final int chunkZ;
    private Block[][][] blocks;
    private boolean isDirty;
    private ChunkMesh chunkMesh;
    private Entity chunkEntity;

    public Chunk(int chunkX, int chunkZ, World world) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new Block[WIDTH][HEIGHT][DEPTH];
        this.isDirty = true;
        generateTerrain();
    }

    private void generateTerrain() {
        WorldGenerator generator = new WorldGenerator();
        generator.generateBaseTerrain(this);
        generator.generateTrees(this);
    }

    public void buildMesh(World world, Scene scene) {
        if (chunkMesh == null) {
            chunkMesh = new ChunkMesh();
        }
        chunkMesh.buildMesh(this, world);

        String modelId = "chunk_model_" + chunkX + "_" + chunkZ;
        if (chunkEntity == null) {
            String entityId = "chunk_" + chunkX + "_" + chunkZ;
            chunkEntity = new Entity(entityId, modelId, new Vector4f(0, 0, 1, 1));
            chunkEntity.setPosition(
                    chunkX * WIDTH * Block.BLOCK_SIZE,
                    0,
                    chunkZ * DEPTH * Block.BLOCK_SIZE);
            chunkEntity.updateModelMatrix();

            if (!scene.getModelMap().containsKey(modelId)) {
                scene.registerChunkModel(modelId, chunkMesh.getMesh());
            }
            scene.addChunkEntity(chunkEntity);
        } else if (scene != null) {
            scene.updateChunkMesh(chunkEntity.getId(), chunkMesh.getMesh());
        }
        resetDirtyFlag();
    }

    public void rebuildFullMesh(World world, Scene scene) {
        if (chunkMesh == null) {
            chunkMesh = new ChunkMesh();
        }
        chunkMesh.buildMesh(this, world);

        if (chunkEntity == null) {
            String modelId = "chunk_model_" + chunkX + "_" + chunkZ;
            String entityId = "chunk_" + chunkX + "_" + chunkZ;
            chunkEntity = new Entity(entityId, modelId, new Vector4f(0, 0, 1, 1));
            chunkEntity.setPosition(
                    chunkX * WIDTH * Block.BLOCK_SIZE,
                    0,
                    chunkZ * DEPTH * Block.BLOCK_SIZE);
            scene.addChunkEntity(chunkEntity);
        }

        scene.updateChunkMesh(chunkEntity.getId(), chunkMesh.getMesh());
        setDirty(false);
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (isValidPosition(x, y, z)) {
            blocks[x][y][z] = block;
            isDirty = true;
        }
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public Block getBlock(int x, int y, int z) {
        return isValidPosition(x, y, z) ? blocks[x][y][z] : null;
    }

    private boolean isValidPosition(int x, int y, int z) {
        return x >= 0 && x < WIDTH &&
                y >= 0 && y < HEIGHT &&
                z >= 0 && z < DEPTH;
    }

    public Entity getChunkEntity() {
        return chunkEntity;
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

    public ChunkMesh getChunkMesh() {
        return chunkMesh;
    }
}