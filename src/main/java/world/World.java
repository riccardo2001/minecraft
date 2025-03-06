package world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import graphics.TextureCacheAtlas;
import scene.Scene;

public class World {
    private Scene scene;
    private Map<ChunkPosition, Chunk> loadedChunks;
    private TextureCacheAtlas textureCache;
    private int renderDistance = 2;


    public World(Scene scene, TextureCacheAtlas textureCache) {
        this.scene = scene;
        this.loadedChunks = new HashMap<>();
        this.textureCache = textureCache;
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
                    Chunk chunk = new Chunk(chunkX, chunkZ, scene, textureCache);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }
    }

    public void updateWorldGeneration(float playerX, float playerZ) {
        int centerChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int centerChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        // Identifica i chunk da rimuovere prima di aggiungerne di nuovi
        Set<ChunkPosition> chunksToRemove = new HashSet<>();

        for (Map.Entry<ChunkPosition, Chunk> entry : loadedChunks.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            int chunkDistanceX = Math.abs(chunkPos.x - centerChunkX);
            int chunkDistanceZ = Math.abs(chunkPos.z - centerChunkZ);

            if (chunkDistanceX > renderDistance || chunkDistanceZ > renderDistance) {
                chunksToRemove.add(chunkPos);
            }
        }

        for (ChunkPosition posToRemove : chunksToRemove) {
            Chunk chunkToRemove = loadedChunks.get(posToRemove);
            if (chunkToRemove != null) {
                chunkToRemove.cleanup();
                System.out.println("Rimozione chunk alla posizione: " + posToRemove);
            }
            loadedChunks.remove(posToRemove);
        }

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);

                if (!loadedChunks.containsKey(chunkPos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ, scene, textureCache);
                    loadedChunks.put(chunkPos, chunk);
                    System.out.println("Aggiunto chunk alla posizione: " + chunkPos);
                }
            }
        }
    }

    public void setBlockAtWorldPosition(float worldX, float worldY, float worldZ, Block.BlockType blockType,
            TextureCacheAtlas textureCache) {
        int chunkX = (int) Math.floor(worldX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int chunkZ = (int) Math.floor(worldZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        int localX = (int) Math.floor((worldX % (Chunk.WIDTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);
        int localY = (int) Math.floor(worldY / Block.BLOCK_SIZE);
        int localZ = (int) Math.floor((worldZ % (Chunk.DEPTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);

        ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);
        Chunk chunk = loadedChunks.get(chunkPos);

        if (chunk != null) {
            Block newBlock = new Block(scene, blockType, textureCache);
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
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
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
}