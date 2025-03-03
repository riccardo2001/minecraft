package core;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.opengl.GL11.*;

import graphics.Camera;
import input.InputHandler;
import world.Block;
import world.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    private Window window;
    private Camera camera;
    private Map<String, Chunk> chunks; // Store chunks by position key
    private int renderDistance = 2; // How many chunks to render in each direction
    private double lastMouseX, lastMouseY;
    private boolean firstMouse = true;
    private float mouseSensitivity = 0.1f;

    public void run() {
        window = new Window();
        window.create();
        camera = new Camera(0, 1, 5);

        // Inizializza la direzione della camera
        camera.updateCameraDirection();

        // Nasconde il cursore e lo imposta in "capture mode"
        glfwSetInputMode(window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Configura il callback per il movimento del mouse
        glfwSetCursorPosCallback(window.getHandle(), (windowHandle, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            double xOffset = xpos - lastMouseX;
            // Inverte l'offset Y per evitare "controlli invertiti"
            double yOffset = lastMouseY - ypos;

            lastMouseX = xpos;
            lastMouseY = ypos;

            // Applica la sensibilità
            xOffset *= mouseSensitivity;
            yOffset *= mouseSensitivity;

            // Ruota la camera
            camera.rotate((float) xOffset, (float) yOffset);
        });

        chunks = new HashMap<>();

        // Generate some initial chunks
        generateChunks();

        while (!window.shouldClose()) {
            update();
            render();
            window.swapBuffers();
        }
    }

    private void generateChunks() {
        // For demonstration, create a 3x1x3 area of chunks
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Chunk chunk = new Chunk(x, 0, z);

                // Fill the bottom layer with blocks
                for (int bx = 0; bx < Chunk.CHUNK_SIZE; bx++) {
                    for (int bz = 0; bz < Chunk.CHUNK_SIZE; bz++) {
                        chunk.setBlock(bx, 0, bz, new Block(1)); // Stone blocks

                        // Add some random blocks above the surface
                        if (Math.random() > 0.8) {
                            int height = 1 + (int) (Math.random() * 3);
                            for (int by = 1; by <= height; by++) {
                                chunk.setBlock(bx, by, bz, new Block(0)); // Grass blocks
                            }
                        }
                    }
                }

                // Store the chunk
                String key = x + "," + 0 + "," + z;
                chunks.put(key, chunk);
            }
        }
    }

    private void update() {
        long windowHandle = window.getHandle();

        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_W))
            camera.move(0, 0, 0.1f); // Changed to positive Z (forward)
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_S))
            camera.move(0, 0, -0.1f); // Changed to negative Z (backward)
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_A))
            camera.move(0.1f, 0, 0); // Left (unchanged)
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_D))
            camera.move(-0.1f, 0, 0); // Right (unchanged)
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_SPACE))
            camera.move(0, 0.1f, 0); // Up (unchanged)
        if (InputHandler.isKeyPressed(windowHandle, GLFW_KEY_LEFT_CONTROL))
            camera.move(0, -0.1f, 0); // Down (unchanged)

        // Check if we need to load new chunks as the player moves
        updateChunks();
    }

    private void updateChunks() {
        // Calculate which chunk the camera is in
        int camChunkX = Chunk.worldToChunk(camera.getX());
        int camChunkY = Chunk.worldToChunk(camera.getY());
        int camChunkZ = Chunk.worldToChunk(camera.getZ());

        // Check if we need to generate new chunks
        for (int x = camChunkX - renderDistance; x <= camChunkX + renderDistance; x++) {
            for (int y = camChunkY - renderDistance; y <= camChunkY + renderDistance; y++) {
                for (int z = camChunkZ - renderDistance; z <= camChunkZ + renderDistance; z++) {
                    String key = x + "," + y + "," + z;
                    if (!chunks.containsKey(key)) {
                        // Create a new chunk if it doesn't exist
                        Chunk newChunk = new Chunk(x, y, z);

                        // Add terrain generation code here
                        // For simplicity, just add a floor of blocks
                        if (y == 0) {
                            for (int bx = 0; bx < Chunk.CHUNK_SIZE; bx++) {
                                for (int bz = 0; bz < Chunk.CHUNK_SIZE; bz++) {
                                    newChunk.setBlock(bx, 0, bz, new Block(1));
                                }
                            }
                        }

                        chunks.put(key, newChunk);
                    }
                }
            }
        }

        // Optionally, unload chunks that are too far away
        // This is important for memory management
        List<String> chunksToRemove = new ArrayList<>();
        for (String key : chunks.keySet()) {
            String[] coords = key.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);

            int distX = Math.abs(x - camChunkX);
            int distY = Math.abs(y - camChunkY);
            int distZ = Math.abs(z - camChunkZ);

            if (distX > renderDistance + 2 || distY > renderDistance + 2 || distZ > renderDistance + 2) {
                chunksToRemove.add(key);
            }
        }

        for (String key : chunksToRemove) {
            chunks.remove(key);
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Imposta la viewport per occupare l'intera finestra
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // Configura la matrice di proiezione
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        // Imposta una prospettiva con FOV di 45 gradi
        setPerspective(90.0f, 1280.0f / 720.0f, 0.1f, 100.0f);

        // Ritorna alla matrice modelview per il rendering della scena
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Il resto del tuo codice lookAt e rendering...
        float[] dir = camera.getDirection();

        lookAt(
                camera.getX(), camera.getY(), camera.getZ(),
                camera.getX() + dir[0], camera.getY() + dir[1], camera.getZ() + dir[2],
                0, 1, 0);

        // Only render chunks that are within view distance
        int camChunkX = Chunk.worldToChunk(camera.getX());
        int camChunkY = Chunk.worldToChunk(camera.getY());
        int camChunkZ = Chunk.worldToChunk(camera.getZ());

        for (Chunk chunk : chunks.values()) {
            // Simple culling: only render chunks that are close enough
            int distX = Math.abs(chunk.getChunkX() - camChunkX);
            int distY = Math.abs(chunk.getChunkY() - camChunkY);
            int distZ = Math.abs(chunk.getChunkZ() - camChunkZ);

            if (distX <= renderDistance && distY <= renderDistance && distZ <= renderDistance) {
                chunk.render();
            }
        }
    }

    private void lookAt(float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ) {
        // Calcola i vettori di direzione della camera
        float dirX = centerX - eyeX;
        float dirY = centerY - eyeY;
        float dirZ = centerZ - eyeZ;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= length;
        dirY /= length;
        dirZ /= length;

        float rightX = dirY * upZ - dirZ * upY;
        float rightY = dirZ * upX - dirX * upZ;
        float rightZ = dirX * upY - dirY * upX;

        length = (float) Math.sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ);
        rightX /= length;
        rightY /= length;
        rightZ /= length;

        float newUpX = rightY * dirZ - rightZ * dirY;
        float newUpY = rightZ * dirX - rightX * dirZ;
        float newUpZ = rightX * dirY - rightY * dirX;

        float[] m = new float[16];
        m[0] = rightX;
        m[1] = newUpX;
        m[2] = -dirX;
        m[3] = 0;
        m[4] = rightY;
        m[5] = newUpY;
        m[6] = -dirY;
        m[7] = 0;
        m[8] = rightZ;
        m[9] = newUpZ;
        m[10] = -dirZ;
        m[11] = 0;
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;

        glMultMatrixf(m);
        glTranslatef(-eyeX, -eyeY, -eyeZ);
    }

    private void setPerspective(float fov, float aspect, float near, float far) {
        float fH = (float) Math.tan(Math.toRadians(fov / 2)) * near;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, near, far);
    }
}