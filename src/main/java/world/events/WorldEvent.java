package world.events;

import world.blocks.Block;
import world.chunks.Chunk;
import world.chunks.ChunkPosition;

public abstract class WorldEvent {
    private final long timestamp;

    public WorldEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public static class BlockChangeEvent extends WorldEvent {
        private final int x, y, z;
        private final Block oldBlock;
        private final Block newBlock;
        
        public BlockChangeEvent(int x, int y, int z, Block oldBlock, Block newBlock) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.oldBlock = oldBlock;
            this.newBlock = newBlock;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
        public Block getOldBlock() { return oldBlock; }
        public Block getNewBlock() { return newBlock; }
    }
    
    public static class ChunkLoadEvent extends WorldEvent {
        private final Chunk chunk;
        
        public ChunkLoadEvent(Chunk chunk) {
            this.chunk = chunk;
        }
        
        public Chunk getChunk() { return chunk; }
    }
    
    public static class ChunkUnloadEvent extends WorldEvent {
        private final ChunkPosition position;
        
        public ChunkUnloadEvent(ChunkPosition position) {
            this.position = position;
        }
        
        public ChunkPosition getPosition() { return position; }
    }
}
