package world.blocks;

public class BlockFactory {
    private static BlockFactory instance;
    
    private BlockFactory() {}
    
    public static BlockFactory getInstance() {
        if (instance == null) {
            instance = new BlockFactory();
        }
        return instance;
    }
    
    public Block createBlock(Block.BlockType type) {
        return new Block(type);
    }

    public Block createAirBlock() {
        return createBlock(Block.BlockType.AIR);
    }
    
    public Block createGrassBlock() {
        return createBlock(Block.BlockType.GRASS);
    }

    public Block createDirtBlock() {
        return createBlock(Block.BlockType.DIRT);
    }
    
    public Block createStoneBlock() {
        return createBlock(Block.BlockType.STONE);
    }
    
    public Block createWoodBlock() {
        return createBlock(Block.BlockType.WOOD);
    }
    
    public Block createLeavesBlock() {
        return createBlock(Block.BlockType.LEAVES);
    }

    public Block createWaterBlock() {
        return createBlock(Block.BlockType.WATER);
    }
}
