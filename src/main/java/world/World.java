package world;

import java.util.HashMap;
import java.util.Map;

public class World {
    private Map<ChunkPosition, Chunk> loadedChunks;
    private int renderDistance = 8;

    public World() {
        this.loadedChunks = new HashMap<>();
    }

    public Map<ChunkPosition, Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPosition position = new ChunkPosition(chunkX, chunkZ);
        return loadedChunks.get(position);
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public int getTerrainHeight(int globalX, int globalZ) {
        double hillFactor = 20.0;
        double frequency = 0.05;
        
        double noiseX = globalX * frequency;
        double noiseZ = globalZ * frequency;
        
        double height = Math.sin(noiseX) * Math.cos(noiseZ) * hillFactor;
        return 64 + (int) height;
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
                    loadedChunks.put(chunkPos, new Chunk(chunkX, chunkZ, this));
                }
            }
        }
    }

    public Block getBlock(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(z, Chunk.DEPTH);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return null;
        }

        int localX = Math.floorMod(x, Chunk.WIDTH);
        int localZ = Math.floorMod(z, Chunk.DEPTH);
        
        return chunk.getBlock(localX, y, localZ);
    }
}