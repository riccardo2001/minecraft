package world.physics;

import org.joml.Vector3f;
import org.joml.Vector3i;
import world.World;
import world.blocks.Block;
import world.blocks.Block.BlockType;
import world.chunks.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Gravity {
    
    private static final float WATER_GRAVITY_STRENGTH = 0.03f;
    private static final float WATER_RESISTANCE = 0.6f;

    
    private static final int MAX_WATER_LEVEL = 8;

    
    private static final Map<String, Integer> waterLevels = new HashMap<>();

    
    private static final int[][] FLOW_DIRECTIONS = {
            { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }
    };

    
    private static final int MAX_WATER_BLOCKS = 500; 
    private static int waterBlockCount = 0; 

    
    public static void resetWaterCount() {
        waterBlockCount = 0;
    }

    
    private static String getWaterLevelKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    
    private static int getWaterLevel(int x, int y, int z) {
        return waterLevels.getOrDefault(getWaterLevelKey(x, y, z), MAX_WATER_LEVEL);
    }

    
    private static void setWaterLevel(int x, int y, int z, int level) {
        waterLevels.put(getWaterLevelKey(x, y, z), level);
    }

    
    
    public static void processFluidBlock(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null || !isFluid(block.getType())) {
            return;
        }

        BlockType fluidType = block.getType();
        int currentLevel = getWaterLevel(x, y, z);
        
        
        if (waterBlockCount > MAX_WATER_BLOCKS) {
            if (currentLevel < MAX_WATER_LEVEL) {
                return;
            }
        }
        
        
        Block blockBelow = world.getBlock(x, y - 1, z);
        if (blockBelow == null) {
            
            world.setBlock(x, y - 1, z, new Block(fluidType));
            setWaterLevel(x, y - 1, z, MAX_WATER_LEVEL); 
            world.addToFluidQueue(x, y - 1, z);
            markChunkDirty(world, x, y - 1, z);
            waterBlockCount++;
            
            
            
            return;
        }
        
        
        if (currentLevel <= 1) {
            return;
        }
        
        
        boolean foundHole = false;
        int holeDirection = -1;
        
        
        for (int i = 0; i < FLOW_DIRECTIONS.length; i++) {
            int[] dir = FLOW_DIRECTIONS[i];
            int adjX = x + dir[0];
            int adjZ = z + dir[1];
            
            Block adjBlock = world.getBlock(adjX, y, adjZ);
            if (adjBlock == null) {
                Block belowAdjBlock = world.getBlock(adjX, y - 1, adjZ);
                if (belowAdjBlock == null) {
                    
                    foundHole = true;
                    holeDirection = i;
                    break; 
                }
            }
        }
        
        
        if (foundHole && holeDirection >= 0) {
            int[] dir = FLOW_DIRECTIONS[holeDirection];
            int adjX = x + dir[0];
            int adjZ = z + dir[1];
            
            world.setBlock(adjX, y, adjZ, new Block(fluidType));
            setWaterLevel(adjX, y, adjZ, Math.max(1, currentLevel - 1));
            world.addToFluidQueue(adjX, y, adjZ);
            markChunkDirty(world, adjX, y, adjZ);
            waterBlockCount++;
            return; 
        }
        
        
        int maxHorizontalSpread = 2; 
        int propagatedBlocks = 0;
        
        for (int[] dir : FLOW_DIRECTIONS) {
            
            if (propagatedBlocks >= maxHorizontalSpread) {
                break;
            }
            
            int adjX = x + dir[0];
            int adjZ = z + dir[1];
            
            Block adjBlock = world.getBlock(adjX, y, adjZ);
            if (adjBlock == null) {
                
                world.setBlock(adjX, y, adjZ, new Block(fluidType));
                int newLevel = Math.max(1, currentLevel - 3); 
                setWaterLevel(adjX, y, adjZ, newLevel);
                world.addToFluidQueue(adjX, y, adjZ);
                markChunkDirty(world, adjX, y, adjZ);
                waterBlockCount++;
                propagatedBlocks++;
            }
        }
    }

    
    public static void applyWaterPhysicsToPlayer(Vector3f position, Vector3f velocity, World world, float deltaTime) {
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);

        Block block = world.getBlock(x, y, z);
        boolean inWater = block != null && block.getType() == BlockType.WATER;

        if (inWater) {
            
            float intensityFactor = 1.0f;
            int waterLevel = getWaterLevel(x, y, z);
            intensityFactor = waterLevel / (float) MAX_WATER_LEVEL;

            
            velocity.mul(WATER_RESISTANCE * intensityFactor + (1 - intensityFactor));

            
            velocity.y -= WATER_GRAVITY_STRENGTH * deltaTime * intensityFactor;

            
            if (velocity.y < -0.1f * intensityFactor) {
                velocity.y = -0.1f * intensityFactor;
            }

            
            addWaterCurrentEffect(position, velocity, world, intensityFactor);
        }
    }

    private static void addWaterCurrentEffect(Vector3f position, Vector3f velocity, World world,
            float intensityFactor) {
        
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);

        float currentX = 0;
        float currentZ = 0;

        for (int[] dir : FLOW_DIRECTIONS) {
            int nx = x + dir[0];
            int nz = z + dir[1];

            Block neighborBlock = world.getBlock(nx, y, nz);
            if (neighborBlock == null) {
                
                currentX += dir[0] * 0.01f;
                currentZ += dir[1] * 0.01f;
            } else if (neighborBlock.getType() == BlockType.WATER) {
                
                int neighborLevel = getWaterLevel(nx, y, nz);
                int currentLevel = getWaterLevel(x, y, z);

                if (currentLevel > neighborLevel) {
                    
                    currentX += dir[0] * 0.02f * (currentLevel - neighborLevel) / MAX_WATER_LEVEL;
                    currentZ += dir[1] * 0.02f * (currentLevel - neighborLevel) / MAX_WATER_LEVEL;
                }
            }
        }

        
        velocity.x += currentX * intensityFactor;
        velocity.z += currentZ * intensityFactor;
    }

    
    public static boolean isPlayerInWater(World world, Vector3f position) {
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);

        Block block = world.getBlock(x, y, z);
        return block != null && block.getType() == BlockType.WATER;
    }

    private static boolean isFluid(BlockType type) {
        return type == BlockType.WATER;
    }

    private static void markChunkDirty(World world, int x, int y, int z) {
        int chunkX = Math.floorDiv(x, Chunk.WIDTH);
        int chunkZ = Math.floorDiv(z, Chunk.DEPTH);
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setDirty(true);
        }
    }
}
