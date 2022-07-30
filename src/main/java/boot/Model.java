package boot;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class Model {
    List<Mesh.Texture> texturesLoaded;
    List<Mesh> meshes;
    String directory;
    boolean gammaCorrection;

    private static final int FLAGS = aiProcess_Triangulate | aiProcess_FlipUVs
            | aiProcess_CalcTangentSpace;

    // Takes a file path to a 3D model
    Model(String path, boolean gamma) {
        this.gammaCorrection = gamma;
        loadModel(path);
    }

    private void loadModel(String path) {
        try (AIScene aiScene = aiImportFile(path, FLAGS)) {
            assert aiScene != null;
            processNode(aiScene.mRootNode(), aiScene);
        }
    }

    // Draws model
    void Draw(Shader shader) {
        for (Mesh mesh : meshes) {
            mesh.draw(shader);
        }
    }

    void processNode(AINode node, AIScene scene) {
        int numMeshes = scene.mNumMeshes();
        PointerBuffer aiMeshes = scene.mMeshes();
        for (int i = 0; i < numMeshes; i++) {
            assert aiMeshes != null;
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            meshes.add(processMesh(aiMesh, scene));
        }
        // After we've processed all the meshes (if any) we then recursively process each of the children nodes
        int numChildren = node.mNumChildren();
        PointerBuffer aiChildren = node.mChildren();
        for (int i = 0; i < numChildren; i++) {
            assert aiChildren != null;
            processNode(AINode.create(aiChildren.get(i)), scene);
        }

    }

    Mesh processMesh(AIMesh mesh, AIScene scene) {
        List<Mesh.Vertex> vertices = new ArrayList<>();
        List<Mesh.Texture> textures = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        AIVector3D.Buffer aiVertices = mesh.mVertices();
        AIVector3D.Buffer aiNormals = mesh.mNormals();
        AIVector3D.Buffer aiTangents = mesh.mTangents();
        AIVector3D.Buffer aiBitangents = mesh.mBitangents();

        // Walk through each of the mesh's vertices
        while (aiVertices.remaining() > 0) {
            Mesh.Vertex vertex = new Mesh.Vertex();
            Vector3f vector = new Vector3f();

            // Positions
            AIVector3D vertexPtr = aiVertices.get();
            vector.x = vertexPtr.x();
            vector.y = vertexPtr.y();
            vector.z = vertexPtr.z();
            vertex.position = vector;

            // Normals
            assert aiNormals != null;
            AIVector3D normalPtr = aiNormals.get();
            vector.x = normalPtr.x();
            vector.y = normalPtr.y();
            vector.z = normalPtr.z();
            vertex.normal = vector;

            // Texture Coordinates
            AIVector3D.Buffer textCords = mesh.mTextureCoords(0);
            int numTextCords = textCords != null ? textCords.remaining() : 0;
            if (numTextCords > 0) {
                // Check if the mesh contains texture coordinates
                AIVector3D textCoord = textCords.get();
                Vector2f vec = new Vector2f();
                vec.x = textCoord.x();
                vec.y = textCoord.y();
                vertex.textureCoordinates = vec;
            } else {
                vertex.textureCoordinates = new Vector2f();
            }

            // Tangent
            assert aiTangents != null;
            AIVector3D tangentsPtr = aiTangents.get();
            vector.x = tangentsPtr.x();
            vector.y = tangentsPtr.y();
            vector.z = tangentsPtr.z();
            vertex.tangent = vector;

            // Bitangent
            assert aiBitangents != null;
            AIVector3D bitangentsPtr = aiBitangents.get();
            vector.x = bitangentsPtr.x();
            vector.y = bitangentsPtr.y();
            vector.z = bitangentsPtr.z();
            vertex.bitangent = vector;

            // Push onto the vector of vertices
            vertices.add(vertex);
        }

        // Loop through each of the mesh's faces and get its vertex indices
        int numFaces = mesh.mNumFaces();
        AIFace.Buffer aiFaces = mesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }

        // Process materials
        if (mesh.mMaterialIndex() >= 0) {
            PointerBuffer aiMaterials = scene.mMaterials();
            assert aiMaterials != null;
            AIMaterial material = AIMaterial.create(aiMaterials.get(mesh.mMaterialIndex()));
            // Sssume a convention for sampler names in the shaders. Each diffuse texture should be named
            // as 'texture_diffuseN' where N is a sequential number ranging from 1 to MAX_SAMPLER_NUMBER.
            // Same applies to other texture as the following list summarizes:
            // Diffuse: texture_diffuseN
            // Specular: texture_specularN
            // Normal: texture_normalN

            // Diffuse maps
            List<Mesh.Texture> diffuseMaps = loadMaterialTextures(material, aiTextureType_DIFFUSE, "texture_diffuse");
            textures.addAll(diffuseMaps);

            // Specular maps
            List<Mesh.Texture> specularMaps = loadMaterialTextures(material, aiTextureType_SPECULAR, "texture_specular");
            textures.addAll(specularMaps);

            // Normal maps
            List<Mesh.Texture> normalMaps = loadMaterialTextures(material, aiTextureType_HEIGHT, "texture_normal");
            textures.addAll(normalMaps);

            // Height maps
            List<Mesh.Texture> heightMaps = loadMaterialTextures(material, aiTextureType_AMBIENT, "texture_height");
            textures.addAll(heightMaps);
        }

        return new Mesh(vertices, indices, textures);
    }


    // Checks all material textures of a given type and loads the textures if they're not loaded yet
    List<Mesh.Texture> loadMaterialTextures(AIMaterial mat, int type, String typeName) {
        return null;
    }
}
