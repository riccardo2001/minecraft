package scene.entities;

import org.joml.Vector3f;

import world.World;
import world.blocks.Block;

public class Player {
    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 1.8f;

    private final Inventory inventory;
    private boolean isGrounded;
    private Vector3f spawnPosition; 

    public Player() {
        inventory = new Inventory();
        isGrounded = false;
        spawnPosition = new Vector3f(0, 70, 0); 
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void update(World world, Vector3f position, float deltaTime) {
        
        AABB playerBox = new AABB(position, WIDTH, HEIGHT);

        
        checkGroundBeneath(world, position);

        
        resolveCollisions(world, playerBox, position);

        
        preventFallingThroughBlocks(world, position);
    }

    private void resolveCollisions(World world, AABB playerBox, Vector3f position) {
        for (int x = (int) (playerBox.min.x - 1); x <= (int) (playerBox.max.x + 1); x++) {
            for (int y = (int) (playerBox.min.y - 1); y <= (int) (playerBox.max.y + 1); y++) {
                for (int z = (int) (playerBox.min.z - 1); z <= (int) (playerBox.max.z + 1); z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null && block.isSolid()) {
                        AABB blockBox = new AABB(
                                new Vector3f(x + 0.5f, y, z + 0.5f),
                                1.0f, 1.0f);

                        if (playerBox.intersects(blockBox)) {
                            Vector3f overlap = new Vector3f(
                                    Math.min(playerBox.max.x - blockBox.min.x, blockBox.max.x - playerBox.min.x),
                                    Math.min(playerBox.max.y - blockBox.min.y, blockBox.max.y - playerBox.min.y),
                                    Math.min(playerBox.max.z - blockBox.min.z, blockBox.max.z - playerBox.min.z));

                            
                            
                            
                            if ((overlap.y < 0.3f && playerBox.min.y < blockBox.max.y &&
                                    playerBox.min.y > blockBox.min.y) ||
                                    (overlap.y < overlap.x && overlap.y < overlap.z)) {

                                
                                if (playerBox.min.y < blockBox.max.y && playerBox.min.y > blockBox.min.y) {
                                    
                                    isGrounded = true;
                                    position.y = blockBox.max.y; 
                                } else {
                                    
                                    position.y = blockBox.min.y - HEIGHT;
                                }
                            } else if (overlap.x < overlap.z) {
                                
                                position.x += overlap.x * (playerBox.min.x < blockBox.min.x ? -1 : 1);
                            } else {
                                
                                position.z += overlap.z * (playerBox.min.z < blockBox.min.z ? -1 : 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkGroundBeneath(World world, Vector3f position) {
        
        isGrounded = false;

        
        float feetY = position.y - 1.05f; 
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(feetY);
        int blockZ = (int) Math.floor(position.z);

        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (checkBlockUnderFeet(world, blockX + dx, blockY, blockZ + dz, position)) {
                    isGrounded = true;
                    return;
                }
            }
        }

        if (!isGrounded) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (checkBlockUnderFeet(world, blockX + dx, blockY - 1, blockZ + dz, position)) {
                        isGrounded = true;
                        return;
                    }
                }
            }
        }
    }

    private boolean checkBlockUnderFeet(World world, int x, int y, int z, Vector3f position) {
        Block block = world.getBlock(x, y, z);
        if (block != null && block.isSolid()) {
            
            float playerMinX = position.x - WIDTH / 2 + 0.05f; 
            float playerMaxX = position.x + WIDTH / 2 - 0.05f;
            float playerMinZ = position.z - WIDTH / 2 + 0.05f;
            float playerMaxZ = position.z + WIDTH / 2 - 0.05f;

            float blockMinX = x;
            float blockMaxX = x + 1.0f;
            float blockMinZ = z;
            float blockMaxZ = z + 1.0f;

            
            if (playerMaxX > blockMinX && playerMinX < blockMaxX &&
                    playerMaxZ > blockMinZ && playerMinZ < blockMaxZ) {

                float topOfBlock = y + 1.0f;
                float distance = Math.abs(position.y - topOfBlock);

                
                if (distance < 0.3f) {
                    position.y = topOfBlock; 
                    return true;
                }
            }
        }
        return false;
    }

    private void preventFallingThroughBlocks(World world, Vector3f position) {
        if (isGrounded)
            return; 

        
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(position.y - 0.1f); 
        int blockZ = (int) Math.floor(position.z);

        Block blockBelow = world.getBlock(blockX, blockY, blockZ);
        if (blockBelow != null && blockBelow.isSolid()) {
            float topOfBlock = blockY + 1.0f;
            float feetY = position.y;

            
            
            if (feetY <= topOfBlock + 0.1f) {
                position.y = topOfBlock;
                isGrounded = true;
            }
        }
    }

    
    public boolean getIsGrounded() {
        return this.isGrounded;
    }

    public void setIsGrounded(boolean isGrounded) {
        this.isGrounded = isGrounded;
    }

    public void resetToSafePosition(World world, Vector3f position) {
        position.set(spawnPosition);
    }

    public void setSpawnPosition(Vector3f spawnPos) {
        this.spawnPosition = new Vector3f(spawnPos);
    }

    public Vector3f getSpawnPosition() {
        return new Vector3f(spawnPosition);
    }

    public String getDebugString(Vector3f position) {
        return String.format(
                "Pos: (%.2f, %.2f, %.2f) | Grounded: %b | Collisioni attive",
                position.x, position.y, position.z,
                isGrounded);
    }

    public void forceGrounded(World world, Vector3f position) {
        isGrounded = true;

        
        int x = (int) position.x;
        int z = (int) position.z;
        for (int y = (int) position.y; y >= 0; y--) {
            Block block = world.getBlock(x, y, z);
            if (block != null && block.isSolid()) {
                position.y = y + 1.0f; 
                break;
            }
        }
    }

    public boolean isBlockDirectlyBelow(World world, Vector3f position) {
        
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(position.y - 0.05f); 
        int blockZ = (int) Math.floor(position.z);

        Block blockBelow = world.getBlock(blockX, blockY, blockZ);

        if (blockBelow != null && blockBelow.isSolid()) {
            float topOfBlock = blockY + 1.0f;
            float feetY = position.y;

            
            return Math.abs(feetY - topOfBlock) < 0.2f;
        }

        
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0)
                    continue; 

                Block adjacentBlock = world.getBlock(blockX + dx, blockY, blockZ + dz);
                if (adjacentBlock != null && adjacentBlock.isSolid()) {
                    float playerMinX = position.x - WIDTH / 2 + 0.05f;
                    float playerMaxX = position.x + WIDTH / 2 - 0.05f;
                    float playerMinZ = position.z - WIDTH / 2 + 0.05f;
                    float playerMaxZ = position.z + WIDTH / 2 - 0.05f;

                    float blockMinX = blockX + dx;
                    float blockMaxX = blockX + dx + 1.0f;
                    float blockMinZ = blockZ + dz;
                    float blockMaxZ = blockZ + dz + 1.0f;

                    
                    if (playerMaxX > blockMinX && playerMinX < blockMaxX &&
                            playerMaxZ > blockMinZ && playerMinZ < blockMaxZ) {

                        float topOfBlock = blockY + 1.0f;
                        float feetY = position.y;

                        
                        if (Math.abs(feetY - topOfBlock) < 0.2f) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}