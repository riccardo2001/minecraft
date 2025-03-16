package world;

import org.joml.Vector3f;

import scene.Camera;
import scene.Scene;

public class Block {
    public static final float BLOCK_SIZE = 1.0f;
    private BlockType type;

    public enum BlockType {
        AIR,
        GRASS,
        DIRT,
        STONE,
        WOOD,
        LEAVES
    }

    public enum Face {
        TOP, BOTTOM, FRONT, BACK, LEFT, RIGHT
    }

    public Block(BlockType type) {
        this.type = type;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return type != BlockType.AIR;
    }

    public boolean isOpaque() {
        return type != BlockType.AIR && type != BlockType.LEAVES;
    }
    
    public static boolean shouldRenderFace(World world, int x, int y, int z, Face face) {
        int checkX = x;
        int checkY = y;
        int checkZ = z;
        
        switch (face) {
            case TOP: checkY = y + 1; break;
            case BOTTOM: checkY = y - 1; break;
            case FRONT: checkZ = z + 1; break;
            case BACK: checkZ = z - 1; break;
            case RIGHT: checkX = x + 1; break;
            case LEFT: checkX = x - 1; break;
        }
        
        Block adjacentBlock = world.getBlock(checkX, checkY, checkZ);
        return adjacentBlock == null || !adjacentBlock.isOpaque();
    }
    
    public static boolean isChunkVisible(Scene scene, Chunk chunk) {
        Camera camera = scene.getCamera();

        float chunkMinX = chunk.getChunkX() * Chunk.WIDTH * BLOCK_SIZE;
        float chunkMinY = 0;
        float chunkMinZ = chunk.getChunkZ() * Chunk.DEPTH * BLOCK_SIZE;
        float chunkMaxX = (chunk.getChunkX() + 1) * Chunk.WIDTH * BLOCK_SIZE;
        float chunkMaxY = Chunk.HEIGHT * BLOCK_SIZE;
        float chunkMaxZ = (chunk.getChunkZ() + 1) * Chunk.DEPTH * BLOCK_SIZE;
        
        var chunkMin = new org.joml.Vector3f(chunkMinX, chunkMinY, chunkMinZ);
        var chunkMax = new org.joml.Vector3f(chunkMaxX, chunkMaxY, chunkMaxZ);

        if (!camera.getFrustum().isBoxInFrustum(chunkMin, chunkMax)) {
            return false;
        }

        float maxRenderDistanceSquared = 256.0f * 256.0f; 
        Vector3f cameraPos = camera.getPosition();
        Vector3f chunkCenterPos = new Vector3f(
            chunkMinX + (chunkMaxX - chunkMinX) / 2,
            chunkMinY + (chunkMaxY - chunkMinY) / 2,
            chunkMinZ + (chunkMaxZ - chunkMinZ) / 2
        );
        
        float distSquared = cameraPos.distanceSquared(chunkCenterPos);
        if (distSquared > maxRenderDistanceSquared) {
            return false;
        }

        return true;
    }
}
