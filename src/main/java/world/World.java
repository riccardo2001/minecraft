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
                    Chunk chunk = new Chunk(chunkX, chunkZ);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }
    }

    public void setBlockAtWorldPosition(float worldX, float worldY, float worldZ, Block.BlockType blockType) {
        // Calcola le coordinate del chunk
        int chunkX = (int) Math.floor(worldX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int chunkZ = (int) Math.floor(worldZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        // Calcola le coordinate locali all'interno del chunk
        int localX = (int) Math.floor((worldX % (Chunk.WIDTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);
        int localY = (int) Math.floor(worldY / Block.BLOCK_SIZE);
        int localZ = (int) Math.floor((worldZ % (Chunk.DEPTH * Block.BLOCK_SIZE)) / Block.BLOCK_SIZE);
        
        // Correggi le coordinate negative
        if (worldX < 0 && localX == 0) {
            chunkX--;
            localX = Chunk.WIDTH - 1;
        }
        if (worldZ < 0 && localZ == 0) {
            chunkZ--;
            localZ = Chunk.DEPTH - 1;
        }

        ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);
        Chunk chunk = loadedChunks.get(chunkPos);

        if (chunk != null) {
            // Crea un nuovo blocco senza entità
            Block newBlock = new Block(blockType);
            chunk.setBlock(localX, localY, localZ, newBlock);
            
            // Segna il chunk come modificato (dirty) per ricostruire la mesh
            // Questo è già fatto implicitamente nel metodo setBlock di Chunk
            
            // Segna anche i chunk adiacenti come dirty se il blocco è sul bordo
            // perché la visibilità delle facce può cambiare
            updateAdjacentChunksIfNeeded(chunkX, chunkZ, localX, localY, localZ);
        }
    }
    
    private void updateAdjacentChunksIfNeeded(int chunkX, int chunkZ, int localX, int localY, int localZ) {
        // Verifica se il blocco è sul bordo del chunk e aggiorna i chunk adiacenti
        if (localX == 0) {
            Chunk adjacentChunk = getChunk(chunkX - 1, chunkZ);
            if (adjacentChunk != null) {
                adjacentChunk.resetDirtyFlag(); // Forza il rebuild della mesh
            }
        }
        if (localX == Chunk.WIDTH - 1) {
            Chunk adjacentChunk = getChunk(chunkX + 1, chunkZ);
            if (adjacentChunk != null) {
                adjacentChunk.resetDirtyFlag(); // Forza il rebuild della mesh
            }
        }
        if (localZ == 0) {
            Chunk adjacentChunk = getChunk(chunkX, chunkZ - 1);
            if (adjacentChunk != null) {
                adjacentChunk.resetDirtyFlag(); // Forza il rebuild della mesh
            }
        }
        if (localZ == Chunk.DEPTH - 1) {
            Chunk adjacentChunk = getChunk(chunkX, chunkZ + 1);
            if (adjacentChunk != null) {
                adjacentChunk.resetDirtyFlag(); // Forza il rebuild della mesh
            }
        }
    }

    public Map<ChunkPosition, Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    public Block getBlock(int x, int y, int z) {
        // Trova il chunk che contiene queste coordinate
        int chunkX = Math.floorDiv(x, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(z, Chunk.DEPTH);

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return null;
        }

        // Trova il blocco all'interno del chunk
        int localX = Math.floorMod(x, Chunk.WIDTH);
        int localZ = Math.floorMod(z, Chunk.DEPTH);
        
        return chunk.getBlock(localX, y, localZ);
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        ChunkPosition position = new ChunkPosition(chunkX, chunkZ);
        return loadedChunks.get(position);
    }

    public int getRenderDistance() {
        return renderDistance;
    }
}