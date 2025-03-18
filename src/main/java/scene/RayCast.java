package scene;

import org.joml.Vector3f;
import org.joml.Vector3i;
import world.World;
import world.Block;

public class RayCast {
    private static final float MAX_DISTANCE = 5.0f;
    private static boolean DEBUG = true;
    
    private boolean hasHit;
    private Vector3i blockPos;
    private Block.Face hitFace;
    
    public RayCast() {
        this.hasHit = false;
        this.blockPos = new Vector3i();
        this.hitFace = null;
    }
    
    public void performRayCast(Camera camera, World world) {
        hasHit = false;
        
        // Posizione iniziale (punto di partenza del raggio)
        Vector3f position = new Vector3f(camera.getPosition());
        // Direzione del raggio normalizzata
        Vector3f direction = camera.getFrontVector();
        
        // Debug della posizione e direzione
        if (DEBUG) {
            System.out.println("------ RAYCAST DEBUG ------");
            System.out.println("Camera position: " + position);
            System.out.println("Camera direction: " + direction);
        }
        
        // Ottieni la posizione del blocco corrente
        int currentX = (int) Math.floor(position.x);
        int currentY = (int) Math.floor(position.y);
        int currentZ = (int) Math.floor(position.z);
        
        // Controlliamo prima se la camera è all'interno di un blocco solido
        Block currentBlock = world.getBlock(currentX, currentY, currentZ);
        if (currentBlock != null && currentBlock.isSolid()) {
            hasHit = true;
            blockPos.set(currentX, currentY, currentZ);
            if (DEBUG) {
                System.out.println("Camera all'interno del blocco: " + blockPos + ", tipo: " + currentBlock.getType());
            }
            return;
        }
        
        // Implementazione algoritmo DDA (Digital Differential Analyzer)
        
        // Lunghezza del raggio dalla posizione corrente a ogni bordo x, y, z
        double deltaDistX = Math.abs(1.0 / direction.x);
        double deltaDistY = Math.abs(1.0 / direction.y);
        double deltaDistZ = Math.abs(1.0 / direction.z);
        
        // Direzione del passo (positiva o negativa)
        int stepX = (direction.x > 0) ? 1 : -1;
        int stepY = (direction.y > 0) ? 1 : -1; 
        int stepZ = (direction.z > 0) ? 1 : -1;
        
        // Distanza dal punto di partenza al primo bordo x, y, z
        double sideDistX, sideDistY, sideDistZ;
        
        // Calcola distanza iniziale per i bordi
        if (direction.x > 0) {
            sideDistX = (currentX + 1 - position.x) * deltaDistX;
        } else {
            sideDistX = (position.x - currentX) * deltaDistX;
        }
        
        if (direction.y > 0) {
            sideDistY = (currentY + 1 - position.y) * deltaDistY;
        } else {
            sideDistY = (position.y - currentY) * deltaDistY;
        }
        
        if (direction.z > 0) {
            sideDistZ = (currentZ + 1 - position.z) * deltaDistZ;
        } else {
            sideDistZ = (position.z - currentZ) * deltaDistZ;
        }
        
        // Debug info
        if (DEBUG) {
            System.out.println("Starting block: (" + currentX + "," + currentY + "," + currentZ + ")");
            System.out.println("Delta dist: (" + deltaDistX + "," + deltaDistY + "," + deltaDistZ + ")");
            System.out.println("Side dist: (" + sideDistX + "," + sideDistY + "," + sideDistZ + ")");
        }
        
        // Variabile per memorizzare la faccia colpita
        int faceHit = -1; // 0=x, 1=y, 2=z
        
        // Esegui DDA
        double totalDist = 0;
        while (totalDist < MAX_DISTANCE) {
            // Trova il piano più vicino da attraversare
            if (sideDistX < sideDistY && sideDistX < sideDistZ) {
                // X plane is closest
                totalDist = sideDistX;
                currentX += stepX;
                sideDistX += deltaDistX;
                faceHit = 0;
            } else if (sideDistY < sideDistZ) {
                // Y plane is closest
                totalDist = sideDistY;
                currentY += stepY;
                sideDistY += deltaDistY;
                faceHit = 1;
            } else {
                // Z plane is closest
                totalDist = sideDistZ;
                currentZ += stepZ;
                sideDistZ += deltaDistZ;
                faceHit = 2;
            }
            
            // Check if we hit a solid block
            Block block = world.getBlock(currentX, currentY, currentZ);
            
            if (DEBUG) {
                System.out.println("Checking: (" + currentX + "," + currentY + "," + currentZ + 
                                  "), distance: " + totalDist + 
                                  ", block: " + (block != null ? block.getType() : "null"));
            }
            
            if (block != null && block.isSolid()) {
                hasHit = true;
                blockPos.set(currentX, currentY, currentZ);
                
                // Set hit face based on which plane we hit
                switch (faceHit) {
                    case 0: hitFace = (stepX > 0) ? Block.Face.LEFT : Block.Face.RIGHT; break;
                    case 1: hitFace = (stepY > 0) ? Block.Face.BOTTOM : Block.Face.TOP; break;
                    case 2: hitFace = (stepZ > 0) ? Block.Face.BACK : Block.Face.FRONT; break;
                }
                
                if (DEBUG) {
                    System.out.println("HIT! Block: " + block.getType() + 
                                      " at (" + currentX + "," + currentY + "," + currentZ + ")" +
                                      ", distance: " + totalDist + 
                                      ", face: " + hitFace);
                }
                break;
            }
        }
        
        if (DEBUG && !hasHit) {
            System.out.println("No block hit within " + MAX_DISTANCE + " blocks");
        }
    }
    
    public boolean hasHit() {
        return hasHit;
    }
    
    public Vector3i getBlockPosition() {
        return blockPos;
    }
    
    public Block.Face getHitFace() {
        return hitFace;
    }
}
