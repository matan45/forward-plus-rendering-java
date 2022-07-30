package boot;

import org.joml.Vector3f;
import org.lwjgl.assimp.AIString;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    private static class Vertex {
        Vector3f position;
        Vector3f normal;
        Vector3f textureCoordinates;
        Vector3f tangent;
        Vector3f bitangent;
    }

    private static class Texture {
        int id;
        String type;
        AIString path;
    }

    List<Vertex> vertices;
    List<Integer> indices;
    List<Texture> textures;
    int VAO;
    int VBO;
    int EBO;

    public Mesh(List<Vertex> vertices, List<Integer> indices, List<Texture> textures) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;
        SetupMesh();
    }

    private void Draw(Shader shader) {

    }

    private void SetupMesh() {
// Create buffers and arrays
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();

        glBindVertexArray(VAO);

        float[] position = fromVertex(vertices);
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(position.length);
        posBuffer.put(position).flip();
        // Load data into vertex buffers
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);

        int[] indice = listIntToArray(indices);
        IntBuffer indBuffer = MemoryUtil.memAllocInt(indice.length);
        indBuffer.put(indBuffer).flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuffer, GL_STATIC_DRAW);

        // Set the vertex attribute pointers
        // Positions
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Normals
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 3);

        // Texture Coords
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 2);

        // Tangent
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 3);

        // Bitangent
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 4);

        glBindVertexArray(0);
    }

    private float[] fromVertex(List<Vertex> vertices) {
        List<Float> ver = new ArrayList<>(vertices.size() * 3);
        for (Vertex vertex : vertices) {
            ver.add(vertex.position.x);
            ver.add(vertex.position.y);
            ver.add(vertex.position.z);
        }
        return listToArray(ver);
    }

    private float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    private int[] listIntToArray(List<Integer> list) {
        return list.stream().mapToInt((Integer v) -> v).toArray();
    }

}
