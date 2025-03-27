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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private static final int MAX_MESH_UPDATES_PER_FRAME = 2;

    private ExecutorService chunkExecutor = Executors.newFixedThreadPool(4);
    private volatile boolean isUpdatingChunks = false;

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
        if (isUpdatingChunks)
            return;

        int newCenterChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int newCenterChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        if (newCenterChunkX == currentCenterChunkX && newCenterChunkZ == currentCenterChunkZ) {
            return;
        }

        isUpdatingChunks = true;
        currentCenterChunkX = newCenterChunkX;
        currentCenterChunkZ = newCenterChunkZ;

        int renderDistance = world.getRenderDistance();
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();

        Vector3f moveDir = getCamera().getFrontVector();
        int priorityX = moveDir.x != 0 ? (int) Math.signum(moveDir.x) : 0;
        int priorityZ = moveDir.z != 0 ? (int) Math.signum(moveDir.z) : 0;

        List<ChunkPosition> chunksToLoad = new ArrayList<>();

        Set<ChunkPosition> chunksToRemove = loadedChunks.keySet().stream()
                .filter(pos -> Math.abs(pos.getX() - currentCenterChunkX) > renderDistance ||
                        Math.abs(pos.getZ() - currentCenterChunkZ) > renderDistance)
                .collect(Collectors.toSet());

        chunksToRemove.forEach(pos -> {
            cleanupChunk(loadedChunks.get(pos));
            loadedChunks.remove(pos);
        });

        for (int i = 0; i <= renderDistance * 2; i++) {
            for (int j = -i; j <= i; j++) {
                int dx = priorityX != 0 ? j * priorityX : j;
                int dz = priorityZ != 0 ? (i - Math.abs(j)) * priorityZ : (i - Math.abs(j));

                addChunkToLoad(chunksToLoad, currentCenterChunkX + dx, currentCenterChunkZ + dz);
                if (priorityX != 0 && priorityZ != 0) {
                    addChunkToLoad(chunksToLoad, currentCenterChunkX - dx, currentCenterChunkZ + dz);
                }
            }
        }

        chunksToLoad.parallelStream().forEach(chunkPos -> {
            if (!loadedChunks.containsKey(chunkPos)) {
                Chunk chunk = new Chunk(chunkPos.getX(), chunkPos.getZ(), world);

                chunkExecutor.submit(() -> {
                    GLFW.glfwPostEmptyEvent();
                    synchronized (loadedChunks) {
                        loadedChunks.put(chunkPos, chunk);
                        chunk.setDirty(true);
                    }
                });
            }
        });

        isUpdatingChunks = false;
    }

    public void updateChunks() {
        Set<Chunk> dirtyChunks = world.getDirtyChunks();
        Iterator<Chunk> it = dirtyChunks.iterator();
        int processed = 0;

        while (it.hasNext() && processed++ < MAX_MESH_UPDATES_PER_FRAME) {
            Chunk chunk = it.next();
            chunk.rebuildFullMesh(world, this);
            it.remove();
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

    private void addChunkToLoad(List<ChunkPosition> list, int x, int z) {
        ChunkPosition pos = new ChunkPosition(x, z);
        if (!list.contains(pos) &&
                Math.abs(x - currentCenterChunkX) <= world.getRenderDistance() &&
                Math.abs(z - currentCenterChunkZ) <= world.getRenderDistance()) {
            list.add(pos);
        }
    }

    public void cleanup() {
        modelMap.clear();
        entityMap.clear();
        chunkExecutor.shutdownNow();
    }

    public void cleanupChunk(Chunk chunk) {
        Entity chunkEntity = chunk.getChunkEntity();
        if (chunkEntity != null) {
            removeEntity(chunkEntity.getId());
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