package boot;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

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
            directory = path.substring(0, path.lastIndexOf('\\'));
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
        List<Mesh.Texture> textures = new ArrayList<>();


        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(mat, type); i++) {
            AIString path = AIString.calloc();
            Assimp.aiGetMaterialTexture(mat, type, i, path, (IntBuffer) null, null, null, null, null, null);
            String textPath = path.dataString();

            // Ignore textures that we have already loaded
            boolean skip = false;
            for (Mesh.Texture value : texturesLoaded) {
                if (value.path.equals(textPath)) {
                    textures.add(value);
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                // If texture hasn't been loaded already, load it
                Mesh.Texture texture = new Mesh.Texture();
                texture.id = TextureFromFile(textPath);
                texture.type = typeName;
                texture.path = textPath;
                textures.add(texture);
                texturesLoaded.add(texture);
            }
        }
        return textures;
    }

    int TextureFromFile(String path) {
        ByteBuffer imageBuffer;
        ByteBuffer image;
        try {
            imageBuffer = ioResourceToByteBuffer(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);

        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info_from_memory(imageBuffer, w, h, comp))
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());


        // Decode the image
        image = stbi_load_from_memory(imageBuffer, w, h, comp, 0);
        if (image == null)
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());


        int textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);
        if (comp.get(0) == 3) {
            if ((w.get(0) & 3) != 0)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w.get(0) & 1));

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w.get(0), h.get(0), 0, GL_RGB,
                    GL_UNSIGNED_BYTE, image);
        } else if (comp.get(0) == 1)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, w.get(0), h.get(0), 0, GL_RED,
                    GL_UNSIGNED_BYTE, image);
        else
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, image);

        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureID;

    }


    protected ByteBuffer ioResourceToByteBuffer(String resource) throws IOException {
        ByteBuffer buffer;
        int bufferSize = 10 * 1024;
        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) ;
            }
        } else {
            try (InputStream source = Model.class.getClassLoader().getResourceAsStream(resource)) {
                assert source != null;
                try (ReadableByteChannel rbc = Channels.newChannel(source)) {
                    buffer = createByteBuffer(bufferSize);

                    while (true) {
                        int bytes = rbc.read(buffer);
                        if (bytes == -1) {
                            break;
                        }
                        if (buffer.remaining() == 0) {
                            buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                        }
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
