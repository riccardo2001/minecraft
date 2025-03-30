package world;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import world.blocks.Block;
import world.chunks.Chunk;
import world.chunks.ChunkPosition;
import world.events.WorldEvent;
import world.events.WorldEvent.BlockChangeEvent;
import world.events.WorldEvent.ChunkLoadEvent;

public class World {
    private Map<ChunkPosition, Chunk> loadedChunks;
    private List<Consumer<WorldEvent>> eventListeners;
    private static final Set<Chunk> dirtyChunks = ConcurrentHashMap.newKeySet();
    private static final int renderDistance = 8;

    public World() {
        this.loadedChunks = new ConcurrentHashMap<>();
        this.eventListeners = new CopyOnWriteArrayList<>();
    }
    
    public void addEventListener(Consumer<WorldEvent> listener) {
        eventListeners.add(listener);
    }
    
    public void removeEventListener(Consumer<WorldEvent> listener) {
        eventListeners.remove(listener);
    }
    
    private void fireEvent(WorldEvent event) {
        for (Consumer<WorldEvent> listener : eventListeners) {
            listener.accept(event);
        }
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
                    Chunk chunk = new Chunk(chunkX, chunkZ, this);
                    loadedChunks.put(chunkPos, chunk);
                    fireEvent(new ChunkLoadEvent(chunk));
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
            
            Block oldBlock = chunk.getBlock(localX, y, localZ);
            chunk.setBlock(localX, y, localZ, block);
            
            fireEvent(new BlockChangeEvent(x, y, z, oldBlock, block));
        }
    }

    public void setDirty(Chunk chunk) {
        dirtyChunks.add(chunk);
    }

    public Set<Chunk> getDirtyChunks() {
        return dirtyChunks;
    }

    public static void markChunkDirty(Chunk chunk) {
        dirtyChunks.add(chunk);
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

}