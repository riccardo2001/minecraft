package scene;

import org.joml.Vector3f;
import org.joml.Vector3i;
import world.World;
import world.Block;

public class RayCast {

    private boolean hasHit;
    private Vector3i blockPos;
    private Block.Face hitFace;
    private float hitDistance;

    public RayCast() {
        this.hasHit = false;
        this.blockPos = new Vector3i();
        this.hitFace = null;
        this.hitDistance = 0.0f;
    }

    public void performRayCast(Camera camera, World world) {
        hasHit = false;
        
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f direction = new Vector3f(camera.getFrontVector()).normalize();
        
        float maxDistance = 10.0f;
        
        int originX = (int) Math.floor(origin.x);
        int originY = (int) Math.floor(origin.y);
        int originZ = (int) Math.floor(origin.z);
        
        Block currentBlock = world.getBlock(originX, originY, originZ);
        if (currentBlock != null && currentBlock.isSolid()) {
            blockPos.set(originX, originY, originZ);
            hasHit = true;
            hitDistance = 0;
            hitFace = null;
            return;
        }
        
        Vector3i voxelPos = new Vector3i(originX, originY, originZ);
        
        Vector3f deltaDist = new Vector3f(
            Math.abs(direction.x) < 1e-6f ? Float.POSITIVE_INFINITY : Math.abs(1.0f / direction.x),
            Math.abs(direction.y) < 1e-6f ? Float.POSITIVE_INFINITY : Math.abs(1.0f / direction.y),
            Math.abs(direction.z) < 1e-6f ? Float.POSITIVE_INFINITY : Math.abs(1.0f / direction.z)
        );
        
        Vector3i step = new Vector3i(
            direction.x >= 0 ? 1 : -1,
            direction.y >= 0 ? 1 : -1,
            direction.z >= 0 ? 1 : -1
        );
        
        Vector3f sideDistX, sideDistY, sideDistZ;
        
        if (direction.x >= 0) {
            sideDistX = new Vector3f((originX + 1 - origin.x) * deltaDist.x, 0, 0);
        } else {
            sideDistX = new Vector3f((origin.x - originX) * deltaDist.x, 0, 0);
        }
        
        if (direction.y >= 0) {
            sideDistY = new Vector3f(0, (originY + 1 - origin.y) * deltaDist.y, 0);
        } else {
            sideDistY = new Vector3f(0, (origin.y - originY) * deltaDist.y, 0);
        }
        
        if (direction.z >= 0) {
            sideDistZ = new Vector3f(0, 0, (originZ + 1 - origin.z) * deltaDist.z);
        } else {
            sideDistZ = new Vector3f(0, 0, (origin.z - originZ) * deltaDist.z);
        }
        
        boolean hitX = false, hitY = false, hitZ = false;

        int iterations = 0;
        int maxIterations = 100;
        
        while (iterations < maxIterations) {
            iterations++;
            
            if (sideDistX.x < sideDistY.y && sideDistX.x < sideDistZ.z) {
                voxelPos.x += step.x;
                if (sideDistX.x > maxDistance) break;
                hitX = true;
                hitY = hitZ = false;
                sideDistX.x += deltaDist.x;
            } else if (sideDistY.y < sideDistZ.z) {
                voxelPos.y += step.y;
                if (sideDistY.y > maxDistance) break;
                hitY = true;
                hitX = hitZ = false;
                sideDistY.y += deltaDist.y;
            } else {
                voxelPos.z += step.z;
                if (sideDistZ.z > maxDistance) break;
                hitZ = true;
                hitX = hitY = false;
                sideDistZ.z += deltaDist.z;
            }
            
            Block block = world.getBlock(voxelPos.x, voxelPos.y, voxelPos.z);
            if (block != null && block.isSolid()) {                
                if (hitX) {
                    hitFace = (step.x > 0) ? Block.Face.LEFT : Block.Face.RIGHT;
                } else if (hitY) {
                    hitFace = (step.y > 0) ? Block.Face.BOTTOM : Block.Face.TOP;
                } else if (hitZ) {
                    hitFace = (step.z > 0) ? Block.Face.FRONT : Block.Face.BACK;
                }
                
                float distance;
                if (hitX) {
                    distance = sideDistX.x - deltaDist.x;
                } else if (hitY) {
                    distance = sideDistY.y - deltaDist.y;
                } else {
                    distance = sideDistZ.z - deltaDist.z;
                }
                
                hasHit = true;
                blockPos.set(voxelPos);
                hitDistance = distance;
                return;
            }            
        }
    }

    public boolean hasHit() {
        return hasHit;
    }

    public Vector3i getBlockPosition() {
        return blockPos;
    }

    public Block.Face getHitFace() {
        return hitFace;
    }

    public float getHitDistance() {
        return hitDistance;
    }
}