package scene;

import org.joml.Vector3f;
import org.joml.Vector4f;

import entities.Player;
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
    private static RayCast rayCast;
    private static World world;
    private static Player player;

    private int currentCenterChunkX = Integer.MIN_VALUE;
    private int currentCenterChunkZ = Integer.MIN_VALUE;

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
        int newCenterChunkX = (int) Math.floor(playerX / (Chunk.WIDTH * Block.BLOCK_SIZE));
        int newCenterChunkZ = (int) Math.floor(playerZ / (Chunk.DEPTH * Block.BLOCK_SIZE));

        if (newCenterChunkX == currentCenterChunkX && newCenterChunkZ == currentCenterChunkZ) {
            return;
        }
        currentCenterChunkX = newCenterChunkX;
        currentCenterChunkZ = newCenterChunkZ;

        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
        int renderDistance = world.getRenderDistance();

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

        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                int chunkX = currentCenterChunkX + dx;
                int chunkZ = currentCenterChunkZ + dz;
                ChunkPosition chunkPos = new ChunkPosition(chunkX, chunkZ);
                if (!loadedChunks.containsKey(chunkPos)) {
                    Chunk chunk = new Chunk(chunkX, chunkZ, world);
                    loadedChunks.put(chunkPos, chunk);

                    chunk.buildMesh(world, this);
                }
            }
        }

        
    }

    public void updateChunks() {
        Map<ChunkPosition, Chunk> loadedChunks = world.getLoadedChunks();
        
        for (var entry : loadedChunks.entrySet()) {
            Chunk chunk = entry.getValue();
            if (chunk.isDirty()) {
                chunk.buildMesh(world, this);
                chunk.setDirty(false);
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
            Model chunkModel = modelMap.get(entity.getModelId()); // Usa modelId unico
            if (chunkModel != null) {
                chunkModel.getMeshList().clear();
                chunkModel.getMeshList().add(newMesh);
            }
        }
    }

    public void cleanup() {
        modelMap.clear();
        entityMap.clear();
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
            System.out.println("DEBUG: World is null!");
            return;
        }

        Camera camera = getCamera();
        Vector3f position = camera.getPosition();
        int x = (int) Math.floor(position.x);
        int y = (int) Math.floor(position.y);
        int z = (int) Math.floor(position.z);

        System.out.println("DEBUG WORLD INFO:");
        System.out.println("Camera at block: (" + x + "," + y + "," + z + ")");

        for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    Block block = world.getBlock(x + xOffset, y + yOffset, z + zOffset);
                    if (block != null && block.isSolid()) {
                        System.out.println("Solid block found at: (" +
                                (x + xOffset) + "," +
                                (y + yOffset) + "," +
                                (z + zOffset) + "), type: " +
                                block.getType());
                    }
                }
            }
        }

        Vector3f direction = camera.getFrontVector();
        System.out.println("Looking direction: " + direction);

        for (int i = 1; i <= 5; i++) {
            int targetX = (int) Math.floor(position.x + direction.x * i);
            int targetY = (int) Math.floor(position.y + direction.y * i);
            int targetZ = (int) Math.floor(position.z + direction.z * i);

            Block block = world.getBlock(targetX, targetY, targetZ);
            System.out.println("Block at distance " + i + " (" +
                    targetX + "," + targetY + "," + targetZ + "): " +
                    (block != null ? block.getType() : "null"));
        }
    }
}