package world;

import java.util.ArrayList;
import java.util.List;

public class World {
    private List<Block> blocks = new ArrayList<>();

    public World() {
        //blocks.add(new Block(0, 0, 0));
    }

    public void render() {
        for (Block block : blocks) {
            block.render();
        }
    }

    
}
