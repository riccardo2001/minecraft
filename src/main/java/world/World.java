package world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import scene.Scene;

public class World {
    private Scene scene;
    private Map<ChunkPosition, Chunk> loadedChunks;
    private int renderDistance = 4; // Chunk da caricare intorno al giocatore

    public World(Scene scene) {
        this.scene = scene;
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
                    Chunk chunk = new Chunk(chunkX, chunkZ, scene);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }
    }

    public void updateWorldGeneration(float playerX, float playerZ) {
        int centerChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int centerChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);

                if (!loadedChunks.containsKey(chunkPos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ, scene);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }

        // Rimuovi chunk lontani
        loadedChunks.entrySet().removeIf(entry -> {
            ChunkPosition chunkPos = entry.getKey();
            int chunkDistanceX = Math.abs(chunkPos.x - centerChunkX);
            int chunkDistanceZ = Math.abs(chunkPos.z - centerChunkZ);
            
            return chunkDistanceX > renderDistance || chunkDistanceZ > renderDistance;
        });
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
            Block newBlock = new Block(scene, blockType, Block.getTexturePathForBlockType(blockType));
            newBlock.setWorldPosition(worldX, worldY, worldZ);
            chunk.setBlock(localX, localY, localZ, newBlock);
        }
    }

    private static class ChunkPosition {
        int x, z;

        ChunkPosition(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public Map<ChunkPosition, Chunk> getLoadedChunks() {
        return loadedChunks;
    }
}