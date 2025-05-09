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

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.List;

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
        if (isPaused)
            return;

        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        Vector3f moveDir = new Vector3f();
        Vector3f originalMoveDir = new Vector3f();

        if (window.isKeyPressed(GLFW_KEY_W))
            originalMoveDir.add(camera.getForward().mul(1));
        if (window.isKeyPressed(GLFW_KEY_S))
            originalMoveDir.add(camera.getForward().mul(-1));
        if (window.isKeyPressed(GLFW_KEY_A))
            originalMoveDir.add(camera.getLeft().mul(1));
        if (window.isKeyPressed(GLFW_KEY_D))
            originalMoveDir.add(camera.getRight().mul(1));

        moveDir.set(originalMoveDir);

        // Gestione del movimento in acqua
        if (scene.getPlayer().isInWater()) {
            move *= 0.7f; // Movimento più lento in acqua
            
            // Gestione del nuoto verso l'alto
            if (window.isKeyPressed(GLFW_KEY_SPACE)) {
                Vector3f waterVelocity = scene.getPlayer().getWaterVelocity();
                waterVelocity.y = 0.05f; // Forza di nuoto verso l'alto
                scene.getPlayer().setWaterVelocity(waterVelocity);
            }
        } 
        
        if (moveDir.lengthSquared() > 0) {
            moveDir.normalize();

            if (scene.getPlayer().canMove(scene.getWorld(), camera.getPosition(), moveDir, move)) {
                camera.move(moveDir, move);
            } else {

                boolean canMoveX = false;
                boolean canMoveZ = false;

                if (Math.abs(moveDir.x) > 0.01f) {
                    Vector3f xDir = new Vector3f(moveDir.x, 0, 0).normalize();
                    canMoveX = scene.getPlayer().canMove(scene.getWorld(), camera.getPosition(), xDir, move);
                    if (canMoveX) {
                        camera.move(xDir, move);
                    }
                }

                if (Math.abs(moveDir.z) > 0.01f) {
                    Vector3f zDir = new Vector3f(0, 0, moveDir.z).normalize();
                    canMoveZ = scene.getPlayer().canMove(scene.getWorld(), camera.getPosition(), zDir, move);
                    if (canMoveZ) {
                        camera.move(zDir, move);
                    }
                }
            }
        }

        if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && moveDir.lengthSquared() > 0) {
            if (scene.getPlayer().canMove(scene.getWorld(), camera.getPosition(), moveDir, 0.04f)) {
                camera.dash(moveDir, 0.04f);
            }
        }

        Vector3f position = camera.getPosition();

        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            if (scene.getPlayer().canMoveUp(scene.getWorld(), position, move)) {
                camera.moveUp(move);
            }
        }

        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            Vector3f downDir = new Vector3f(0, -1, 0);
            if (scene.getPlayer().canMove(scene.getWorld(), position, downDir, move)) {
                camera.moveDown(move);
            }
        }

        scene.getPlayer().update(scene.getWorld(), position, diffTimeMillis);

        camera.setPosition(position.x, position.y, position.z);
    }

    private void handleMouseInput(Window window, Scene scene, boolean isPaused) {
        if (isPaused)
            return;

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

                int chunkX = Math.floorDiv(blockPos.x, Chunk.WIDTH);
                int chunkZ = Math.floorDiv(blockPos.z, Chunk.DEPTH);

                boolean isOnBorderX = blockPos.x % Chunk.WIDTH == 0 || blockPos.x % Chunk.WIDTH == Chunk.WIDTH - 1;
                boolean isOnBorderZ = blockPos.z % Chunk.DEPTH == 0 || blockPos.z % Chunk.DEPTH == Chunk.DEPTH - 1;

                List<Chunk> chunksToUpdate = new ArrayList<>();

                Chunk mainChunk = scene.getWorld().getChunk(chunkX, chunkZ);
                if (mainChunk != null) {
                    chunksToUpdate.add(mainChunk);
                }

                if (isOnBorderX || isOnBorderZ) {
                    int borderX = isOnBorderX ? (blockPos.x % Chunk.WIDTH == 0 ? -1 : 1) : 0;
                    int borderZ = isOnBorderZ ? (blockPos.z % Chunk.DEPTH == 0 ? -1 : 1) : 0;

                    if (isOnBorderX) {
                        Chunk adjacentX = scene.getWorld().getChunk(chunkX + borderX, chunkZ);
                        if (adjacentX != null) {
                            chunksToUpdate.add(adjacentX);
                        }
                    }

                    if (isOnBorderZ) {
                        Chunk adjacentZ = scene.getWorld().getChunk(chunkX, chunkZ + borderZ);
                        if (adjacentZ != null) {
                            chunksToUpdate.add(adjacentZ);
                        }
                    }

                    if (isOnBorderX && isOnBorderZ) {
                        Chunk diagonalChunk = scene.getWorld().getChunk(chunkX + borderX, chunkZ + borderZ);
                        if (diagonalChunk != null) {
                            chunksToUpdate.add(diagonalChunk);
                        }
                    }
                }

                for (Chunk chunk : chunksToUpdate) {
                    chunk.rebuildFullMesh(scene.getWorld(), scene);
                }

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Chunk chunk = scene.getWorld().getChunk(chunkX + dx, chunkZ + dz);
                        if (chunk != null && !chunksToUpdate.contains(chunk)) {
                            chunk.setDirty(true);
                        }
                    }
                }

                glfwPostEmptyEvent();
            }
        }
    }

    private void handleBlockPlacement(Scene scene) {
        RayCast ray = scene.getRayCast();
        if (ray.hasHit() && ray.getHitDistance() <= 10.0f) {
            Vector3i blockPos = ray.getBlockPosition();
            Vector3i adjacentPos = Block.calculateAdjacentPosition(blockPos, ray.getHitFace());

            int zOffset = adjacentPos.z - blockPos.z;
            if (zOffset != 0) {
                adjacentPos.z = blockPos.z - zOffset;
            }

            Block.BlockType selectedType = scene.getPlayer().getInventory().getSelectedBlock();

            if (scene.getWorld().getBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z) == null &&
                    scene.getPlayer().getInventory().useSelectedBlock()) {

                scene.getWorld().setBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z, new Block(selectedType));

                int chunkX = Math.floorDiv(adjacentPos.x, Chunk.WIDTH);
                int chunkZ = Math.floorDiv(adjacentPos.z, Chunk.DEPTH);

                boolean isOnBorderX = adjacentPos.x % Chunk.WIDTH == 0
                        || adjacentPos.x % Chunk.WIDTH == Chunk.WIDTH - 1;
                boolean isOnBorderZ = adjacentPos.z % Chunk.DEPTH == 0
                        || adjacentPos.z % Chunk.DEPTH == Chunk.DEPTH - 1;

                List<Chunk> chunksToUpdate = new ArrayList<>();

                Chunk mainChunk = scene.getWorld().getChunk(chunkX, chunkZ);
                if (mainChunk != null) {
                    chunksToUpdate.add(mainChunk);
                }

                if (isOnBorderX || isOnBorderZ) {
                    int borderX = isOnBorderX ? (adjacentPos.x % Chunk.WIDTH == 0 ? -1 : 1) : 0;
                    int borderZ = isOnBorderZ ? (adjacentPos.z % Chunk.DEPTH == 0 ? -1 : 1) : 0;

                    if (isOnBorderX) {
                        Chunk adjacentX = scene.getWorld().getChunk(chunkX + borderX, chunkZ);
                        if (adjacentX != null) {
                            chunksToUpdate.add(adjacentX);
                        }
                    }

                    if (isOnBorderZ) {
                        Chunk adjacentZ = scene.getWorld().getChunk(chunkX, chunkZ + borderZ);
                        if (adjacentZ != null) {
                            chunksToUpdate.add(adjacentZ);
                        }
                    }

                    if (isOnBorderX && isOnBorderZ) {
                        Chunk diagonalChunk = scene.getWorld().getChunk(chunkX + borderX, chunkZ + borderZ);
                        if (diagonalChunk != null) {
                            chunksToUpdate.add(diagonalChunk);
                        }
                    }
                }

                for (Chunk chunk : chunksToUpdate) {
                    chunk.rebuildFullMesh(scene.getWorld(), scene);
                }

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Chunk chunk = scene.getWorld().getChunk(chunkX + dx, chunkZ + dz);
                        if (chunk != null && !chunksToUpdate.contains(chunk)) {
                            chunk.setDirty(true);
                        }
                    }
                }

                glfwPostEmptyEvent();
            }
        }
    }
}
