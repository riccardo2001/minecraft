package world;

public class Chunk {
    public static final int CHUNK_SIZE = 16; // Typical Minecraft chunk size (16x16x16)
    private Block[][][] blocks;
    public int chunkX; // Chunk position in the world (in chunk coordinates)
    private int chunkY;
    private int chunkZ;

    public Chunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    }

    public void setBlock(int x, int y, int z, Block block) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            blocks[x][y][z] = block;
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            return blocks[x][y][z];
        }
        return null;
    }

    public void render() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (blocks[x][y][z] != null) {
                        // Calculate world position
                        float worldX = (chunkX * CHUNK_SIZE) + x;
                        float worldY = (chunkY * CHUNK_SIZE) + y;
                        float worldZ = (chunkZ * CHUNK_SIZE) + z;

                        // Render the block at the world position
                        blocks[x][y][z].render(worldX, worldY, worldZ);
                    }
                }
            }
        }
    }

    // Convert world coordinates to chunk coordinates
    public static int worldToChunk(float worldCoord) {
        return Math.floorDiv((int) worldCoord, CHUNK_SIZE);
    }

    public Block[][][] getBlocks() {
        return this.blocks;
    }

    public void setBlocks(Block[][][] blocks) {
        this.blocks = blocks;
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public int getChunkY() {
        return this.chunkY;
    }

    public void setChunkY(int chunkY) {
        this.chunkY = chunkY;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }

}
