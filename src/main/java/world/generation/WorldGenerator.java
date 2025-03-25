package world.generation;

import java.util.Random;

import world.World;
import world.blocks.Block;
import world.blocks.BlockFactory;
import world.chunks.Chunk;


public class WorldGenerator {
    private final World world;
    private final BlockFactory blockFactory;
    
    public WorldGenerator(World world) {
        this.world = world;
        this.blockFactory = BlockFactory.getInstance();
    }
    
    public void generateBaseTerrain(Chunk chunk) {
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.DEPTH; z++) {
                int globalX = chunk.getChunkX() * Chunk.WIDTH + x;
                int globalZ = chunk.getChunkZ() * Chunk.DEPTH + z;
                int terrainHeight = world.getTerrainHeight(globalX, globalZ);

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    Block.BlockType blockType = determineBlockType(y, terrainHeight);
                    
                    if (blockType != Block.BlockType.AIR) {
                        Block block = blockFactory.createBlock(blockType);
                        chunk.setBlock(x, y, z, block);
                    }
                }
            }
        }
    }
    
    private Block.BlockType determineBlockType(int y, int terrainHeight) {
        if (y == 0) 
            return Block.BlockType.STONE;
        if (y < terrainHeight - 1)
            return Block.BlockType.DIRT;
        if (y < terrainHeight)
            return Block.BlockType.GRASS;
        return Block.BlockType.AIR;
    }
    
    public void generateTrees(Chunk chunk) {
        Random random = new Random(generateChunkSeed(chunk));
        
        int maxTrees = 5 + random.nextInt(4);
        int numTrees = random.nextInt(maxTrees + 1);

        numTrees += (int) (Math.abs(Math.sin(chunk.getChunkX() * chunk.getChunkZ() * 0.1)) * 2);
        numTrees = Math.min(numTrees, 8);

        int treesGenerated = 0;
        int attempts = 0;
        int maxAttempts = 10;

        boolean[][] positionTried = new boolean[Chunk.WIDTH][Chunk.DEPTH];

        while (treesGenerated < numTrees && attempts < maxAttempts) {
            attempts++;

            int treeX, treeZ;
            do {
                treeX = 3 + random.nextInt(Chunk.WIDTH - 6) + random.nextInt(2);
                treeZ = 3 + random.nextInt(Chunk.DEPTH - 6) + random.nextInt(2);
            } while (positionTried[treeX][treeZ]);

            positionTried[treeX][treeZ] = true;

            int globalX = chunk.getChunkX() * Chunk.WIDTH + treeX;
            int globalZ = chunk.getChunkZ() * Chunk.DEPTH + treeZ;
            int terrainHeight = world.getTerrainHeight(globalX, globalZ);

            Block block = chunk.getBlock(treeX, terrainHeight - 1, treeZ);
            if (block != null && block.getType() == Block.BlockType.GRASS) {
                if (terrainHeight + 11 < Chunk.HEIGHT && hasTreeSpace(chunk, treeX, terrainHeight, treeZ)) {
                    generateTree(chunk, treeX, terrainHeight, treeZ, random.nextInt(3));
                    treesGenerated++;
                }
            }
        }
    }

    private long generateChunkSeed(Chunk chunk) {
        return (chunk.getChunkX() * 0x5F24F) ^ (chunk.getChunkZ() * 0x9E3779B9L) ^ 0xDEADBEEFL;
    }
    
    private boolean hasTreeSpace(Chunk chunk, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;
                if (isValidPosition(checkX, y, checkZ)) {
                    Block block = chunk.getBlock(checkX, y, checkZ);
                    if (block != null && block.getType() == Block.BlockType.WOOD) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean isValidPosition(int x, int y, int z) {
        return x >= 0 && x < Chunk.WIDTH &&
                y >= 0 && y < Chunk.HEIGHT &&
                z >= 0 && z < Chunk.DEPTH;
    }
    
    private boolean isTreeAreaValid(int x, int y, int z) {
        int leafSize = 3;
        if (x - leafSize < 0 || x + leafSize >= Chunk.WIDTH ||
                z - leafSize < 0 || z + leafSize >= Chunk.DEPTH ||
                y + 10 >= Chunk.HEIGHT) {
            return false;
        }
        return true;
    }
    
    private void generateTree(Chunk chunk, int x, int y, int z, int treeType) {
        if (!isValidPosition(x, y, z) || !isTreeAreaValid(x, y, z)) {
            return;
        }

        int trunkHeight;
        int leafSize;

        switch (treeType) {
            case 0:
                trunkHeight = 4 + new Random().nextInt(2);
                leafSize = 1;
                break;
            case 1:
                trunkHeight = 5 + new Random().nextInt(2);
                leafSize = 2;
                break;
            case 2:
            default:
                trunkHeight = 6 + new Random().nextInt(2);
                leafSize = 2;
                break;
        }

        for (int i = 0; i < trunkHeight; i++) {
            chunk.setBlock(x, y + i, z, blockFactory.createWoodBlock());
        }

        generateLeaves(chunk, x, y + trunkHeight - 2, z, leafSize, treeType);

        chunk.setBlock(x, y + trunkHeight, z, blockFactory.createLeavesBlock());
        if (treeType == 2 && y + trunkHeight + 1 < Chunk.HEIGHT) {
            chunk.setBlock(x, y + trunkHeight + 1, z, blockFactory.createLeavesBlock());
        }
    }
    
    private void generateLeaves(Chunk chunk, int x, int y, int z, int leafSize, int treeType) {
        Random random = new Random();
        
        for (int dx = -leafSize - 1; dx <= leafSize + 1; dx++) {
            for (int dz = -leafSize - 1; dz <= leafSize + 1; dz++) {
                int maxDistance = Math.abs(dx) + Math.abs(dz);
                int leafHeight = treeType == 2 ? 4 : 3;

                for (int dy = 0; dy < leafHeight; dy++) {
                    boolean shouldPlace = false;

                    if (treeType == 0) {
                        shouldPlace = maxDistance <= leafSize + 1;
                    } else if (treeType == 1) {
                        shouldPlace = (maxDistance <= leafSize + 1) &&
                                !(Math.abs(dx) == leafSize + 1 && Math.abs(dz) == leafSize + 1);
                    } else {
                        shouldPlace = (maxDistance <= leafSize + 1) &&
                                !((Math.abs(dx) == leafSize + 1 && Math.abs(dz) == leafSize + 1) && dy != 1);

                        if (!shouldPlace && random.nextDouble() < 0.2 && maxDistance <= leafSize + 2) {
                            shouldPlace = true;
                        }
                    }

                    if (shouldPlace) {
                        int leafX = x + dx;
                        int leafY = y + dy;
                        int leafZ = z + dz;

                        if (isValidPosition(leafX, leafY, leafZ)) {
                            if (!(dx == 0 && dz == 0 && dy == 0)) {
                                Block existingBlock = chunk.getBlock(leafX, leafY, leafZ);
                                if (existingBlock == null || existingBlock.getType() == Block.BlockType.AIR) {
                                    if (random.nextDouble() > 0.1 || maxDistance <= leafSize) {
                                        chunk.setBlock(leafX, leafY, leafZ, blockFactory.createLeavesBlock());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
