package world;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<ChunkPosition, Chunk> loadedChunks;
    private int renderDistance = 1;

    public World() {
        this.loadedChunks = new HashMap<>();
    }

    public void generateInitialWorld(float centerX, float centerZ) {
        int centerChunkX = (int) Math.floor(centerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int centerChunkZ = (int) Math.floor(centerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);

                if (!loadedChunks.containsKey(chunkPos)) {
                    // Passa il textureCache al costruttore di Chunk
                    Chunk chunk = new Chunk(chunkX, chunkZ);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }
    }

    public void setBlockAtWorldPosition(float worldX, float worldY, float worldZ, Block.BlockType blockType) {
        int chunkX = (int) Math.floor(worldX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int chunkZ = (int) Math.floor(worldZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        int localX = (int) Math.floor((worldX % (Chunk.WIDTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);
        int localY = (int) Math.floor(worldY / Block.BLOCK_SIZE);
        int localZ = (int) Math.floor((worldZ % (Chunk.DEPTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);

        ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);
        Chunk chunk = loadedChunks.get(chunkPos);

        if (chunk != null) {
            Block newBlock = new Block(blockType);
            newBlock.setWorldPosition(worldX, worldY, worldZ);
            chunk.setBlock(localX, localY, localZ, newBlock);
        }
    }

    public Map<ChunkPosition, Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    public Block getBlock(int x, int y, int z) {
        // Trova il chunk che contiene queste coordinate
        int chunkX = x >> 4; // Dividi per 16
        int chunkZ = z >> 4;

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return null;
        }

        // Trova il blocco all'interno del chunk
        return chunk.getBlock(x & 15, y, z & 15);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPosition position = new ChunkPosition(chunkX, chunkZ);
        return loadedChunks.get(position);
    }

    public int getRenderDistance() {
        return renderDistance;
    }
}