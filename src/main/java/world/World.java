package world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void setBlock(int x, int y, int z, Block block) {
        int chunkX = Math.floorDiv(x, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(z, Chunk.DEPTH);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk != null) {
            int localX = Math.floorMod(x, Chunk.WIDTH);
            int localZ = Math.floorMod(z, Chunk.DEPTH);
            chunk.setBlock(localX, y, localZ, block);
        }
    }

    public Chunk getChunkContaining(int blockX, int blockZ) {
        int chunkX = Math.floorDiv(blockX, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(blockZ, Chunk.DEPTH);
        return loadedChunks.get(new ChunkPosition(chunkX, chunkZ));
    }

    public List<Chunk> getAdjacentChunks(int worldX, int worldZ) {
        List<Chunk> chunks = new ArrayList<>();

        // Calcola le coordinate del chunk corrente
        int chunkX = Math.floorDiv(worldX, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(worldZ, Chunk.DEPTH);

        // Controlla tutti i chunk nel raggio 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Chunk chunk = getChunk(chunkX + dx, chunkZ + dz);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }
        return chunks;
    }
}