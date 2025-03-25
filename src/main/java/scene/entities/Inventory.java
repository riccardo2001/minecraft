package scene.entities;

import java.util.HashMap;

import world.blocks.Block.BlockType;

public class Inventory {
    private HashMap<BlockType, Integer> items;
    private BlockType[] hotbar;
    private int selectedSlot;

    public Inventory() {
        items = new HashMap<>();
        hotbar = new BlockType[9];
        selectedSlot = 0;
    }

    public void addBlock(BlockType type) {
        int count = items.getOrDefault(type, 0);
        items.put(type, count + 1);
        
        if(count == 0) {
            addToFirstEmptySlot(type);
        }
    }

    private void addToFirstEmptySlot(BlockType type) {
        for(int i = 0; i < hotbar.length; i++) {
            if(hotbar[i] == null) {
                hotbar[i] = type;
                break;
            }
        }
    }

    public boolean useSelectedBlock() {
        BlockType type = hotbar[selectedSlot];
        if(type == null) return false;
        
        int count = items.getOrDefault(type, 0);
        if(count > 0) {
            items.put(type, count - 1);
            if(count - 1 == 0) {
                hotbar[selectedSlot] = null;
            }
            return true;
        }
        return false;
    }

    public void selectSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            selectedSlot = slot;
        }
    }

    public BlockType getSelectedBlock() {
        return hotbar[selectedSlot];
    }

    public HashMap<BlockType, Integer> getItems() {
        return items;
    }

    public BlockType[] getHotbar() {
        return hotbar;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public int getItemCount(BlockType type) {
        return items.getOrDefault(type, 0);
    }
}
