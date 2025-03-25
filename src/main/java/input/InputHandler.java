package input;

import scene.Camera;
import scene.RayCast;
import scene.Scene;
import world.blocks.Block;
import world.chunks.Chunk;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import core.Window;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;

    public void handleInput(Window window, Scene scene, float diffTimeMillis) {
        boolean isPaused = window.isCursorVisible();

        if (!isPaused) {
            window.getMouseInput().input(window.getWindowHandle(), false);
        }

        handleKeyboardInput(window, scene, diffTimeMillis, isPaused);
        handleMouseInput(window, scene, isPaused);
        
        handleHotbarSelection(window, scene);
    }
    
    private void handleKeyboardInput(Window window, Scene scene, float diffTimeMillis, boolean isPaused) {
        if (isPaused) return;
        
        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        Vector3f moveDir = new Vector3f();

        if (window.isKeyPressed(GLFW_KEY_W))
            moveDir.add(camera.getForward().mul(1));
        if (window.isKeyPressed(GLFW_KEY_S))
            moveDir.add(camera.getForward().mul(-1));
        if (window.isKeyPressed(GLFW_KEY_A))
            moveDir.add(camera.getLeft().mul(1));
        if (window.isKeyPressed(GLFW_KEY_D))
            moveDir.add(camera.getRight().mul(1));

        if (moveDir.lengthSquared() > 0) {
            moveDir.normalize();
        }

        camera.move(moveDir, move);

        if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && moveDir.lengthSquared() > 0) {
            camera.dash(moveDir, 0.04f);
        }

        if (window.isKeyPressed(GLFW_KEY_SPACE))
            camera.moveUp(move);
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT))
            camera.moveDown(move);
    }
    
    private void handleMouseInput(Window window, Scene scene, boolean isPaused) {
        if (isPaused) return;
        
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        Camera camera = scene.getCamera();
        
        camera.addRotation(
            (float) Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
            (float) Math.toRadians(displVec.y * MOUSE_SENSITIVITY));
            
        if (mouseInput.isLeftButtonJustPressed()) {
            handleBlockDestruction(scene);
        }
        
        if (mouseInput.isRightButtonJustPressed()) {
            handleBlockPlacement(scene);
        }
    }
    
    private void handleHotbarSelection(Window window, Scene scene) {
        MouseInput mouseInput = window.getMouseInput();
        double scrollY = mouseInput.getScrollOffsetY();
        if (scrollY != 0) {
            int currentSlot = scene.getPlayer().getInventory().getSelectedSlot();
            int slotChange = (int) Math.signum(scrollY);
            int newSlot = currentSlot - slotChange;

            newSlot = (newSlot + 9) % 9;

            scene.getPlayer().getInventory().selectSlot(newSlot);
            mouseInput.resetScroll();
        }

        // Gestione selezione slot tramite tasti numerici
        for (int i = 0; i < 9; i++) {
            if (window.isKeyPressed(GLFW_KEY_1 + i)) {
                scene.getPlayer().getInventory().selectSlot(i);
            }
        }
    }
    
    private void handleBlockDestruction(Scene scene) {
        RayCast ray = scene.getRayCast();
        if (ray.hasHit()) {
            Vector3i blockPos = ray.getBlockPosition();
            Block targetBlock = scene.getWorld().getBlock(blockPos.x, blockPos.y, blockPos.z);

            if (targetBlock != null && targetBlock.getType() != Block.BlockType.AIR) {
                scene.getWorld().setBlock(blockPos.x, blockPos.y, blockPos.z, null);
                scene.getPlayer().getInventory().addBlock(targetBlock.getType());

                Set<Chunk> affectedChunks = new HashSet<>();

                int chunkX = Math.floorDiv(blockPos.x, Chunk.WIDTH);
                int chunkZ = Math.floorDiv(blockPos.z, Chunk.DEPTH);

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        Chunk chunk = scene.getWorld().getChunk(chunkX + dx, chunkZ + dz);
                        if (chunk != null) {
                            affectedChunks.add(chunk);
                        }
                    }
                }

                Vector3i[] directions = {
                        new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
                        new Vector3i(0, 0, 1), new Vector3i(0, 0, -1),
                        new Vector3i(0, 1, 0), new Vector3i(0, -1, 0)
                };

                for (Vector3i dir : directions) {
                    Vector3i adjacentPos = new Vector3i(blockPos).add(dir);
                    Chunk adjacentChunk = scene.getWorld().getChunkContaining(
                            adjacentPos.x,
                            adjacentPos.z);
                    if (adjacentChunk != null) {
                        affectedChunks.add(adjacentChunk);
                    }
                }

                for (Chunk chunk : affectedChunks) {
                    chunk.setDirty(true);
                }

                scene.updateChunks();
            }
        }
    }
    
    private void handleBlockPlacement(Scene scene) {
        RayCast ray = scene.getRayCast();
        if (ray.hasHit() && ray.getHitDistance() <= 10.0f) {
            Vector3i adjacentPos = Block.calculateAdjacentPosition(ray.getBlockPosition(), ray.getHitFace());
            Block.BlockType selectedType = scene.getPlayer().getInventory().getSelectedBlock();

            if (scene.getWorld().getBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z) == null &&
                    scene.getPlayer().getInventory().useSelectedBlock()) {

                scene.getWorld().setBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z, new Block(selectedType));

                Chunk chunk = scene.getWorld().getChunkContaining(adjacentPos.x, adjacentPos.z);
                if (chunk != null) {
                    chunk.setDirty(true);
                }
                scene.updateChunks();
            }
        }
    }
}
