package world;

import java.util.ArrayList;
import java.util.List;
import graphics.Mesh;
import org.joml.Vector4f;
import scene.Scene;

public class ChunkMesh {
    
    private Mesh mesh;
    
    public Mesh getMesh() {
        return mesh;
    }
    
    public void buildMesh(Chunk chunk, World world) {
        // Elenchi per memorizzare i dati della mesh
        List<Float> positions = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        int indexCount = 0;
        
        // Per ogni blocco nel chunk
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.DEPTH; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    
                    if (block != null && block.getType() != Block.BlockType.AIR) {
                        // Calcola la posizione mondiale del blocco
                        int worldX = chunk.getChunkX() * Chunk.WIDTH + x;
                        int worldY = y;
                        int worldZ = chunk.getChunkZ() * Chunk.DEPTH + z;
                        
                        // Per ogni faccia del blocco
                        for (Block.Face face : Block.Face.values()) {
                            // Controlla se la faccia deve essere renderizzata
                            if (Block.shouldRenderFace(world, worldX, worldY, worldZ, face)) {
                                // Aggiungi la faccia alla mesh
                                indexCount = addFaceToMesh(
                                    positions, textureCoords, indices, 
                                    x, y, z, indexCount, face, block.getType()
                                );
                            }
                        }
                    }
                }
            }
        }
        
        // Converte le liste in array
        float[] posArray = new float[positions.size()];
        float[] texArray = new float[textureCoords.size()];
        int[] indArray = new int[indices.size()];
        
        for (int i = 0; i < positions.size(); i++) {
            posArray[i] = positions.get(i);
        }
        
        for (int i = 0; i < textureCoords.size(); i++) {
            texArray[i] = textureCoords.get(i);
        }
        
        for (int i = 0; i < indices.size(); i++) {
            indArray[i] = indices.get(i);
        }
        
        // Pulisce la vecchia mesh se esiste
        if (mesh != null) {
            mesh.cleanup();
        }
        
        // Crea la nuova mesh
        if (positions.size() > 0) {
            mesh = new Mesh(posArray, texArray, indArray);
        }
    }
    
    private int addFaceToMesh(
            List<Float> positions, List<Float> textureCoords, List<Integer> indices,
            int x, int y, int z, int indexStart, Block.Face face, Block.BlockType blockType) {
        
        float blockSize = Block.BLOCK_SIZE;
        
        // Calcola le coordinate dei vertici in base alla faccia
        float x1 = x * blockSize;
        float y1 = y * blockSize;
        float z1 = z * blockSize;
        float x2 = x1 + blockSize;
        float y2 = y1 + blockSize;
        float z2 = z1 + blockSize;
        
        // Aggiungi i vertici in base alla faccia
        switch (face) {
            case TOP:
                positions.add(x1); positions.add(y2); positions.add(z1);
                positions.add(x1); positions.add(y2); positions.add(z2);
                positions.add(x2); positions.add(y2); positions.add(z2);
                positions.add(x2); positions.add(y2); positions.add(z1);
                break;
            case BOTTOM:
                positions.add(x1); positions.add(y1); positions.add(z1);
                positions.add(x2); positions.add(y1); positions.add(z1);
                positions.add(x2); positions.add(y1); positions.add(z2);
                positions.add(x1); positions.add(y1); positions.add(z2);
                break;
            case FRONT:
                positions.add(x1); positions.add(y1); positions.add(z2);
                positions.add(x2); positions.add(y1); positions.add(z2);
                positions.add(x2); positions.add(y2); positions.add(z2);
                positions.add(x1); positions.add(y2); positions.add(z2);
                break;
            case BACK:
                positions.add(x1); positions.add(y1); positions.add(z1);
                positions.add(x1); positions.add(y2); positions.add(z1);
                positions.add(x2); positions.add(y2); positions.add(z1);
                positions.add(x2); positions.add(y1); positions.add(z1);
                break;
            case RIGHT:
                positions.add(x2); positions.add(y1); positions.add(z1);
                positions.add(x2); positions.add(y2); positions.add(z1);
                positions.add(x2); positions.add(y2); positions.add(z2);
                positions.add(x2); positions.add(y1); positions.add(z2);
                break;
            case LEFT:
                positions.add(x1); positions.add(y1); positions.add(z1);
                positions.add(x1); positions.add(y1); positions.add(z2);
                positions.add(x1); positions.add(y2); positions.add(z2);
                positions.add(x1); positions.add(y2); positions.add(z1);
                break;
        }
        
        // Aggiungi coordinate texture in base al tipo di blocco e alla faccia
        Vector4f textureRegion = getTextureRegion(blockType, face);
        addTextureCoords(textureCoords, textureRegion);
        
        // Aggiungi gli indici
        indices.add(indexStart);
        indices.add(indexStart + 1);
        indices.add(indexStart + 2);
        indices.add(indexStart);
        indices.add(indexStart + 2);
        indices.add(indexStart + 3);
        
        return indexStart + 4;
    }
    
    private void addTextureCoords(List<Float> textureCoords, Vector4f region) {
        // Coordinate texture (U,V) per ogni vertice della faccia
        // Utilizza region per mappare l'UV corretto dall'atlante texture
        float minU = region.x;
        float minV = region.y;
        float maxU = region.z;
        float maxV = region.w;
        
        // Ordine corretto: in basso a sinistra, in basso a destra, in alto a destra, in alto a sinistra
        textureCoords.add(minU); textureCoords.add(maxV); // In basso a sinistra
        textureCoords.add(maxU); textureCoords.add(maxV); // In basso a destra
        textureCoords.add(maxU); textureCoords.add(minV); // In alto a destra
        textureCoords.add(minU); textureCoords.add(minV); // In alto a sinistra
    }
    
    private Vector4f getTextureRegion(Block.BlockType blockType, Block.Face face) {
        // Utilizziamo il metodo aggiornato della classe Scene
        return Scene.getBlockTextureRegion(blockType, face);
    }
}
