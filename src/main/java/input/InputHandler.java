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
                // Operazione sincrona immediata
                synchronized (scene.getWorld().getLoadedChunks()) {
                    // 1. Rimozione blocco
                    scene.getWorld().setBlock(blockPos.x, blockPos.y, blockPos.z, null);
                    scene.getPlayer().getInventory().addBlock(targetBlock.getType());

                    // 2. Calcolo chunk interessati
                    int chunkX = Math.floorDiv(blockPos.x, Chunk.WIDTH);
                    int chunkZ = Math.floorDiv(blockPos.z, Chunk.DEPTH);

                    // 3. Aggiornamento immediato mesh
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Chunk chunk = scene.getWorld().getChunk(chunkX + dx, chunkZ + dz);
                            if (chunk != null) {
                                chunk.setDirty(true);
                                chunk.rebuildFullMesh(scene.getWorld(), scene); // Sincrono
                            }
                        }
                    }

                    // 4. Aggiornamento immediato delle entità
                    scene.updateChunks();
                }
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

            synchronized (scene.getWorld().getLoadedChunks()) {
                if (scene.getWorld().getBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z) == null &&
                        scene.getPlayer().getInventory().useSelectedBlock()) {

                    // 1. Operazione sincrona di modifica del mondo
                    scene.getWorld().setBlock(adjacentPos.x, adjacentPos.y, adjacentPos.z, new Block(selectedType));

                    // 2. Calcolo chunk interessati (3x3 area)
                    int chunkX = Math.floorDiv(adjacentPos.x, Chunk.WIDTH);
                    int chunkZ = Math.floorDiv(adjacentPos.z, Chunk.DEPTH);

                    // 3. Aggiornamento mesh immediato per chunk vicini
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Chunk chunk = scene.getWorld().getChunk(chunkX + dx, chunkZ + dz);
                            if (chunk != null) {
                                chunk.setDirty(true);
                                chunk.rebuildFullMesh(scene.getWorld(), scene); // Ricostruzione sincrona
                            }
                        }
                    }

                    // 4. Aggiornamento entità e renderer
                    scene.updateChunks();
                }
            }
        }
    }
}
