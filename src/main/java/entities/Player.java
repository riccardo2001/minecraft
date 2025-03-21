package entities;

public class Player {
    private Inventory inventory;
    
    public Player() {
        inventory = new Inventory();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}