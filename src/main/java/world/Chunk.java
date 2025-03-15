package world;

import scene.Entity;
import scene.Scene;
import org.joml.Vector4f;
import java.util.Random;

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
    private Random random;

    private static final int BASE_HEIGHT = 64;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new Block[WIDTH][HEIGHT][DEPTH];
        this.isDirty = true;

        long seed = ((long) chunkX << 32) | ((long) chunkZ & 0xFFFFFFFFL);
        this.random = new Random(seed ^ 0xDEADBEEF);

        generateInitialTerrain();
    }

    private void generateInitialTerrain() {
        int[][] heightMap = generateHeightMap();

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int terrainHeight = heightMap[x][z];

                for (int y = 0; y < HEIGHT; y++) {
                    Block.BlockType blockType;

                    blockType = determineBlockType(x, y, z, terrainHeight);

                    if (blockType != Block.BlockType.AIR) {
                        Block block = new Block(blockType);
                        setBlock(x, y, z, block);
                    }
                }
            }
        }

        generateTrees(heightMap);
    }

    private int[][] generateHeightMap() {
        int[][] heightMap = new int[WIDTH][DEPTH];

        // Terreno piatto con altezza costante
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                heightMap[x][z] = BASE_HEIGHT;
            }
        }

        return heightMap;
    }

    private void generateTrees(int[][] heightMap) {
        long dynamicSeed = System.nanoTime() ^ (chunkX * 73856093L) ^ (chunkZ * 19349663L);
        Random treeRandom = new Random(dynamicSeed);

        int maxTrees = 5;
        int numTrees = treeRandom.nextInt(maxTrees + 1);

        numTrees += (int) (Math.abs(Math.sin(chunkX * chunkZ * 0.1)) * 2);
        numTrees = Math.min(numTrees, 8);

        System.out.println("Tentativo di generare " + numTrees + " alberi nel chunk " + chunkX + "," + chunkZ);

        int alberiGenerati = 0;
        int tentativi = 0;
        int maxTentativi = 30;

        boolean[][] positionTried = new boolean[WIDTH][DEPTH];

        while (alberiGenerati < numTrees && tentativi < maxTentativi) {
            tentativi++;

            int treeX, treeZ;
            do {
                treeX = 3 + treeRandom.nextInt(WIDTH - 6);
                treeZ = 3 + treeRandom.nextInt(DEPTH - 6);
            } while (positionTried[treeX][treeZ]);

            positionTried[treeX][treeZ] = true;

            int terrainHeight = heightMap[treeX][treeZ];

            Block block = getBlock(treeX, terrainHeight - 1, treeZ);
            if (block != null && block.getType() == Block.BlockType.GRASS) {
                if (terrainHeight + 11 < HEIGHT && hasEnoughSpace(treeX, terrainHeight, treeZ)) {

                    generateTree(treeX, terrainHeight, treeZ, treeRandom.nextInt(3));
                    alberiGenerati++;
                }
            }
        }

        System.out.println("Generati " + alberiGenerati + " alberi nel chunk " + chunkX + "," + chunkZ);
    }

    private boolean hasEnoughSpace(int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;
                if (isValidPosition(checkX, y, checkZ)) {
                    Block block = getBlock(checkX, y, checkZ);
                    if (block != null && block.getType() == Block.BlockType.WOOD) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void generateTree(int x, int y, int z, int treeType) {
        if (!isValidPosition(x, y, z) || !isTreeAreaValid(x, y, z)) {
            return;
        }

        int trunkHeight;
        int leafSize;

        switch (treeType) {
            case 0:
                trunkHeight = 4 + random.nextInt(2);
                leafSize = 1;
                break;
            case 1:
                trunkHeight = 5 + random.nextInt(2);
                leafSize = 2;
                break;
            case 2:
            default:
                trunkHeight = 6 + random.nextInt(2);
                leafSize = 2;
                break;
        }

        for (int i = 0; i < trunkHeight; i++) {
            setBlock(x, y + i, z, new Block(Block.BlockType.WOOD));
        }

        generateLeaves(x, y + trunkHeight - 2, z, leafSize, treeType);

        setBlock(x, y + trunkHeight, z, new Block(Block.BlockType.LEAVES));
        if (treeType == 2 && y + trunkHeight + 1 < HEIGHT) {
            setBlock(x, y + trunkHeight + 1, z, new Block(Block.BlockType.LEAVES));
        }
    }

    private boolean isTreeAreaValid(int x, int y, int z) {
        int leafSize = 3;
        if (x - leafSize < 0 || x + leafSize >= WIDTH ||
                z - leafSize < 0 || z + leafSize >= DEPTH ||
                y + 10 >= HEIGHT) {
            return false;
        }
        return true;
    }

    private void generateLeaves(int x, int y, int z, int leafSize, int treeType) {
        for (int dx = -leafSize - 1; dx <= leafSize + 1; dx++) {
            for (int dz = -leafSize - 1; dz <= leafSize + 1; dz++) {
                int maxDistance = Math.abs(dx) + Math.abs(dz);
                int leafHeight = treeType == 2 ? 4 : 3;

                for (int dy = 0; dy < leafHeight; dy++) {
                    boolean shouldPlace = false;

                    if (treeType == 0) {
                        shouldPlace = maxDistance <= leafSize + 1;
                    } else if (treeType == 1) {
                        shouldPlace = (maxDistance <= leafSize + 1) &&
                                !(Math.abs(dx) == leafSize + 1 && Math.abs(dz) == leafSize + 1);
                    } else {
                        shouldPlace = (maxDistance <= leafSize + 1) &&
                                !((Math.abs(dx) == leafSize + 1 && Math.abs(dz) == leafSize + 1) && dy != 1);

                        if (!shouldPlace && random.nextDouble() < 0.2 && maxDistance <= leafSize + 2) {
                            shouldPlace = true;
                        }
                    }

                    if (shouldPlace) {
                        int leafX = x + dx;
                        int leafY = y + dy;
                        int leafZ = z + dz;

                        if (isValidPosition(leafX, leafY, leafZ)) {
                            // Non sostituire il tronco con le foglie
                            if (!(dx == 0 && dz == 0 && dy == 0)) {
                                Block existingBlock = getBlock(leafX, leafY, leafZ);
                                if (existingBlock == null || existingBlock.getType() == Block.BlockType.AIR) {
                                    // Variazione casuale per rendere la chioma meno regolare
                                    if (random.nextDouble() > 0.1 || maxDistance <= leafSize) {
                                        setBlock(leafX, leafY, leafZ, new Block(Block.BlockType.LEAVES));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Block.BlockType determineBlockType(int x, int y, int z, int terrainHeight) {
        if (y == 0)
            return Block.BlockType.STONE;
        if (y < terrainHeight - 1)
            return Block.BlockType.DIRT;
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
                    chunkZ * DEPTH * Block.BLOCK_SIZE);
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