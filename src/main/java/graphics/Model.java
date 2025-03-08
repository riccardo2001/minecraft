package graphics;

import java.util.ArrayList;
import java.util.List;
import scene.Entity;

public class Model {
    private final String id;
    private List<Entity> entitiesList;
    private List<Mesh> meshList;

    public Model(String id, List<Mesh> meshList) {
        this.id = id;
        this.meshList = meshList;
        this.entitiesList = new ArrayList<>();
    }

    public List<Entity> getEntitiesList() {
        return entitiesList;
    }

    public String getId() {
        return id;
    }

    public List<Mesh> getMeshList() {
        return meshList;
    }
}