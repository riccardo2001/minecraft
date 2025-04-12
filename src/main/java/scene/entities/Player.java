package scene.entities;

import org.joml.Vector3f;

import world.World;
import world.blocks.Block;
import world.physics.Gravity;

public class Player {
    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 1.8f;

    private final Inventory inventory;
    private boolean isGrounded;
    private Vector3f spawnPosition; 
    private boolean isInWater;
    private Vector3f waterVelocity;

    public Player() {
        inventory = new Inventory();
        isGrounded = false;
        spawnPosition = new Vector3f(0, 70, 0); 
        isInWater = false;
        waterVelocity = new Vector3f(0, 0, 0);
        
        
        inventory.addBlock(Block.BlockType.WATER);
        inventory.addBlock(Block.BlockType.WATER);

        inventory.addBlock(Block.BlockType.WATER);

        inventory.addBlock(Block.BlockType.WATER);

    }

    public Inventory getInventory() {
        return inventory;
    }

    public void update(World world, Vector3f position, float deltaTime) {
        
        Delimiter playerBox = new Delimiter(position, WIDTH, HEIGHT);

        
        isInWater = Gravity.isPlayerInWater(world, position);
        
        if (isInWater) {
            
            Gravity.applyWaterPhysicsToPlayer(position, waterVelocity, world, deltaTime);
            position.add(new Vector3f(waterVelocity).mul(deltaTime));
        }

        checkGroundBeneath(world, position);

        resolveCollisions(world, playerBox, position);

        preventFallingThroughBlocks(world, position);
    }

    private void resolveCollisions(World world, Delimiter playerBox, Vector3f position) {
        for (int x = (int) (playerBox.min.x - 1); x <= (int) (playerBox.max.x + 1); x++) {
            for (int y = (int) (playerBox.min.y - 1); y <= (int) (playerBox.max.y + 1); y++) {
                for (int z = (int) (playerBox.min.z - 1); z <= (int) (playerBox.max.z + 1); z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null && block.isSolid()) {
                        Delimiter blockBox = new Delimiter(
                                new Vector3f(x + 0.5f, y + 1.0f, z + 0.5f), 
                                1.0f, 1.0f);

                        if (playerBox.intersects(blockBox)) {
                            Vector3f overlap = playerBox.getPenetrationVector(blockBox);
                            
                            if (overlap != null) {
                                
                                if (overlap.y < 0) {
                                    
                                    overlap.y -= 0.2f;
                                }
                                
                                
                                position.add(overlap);
                                
                                
                                if (overlap.y > 0) {
                                    isGrounded = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkGroundBeneath(World world, Vector3f position) {
        isGrounded = false;

        
        float feetY = position.y - HEIGHT;
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(feetY);
        int blockZ = (int) Math.floor(position.z);

        
        Block blockBelow = world.getBlock(blockX, blockY, blockZ);
        if (blockBelow != null && blockBelow.isSolid()) {
            float distanceToTop = Math.abs(feetY - (blockY + 1.0f));
            if (distanceToTop < 0.3f) {
                position.y = (blockY + 1.0f) + HEIGHT; 
                isGrounded = true;
                return;
            }
        }

        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                if (checkBlockUnderFeet(world, blockX + dx, blockY, blockZ + dz, position)) {
                    isGrounded = true;
                    return;
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
                float feetY = position.y - HEIGHT;
                float distance = Math.abs(feetY - topOfBlock);

                if (distance < 0.3f) {
                    position.y = topOfBlock + HEIGHT;
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
        int blockY = (int) Math.floor(position.y - HEIGHT); 
        int blockZ = (int) Math.floor(position.z);

        Block blockBelow = world.getBlock(blockX, blockY, blockZ);
        if (blockBelow != null && blockBelow.isSolid()) {
            float topOfBlock = blockY + 1.0f;
            float feetY = position.y - HEIGHT;

            if (feetY <= topOfBlock + 0.1f) {
                position.y = topOfBlock + HEIGHT;
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
        for (int y = (int) (position.y - HEIGHT); y >= 0; y--) { 
            Block block = world.getBlock(x, y, z);
            if (block != null && block.isSolid()) {
                position.y = (y + 1.0f) + HEIGHT; 
                break;
            }
        }
    }

    public boolean isBlockDirectlyBelow(World world, Vector3f position) {
        
        float feetY = position.y - HEIGHT;
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(feetY - 0.05f);
        int blockZ = (int) Math.floor(position.z);

        Block blockBelow = world.getBlock(blockX, blockY, blockZ);

        if (blockBelow != null && blockBelow.isSolid()) {
            float topOfBlock = blockY + 1.0f;
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
                        feetY = position.y;

                        if (Math.abs(feetY - topOfBlock) < 0.2f) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean canMoveUp(World world, Vector3f position, float amount) {
        
        int blockX = (int) Math.floor(position.x);
        int blockY = (int) Math.floor(position.y + 0.2f); 
        int blockZ = (int) Math.floor(position.z);
        
        
        Block blockAbove = world.getBlock(blockX, blockY, blockZ);
        if (blockAbove != null && blockAbove.isSolid()) {
            float bottomOfBlock = blockY;
            if (position.y + amount > bottomOfBlock - 0.2f) {
                return false;
            }
        }
        
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                float playerMinX = position.x - WIDTH / 2 + 0.1f;
                float playerMaxX = position.x + WIDTH / 2 - 0.1f;
                float playerMinZ = position.z - WIDTH / 2 + 0.1f;
                float playerMaxZ = position.z + WIDTH / 2 - 0.1f;
                
                float blockMinX = blockX + dx;
                float blockMaxX = blockX + dx + 1.0f;
                float blockMinZ = blockZ + dz;
                float blockMaxZ = blockZ + dz + 1.0f;
                
                
                if (playerMaxX > blockMinX && playerMinX < blockMaxX &&
                        playerMaxZ > blockMinZ && playerMinZ < blockMaxZ) {
                    
                    Block adjacentBlockAbove = world.getBlock(blockX + dx, blockY, blockZ + dz);
                    if (adjacentBlockAbove != null && adjacentBlockAbove.isSolid()) {
                        float bottomOfBlock = blockY;
                        if (position.y + amount > bottomOfBlock - 0.2f) {
                            return false;
                        }
                    }
                }
            }
        }
        
        return true;
    }

    public boolean canMove(World world, Vector3f position, Vector3f direction, float amount) {
        
        if (direction.lengthSquared() < 0.0001f) {
            return true;
        }
        
        
        Vector3f newPosition = new Vector3f(position).add(new Vector3f(direction).mul(amount));
        
        
        if (isPositionValid(world, newPosition)) {
            return true;
        }
        
        
        
        if (Math.abs(direction.x) > 0.01f) {
            Vector3f xOnlyMove = new Vector3f(direction.x, 0, 0).normalize().mul(amount);
            Vector3f xPosition = new Vector3f(position).add(xOnlyMove);
            if (isPositionValid(world, xPosition)) {
                return true;
            }
        }
        
        
        if (Math.abs(direction.z) > 0.01f) {
            Vector3f zOnlyMove = new Vector3f(0, 0, direction.z).normalize().mul(amount);
            Vector3f zPosition = new Vector3f(position).add(zOnlyMove);
            if (isPositionValid(world, zPosition)) {
                return true;
            }
        }
        
        
        return false;
    }

    private boolean isPositionValid(World world, Vector3f position) {
        
        Delimiter playerBox = new Delimiter(position, WIDTH, HEIGHT);
        
        
        int minX = (int) Math.floor(playerBox.min.x);
        int maxX = (int) Math.ceil(playerBox.max.x);
        int minY = (int) Math.floor(playerBox.min.y);
        int maxY = (int) Math.ceil(playerBox.max.y);
        int minZ = (int) Math.floor(playerBox.min.z);
        int maxZ = (int) Math.ceil(playerBox.max.z);
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null && block.isSolid()) {
                        Delimiter blockBox = new Delimiter(
                                new Vector3f(x + 0.5f, y + 1.0f, z + 0.5f),
                                1.0f, 1.0f);
                        
                        if (playerBox.intersects(blockBox)) {
                            
                            float overlapX = Math.min(playerBox.max.x - blockBox.min.x, blockBox.max.x - playerBox.min.x);
                            float overlapZ = Math.min(playerBox.max.z - blockBox.min.z, blockBox.max.z - playerBox.min.z);
                            
                            
                            if (overlapX > 0.1f && overlapZ > 0.1f) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }

    public boolean isInWater() {
        return isInWater;
    }
    
    public void setWaterVelocity(Vector3f velocity) {
        this.waterVelocity.set(velocity);
    }
    
    public Vector3f getWaterVelocity() {
        return waterVelocity;
    }
}