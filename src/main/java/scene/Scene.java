package scene;

import org.joml.Vector4f;
import graphics.Mesh;
import graphics.Model;
import graphics.Projection;
import graphics.TextureCacheAtlas;
import world.Block;
import world.Block.BlockType;
import world.Chunk;
import world.ChunkPosition;
import world.World;
import java.util.*;

public class Scene {
    private static TextureCacheAtlas textureCacheAtlas;
    private static Map<String, Model> modelMap;
    private static Map<String, Entity> entityMap;

    private static Projection projection;
    private static Camera camera;
    private static World world;

    // Aggiungi questi campi per memorizzare il chunk corrente del giocatore
    private int currentCenterChunkX = Integer.MIN_VALUE;
    private int currentCenterChunkZ = Integer.MIN_VALUE;

    // Cubo a 24 vertici (6 facce x 4 vertici)
    private static final float[] POSITIONS = {
            -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f
    };

    private static final int[] INDICES = {
            0, 1, 2, 0, 2, 3, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11,
            12, 13, 14, 12, 14, 15, 16, 17, 18, 16, 18, 19, 20, 21, 22, 20, 22, 23
    };

    static {
        modelMap = new HashMap<>();
        entityMap = new HashMap<>();
    }

    public Scene(int width, int height) {
        projection = new Projection(width, height);
        camera = new Camera();
        textureCacheAtlas = new TextureCacheAtlas("textures/atlas.png", 256, 16);
    }

    public static void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null)
            throw new RuntimeException("Modello non trovato: " + modelId);
        model.getEntitiesList().add(entity);
        entityMap.put(entity.getId(), entity);
    }

    public static void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public void cleanup() {
        modelMap.clear();
        entityMap.clear();
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

    public static Entity createEntity(BlockType type) {
        String modelId = "block-model-" + type.name().toLowerCase();
        String entityId = "block-entity-" + type.name().toLowerCase() + "-" + System.currentTimeMillis();
        Model blockModel = modelMap.get(modelId);
        if (blockModel == null) {
            Vector4f textureRegion = textureCacheAtlas.getTextureRegion(type);
            float[] textCoords = generateUVCoordinates(textureRegion);
            Mesh mesh = new Mesh(POSITIONS, textCoords, INDICES);
            blockModel = new Model(modelId, List.of(mesh));
            addModel(blockModel);
        }
        Entity blockEntity = new Entity(entityId, blockModel.getId(), textureCacheAtlas.getTextureRegion(type));
        addEntity(blockEntity);
        return blockEntity;
    }

    /**
     * Cleanup di un chunk: per ogni blocco, se esiste un entity associato, lo
     * rimuove.
     */
    public void cleanupChunk(Chunk chunk) {
        Block[][][] blocks = chunk.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    Block block = blocks[x][y][z];
                    if (block != null && block.getEntity() != null)
                        removeEntity(block.getEntity().getId());
                }
            }
        }
    }

    public void updateWorldGeneration(float playerX, float playerZ) {
        int newCenterChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int newCenterChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        // Aggiorna solo se il centro in chunk Ã¨ cambiato
        if (newCenterChunkX == currentCenterChunkX && newCenterChunkZ == currentCenterChunkZ) {
            return;
        }
        currentCenterChunkX = newCenterChunkX;
        currentCenterChunkZ = newCenterChunkZ;

        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
        int renderDistance = world.getRenderDistance();

        // Raccogli i chunk da rimuovere (oltre il renderDistance)
        Set<ChunkPosition> chunksToRemove = new HashSet<>();
        for (Map.Entry<ChunkPosition, Chunk> entry : loadedChunks.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            int chunkDistanceX = Math.abs(chunkPos.getX() - currentCenterChunkX);
            int chunkDistanceZ = Math.abs(chunkPos.getZ() - currentCenterChunkZ);
            if (chunkDistanceX > renderDistance || chunkDistanceZ > renderDistance) {
                chunksToRemove.add(chunkPos);
            }
        }
        for (ChunkPosition posToRemove : chunksToRemove) {
            Chunk chunkToRemove = loadedChunks.get(posToRemove);
            if (chunkToRemove != null) {
                cleanupChunk(chunkToRemove);
            }
            loadedChunks.remove(posToRemove);
        }

        // Aggiungi i chunk mancanti intorno al nuovo centro
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = currentCenterChunkX + dx;
                int chunkZ = currentCenterChunkZ + dz;
                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);
                if (!loadedChunks.containsKey(chunkPos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ);
                    loadedChunks.put(chunkPos, chunk);
                }
            }
        }
    }

    private static float[] generateUVCoordinates(Vector4f region) {
        float[] textCoords = new float[48]; // 6 facce * 4 vertici * 2 coordinate
        for (int face = 0; face < 6; face++) {
            int offset = face * 8;
            textCoords[offset] = region.x;
            textCoords[offset + 1] = region.y;
            textCoords[offset + 2] = region.x;
            textCoords[offset + 3] = region.w;
            textCoords[offset + 4] = region.z;
            textCoords[offset + 5] = region.w;
            textCoords[offset + 6] = region.z;
            textCoords[offset + 7] = region.y;
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

    public void removeEntity(String entityId) {
        Entity entity = entityMap.remove(entityId);
        if (entity == null)
            return;
        Model model = modelMap.get(entity.getModelId());
        if (model != null) {
            var list = model.getEntitiesList();
            int index = list.indexOf(entity);
            if (index != -1) {
                int last = list.size() - 1;
                list.set(index, list.get(last));
                list.remove(last);
            }
        }
    }
}