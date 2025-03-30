package world.chunks;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import rendering.meshes.Mesh;
import scene.Scene;
import world.World;
import world.blocks.Block;

public class ChunkMesh {

    private Mesh mesh;

    public Mesh getMesh() {
        return mesh;
    }

    public void buildMesh(Chunk chunk, World world) {
        List<Float> positions = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        int indexCount = 0;

        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.HEIGHT; y++) {
                for (int z = 0; z < Chunk.DEPTH; z++) {
                    Block block = chunk.getBlock(x, y, z);
                    
                    if (block != null && block.getType() != Block.BlockType.AIR) {
                        int worldX = chunk.getChunkX() * Chunk.WIDTH + x;
                        int worldZ = chunk.getChunkZ() * Chunk.DEPTH + z;
                        
                        for (Block.Face face : Block.Face.values()) {
                            int adjX = worldX + face.getOffsetX();
                            int adjY = y + face.getOffsetY();
                            int adjZ = worldZ + face.getOffsetZ();
                            
                            Block adjacent = world.getBlock(adjX, adjY, adjZ);
                            if (adjacent == null || !adjacent.isOpaque()) {
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

        if (mesh != null) {
            mesh.cleanup();
        }

        mesh = new Mesh(posArray, texArray, indArray);
    }

    private int addFaceToMesh(
            List<Float> positions, List<Float> textureCoords, List<Integer> indices,
            int x, int y, int z, int indexStart, Block.Face face, Block.BlockType blockType) {

        float blockSize = Block.BLOCK_SIZE;

        float x1 = x * blockSize;
        float y1 = y * blockSize;
        float z1 = z * blockSize;
        float x2 = x1 + blockSize;
        float y2 = y1 + blockSize;
        float z2 = z1 + blockSize;

        switch (face) {
            case TOP:
                positions.add(x1);
                positions.add(y2);
                positions.add(z1);
                positions.add(x1);
                positions.add(y2);
                positions.add(z2);
                positions.add(x2);
                positions.add(y2);
                positions.add(z2);
                positions.add(x2);
                positions.add(y2);
                positions.add(z1);
                break;
            case BOTTOM:
                positions.add(x1);
                positions.add(y1);
                positions.add(z1);
                positions.add(x2);
                positions.add(y1);
                positions.add(z1);
                positions.add(x2);
                positions.add(y1);
                positions.add(z2);
                positions.add(x1);
                positions.add(y1);
                positions.add(z2);
                break;
            case FRONT:
                positions.add(x1);
                positions.add(y1);
                positions.add(z2);
                positions.add(x2);
                positions.add(y1);
                positions.add(z2);
                positions.add(x2);
                positions.add(y2);
                positions.add(z2);
                positions.add(x1);
                positions.add(y2);
                positions.add(z2);
                break;
            case BACK:
                positions.add(x1);
                positions.add(y1);
                positions.add(z1);
                positions.add(x1);
                positions.add(y2);
                positions.add(z1);
                positions.add(x2);
                positions.add(y2);
                positions.add(z1);
                positions.add(x2);
                positions.add(y1);
                positions.add(z1);
                break;
            case RIGHT:
                positions.add(x2);
                positions.add(y1);
                positions.add(z1);
                positions.add(x2);
                positions.add(y2);
                positions.add(z1);
                positions.add(x2);
                positions.add(y2);
                positions.add(z2);
                positions.add(x2);
                positions.add(y1);
                positions.add(z2);
                break;
            case LEFT:
                positions.add(x1);
                positions.add(y1);
                positions.add(z1);
                positions.add(x1);
                positions.add(y1);
                positions.add(z2);
                positions.add(x1);
                positions.add(y2);
                positions.add(z2);
                positions.add(x1);
                positions.add(y2);
                positions.add(z1);
                break;
        }

        Vector4f textureRegion = getTextureRegion(blockType, face);
        addTextureCoords(textureCoords, textureRegion, face);

        indices.add(indexStart);
        indices.add(indexStart + 1);
        indices.add(indexStart + 2);
        indices.add(indexStart);
        indices.add(indexStart + 2);
        indices.add(indexStart + 3);

        return indexStart + 4;
    }

    private void addTextureCoords(List<Float> textureCoords, Vector4f region, Block.Face face) {
        float minU = region.x;
        float minV = region.y;
        float maxU = region.z;
        float maxV = region.w;

        switch (face) {
            case FRONT:
                textureCoords.add(minU);
                textureCoords.add(maxV); 
                textureCoords.add(maxU);
                textureCoords.add(maxV); 
                textureCoords.add(maxU);
                textureCoords.add(minV); 
                textureCoords.add(minU);
                textureCoords.add(minV); 
                break;
            case BACK:
                textureCoords.add(minU);
                textureCoords.add(maxV); 
                textureCoords.add(minU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(maxV); 
                break;
            case LEFT:
                textureCoords.add(maxU);
                textureCoords.add(maxV); 
                textureCoords.add(minU);
                textureCoords.add(maxV); 
                textureCoords.add(minU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(minV); 
                break;
            case RIGHT:
                textureCoords.add(minU);
                textureCoords.add(maxV); 
                textureCoords.add(minU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(maxV); 
                break;
            case TOP:
                textureCoords.add(minU);
                textureCoords.add(maxV); 
                textureCoords.add(minU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(minV); 
                textureCoords.add(maxU);
                textureCoords.add(maxV); 
                break;
            case BOTTOM:
                textureCoords.add(minU);
                textureCoords.add(maxV);
                textureCoords.add(maxU);
                textureCoords.add(maxV);
                textureCoords.add(maxU);
                textureCoords.add(minV);
                textureCoords.add(minU);
                textureCoords.add(minV);
                break;
        }
    }

    private Vector4f getTextureRegion(Block.BlockType blockType, Block.Face face) {
        return Scene.getBlockTextureRegion(blockType, face);
    }


    public void cleanup() {
        if (mesh != null) {
            mesh.cleanup();
            mesh = null;
        }
    }
}
