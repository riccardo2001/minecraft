package scene;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import rendering.meshes.Mesh;
import rendering.meshes.Model;
import rendering.textures.TextureCacheAtlas;
import scene.entities.Player;
import utils.Logger;
import world.chunks.Chunk;
import world.chunks.ChunkPosition;
import world.World;
import world.blocks.Block;
import world.blocks.Block.BlockType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scene {
    private static TextureCacheAtlas textureCacheAtlas;
    private static Map<String, Model> modelMap;
    private static Map<String, Entity> entityMap;

    private static Projection projection;
    private static Camera camera;
    private static RayCast rayCast;
    private static World world;
    private static Player player;

    private int currentCenterChunkX = Integer.MIN_VALUE;
    private int currentCenterChunkZ = Integer.MIN_VALUE;
    private static final Object chunkLock = new Object();

    private ExecutorService chunkGenerationExecutor;
    private AtomicBoolean isUpdatingChunks = new AtomicBoolean(false);

    private final Queue<Chunk> meshGenerationQueue = new ConcurrentLinkedQueue<>();

    private static final int BUFFER_DISTANCE = 2;

    private Vector3f lastPlayerPosition = new Vector3f(0, 0, 0);
    private Vector3f playerMoveDirection = new Vector3f(0, 0, 0);

    static {
        modelMap = new HashMap<>();
        entityMap = new HashMap<>();
    }

    public Scene(int width, int height) {
        projection = new Projection(width, height);
        camera = new Camera();
        player = new Player();
        rayCast = new RayCast();
        world = new World();
        textureCacheAtlas = new TextureCacheAtlas("textures/atlas2.png", 512, 512, 16);

        int processors = Runtime.getRuntime().availableProcessors();
        chunkGenerationExecutor = Executors.newFixedThreadPool(processors / 2);

        Logger.info("Sistema multithreading inizializzato con " + processors / 2 + " available threads.");

        currentCenterChunkX = (int) Math.floor(0 / (Chunk.WIDTH * Block.BLOCK_SIZE));
        currentCenterChunkZ = (int) Math.floor(0 / (Chunk.DEPTH * Block.BLOCK_SIZE));

        lastPlayerPosition.set(0, 0, 0);
    }

    public static void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null)
            throw new RuntimeException("Modello non trovato: " + modelId);
        model.getEntitiesList().add(entity);
        entityMap.put(entity.getId(), entity);
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

    public static Vector4f getBlockTextureRegion(BlockType type, Block.Face face) {
        String textureKey;

        switch (type) {
            case GRASS:
                if (face == Block.Face.TOP) {
                    textureKey = "grass_top";
                } else if (face == Block.Face.BOTTOM) {
                    textureKey = "dirt";
                } else {
                    textureKey = "grass_side";
                }
                break;
            case DIRT:
                textureKey = "dirt";
                break;
            case STONE:
                textureKey = "stone";
                break;
            case WATER:
                textureKey = "water";
                break;
            case WOOD:
                textureKey = "wood";
                break;
            case LEAVES:
                textureKey = "leaves";
                break;
            default:
                textureKey = "default";
                break;
        }

        return textureCacheAtlas.getTextureRegion(textureKey);
    }

    public void updateWorldGeneration(float playerX, float playerZ) {
        if (isUpdatingChunks.getAndSet(true)) {
            return;
        }

        try {
            Vector3f currentPos = new Vector3f(playerX, 0, playerZ);
            if (!lastPlayerPosition.equals(0, 0, 0)) {
                playerMoveDirection.set(currentPos).sub(lastPlayerPosition).normalize();
            }
            lastPlayerPosition.set(currentPos);

            int newCenterChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
            int newCenterChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

            if (newCenterChunkX == currentCenterChunkX && newCenterChunkZ == currentCenterChunkZ) {
                processDirtyChunks();
                isUpdatingChunks.set(false);
                return;
            }

            currentCenterChunkX = newCenterChunkX;
            currentCenterChunkZ = newCenterChunkZ;

            Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
            int renderDistance = world.getRenderDistance();
            int totalDistance = renderDistance + BUFFER_DISTANCE;

            Set<ChunkPosition> chunksToRemove = new HashSet<>();
            synchronized (chunkLock) {
                for (Map.Entry<ChunkPosition, Chunk> entry : loadedChunks.entrySet()) {
                    ChunkPosition chunkPos = entry.getKey();
                    int chunkDistanceX = Math.abs(chunkPos.getX() - currentCenterChunkX);
                    int chunkDistanceZ = Math.abs(chunkPos.getZ() - currentCenterChunkZ);
                    if (chunkDistanceX > totalDistance + 1 || chunkDistanceZ > totalDistance + 1) {
                        chunksToRemove.add(chunkPos);
                    }
                }
            }

            if (!chunksToRemove.isEmpty()) {
                for (ChunkPosition posToRemove : chunksToRemove) {
                    synchronized (chunkLock) {
                        Chunk chunkToRemove = loadedChunks.get(posToRemove);
                        if (chunkToRemove != null) {
                            cleanupChunk(chunkToRemove);
                            loadedChunks.remove(posToRemove);
                            if (loadedChunks.containsKey(posToRemove)) {
                                loadedChunks.remove(posToRemove);
                            }
                        }
                    }
                }
            }

            List<ChunkPosition> chunksToLoad = new ArrayList<>();

            int lookAheadX = 0;
            int lookAheadZ = 0;

            if (Math.abs(playerMoveDirection.x) > 0.3f) {
                lookAheadX = playerMoveDirection.x > 0 ? 2 : -2;
            }

            if (Math.abs(playerMoveDirection.z) > 0.3f) {
                lookAheadZ = playerMoveDirection.z > 0 ? 2 : -2;
            }

            for (int dx = -totalDistance; dx <= totalDistance; dx++) {
                for (int dz = -totalDistance; dz <= totalDistance; dz++) {
                    int chunkX = currentCenterChunkX + dx;
                    int chunkZ = currentCenterChunkZ + dz;
                    ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);

                    synchronized (chunkLock) {
                        Chunk existingChunk = loadedChunks.get(chunkPos);
                        if (existingChunk == null) {
                            chunksToLoad.add(chunkPos);
                        } else if (existingChunk.getChunkEntity() == null) {
                            cleanupChunk(existingChunk);
                            loadedChunks.remove(chunkPos);
                            chunksToLoad.add(chunkPos);
                        }
                    }
                }
            }

            for (int i = 1; i <= 3; i++) {
                int aheadX = currentCenterChunkX + (lookAheadX * i);
                int aheadZ = currentCenterChunkZ + (lookAheadZ * i);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        ChunkPosition pos = new ChunkPosition(aheadX + dx, aheadZ + dz);
                        if (!loadedChunks.containsKey(pos) && !chunksToLoad.contains(pos)) {
                            chunksToLoad.add(pos);
                        }
                    }
                }
            }

            if (!chunksToLoad.isEmpty()) {
                chunksToLoad.sort((pos1, pos2) -> {
                    int dx1 = pos1.getX() - currentCenterChunkX;
                    int dz1 = pos1.getZ() - currentCenterChunkZ;
                    int dx2 = pos2.getX() - currentCenterChunkX;
                    int dz2 = pos2.getZ() - currentCenterChunkZ;
                    return Integer.compare((Math.abs(dx1) + Math.abs(dz1)),
                            (Math.abs(dx2) + Math.abs(dz2)));
                });

                int immediateCount = Math.min(4, chunksToLoad.size());
                for (int i = 0; i < immediateCount; i++) {
                    ChunkPosition pos = chunksToLoad.get(i);
                    Chunk chunk = new Chunk(pos.getX(), pos.getZ(), world);
                    synchronized (chunkLock) {
                        loadedChunks.put(pos, chunk);
                    }
                    meshGenerationQueue.add(chunk);

                    ChunkPosition[] neighbors = {
                            new ChunkPosition(pos.getX() + 1, pos.getZ()),
                            new ChunkPosition(pos.getX() - 1, pos.getZ()),
                            new ChunkPosition(pos.getX(), pos.getZ() + 1),
                            new ChunkPosition(pos.getX(), pos.getZ() - 1)
                    };

                    synchronized (chunkLock) {
                        for (ChunkPosition neighborPos : neighbors) {
                            Chunk neighbor = loadedChunks.get(neighborPos);
                            if (neighbor != null) {
                                world.getDirtyChunks().add(neighbor);
                            }
                        }
                    }
                }
                chunksToLoad.subList(0, immediateCount).clear();

                if (!chunksToLoad.isEmpty()) {
                    final int MAX_CONCURRENT_LOADING = world.getRenderDistance();
                    final List<ChunkPosition> currentBatch = chunksToLoad.subList(0,
                            Math.min(MAX_CONCURRENT_LOADING, chunksToLoad.size()));

                    for (ChunkPosition pos : currentBatch) {
                        final int chunkX = pos.getX();
                        final int chunkZ = pos.getZ();

                        chunkGenerationExecutor.submit(() -> {
                            try {
                                Chunk chunk = new Chunk(chunkX, chunkZ, world);

                                synchronized (chunkLock) {
                                    loadedChunks.put(new ChunkPosition(chunkX, chunkZ), chunk);
                                }

                                meshGenerationQueue.add(chunk);
                                GLFW.glfwPostEmptyEvent();

                                ChunkPosition[] neighbors = {
                                        new ChunkPosition(chunkX + 1, chunkZ),
                                        new ChunkPosition(chunkX - 1, chunkZ),
                                        new ChunkPosition(chunkX, chunkZ + 1),
                                        new ChunkPosition(chunkX, chunkZ - 1)
                                };

                                synchronized (chunkLock) {
                                    for (ChunkPosition neighborPos : neighbors) {
                                        Chunk neighbor = loadedChunks.get(neighborPos);
                                        if (neighbor != null) {
                                            world.getDirtyChunks().add(neighbor);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            processDirtyChunks();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isUpdatingChunks.set(false);
        }
    }

    private void processDirtyChunks() {
        List<Chunk> dirtyChunks = new ArrayList<>(world.getDirtyChunks());
        if (dirtyChunks.isEmpty()) {
            return;
        }

        int maxChunksToProcess = Math.min(dirtyChunks.size(), 3);

        for (int i = 0; i < maxChunksToProcess; i++) {
            Chunk chunk = dirtyChunks.get(i);
            if (chunk != null) {
                chunk.buildMesh(world, this);
                world.getDirtyChunks().remove(chunk);
            }
        }
    }

    public void registerChunkModel(String modelId, Mesh mesh) {
        if (mesh == null)
            return;
        Model existingModel = modelMap.get(modelId);
        if (existingModel == null) {
            List<Mesh> meshes = new ArrayList<>();
            meshes.add(mesh);
            Model model = new Model(modelId, meshes);
            modelMap.put(modelId, model);
        }
    }

    public void addChunkEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);

        Model model = modelMap.get(entity.getModelId());
        if (model != null) {
            model.getEntitiesList().add(entity);
        }
    }

    public void updateChunkMesh(String chunkId, Mesh newMesh) {
        Entity entity = entityMap.get(chunkId);
        if (entity != null) {
            Model chunkModel = modelMap.get(entity.getModelId());
            if (chunkModel != null) {
                chunkModel.getMeshList().clear();
                chunkModel.getMeshList().add(newMesh);
            }
        }
    }

    public void cleanup() {
        modelMap.clear();
        entityMap.clear();

        if (chunkGenerationExecutor != null) {
            chunkGenerationExecutor.shutdown();
        }
    }

    public void cleanupChunk(Chunk chunk) {
        if (chunk == null)
            return;

        try {
            int chunkX = chunk.getChunkX();
            int chunkZ = chunk.getChunkZ();
            String entityId = "chunk_" + chunkX + "_" + chunkZ;
            String modelId = "chunk_model_" + chunkX + "_" + chunkZ;

            Entity chunkEntity = chunk.getChunkEntity();
            if (chunkEntity != null) {
                entityMap.remove(entityId);

                Model model = modelMap.get(modelId);
                if (model != null) {
                    var list = model.getEntitiesList();
                    list.remove(chunkEntity);
                }

                chunk.setChunkEntity(null);
            } else {
                Entity orphanEntity = entityMap.get(entityId);
                if (orphanEntity != null) {
                    Logger.info("Trovata entità orfana: " + entityId);
                    entityMap.remove(entityId);
                }
            }

            Model chunkModel = modelMap.get(modelId);
            if (chunkModel != null) {
                for (Mesh mesh : chunkModel.getMeshList()) {
                    if (mesh != null) {
                        mesh.cleanup();
                    }
                }
                modelMap.remove(modelId);
            }

            meshGenerationQueue.remove(chunk);

            world.getDirtyChunks().remove(chunk);

            chunk.releaseResources();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void forceReloadChunk(int chunkX, int chunkZ) {
        ChunkPosition pos = new ChunkPosition(chunkX, chunkZ);
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();

        synchronized (chunkLock) {
            Chunk existingChunk = loadedChunks.get(pos);
            if (existingChunk != null) {
                cleanupChunk(existingChunk);
                loadedChunks.remove(pos);
            }

            String entityId = "chunk_" + chunkX + "_" + chunkZ;
            String modelId = "chunk_model_" + chunkX + "_" + chunkZ;

            if (entityMap.containsKey(entityId)) {
                removeEntity(entityId);
            }

            if (modelMap.containsKey(modelId)) {
                Model phantom = modelMap.remove(modelId);
                if (phantom != null) {
                    for (Mesh mesh : phantom.getMeshList()) {
                        if (mesh != null)
                            mesh.cleanup();
                    }
                }
            }

            Chunk newChunk = new Chunk(chunkX, chunkZ, world);
            loadedChunks.put(pos, newChunk);

            newChunk.buildMesh(world, this);
        }
    }

    public void validateAllChunks() {
        Logger.info("Validazione di tutti i chunk caricati...");
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
        List<ChunkPosition> ghostChunks = new ArrayList<>();
        List<ChunkPosition> missingChunks = new ArrayList<>();

        int renderDistance = world.getRenderDistance();

        synchronized (chunkLock) {
            for (Map.Entry<ChunkPosition, Chunk> entry : loadedChunks.entrySet()) {
                ChunkPosition pos = entry.getKey();
                Chunk chunk = entry.getValue();

                if (chunk.getChunkEntity() == null) {
                    ghostChunks.add(pos);
                } else {
                    String entityId = chunk.getChunkEntity().getId();
                    if (!entityMap.containsKey(entityId)) {
                        ghostChunks.add(pos);
                    }
                }
            }

            for (int dx = -renderDistance; dx <= renderDistance; dx++) {
                for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                    ChunkPosition pos = new ChunkPosition(currentCenterChunkX + dx, currentCenterChunkZ + dz);
                    if (!loadedChunks.containsKey(pos)) {
                        missingChunks.add(pos);
                    }
                }
            }

            if (!ghostChunks.isEmpty()) {
                for (ChunkPosition pos : ghostChunks) {
                    forceReloadChunk(pos.getX(), pos.getZ());
                }
            }

            if (!missingChunks.isEmpty()) {
                for (ChunkPosition pos : missingChunks) {
                    Chunk newChunk = new Chunk(pos.getX(), pos.getZ(), world);
                    loadedChunks.put(pos, newChunk);
                    meshGenerationQueue.add(newChunk);
                }
            }
        }
    }

    public void resetAllChunks() {
        Logger.info("RESET COMPLETO DEI CHUNK IN CORSO...");

        synchronized (chunkLock) {
            Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
            Set<ChunkPosition> positions = new HashSet<>(loadedChunks.keySet());

            for (ChunkPosition pos : positions) {
                Chunk chunk = loadedChunks.get(pos);
                cleanupChunk(chunk);
                loadedChunks.remove(pos);
            }

            meshGenerationQueue.clear();
            world.getDirtyChunks().clear();

            int renderDistance = world.getRenderDistance();
            List<ChunkPosition> chunksToReload = new ArrayList<>();

            for (int dx = -renderDistance; dx <= renderDistance; dx++) {
                for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                    int chunkX = currentCenterChunkX + dx;
                    int chunkZ = currentCenterChunkZ + dz;
                    chunksToReload.add(new ChunkPosition(chunkX, chunkZ));
                }
            }

            chunksToReload.sort((p1, p2) -> {
                double dist1 = Math.sqrt(Math.pow(p1.getX() - currentCenterChunkX, 2) +
                        Math.pow(p1.getZ() - currentCenterChunkZ, 2));
                double dist2 = Math.sqrt(Math.pow(p2.getX() - currentCenterChunkX, 2) +
                        Math.pow(p2.getZ() - currentCenterChunkZ, 2));
                return Double.compare(dist1, dist2);
            });

            for (int i = 0; i < Math.min(9, chunksToReload.size()); i++) {
                ChunkPosition pos = chunksToReload.get(i);
                Chunk chunk = new Chunk(pos.getX(), pos.getZ(), world);
                loadedChunks.put(pos, chunk);
                chunk.buildMesh(world, this);
            }

            for (int i = 9; i < chunksToReload.size(); i++) {
                ChunkPosition pos = chunksToReload.get(i);
                meshGenerationQueue.add(new Chunk(pos.getX(), pos.getZ(), world));
            }
        }
    }

    public void forceInitialChunksGeneration() {
        Logger.info("Generazione forzata dei chunk iniziali");

        int renderDistance = world.getRenderDistance();
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = currentCenterChunkX + dx;
                int chunkZ = currentCenterChunkZ + dz;
                ChunkPosition pos = new ChunkPosition(chunkX, chunkZ);

                if (!loadedChunks.containsKey(pos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ, world);
                    loadedChunks.put(pos, chunk);
                    chunk.buildMesh(world, this);
                }
            }
        }
    }

    public static void addModel(Model model) {
        modelMap.put(model.getId(), model);
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

    public World getWorld() {
        return world;
    }

    public TextureCacheAtlas getTextureCacheAtlas() {
        return textureCacheAtlas;
    }

    public int getCurrentCenterChunkX() {
        return this.currentCenterChunkX;
    }

    public void setCurrentCenterChunkX(int currentCenterChunkX) {
        this.currentCenterChunkX = currentCenterChunkX;
    }

    public int getCurrentCenterChunkZ() {
        return this.currentCenterChunkZ;
    }

    public void setCurrentCenterChunkZ(int currentCenterChunkZ) {
        this.currentCenterChunkZ = currentCenterChunkZ;
    }

    public Player getPlayer() {
        return player;
    }

    public RayCast getRayCast() {
        return rayCast;
    }

    public void debugWorldInfo() {
        if (world == null) {
            return;
        }

        Camera camera = getCamera();
        Vector3f position = camera.getPosition();
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);

        Logger.info("DEBUG WORLD INFO:");
        Logger.info("Camera at block: (" + x + "," + y + "," + z + ")");

        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    Block block = world.getBlock(x + xOffset, y + yOffset, z + zOffset);
                    if (block != null && block.isSolid()) {
                        Logger.info("Solid block found at: (" +
                                (x + xOffset) + "," +
                                (y + yOffset) + "," +
                                (z + zOffset) + "), type: " +
                                block.getType());
                    }
                }
            }
        }

        Vector3f direction = camera.getFrontVector();
        Logger.info("Looking direction: " + direction);

        for (int i = 1; i <= 5; i++) {
            int targetX = (int) Math.floor(position.x + direction.x * i);
            int targetY = (int) Math.floor(position.y + direction.y * i);
            int targetZ = (int) Math.floor(position.z + direction.z * i);

            Block block = world.getBlock(targetX, targetY, targetZ);
            Logger.info("Block at distance " + i + " (" +
                    targetX + "," + targetY + "," + targetZ + "): " +
                    (block != null ? block.getType() : "null"));
        }
    }

}