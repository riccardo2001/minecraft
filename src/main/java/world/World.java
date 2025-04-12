package world;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import world.blocks.Block;
import world.chunks.Chunk;
import world.chunks.ChunkPosition;
import world.events.WorldEvent;
import world.events.WorldEvent.BlockChangeEvent;
import world.events.WorldEvent.ChunkLoadEvent;
import world.physics.Gravity;

public class World {
    private Map<ChunkPosition, Chunk> loadedChunks;
    private List<Consumer<WorldEvent>> eventListeners;
    private static final Set<Chunk> dirtyChunks = ConcurrentHashMap.newKeySet();
    private static final int renderDistance = 8;
    private Queue<BlockPosition> fluidUpdateQueue = new ArrayDeque<>();
    private Set<BlockPosition> queuedFluidBlocks = new HashSet<>();
    private long lastFluidUpdateTime = 0;
    private static final long FLUID_UPDATE_INTERVAL = 200; // millisecondi
    private static final int MAX_FLUID_UPDATES_PER_TICK = 5;

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

        // Se abbiamo aggiunto un blocco d'acqua, aggiungiamolo alla coda
        if (block != null && block.getType() == Block.BlockType.WATER) {
            addToFluidQueue(x, y, z);
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

    public void update(float deltaTime) {
        // Gestione ottimizzata dell'aggiornamento dei fluidi
        updateFluidBlocks();
    }
    
    private void updateFluidBlocks() {
        // Aggiorna i fluidi solo a intervalli specifici
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFluidUpdateTime < FLUID_UPDATE_INTERVAL) {
            return;
        }
        
        lastFluidUpdateTime = currentTime;
        
        // Se la coda è vuota, scansiona i chunks attorno al giocatore
        if (fluidUpdateQueue.isEmpty()) {
            scanPlayerAreaForFluidBlocks();
        }
        
        // Processa un numero limitato di blocchi per tick
        int updatedBlocks = 0;
        while (!fluidUpdateQueue.isEmpty() && updatedBlocks < MAX_FLUID_UPDATES_PER_TICK) {
            BlockPosition pos = fluidUpdateQueue.poll();
            queuedFluidBlocks.remove(pos);
            
            // Usa un metodo più leggero per l'aggiornamento del singolo blocco
            Gravity.processFluidBlock(this, pos.x, pos.y, pos.z);
            updatedBlocks++;
        }
    }
    
    private void scanPlayerAreaForFluidBlocks() {
        // Scansiona solo un piccolo raggio attorno al giocatore
        for (Chunk chunk : getActiveChunks()) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    for (int z = 0; z < Chunk.DEPTH; z++) {
                        int worldX = chunk.getChunkX() * Chunk.WIDTH + x;
                        int worldZ = chunk.getChunkZ() * Chunk.DEPTH + z;
                        
                        Block block = getBlock(worldX, y, worldZ);
                        if (block != null && block.getType() == Block.BlockType.WATER) {
                            addToFluidQueue(worldX, y, worldZ);
                        }
                    }
                }
            }
        }
    }
    
    public void addToFluidQueue(int x, int y, int z) {
        BlockPosition pos = new BlockPosition(x, y, z);
        if (!queuedFluidBlocks.contains(pos)) {
            fluidUpdateQueue.offer(pos);
            queuedFluidBlocks.add(pos);
        }
    }
    
    // Ottiene i chunks attivi vicino al giocatore
    private List<Chunk> getActiveChunks() {
        List<Chunk> activeChunks = new ArrayList<>();
        // Considera solo i chunks più vicini al giocatore
        int scanRadius = 2; // Raggio ridotto per migliori performance
        
        // Assumiamo che il centro della scena sia vicino al giocatore
        int centerX = loadedChunks.keySet().stream()
                .mapToInt(ChunkPosition::getX)
                .sum() / Math.max(1, loadedChunks.size());
        int centerZ = loadedChunks.keySet().stream()
                .mapToInt(ChunkPosition::getZ)
                .sum() / Math.max(1, loadedChunks.size());
        
        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                Chunk chunk = getChunk(centerX + dx, centerZ + dz);
                if (chunk != null) {
                    activeChunks.add(chunk);
                }
            }
        }
        return activeChunks;
    }

    // Classe di supporto per tenere traccia delle posizioni dei blocchi
    public static class BlockPosition {
        public final int x, y, z;
        
        public BlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPosition that = (BlockPosition) o;
            return x == that.x && y == that.y && z == that.z;
        }
        
        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}