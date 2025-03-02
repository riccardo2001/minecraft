package world;

public class Chunk {
    private Block[][][] blocks;

    public Chunk(int size) {
        blocks = new Block[size][size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    blocks[x][y][z] = new Block(x, y, z);
                }
            }
        }
    }

    public void render() {
        for (Block[][] layer : blocks) {
            for (Block[] row : layer) {
                for (Block block : row) {
                    block.render();
                }
            }
        }
    }
}
