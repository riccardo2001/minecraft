package world;

import org.joml.Vector3f;

import scene.Camera;
import scene.Entity;
import scene.Scene;

public class Block {
    public static final float BLOCK_SIZE = 1.0f;
    private Entity blockEntity;
    private BlockType type;

    public enum BlockType {
        AIR,
        GRASS,
        DIRT,
        STONE,
        WOOD
    }

    public enum Face {
        TOP, BOTTOM, FRONT, BACK, LEFT, RIGHT
    }

    public Block(BlockType type) {
        this.type = type;
        this.blockEntity = Scene.createEntity(type);
    }

    public void setWorldPosition(float x, float y, float z) {
        blockEntity.setPosition(x, y, z);
    }

    public Entity getEntity() {
        return blockEntity;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return type != BlockType.AIR;
    }

    public void updateModelMatrix() {
        blockEntity.updateModelMatrix();
    }

    public static boolean isBlockVisible(Scene scene, Entity entity) {
        Camera camera = scene.getCamera();

        // 1. Frustum Culling - verifica se il blocco è nel campo visivo della camera
        if (!isInFrustum(camera, entity)) {
            return false;
        }

        // 2. Distance Culling - ignora blocchi troppo lontani
        float maxDistance = 100.0f; // Distanza massima di rendering
        Vector3f cameraPos = camera.getPosition();
        Vector3f blockPos = entity.getPosition();

        float distSquared = cameraPos.distanceSquared(blockPos);
        if (distSquared > maxDistance * maxDistance) {
            return false;
        }

        // 3. Occlusion Culling - verifica se il blocco è coperto
        // Questo è più complesso - implementazione semplificata
        if (isOccluded(scene, entity)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se un blocco è all'interno del frustum della camera
     */
    private static boolean isInFrustum(Camera camera, Entity entity) {
        // Ottieni le dimensioni dell'AABB del blocco (Axis-Aligned Bounding Box)
        Vector3f position = entity.getPosition();
        Vector3f min = new Vector3f(position.x - BLOCK_SIZE / 2,
                position.y - BLOCK_SIZE / 2,
                position.z - BLOCK_SIZE / 2);
        Vector3f max = new Vector3f(position.x + BLOCK_SIZE / 2,
                position.y + BLOCK_SIZE / 2,
                position.z + BLOCK_SIZE / 2);

        // Controlla se l'AABB è all'interno del frustum
        return camera.getFrustum().isBoxInFrustum(min, max);
    }

    /**
     * Verifica se un blocco è completamente coperto da altri blocchi
     * Questa è un'implementazione semplificata
     */
    private static boolean isOccluded(Scene scene, Entity entity) {
        // Implementazione semplice: un blocco è occluso se tutti i 6 lati sono coperti
        // da altri blocchi opachi (non trasparenti come vetro, acqua, ecc.)

        World world = scene.getWorld();
        Vector3f pos = entity.getPosition();
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        int z = Math.round(pos.z);

        // Verifica tutti i 6 lati adiacenti
        boolean allSidesCovered = true;

        // Direzioni: su, giù, nord, sud, est, ovest
        int[][] directions = {
                { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 }, { 1, 0, 0 }, { -1, 0, 0 }
        };

        for (int[] dir : directions) {
            Block adjacent = world.getBlock(x + dir[0], y + dir[1], z + dir[2]);
            if (adjacent == null || !adjacent.isOpaque()) {
                allSidesCovered = false;
                break;
            }
        }

        return allSidesCovered;
    }

    private boolean isOpaque() {
        return type != BlockType.AIR;
    }
}
