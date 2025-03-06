package scene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import graphics.Model;
import graphics.Projection;
import graphics.TextureCache;
import world.World;

public class Scene {

    private Map<String, Model> modelMap;
    private Projection projection;
    private TextureCache textureCache;
    private Camera camera;
    private World world;

    public Scene(int width, int height) {
        modelMap = new HashMap<>();
        projection = new Projection(width, height);
        textureCache = new TextureCache();
        camera = new Camera();
    }

    public void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        model.getEntitiesList().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public void cleanup() {
        modelMap.values().forEach(Model::cleanup);
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

    public TextureCache getTextureCache() {
        return textureCache;
    }

    public Camera getCamera() {
        return camera;
    }

    public void removeEntity(String entityId) {
        // Cerca l'entità in tutti i modelli
        for (Model model : modelMap.values()) {
            List<Entity> entities = model.getEntitiesList();
            // Usa un iteratore per evitare ConcurrentModificationException
            Iterator<Entity> iterator = entities.iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (entity.getId().equals(entityId)) {
                    // Rimuovi l'entità dalla lista
                    iterator.remove();
                    // Opzionale: stampa un messaggio di debug
                    System.out.println("Entità rimossa: " + entityId);
                    return; // Esci dal metodo una volta trovata e rimossa l'entità
                }
            }
        }
        // Opzionale: avvisa se l'entità non è stata trovata
        System.out.println("Entità non trovata per la rimozione: " + entityId);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
