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
        items.put(type, items.getOrDefault(type, 0) + 1);
    }

    public boolean useSelectedBlock() {
        BlockType type = hotbar[selectedSlot];
        if (items.getOrDefault(type, 0) > 0) {
            items.put(type, items.get(type) - 1);
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
