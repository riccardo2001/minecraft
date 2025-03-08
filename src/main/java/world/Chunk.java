package world;

public class Chunk {
    public static final int WIDTH = 16;
    public static final int HEIGHT = 256;
    public static final int DEPTH = 16;

    private final int chunkX;
    private final int chunkZ;

    private Block[][][] blocks;

    private boolean isDirty;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.blocks = new Block[WIDTH][HEIGHT][DEPTH];
        this.isDirty = true;
        System.out.println("Generating Chunk at: " + chunkX + ", " + chunkZ);

        generateInitialTerrain();
        printChunkBlocks();
    }

    private void generateInitialTerrain() {
        for (int x = 0; x < 1; x++) {
            for (int z = 0; z < 1; z++) {
                int terrainHeight = calculateTerrainHeight(x, z);

                for (int y = 0; y < 1; y++) {
                    Block.BlockType blockType = determineBlockType(x, y, z, terrainHeight);

                    if (blockType != Block.BlockType.AIR) {
                        float worldX = (chunkX * WIDTH + x) * Block.BLOCK_SIZE;
                        float worldY = y * Block.BLOCK_SIZE;
                        float worldZ = (chunkZ * DEPTH + z) * Block.BLOCK_SIZE;

                        Block block = new Block(blockType);
                        block.setWorldPosition(worldX, worldY, worldZ);

                        setBlock(x, y, z, block);
                    }
                }
            }
        }
    }

    public void printChunkBlocks() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    if (blocks[x][y][z] != null) {
                        System.out.println("Block at: x=" + x + ", y=" + y + ", z=" + z +
                                ", Type=" + blocks[x][y][z].getType());
                    }
                }
            }
        }
    }

    private int calculateTerrainHeight(int localX, int localZ) {
        // double noise1 = Math.sin(localX * 0.1) * 10;
        // double noise2 = Math.cos(localZ * 0.2) * 8;
        // double noise3 = Math.sin(localX * 0.05 + localZ * 0.05) * 15;

        // return 64 + (int) (noise1 + noise2 + noise3);
        return 4;
    }

    private Block.BlockType determineBlockType(int x, int y, int z, int terrainHeight) {
        // Bedrock al fondo
        if (y == 0)
            return Block.BlockType.STONE;

        // Strati del terreno
        if (y < terrainHeight - 3)
            return Block.BlockType.STONE; // Pietra profonda
        if (y < terrainHeight - 1)
            return Block.BlockType.STONE; // Pietra vicino alla superficie
        if (y < terrainHeight)
            return Block.BlockType.GRASS; // Erba in superficie

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
}