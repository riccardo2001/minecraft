package world.blocks;

import org.joml.Vector3f;
import org.joml.Vector3i;

import scene.Camera;
import scene.Scene;
import world.World;
import world.chunks.Chunk;

public class Block {
    public static final float BLOCK_SIZE = 1.0f;
    private BlockType type;

    public enum BlockType {
        AIR,
        GRASS,
        DIRT,
        STONE,
        WOOD,
        LEAVES,
        WATER,
    }

    public enum Face {
        TOP, BOTTOM, FRONT, BACK, LEFT, RIGHT;

        public int getOffsetX() {
            return switch (this) {
                case LEFT -> -1;
                case RIGHT -> 1;
                default -> 0;
            };
        }
        
        public int getOffsetY() {
            return switch (this) {
                case BOTTOM -> -1;
                case TOP -> 1;
                default -> 0;
            };
        }
        
        public int getOffsetZ() {
            return switch (this) {
                case FRONT -> 1;
                case BACK -> -1;
                default -> 0;
            };
        }
    }

    public Block(BlockType type) {
        this.type = type;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return type != BlockType.AIR && type != BlockType.WATER; 
    }

    public boolean isOpaque() {
        return type != BlockType.AIR && type != BlockType.LEAVES;
    }

    public static boolean shouldRenderFace(World world, int x, int y, int z, Face face) {
        int checkX = x;
        int checkY = y;
        int checkZ = z;

        switch (face) {
            case TOP:
                checkY++;
                break;
            case BOTTOM:
                checkY--;
                break;
            case FRONT:
                checkZ++;
                break;
            case BACK:
                checkZ--;
                break;
            case RIGHT:
                checkX++;
                break;
            case LEFT:
                checkX--;
                break;
        }

        Block adjacentBlock = world.getBlock(checkX, checkY, checkZ);
        if (adjacentBlock == null) {
            return true;
        }
        return !adjacentBlock.isOpaque();
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
                chunkMinZ + (chunkMaxZ - chunkMinZ) / 2);

        float distSquared = cameraPos.distanceSquared(chunkCenterPos);
        if (distSquared > maxRenderDistanceSquared) {
            return false;
        }

        return true;
    }

    public static Vector3i calculateAdjacentPosition(Vector3i blockPos, Face face) {
        return new Vector3i(blockPos).add(
                face == Face.LEFT ? -1 : face == Face.RIGHT ? 1 : 0,
                face == Face.BOTTOM ? -1 : face == Face.TOP ? 1 : 0,
                face == Face.FRONT ? 1 : face == Face.BACK ? -1 : 0);
    }
}
