package scene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector4f;

import graphics.Mesh;
import graphics.Model;
import graphics.Projection;
import graphics.TextureCacheAtlas;
import world.World;
import world.Block;
import world.Block.BlockType;
import world.Chunk;
import world.ChunkPosition;

public class Scene {

    private static TextureCacheAtlas textureCacheAtlas; // Rendi statico
    private static Map<String, Model> modelMap; // Rendi statico
    private static Projection projection;
    private static Camera camera;
    private static World world;

    private static final float[] POSITIONS = new float[] {
            // Front face
            -0.5f, 0.5f, 0.5f, // V0
            -0.5f, -0.5f, 0.5f, // V1
            0.5f, -0.5f, 0.5f, // V2
            0.5f, 0.5f, 0.5f, // V3

            // Back face
            -0.5f, 0.5f, -0.5f, // V4
            0.5f, 0.5f, -0.5f, // V5
            -0.5f, -0.5f, -0.5f, // V6
            0.5f, -0.5f, -0.5f, // V7
    };

    private static final int[] INDICES = new int[] {
            // Front face
            0, 1, 3, 3, 1, 2,
            // Back face
            4, 6, 5, 5, 6, 7,
            // Top face
            4, 5, 0, 0, 5, 3,
            // Bottom face
            6, 7, 1, 1, 7, 2,
            // Left face
            4, 6, 0, 0, 6, 1,
            // Right face
            5, 7, 3, 3, 7, 2
    };

    public Scene(int width, int height) {
        modelMap = new HashMap<>();
        projection = new Projection(width, height);
        camera = new Camera();
        textureCacheAtlas = new TextureCacheAtlas("textures/atlas.png", 256, 16);
    }

    public static void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        model.getEntitiesList().add(entity);
    }

    public static void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public void cleanup() {

    }

    public Map<String, Model> getModelMap() {
        return modelMap;
    }

    public Projection getProjection() {
        return projection;
    }

    public void resize(int width, int height) {
        projection.updateProjMatrix(width, height);
    }

    public Camera getCamera() {
        return camera;
    }

    // Fix the removeEntity method to be non-static
    public void removeEntity(String entityId) {
        // Look for the entity in all models
        for (Model model : modelMap.values()) {
            List<Entity> entities = model.getEntitiesList();
            // Use an iterator to avoid ConcurrentModificationException
            Iterator<Entity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (entity.getId().equals(entityId)) {
                    // Remove entity from the list
                    iterator.remove();
                    // Optional: print debug message
                    System.out.println("Entity removed: " + entityId);
                    return; // Exit once entity is found and removed
                }
            }
        }
        // Optional: notify if entity wasn't found
        System.out.println("Entity not found for removal: " + entityId);
    }

    public static Entity createEntity(BlockType type) {
        String modelId = "block-model-" + type.name().toLowerCase();
        String entityId = "block-entity-" + type.name().toLowerCase() + "-" + System.currentTimeMillis();

        Model blockModel = modelMap.get(modelId);
        if (blockModel == null) {
            // Otteniamo la regione di texture per questo tipo di blocco
            Vector4f textureRegion = textureCacheAtlas.getTextureRegion(type);

            // Generiamo le coordinate UV usando la regione di texture
            float[] textCoords = generateUVCoordinates(textureRegion);
            Mesh mesh = new Mesh(POSITIONS, textCoords, INDICES);

            blockModel = new Model(modelId, List.of(mesh));
            addModel(blockModel);
        }

        // Creiamo un'entità con la regione della texture
        Entity blockEntity = new Entity(entityId, blockModel.getId(), textureCacheAtlas.getTextureRegion(type));
        addEntity(blockEntity);

        return blockEntity;
    }

    public void cleanupChunk(Chunk chunk) {
        Block[][][] blocks = chunk.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    Block block = blocks[x][y][z];
                    if (block != null && block.getEntity() != null) {
                        this.removeEntity(block.getEntity().getId());
                    }
                }
            }
        }
    }

    public void updateWorldGeneration(float playerX, float playerZ) {
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
        int renderDistance = world.getRenderDistance();

        int centerChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int centerChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        // Identifica i chunk da rimuovere prima di aggiungerne di nuovi
        Set<ChunkPosition> chunksToRemove = new HashSet<>();

        for (Map.Entry<ChunkPosition, Chunk> entry : loadedChunks.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            int chunkDistanceX = Math.abs(chunkPos.getX() - centerChunkX);
            int chunkDistanceZ = Math.abs(chunkPos.getZ() - centerChunkZ);

            if (chunkDistanceX > renderDistance || chunkDistanceZ > renderDistance) {
                chunksToRemove.add(chunkPos);
            }
        }

        for (ChunkPosition posToRemove : chunksToRemove) {
            Chunk chunkToRemove = loadedChunks.get(posToRemove);
            if (chunkToRemove != null) {
                this.cleanupChunk(chunkToRemove);
                System.out.println("Rimozione chunk alla posizione: " + posToRemove);
            }
            loadedChunks.remove(posToRemove);
        }

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = centerChunkX + dx;
                int chunkZ = centerChunkZ + dz;

                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);

                if (!loadedChunks.containsKey(chunkPos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ);
                    loadedChunks.put(chunkPos, chunk);
                    System.out.println("Aggiunto chunk alla posizione: " + chunkPos);
                }
            }
        }
    }

    private static float[] generateUVCoordinates(Vector4f region) {
        float[] textCoords = new float[6 * 4 * 2]; // 6 facce, 4 vertici per faccia, 2 coordinate per vertice

        // Per ogni faccia del cubo
        for (int face = 0; face < 6; face++) {
            int offset = face * 8;

            // Top-left, Bottom-left, Bottom-right, Top-right
            textCoords[offset] = region.x; // Top-left U
            textCoords[offset + 1] = region.y; // Top-left V

            textCoords[offset + 2] = region.x; // Bottom-left U
            textCoords[offset + 3] = region.w; // Bottom-left V

            textCoords[offset + 4] = region.z; // Bottom-right U
            textCoords[offset + 5] = region.w; // Bottom-right V

            textCoords[offset + 6] = region.z; // Top-right U
            textCoords[offset + 7] = region.y; // Top-right V
        }

        return textCoords;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World worldd) {
        world = worldd;
    }

    public TextureCacheAtlas getTextureCacheAtlas() {
        return textureCacheAtlas;
    }
}
